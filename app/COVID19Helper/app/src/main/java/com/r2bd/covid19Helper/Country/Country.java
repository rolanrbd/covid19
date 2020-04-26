package com.r2bd.covid19Helper.Country;

import com.r2bd.covid19Helper.Models.CasesValues;

import java.io.IOException;

public abstract class Country {
    protected CasesValues casesValues;

    protected int totalCases = 0;
    protected int totalRecoveredCases = 0;
    protected int totalDeathCases = 0;
    protected int totalActiveCases = 0;
    protected int totalNewCases = 0;
    protected int totalNewDeath = 0;
    protected String lastDate = "";

    protected String url="";
    public abstract void refreshData() throws IOException;

    public Country(){}
    public Country(CasesValues cv){
        this.casesValues = cv;
    }

    public CasesValues getCasesValues(){return casesValues;}
    public void setCasesValues(CasesValues cv){casesValues = cv;}
    public String getUrl(){return url;}
}
