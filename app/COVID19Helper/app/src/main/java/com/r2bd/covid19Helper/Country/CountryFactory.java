package com.r2bd.covid19Helper.Country;

import com.r2bd.covid19Helper.Country.Canada.Canada;
import com.r2bd.covid19Helper.Country.Cuba.Cuba;
import com.r2bd.covid19Helper.Country.Spain.Spain;
import com.r2bd.covid19Helper.Country.USA.USA;

public  class CountryFactory {
    public static Country getCountry(String cName){
        Country c = null;
        switch (cName){
            case "Cuba":
                    c = new Cuba();
                break;
            case "USA":
                c = new USA();
                break;
            case "Spain":
                c = new Spain();
                break;
            case "Canada":
                c = new Canada();
                break;
        }
        return c;
    }
}
