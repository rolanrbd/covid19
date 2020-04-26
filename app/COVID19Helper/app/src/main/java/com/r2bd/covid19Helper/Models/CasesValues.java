package com.r2bd.covid19Helper.Models;

import com.r2bd.covid19Helper.CountryLine;

import java.util.ArrayList;
import java.util.List;

public class CasesValues {

    public static final int WORLD_CASES_NOW = 0;
    public static final int WORLD_CASES_YESTERDAY = 1;
    public static final int YOUR_COUNTRY_CASES_NOW = 2;
    public static final int SELECTED_COUNTRY_CASES_NOW = 3;

    private boolean empty = true;
    private boolean hasNowData = false;
    private boolean hasYesterdayData = false;
    private boolean hasYourCountryData = false;

    private List<String> urlList = new ArrayList<>();
    private List<String> totalCases = new ArrayList<>();
    private List<String> totalActiveCases = new ArrayList<>();
    private List<String> totalRecoveredCases = new ArrayList<>();
    private List<String> totalDeath = new ArrayList<>();
    private List<String> totalNewCases = new ArrayList<>();
    private List<String> totalNewDeath = new ArrayList<>();
    private List<String> allDates = new ArrayList<>();

    private List<ArrayList<CountryLine>> allCountriesResults = new ArrayList<>();

    public  CasesValues(){
        empty = true;
    }
    public List<String> getUrlList(){
        return urlList;
    }

    public String getUrl(int idx){
        return urlList.get(idx);
    }
    public List<String> getTotalCases() {
        return totalCases;
    }

    public String getTotalCases(int i) {
        return totalCases.get(i);
    }

    public List<String> getTotalActiveCases() {
        return totalActiveCases;
    }

    public String getTotalActiveCases(int i) {
        return totalActiveCases.get(i);
    }

    public List<String> getTotalRecoveredCases() {
        return totalRecoveredCases;
    }

    public String getTotalRecoveredCases(int i) {
        return totalRecoveredCases.get(i);
    }

    public List<String> getTotalDeath() {
        return totalDeath;
    }

    public String getTotalDeath(int i) {
        return totalDeath.get(i);
    }

    public List<String> getTotalNewCases() {
        return totalNewCases;
    }

    public String getTotalNewCases(int i) {
        return totalNewCases.get(i);
    }

    public List<String> getTotalNewDeath() {
        return totalNewDeath;
    }

    public String getTotalNewDeath(int i) {
        return totalNewDeath.get(i);
    }

    public List<ArrayList<CountryLine>> getAllCountriesResults() {
        return allCountriesResults;
    }

    public ArrayList<CountryLine> getAllCountriesResults(int i) {
        return allCountriesResults.get(i);
    }

    public boolean isEmpty() {
        empty = !(totalActiveCases != null && !totalActiveCases.isEmpty() &&
                totalRecoveredCases != null && !totalRecoveredCases.isEmpty() &&
                totalDeath != null && !totalDeath.isEmpty() &&
                totalNewCases != null && !totalNewCases.isEmpty() &&
                totalNewDeath != null && !totalNewDeath.isEmpty() &&
                allCountriesResults != null && !allCountriesResults.isEmpty());
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean hasNowData() {
        hasNowData = !isEmpty() && totalCases.size() >= 1 &&  totalActiveCases.size() >= 1
                    &&  totalRecoveredCases.size() >= 1 && totalDeath.size() >= 1
                    && totalNewCases.size() >= 1 && totalNewDeath.size() >= 1;
        return  hasNowData;
    }

    public boolean hasYesterdayData() {
        hasYesterdayData = !isEmpty() && totalCases.size() >= 2 &&  totalActiveCases.size() >= 2
                &&  totalRecoveredCases.size() >= 2  && totalDeath.size() >= 2 &&
                totalNewCases.size() >= 2 && totalNewDeath.size() >= 2;
        return hasYesterdayData;
    }

    public boolean hasYourCountryData(){
        hasYourCountryData = !isEmpty() && totalCases.size() >= 3 &&  totalActiveCases.size() >= 3
                &&  totalRecoveredCases.size() >= 3  && totalDeath.size() >= 3 &&
                totalNewCases.size() >= 3 && totalNewDeath.size() >= 3;
        return hasYourCountryData;
    }

    public void removeCurrentSelectedCountry(){
        totalCases.remove(2);
        totalActiveCases.remove(2);
        totalRecoveredCases.remove(2);
        totalDeath.remove(2);
        totalNewCases.remove(2);
        totalNewDeath.remove(2);
        allDates.remove(2);
        allCountriesResults.remove(2);
    }

    public List<String> getAllDates() {
        return allDates;
    }

    public String getAllDates(int i) {
        return allDates.get(i);
    }

    static public void copyCaseValues(CasesValues source, CasesValues copy){
        copy.empty = source.empty;
        copy.hasNowData = source.hasNowData;
        copy.hasYesterdayData = source.hasYesterdayData;

        copy.urlList.add(source.urlList.get(0));
        copy.urlList.add(source.urlList.get(1));

        copy.totalCases.add(source.totalCases.get(0));
        copy.totalCases.add(source.totalCases.get(1));

        copy.totalActiveCases.add(source.totalActiveCases.get(0));
        copy.totalActiveCases.add(source.totalActiveCases.get(1));

        copy.totalRecoveredCases.add(source.totalRecoveredCases.get(0));
        copy.totalRecoveredCases.add(source.totalRecoveredCases.get(1));

        copy.totalDeath.add(source.totalDeath.get(0));
        copy.totalDeath.add(source.totalDeath.get(1));

        copy.totalNewCases.add(source.totalNewCases.get(0));
        copy.totalNewCases.add(source.totalNewCases.get(1));

        copy.totalNewDeath.add(source.totalNewDeath.get(0));
        copy.totalNewDeath.add(source.totalNewDeath.get(1));

        copy.allDates.add(source.allDates.get(0));
        copy.allDates.add(source.allDates.get(1));

        copy.allCountriesResults.add(source.allCountriesResults.get(0));
        copy.allCountriesResults.add(source.allCountriesResults.get(1));
    }
}
