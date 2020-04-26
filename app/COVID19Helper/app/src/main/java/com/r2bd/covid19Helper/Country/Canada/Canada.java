package com.r2bd.covid19Helper.Country.Canada;

import com.r2bd.covid19Helper.Country.Country;
import com.r2bd.covid19Helper.CountryLine;
import com.r2bd.covid19Helper.Models.CasesValues;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Canada extends Country {

    String urlToCSV = "https://health-infobase.canada.ca/src/data/covidLive/covid19.csv";
    public Canada() {
        url="https://www.ctvnews.ca/mobile/health/coronavirus/tracking-every-case-of-covid-19-in-canada-1.4852102";
    }

    public Canada(CasesValues cv) {
        super(cv);
        url="https://www.ctvnews.ca/mobile/health/coronavirus/tracking-every-case-of-covid-19-in-canada-1.4852102";
    }
    private String cvsSplitBy = ",";
    Map<String, String[]> stateDetailCurr = new TreeMap<>();
    Map<String, String[]> stateDetailPrevious = new TreeMap<>();
    Document doc;
    Element countriesTable, row;
    Elements countriesRows, cols;
    Iterator<Element> rowIterator;

    ArrayList<CountryLine> canadaStates = new ArrayList<CountryLine>();
    @Override
    public void refreshData() throws IOException {


        InputStream inputStream = null;
        URL mUrl = new URL(urlToCSV);
        try {

            //Getting the old information
            HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
            BufferedReader br = null;
            inputStream = con.getInputStream();

            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            int i = 0;;
            String[] datosCurrent = null;
            while ((line = br.readLine()) != null) {
                ++i;
                if(i==0)
                    continue;
                datosCurrent = line.split(cvsSplitBy);
                String[] dataC = stateDetailCurr.get(datosCurrent[1]);

                stateDetailCurr.put(datosCurrent[1], datosCurrent);
                stateDetailPrevious.put(datosCurrent[1], dataC);
            }
            con.disconnect();//*/

            doc = Jsoup.connect(url).timeout(10000).get();

            //Getting the total of the country
            countriesTable = doc.select("table").get(0);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();
            boolean hasSelectedCountry = casesValues.hasYourCountryData();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            String strTotalCase= cols.get(0).text();
            if(hasSelectedCountry)
                casesValues.getTotalCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strTotalCase);
            else
                casesValues.getTotalCases().add(strTotalCase);

            String strNewDeath = cols.get(1).text();
            if(hasSelectedCountry)
                casesValues.getTotalNewCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strNewDeath);
            else
                casesValues.getTotalNewCases().add(strNewDeath);

            countriesTable = doc.select("table").get(1);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }

            String strTotalActive= cols.get(0).text();
            if(hasSelectedCountry)
                casesValues.getTotalActiveCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strTotalActive);
            else
                casesValues.getTotalActiveCases().add(strTotalActive);

            String strRecovered = cols.get(1).text();
            if(hasSelectedCountry)
                casesValues.getTotalRecoveredCases().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strRecovered);
            else
                casesValues.getTotalRecoveredCases().add(strRecovered);

            String strTotalDeath = cols.get(2).text();
            if(hasSelectedCountry)
                casesValues.getTotalDeath().set(CasesValues.YOUR_COUNTRY_CASES_NOW,strTotalDeath);
            else
                casesValues.getTotalDeath().add(strTotalDeath);

            //Britis Columbia
            countriesTable = doc.select("table").get(2);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            //Bristish Columbia Total
            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Bristish Columbia Active, Recovered and deceased
            countriesTable = doc.select("table").get(3);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("British Columbia", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Alberta
            countriesTable = doc.select("table").get(5);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Alberta Active, Recovered and deceased
            countriesTable = doc.select("table").get(6);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Alberta", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Saskatchewan
            countriesTable = doc.select("table").get(8);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Saskatchewan Active, Recovered and deceased
            countriesTable = doc.select("table").get(9);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Saskatchewan", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Manitoba
            countriesTable = doc.select("table").get(11);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Manitoba Active, Recovered and deceased
            countriesTable = doc.select("table").get(12);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Manitoba", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Ontario
            countriesTable = doc.select("table").get(14);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Ontario Active, Recovered and deceased
            countriesTable = doc.select("table").get(15);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Ontario", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Quebec
            countriesTable = doc.select("table").get(17);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Quebec Active, Recovered and deceased
            countriesTable = doc.select("table").get(18);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Quebec", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //New Brunswick
            countriesTable = doc.select("table").get(20);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //New Brunswick Active, Recovered and deceased
            countriesTable = doc.select("table").get(21);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("New Brunswick", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Nova Scotia
            countriesTable = doc.select("table").get(23);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Nova Scotia Active, Recovered and deceased
            countriesTable = doc.select("table").get(24);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Nova Scotia", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Prince Edward Island
            countriesTable = doc.select("table").get(26);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Prince Edward Island Active, Recovered and deceased
            countriesTable = doc.select("table").get(27);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Prince Edward Island", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Newfoundland and Labrador
            countriesTable = doc.select("table").get(29);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Newfoundland and Labrador Active, Recovered and deceased
            countriesTable = doc.select("table").get(30);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Newfoundland and Labrador", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Yukon
            countriesTable = doc.select("table").get(32);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Yukon Active, Recovered and deceased
            countriesTable = doc.select("table").get(33);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Yukon", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Northwest Territories
            countriesTable = doc.select("table").get(35);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Northwest Territories Active, Recovered and deceased
            countriesTable = doc.select("table").get(36);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Northwest Territories", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            //Nunavut
            countriesTable = doc.select("table").get(38);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Total")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strTotalCase = cols.get(0).text();

            //Nunavut Active, Recovered and deceased
            countriesTable = doc.select("table").get(39);
            countriesRows = countriesTable.select("tr");
            rowIterator = countriesRows.iterator();

            row = rowIterator.next();
            cols = row.select("th");
            if(cols.get(0).text().contains("Active")) {
                row = rowIterator.next();
                cols = row.select("td");
            }
            strRecovered = cols.get(1).text();
            strTotalDeath = cols.get(2).text();

            canadaStates.add(new CountryLine("Nunavut", strTotalCase, "0", strRecovered, strTotalDeath, "0"));

            populateCasesValues();
            boolean b = true;
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void populateCasesValues(){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat generalDecimalFormat = new DecimalFormat("###,###", symbols);

        String[] data = stateDetailPrevious.get("Canada");
        String strOldDeath = data[6];
        String crrDeath = casesValues.getTotalDeath(CasesValues.YOUR_COUNTRY_CASES_NOW);
        String newDeath = generalDecimalFormat.format(Integer.parseInt(crrDeath.replace(",","")) - Integer.parseInt(strOldDeath));
        casesValues.getTotalNewDeath().add(newDeath);

        for ( CountryLine itState: canadaStates ) {
            String[] oldData = stateDetailPrevious.get(itState.getCountryName());
            if(oldData == null)
                continue;
            strOldDeath = oldData[6];
            crrDeath = itState.getDeaths();

            newDeath = generalDecimalFormat.format(Math.abs(Integer.parseInt(crrDeath.replace(",","")) -
                                                            Integer.parseInt(strOldDeath.replace(",",""))));
            itState.setNewDeaths(newDeath);

            String oldCases = oldData[4];
            String crrCases = itState.getCases();
            String newCases = generalDecimalFormat.format(Math.abs(Integer.parseInt(crrCases.replace(",","")) -
                                                                  Integer.parseInt(oldCases.replace(",",""))));
            itState.setNewCases(newCases);
        }

        Calendar myCalender = Calendar.getInstance();
        SimpleDateFormat myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        casesValues.getAllDates().add(myFormat.format(myCalender.getTime()));

        casesValues.getAllCountriesResults().add(canadaStates);
        casesValues.getAllDates().add(lastDate);
        casesValues.getUrlList().add(url);
    }
}
