package com.r2bd.covid19Helper.Country.Spain;

import android.util.Log;

import com.r2bd.covid19Helper.Country.Country;
import com.r2bd.covid19Helper.Country.Cuba.Cuba;
import com.r2bd.covid19Helper.CountryLine;
import com.r2bd.covid19Helper.Models.CasesValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Spain extends Country {

    private String deathURL = "https://raw.githubusercontent.com/datadista/datasets/master/COVID%2019/ccaa_covid19_fallecidos_long.csv";
    private String cvsDeathSplitBy = ",";
    private String newCasesURL = "https://raw.githubusercontent.com/datadista/datasets/master/COVID%2019/ccaa_covid19_casos_long.csv";
    private String cvsNewCasesSplitBy = ",";
    private String recoveredCasesURL = "https://raw.githubusercontent.com/datadista/datasets/master/COVID%2019/ccaa_covid19_altas_long.csv";
    private String cvsRecoveredSplitBy = ",";

    public Spain() {
        url = "https://github.com/datadista/datasets/tree/master/COVID%2019";
    }

    public Spain(CasesValues cv) {
        super(cv);
        url = "https://github.com/datadista/datasets/tree/master/COVID%2019";
    }
    private Map<String, CountryLine> countryDetail = new TreeMap<>();

    private void getDeath() throws MalformedURLException {
        InputStream inputStream = null;
        URL mUrl = new URL(deathURL);
        try{

            HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
            BufferedReader br = null;
            inputStream = con.getInputStream();

            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            int i = 0;
            String[] datosPrevious = null;
            String[] datosCurrent = null;
            String[] datosBefPrev = null;
            while ((line = br.readLine()) != null) {
                ++i;
                if(i==0)
                    continue;
                datosBefPrev = datosPrevious;
                datosPrevious = datosCurrent;
                datosCurrent = line.split(cvsDeathSplitBy);

                if(datosBefPrev == null || datosPrevious == null || datosPrevious[2].equals(datosCurrent[2]) || datosCurrent[2].equals("Total"))
                    continue;
                CountryLine state = countryDetail.get(datosPrevious[2]);
                if(state == null){
                    String tmpNewDeath = String.valueOf(Integer.parseInt(datosPrevious[3]) - Integer.parseInt(datosBefPrev == null ? "0" : datosBefPrev[3]));
                    state = new CountryLine(datosPrevious[2],"0", "0","0", datosPrevious[3], tmpNewDeath);
                    countryDetail.put(datosPrevious[2],state);
                }
                else{
                    String tmpNewDeath = String.valueOf(Integer.parseInt(datosPrevious[3]) - Integer.parseInt(datosBefPrev == null ? "0" : datosBefPrev[3]));
                    state.setDeaths(datosPrevious[3]);
                    state.setNewDeaths(tmpNewDeath);
                }
            }
            con.disconnect();

            String tmpNewDeath = String.valueOf(Integer.parseInt(datosCurrent[3]) - Integer.parseInt(datosBefPrev == null ? "0" : datosPrevious[3]));
            totalDeathCases = Integer.parseInt(datosCurrent[3]);
            totalNewDeath = Integer.parseInt(tmpNewDeath);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getCases() throws MalformedURLException {
        InputStream inputStream = null;
        URL mUrl = new URL(newCasesURL);
        try{

            HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
            BufferedReader br = null;
            inputStream = con.getInputStream();

            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            int i = 0;
            String[] datosPrevious = null;
            String[] datosCurrent = null;
            String[] datosBefPrev = null;
            while ((line = br.readLine()) != null) {
                ++i;
                if(i==0)
                    continue;
                datosBefPrev = datosPrevious;
                datosPrevious = datosCurrent;
                datosCurrent = line.split(cvsNewCasesSplitBy);

                if(datosBefPrev == null || datosPrevious == null || datosPrevious[2].equals(datosCurrent[2]) || datosCurrent[2].equals("Total"))
                    continue;
                CountryLine state = countryDetail.get(datosPrevious[2]);
                if(state == null){
                    String tmpNewCases = String.valueOf(Integer.parseInt(datosPrevious[3]) - Integer.parseInt(datosBefPrev == null ? "0" : datosBefPrev[3]));
                    state = new CountryLine(datosPrevious[2], datosPrevious[3], tmpNewCases,"0", "0", "0");
                    countryDetail.put(datosPrevious[2],state);
                }
                else {
                    String tmpNewCases = String.valueOf(Integer.parseInt(datosPrevious[3]) - Integer.parseInt(datosBefPrev == null ? "0" : datosBefPrev[3]));
                    state.setCases(datosPrevious[3]);
                    state.setNewCases(tmpNewCases);
                }
            }
            con.disconnect();

            String tmpNewCases = String.valueOf(Integer.parseInt(datosCurrent[3]) - Integer.parseInt(datosPrevious == null ? "0" : datosPrevious[3]));
            totalNewCases = Integer.parseInt(tmpNewCases);
            totalCases = Integer.parseInt(datosCurrent[3]);
            lastDate = datosCurrent[0];

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getRecovered() throws MalformedURLException {
        InputStream inputStream = null;
        URL mUrl = new URL(recoveredCasesURL);
        try{

            HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
            BufferedReader br = null;
            inputStream = con.getInputStream();

            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            int i = 0;
            String[] datosPrevious = null;
            String[] datosCurrent = null;
            String[] datosBefPrev = null;
            while ((line = br.readLine()) != null) {
                ++i;
                if(i==0)
                    continue;
                datosBefPrev = datosPrevious;
                datosPrevious = datosCurrent;
                datosCurrent = line.split(cvsRecoveredSplitBy);

                if(datosBefPrev == null || datosPrevious == null || datosPrevious[2].equals(datosCurrent[2]) || datosCurrent[2].equals("Total"))
                    continue;
                CountryLine state = countryDetail.get(datosPrevious[2]);
                if(state == null){
                    state = new CountryLine(datosPrevious[2], "0", "0",datosPrevious[3], "0", "0");
                    countryDetail.put(datosPrevious[3],state);
                }
                else {
                    state.setRecovered(datosPrevious[3]);
                }
            }
            con.disconnect();

            totalRecoveredCases = Integer.parseInt(datosCurrent[3]);
            totalActiveCases = totalCases - totalRecoveredCases;

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void refreshData() throws IOException {
        try{
            getDeath();
            getCases();
            getRecovered();
            populateCasesValues();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void populateCasesValues(){

        ArrayList<CountryLine> countryLines = new ArrayList<>();
        for (Map.Entry<String, CountryLine> it : countryDetail.entrySet()) {
            countryLines.add(it.getValue());
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat generalDecimalFormat = new DecimalFormat("###,###", symbols);

        casesValues.getAllCountriesResults().add(countryLines);
        casesValues.getTotalCases().add(generalDecimalFormat.format(totalCases));
        casesValues.getTotalActiveCases().add(generalDecimalFormat.format(totalActiveCases));
        casesValues.getTotalRecoveredCases().add(generalDecimalFormat.format(totalRecoveredCases));
        casesValues.getTotalDeath().add(generalDecimalFormat.format(totalDeathCases));
        casesValues.getTotalNewDeath().add(generalDecimalFormat.format(totalNewDeath));
        casesValues.getTotalNewCases().add(generalDecimalFormat.format(totalNewCases));

        casesValues.getAllDates().add(lastDate);
        casesValues.getUrlList().add(url);
    }

}
