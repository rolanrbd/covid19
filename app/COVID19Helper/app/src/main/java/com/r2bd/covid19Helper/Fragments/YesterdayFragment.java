package com.r2bd.covid19Helper.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.r2bd.covid19Helper.Adapters.ListCountriesAdapter;
import com.r2bd.covid19Helper.CountryLine;
import com.r2bd.covid19Helper.MainActivity;
import com.r2bd.covid19Helper.Models.CasesValues;
import com.r2bd.covid19Helper.Models.SharedViewModel;
import com.r2bd.covid19Helper.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class YesterdayFragment extends Fragment {

    OnRefreshCasesDataYesterday refreshListener;

    private SharedViewModel sharedViewModel;
    private CasesValues casesData;
    private boolean updated = false;

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle;
    SharedPreferences.Editor editor;
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    SwipeRefreshLayout fragmentSwipeRefreshY;

    public YesterdayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            refreshListener = (OnRefreshCasesDataYesterday) context;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        updated = false;
        return inflater.inflate(R.layout.fragment_yesterday, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TextView textView = view.findViewById(R.id.txtHello);

        textViewCases = view.findViewById(R.id.textViewCasesY);
        textViewRecovered = view.findViewById(R.id.textViewRecoveredY);
        textViewDeaths = view.findViewById(R.id.textViewDeathsY);
        textViewDate = view.findViewById(R.id.textViewDateY);
        textViewRecoveredTitle = view.findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = view.findViewById(R.id.textViewDeathsTitle);
        textViewActiveTitle = view.findViewById(R.id.textViewActiveTitle);
        textViewActive = view.findViewById(R.id.textViewActiveY);
        textViewNewDeaths = view.findViewById(R.id.textViewNewDeathsY);
        textViewNewCases = view.findViewById(R.id.textViewNewCasesY);
        textViewNewCasesTitle = view.findViewById(R.id.textViewNewCasesTitle);
        textViewNewDeathsTitle = view.findViewById(R.id.textViewNewDeathsTitle);
        listViewCountries = view.findViewById(R.id.listViewCountriesY);

        listViewCountries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) fragmentSwipeRefreshY.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        fragmentSwipeRefreshY.setEnabled(true);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }

            private boolean listIsAtTop() {
                int tmp = listViewCountries.getChildCount();
                if (listViewCountries.getChildCount() == 0) return true;
                tmp = listViewCountries.getChildAt(0).getTop();
                return listViewCountries.getChildAt(0).getTop() == 0;
            }
        });
        // Implement Swipe to Refresh
        fragmentSwipeRefreshY = view.findViewById(R.id.fragmentSwipeRefreshY);
        fragmentSwipeRefreshY.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        fragmentSwipeRefreshY.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshListener.onRefreshCasesDataYesterday();
                    }
                }
        );

        if(casesData == null){
            ((MainActivity)getActivity()).onRefreshCasesDataNow();
        }

        if(!updated)
            updateUI();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        if (preferences.getString(getString(R.string.txtViewCasesPrefY), null) != null) {
            textViewCases.setText(preferences.getString(getString(R.string.txtViewCasesPrefY), null));
            textViewRecovered.setText(preferences.getString(getString(R.string.txtViewRecoveredPrefY), null));
            textViewDeaths.setText(preferences.getString(getString(R.string.txtViewDeathsPrefY), null));
            textViewDate.setText(preferences.getString(getString(R.string.txtViewDatePrefY), null));
            textViewActive.setText(preferences.getString(getString(R.string.txtViewActivePrefY), null));
            calculate_percentages();
        }

        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        sharedViewModel.getCasesData().observe(getViewLifecycleOwner(), new Observer<CasesValues>() {
            @Override
            public void onChanged(CasesValues casesValues) {
                casesData = casesValues;
                //update de UI here.
                updated = false;
                updateUI();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!updated)
            updateUI();
    }

    private  void updateUI(){
        if(casesData == null || casesData.isEmpty() || !casesData.hasYesterdayData())
            return;
        String str = casesData.getTotalCases(CasesValues.WORLD_CASES_YESTERDAY);
        textViewCases.setText(casesData.getTotalCases(CasesValues.WORLD_CASES_YESTERDAY));
        textViewRecovered.setText(casesData.getTotalRecoveredCases(CasesValues.WORLD_CASES_YESTERDAY));
        textViewDeaths.setText(casesData.getTotalDeath(CasesValues.WORLD_CASES_YESTERDAY));
        textViewDate.setText(casesData.getAllDates(CasesValues.WORLD_CASES_YESTERDAY));
        textViewActive.setText(casesData.getTotalActiveCases(CasesValues.WORLD_CASES_YESTERDAY));
        textViewNewDeaths.setText(casesData.getTotalNewDeath(CasesValues.WORLD_CASES_YESTERDAY));
        textViewNewCases.setText(casesData.getTotalNewCases(CasesValues.WORLD_CASES_YESTERDAY));

        setListViewCountries(casesData.getAllCountriesResults(CasesValues.WORLD_CASES_YESTERDAY));
        updated = true;
        fragmentSwipeRefreshY.setEnabled(false);
    }

    void setListViewCountries(ArrayList<CountryLine> allCountriesResults) {
        Collections.sort(allCountriesResults);
        listCountriesAdapter = new ListCountriesAdapter(getActivity(), allCountriesResults);
        listViewCountries.setAdapter(listCountriesAdapter);
    }

    public interface OnRefreshCasesDataYesterday{
        public void onRefreshCasesDataYesterday();
    }

    public SwipeRefreshLayout getFragmentSwipeRefreshY(){
        return fragmentSwipeRefreshY;
    }

    void calculate_percentages () {
        DecimalFormat generalDecimalFormat;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        generalDecimalFormat = new DecimalFormat("0.00", symbols);

        double tmpNumber = Double.parseDouble(textViewRecovered.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewRecoveredTitle.setText(getResources().getString(R.string.textViewRecoveredTitle)  + "  " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewDeaths.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewDeathsTitle.setText(getResources().getString(R.string.textViewDeathsTitle) + "  " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewActive.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewActiveTitle.setText(getResources().getString(R.string.textViewActive) + "  " + generalDecimalFormat.format(tmpNumber) + "%");
    }
}
