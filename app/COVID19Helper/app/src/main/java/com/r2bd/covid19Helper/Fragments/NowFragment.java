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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public class NowFragment extends Fragment {

    OnRefreshCasesDataNow refeshListener;
    OnClickCountryListView itemClickListener;

    private SharedViewModel sharedViewModel;
    private CasesValues casesData;
    private boolean updated = false;

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle;
    SharedPreferences.Editor editor;
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    SwipeRefreshLayout fragmentSwipeRefreshN;
    ProgressBar countryProgressBarN;

    public NowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {

            refeshListener = (OnRefreshCasesDataNow) context;
            //itemClickListener = (OnClickCountryListView) context;

        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        updated = false;
        return inflater.inflate(R.layout.fragment_now, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TextView textView = view.findViewById(R.id.txtHello);

        textViewCases = view.findViewById(R.id.textViewCasesN);
        textViewRecovered = view.findViewById(R.id.textViewRecoveredN);
        textViewDeaths = view.findViewById(R.id.textViewDeathsN);
        textViewDate = view.findViewById(R.id.textViewDateN);
        textViewRecoveredTitle = view.findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = view.findViewById(R.id.textViewDeathsTitle);
        textViewActiveTitle = view.findViewById(R.id.textViewActiveTitle);
        textViewActive = view.findViewById(R.id.textViewActiveN);
        textViewNewDeaths = view.findViewById(R.id.textViewNewDeathsN);
        textViewNewCases = view.findViewById(R.id.textViewNewCasesN);
        textViewNewCasesTitle = view.findViewById(R.id.textViewNewCasesTitle);
        textViewNewDeathsTitle = view.findViewById(R.id.textViewNewDeathsTitle);
        listViewCountries = view.findViewById(R.id.listViewCountriesN);
        countryProgressBarN = view.findViewById(R.id.countryProgressBarN);

        listViewCountries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) fragmentSwipeRefreshN.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        fragmentSwipeRefreshN.setEnabled(true);
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
        listViewCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = casesData.getAllCountriesResults(CasesValues.WORLD_CASES_NOW).get(position).getCountryName();
                itemClickListener = (OnClickCountryListView) getContext();
                itemClickListener.onClickCountryListViewItem(str);
            }
        });
        // Implement Swipe to Refresh
        fragmentSwipeRefreshN = view.findViewById(R.id.fragmentSwipeRefreshN);
        fragmentSwipeRefreshN.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        fragmentSwipeRefreshN.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refeshListener.onRefreshCasesDataNow();
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
        if (preferences.getString(getString(R.string.txtViewCasesPrefNow), null) != null) {
            textViewCases.setText(preferences.getString(getString(R.string.txtViewCasesPrefNow), null));
            textViewRecovered.setText(preferences.getString(getString(R.string.txtViewRecoveredPrefNow), null));
            textViewDeaths.setText(preferences.getString(getString(R.string.txtViewDeathsPrefNow), null));
            textViewDate.setText(preferences.getString(getString(R.string.txtViewDatePrefNow), null));
            textViewActive.setText(preferences.getString(getString(R.string.txtViewActivePrefNow), null));
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
        if(casesData == null || casesData.isEmpty() || !casesData.hasNowData())
            return;

        textViewCases.setText(casesData.getTotalCases(CasesValues.WORLD_CASES_NOW));
        textViewRecovered.setText(casesData.getTotalRecoveredCases(CasesValues.WORLD_CASES_NOW));
        textViewDeaths.setText(casesData.getTotalDeath(CasesValues.WORLD_CASES_NOW));
        textViewDate.setText(casesData.getAllDates(CasesValues.WORLD_CASES_NOW));
        textViewActive.setText(casesData.getTotalActiveCases(CasesValues.WORLD_CASES_NOW));
        textViewNewDeaths.setText(casesData.getTotalNewDeath(CasesValues.WORLD_CASES_NOW));
        textViewNewCases.setText(casesData.getTotalNewCases(CasesValues.WORLD_CASES_NOW));

        setListViewCountries(casesData.getAllCountriesResults(CasesValues.WORLD_CASES_NOW));
        updated = true;
    }

    void setListViewCountries(ArrayList<CountryLine> allCountriesResults) {
        Collections.sort(allCountriesResults);
        listCountriesAdapter = new ListCountriesAdapter(getActivity(), allCountriesResults);
        listViewCountries.setAdapter(listCountriesAdapter);
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

    public SwipeRefreshLayout getFragmentSwipeRefreshNow(){
        return fragmentSwipeRefreshN;
    }

    public interface OnRefreshCasesDataNow {
        public void onRefreshCasesDataNow();
    }

    public interface OnClickCountryListView{
        public void onClickCountryListViewItem(String itemClicked);
    }

    public ProgressBar getCountryProgressBar(){
        return countryProgressBarN;
    }
}
