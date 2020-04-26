package com.r2bd.covid19Helper.Country.USA;

import android.util.Log;

import com.r2bd.covid19Helper.Country.Country;
import com.r2bd.covid19Helper.CountryLine;
import com.r2bd.covid19Helper.Models.CasesValues;
import com.r2bd.covid19Helper.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class USA extends Country {

    private int totalEvacuados = 0;

    public USA(){
        super();
        url = "https://www.worldometers.info/coronavirus/country/us/";
    }

    public USA(CasesValues cv){
        super(cv);
        url = "https://www.worldometers.info/coronavirus/country/us/";
    }

    @Override
    public void refreshData() throws IOException {

        Document doc;
        Element countriesTable, row;
        Elements countriesRows, cols;
        Iterator<Element> rowIterator;
        int colNumCountry = 0, colNumCases = 1, colNumDeaths = 0, colNumActive = 0, colNumNewCases = 0, colNumNewDeaths = 0;
        String tmpState, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, tmpNewCases, tmpNewDeaths;
        SimpleDateFormat myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat generalDecimalFormat = new DecimalFormat("0.00", symbols);
        DecimalFormat generalMilesFormat = new DecimalFormat("###,###", symbols);

        doc = Jsoup.connect(url).timeout(10000).get();

        countriesTable = doc.select("table").get(0);
        countriesRows = countriesTable.select("tr");

        rowIterator = countriesRows.iterator();
        boolean hasSelectedCountry = casesValues.hasYourCountryData();

        ArrayList<CountryLine> usaStates = new ArrayList<CountryLine>();
        if(hasSelectedCountry)
            casesValues.getAllCountriesResults().set(CasesValues.YOUR_COUNTRY_CASES_NOW,usaStates);
        else
            casesValues.getAllCountriesResults().add(usaStates);

        // read table header and find correct column number for each category
        row = rowIterator.next();
        cols = row.select("th");
        //Log.e("COLS: ", cols.text());
        if (cols.get(0).text().contains("USA")) {
            for(int i=1; i < cols.size(); i++){
                if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases"))
                {colNumCases = i; Log.e("Cases: ", cols.get(i).text());}
                else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths"))
                {colNumDeaths = i; Log.e("Deaths: ", cols.get(i).text());}
                else if (cols.get(i).text().contains("Active") && cols.get(i).text().contains("Cases"))
                {colNumActive = i; Log.e("Active: ", cols.get(i).text());}
                else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Cases"))
                {colNumNewCases = i; Log.e("NewCases: ", cols.get(i).text());}
                else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Deaths"))
                {colNumNewDeaths = i; Log.e("NewDeaths: ", cols.get(i).text());}
            }
        }

        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            cols = row.select("td");
            int localCases = 0, localDeath = 0, localActive = 0, localRecovered = 0;
            if (cols.get(0).text().contains("USA Total")) {

                String strNumCases = cols.get(colNumCases).text();
                localCases = Integer.parseInt(strNumCases.replaceAll(",", ""));
                if(!hasSelectedCountry)
                    casesValues.getTotalCases().add(strNumCases);
                else
                    casesValues.getTotalCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strNumCases);

                String strNumDeath = cols.get(colNumDeaths).text();
                localDeath = Integer.parseInt(strNumDeath.replaceAll(",", ""));
                if(!hasSelectedCountry)
                    casesValues.getTotalDeath().add(strNumDeath);
                else
                    casesValues.getTotalDeath().set(CasesValues.YOUR_COUNTRY_CASES_NOW, strNumCases);

                String strActiveCases = "0";
                if (cols.get(colNumActive).hasText())
                    strActiveCases = cols.get(colNumActive).text();
                if(!hasSelectedCountry)
                    casesValues.getTotalActiveCases().add(strActiveCases);
                else
                    casesValues.getTotalActiveCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW, strActiveCases);
                localActive = Integer.parseInt(strActiveCases.replaceAll(",", ""));

                String strTotalNewCases = "0";
                if (cols.get(colNumNewCases).hasText())
                    strTotalNewCases = cols.get(colNumNewCases).text();
                if(!hasSelectedCountry)
                    casesValues.getTotalNewCases().add(strTotalNewCases);
                else
                    casesValues.getTotalNewCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW, strTotalNewCases);

                String strTotalNewDeath = "0";
                if (cols.get(colNumNewDeaths).hasText())
                    strTotalNewDeath = cols.get(colNumNewDeaths).text();
                if(!hasSelectedCountry)
                    casesValues.getTotalNewDeath().add(strTotalNewDeath);
                else
                    casesValues.getTotalNewDeath().set(CasesValues.YOUR_COUNTRY_CASES_NOW, strTotalNewDeath);

                String strTotalRecovered = generalMilesFormat.format(Integer.parseInt(strNumCases.replaceAll(",", "")) - Integer.parseInt(strNumDeath.replaceAll(",", "")) - Integer.parseInt(strActiveCases.replaceAll(",", "")));
                if(!hasSelectedCountry)
                    casesValues.getTotalRecoveredCases().add(strTotalRecovered);
                else
                    casesValues.getTotalRecoveredCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW, strTotalRecovered);

                continue;
            }

            if(cols.get(colNumCountry).hasText() && cols.get(colNumCountry).text().equals("Total:"))
                continue;

            if (cols.get(colNumCountry).hasText()) {tmpState = cols.get(0).text();}
            else {tmpState = "NA";}

            if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
            else {tmpCases = "0";}

            if(cols.get(colNumDeaths).hasText()) {
                tmpDeaths = cols.get(colNumDeaths).text();
                tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpDeaths.replaceAll(",", ""))
                        / Double.parseDouble(tmpCases.replaceAll(",", ""))
                        * 100)) + "%";
                tmpDeaths = tmpDeaths + "\n" + tmpPercentage;
            }
            else {tmpDeaths = "0";}

            if (cols.get(colNumNewCases).hasText()) {tmpNewCases = cols.get(colNumNewCases).text();}
            else {tmpNewCases = "0";}

            if (cols.get(colNumNewDeaths).hasText()) {tmpNewDeaths = cols.get(colNumNewDeaths).text();}
            else {tmpNewDeaths = "0";}

            localRecovered = localCases - localActive - localDeath;
            tmpRecovered = String.valueOf(localRecovered);
            tmpPercentage = (generalDecimalFormat.format( localRecovered/ Double.parseDouble(tmpCases.replaceAll(",", "")) * 100)) + "%";
            tmpRecovered = tmpRecovered + "\n" + tmpPercentage;


            usaStates.add(new CountryLine(tmpState, tmpCases, tmpNewCases, tmpRecovered, tmpDeaths, tmpNewDeaths));
        }
        Calendar myCalender = Calendar.getInstance();
        casesValues.getAllDates().add(myFormat.format(myCalender.getTime()));
        casesValues.getUrlList().add(url);
    }
}
