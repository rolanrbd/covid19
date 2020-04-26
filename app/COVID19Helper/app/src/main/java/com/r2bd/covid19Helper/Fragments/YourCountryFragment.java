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

import android.util.Log;
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
import java.util.Observable;

public class YourCountryFragment extends Fragment{
    private SharedViewModel sharedViewModel;
    OnRefreshCasesDataOfYourCountry refeshListener;

    private CasesValues casesData;
    private boolean updated = false;

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle, textViewHEADSource;
    SharedPreferences.Editor editor;
    ListView listViewStates;
    ListCountriesAdapter listSatesAdapter;
    SwipeRefreshLayout fragmentSwipeRefreshYC;

    public YourCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            refeshListener = (OnRefreshCasesDataOfYourCountry) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        updated = false;
        return inflater.inflate(R.layout.fragment_your_country, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        if (preferences.getString(getString(R.string.txtViewCasesPrefYC), null) != null) {
            textViewCases.setText(preferences.getString(getString(R.string.txtViewCasesPrefYC), null));
            textViewRecovered.setText(preferences.getString(getString(R.string.txtViewRecoveredPrefYC), null));
            textViewDeaths.setText(preferences.getString(getString(R.string.txtViewDeathsPrefYC), null));
            textViewDate.setText(preferences.getString(getString(R.string.txtViewDatePrefYC), null));
            textViewActive.setText(preferences.getString(getString(R.string.txtViewActivePrefYC), null));
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TextView textView = view.findViewById(R.id.txtHello);
        textViewCases = view.findViewById(R.id.textViewCasesYC);
        textViewRecovered = view.findViewById(R.id.textViewRecoveredYC);
        textViewDeaths = view.findViewById(R.id.textViewDeathsYC);
        textViewDate = view.findViewById(R.id.textViewDateYC);
        textViewRecoveredTitle = view.findViewById(R.id.textViewRecoveredTitleYC);
        textViewDeathsTitle = view.findViewById(R.id.textViewDeathsTitleYC);
        textViewActiveTitle = view.findViewById(R.id.textViewActiveTitleYC);
        textViewActive = view.findViewById(R.id.textViewActiveYC);
        textViewNewDeaths = view.findViewById(R.id.textViewNewDeathsYC);
        textViewNewCases = view.findViewById(R.id.textViewNewCasesYC);
        textViewNewCasesTitle = view.findViewById(R.id.textViewNewCasesTitleYC);
        textViewNewDeathsTitle = view.findViewById(R.id.textViewNewDeathsTitleYC);
        listViewStates = view.findViewById(R.id.listViewCountriesYC);
        textViewHEADSource = view.findViewById(R.id.textViewHEADSourceYC);

        listViewStates.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) fragmentSwipeRefreshYC.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        fragmentSwipeRefreshYC.setEnabled(true);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }

            private boolean listIsAtTop() {
                int tmp = listViewStates.getChildCount();
                if (listViewStates.getChildCount() == 0) return true;
                tmp = listViewStates.getChildAt(0).getTop();
                return listViewStates.getChildAt(0).getTop() == 0;
            }
        });
        // Implement Swipe to Refresh
        fragmentSwipeRefreshYC = view.findViewById(R.id.fragmentSwipeRefreshYC);
        fragmentSwipeRefreshYC.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        fragmentSwipeRefreshYC.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updated = false;
                        refeshListener.onRefreshCasesDataOfYourCountry();
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
    public void onResume() {
        if(!updated)
            updateUI();
        super.onResume();
    }

    private  void updateUI(){
        if(casesData == null || casesData.isEmpty() || !casesData.hasYourCountryData())
            return;

        textViewCases.setText(casesData.getTotalCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewRecovered.setText(casesData.getTotalRecoveredCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewDeaths.setText(casesData.getTotalDeath(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewDate.setText(casesData.getAllDates(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewActive.setText(casesData.getTotalActiveCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewNewDeaths.setText(casesData.getTotalNewDeath(CasesValues.YOUR_COUNTRY_CASES_NOW));
        textViewNewCases.setText(casesData.getTotalNewCases(CasesValues.YOUR_COUNTRY_CASES_NOW));
        setListViewCountries(casesData.getAllCountriesResults(CasesValues.YOUR_COUNTRY_CASES_NOW));

        textViewHEADSource.setText(getString(R.string.textViewSource) + casesData.getUrl(CasesValues.YOUR_COUNTRY_CASES_NOW));

        updated = true;
    }

    void setListViewCountries(ArrayList<CountryLine> allCountriesResults) {
        Collections.sort(allCountriesResults);
        listSatesAdapter = new ListCountriesAdapter(getActivity(), allCountriesResults);
        listViewStates.setAdapter(listSatesAdapter);
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

    public interface OnRefreshCasesDataOfYourCountry {
        public void onRefreshCasesDataOfYourCountry();
    }

    public SwipeRefreshLayout getFragmentSwipeRefreshYourCountry(){
        return fragmentSwipeRefreshYC;
    }

    public void setUpdated(boolean b){ updated = b;}

}
