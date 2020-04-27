package com.r2bd.covid19Helper.Country.Cuba;

import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.r2bd.covid19Helper.Country.Country;
import com.r2bd.covid19Helper.CountryLine;
import com.r2bd.covid19Helper.Models.CasesValues;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Cuba extends Country {

    protected int totalEvacuados = 0;

    class StateDetailCounter{
        public int newDeath = 0;
        public int newCase = 0;
        public int caseTotal = 0;
    }

    private Map<String, CountryLine> countryDetail = new TreeMap<>();
    private List<String> deathByState = new ArrayList<>();
    List<CubaPatientDetail> patientList = new ArrayList<>();

    public Cuba(){
        super();
        url = "https://covid19cubadata.github.io/data/covid19-cuba.json";
    }
    public Cuba(CasesValues cv){
        super(cv);
        url = "https://covid19cubadata.github.io/data/covid19-cuba.json";
    }

    @Override
    public void refreshData() {
        try {
            URL mUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
            JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));

            reader.beginObject();

                //ignoring schema-version fild
                reader.skipValue();reader.skipValue();
                //ignoring notes fild
                reader.skipValue();reader.skipValue();
                //ignoring numero-reproductivo object fild
                reader.skipValue();reader.skipValue();
                //ignoring event object fild
                reader.skipValue();reader.skipValue();
                //ignoring isolation centers object fild
                reader.skipValue();reader.skipValue();
                //ignoring diagnostic centers object fild
                reader.skipValue();reader.skipValue();

                JsonToken jtoken = reader.peek();
                if(reader.peek() == JsonToken.NAME && reader.nextName().equals("casos")){
                    reader.beginObject();
                        if(reader.peek() == JsonToken.NAME && reader.nextName().equals("dias")){
                            reader.beginObject();
                                int dayCounter = 0;
                                while (reader.hasNext()/*day Object*/) {
                                    jtoken = reader.peek();
                                    if(jtoken == JsonToken.END_OBJECT) {
                                        break;
                                    }
                                    ++dayCounter;
                                    reader.nextName();
                                    reader.beginObject();
                                        jtoken = reader.peek();
                                        totalNewDeath = 0;
                                        totalNewCases = 0;
                                        while (jtoken != JsonToken.END_OBJECT){
                                            jtoken = reader.peek();
                                            if(jtoken == JsonToken.END_OBJECT) {
                                                break;
                                            }
                                            String tokenDayName = reader.nextName();
                                            switch (tokenDayName){
                                                case "fecha":
                                                    lastDate = reader.nextString();
                                                    break;
                                                case "muertes_numero":
                                                    totalNewDeath = reader.nextInt();
                                                    break;
                                                case "muertes_id":{
                                                    jtoken = reader.peek();
                                                    reader.beginArray();
                                                    while (jtoken != JsonToken.END_ARRAY){
                                                        jtoken = reader.peek();
                                                        if(jtoken == JsonToken.END_ARRAY)
                                                            break;
                                                        deathByState.add(reader.nextString());
                                                    }
                                                    reader.endArray();
                                                }
                                                    break;
                                                case "evacuados_numero":
                                                    totalEvacuados += reader.nextInt();
                                                    break;
                                                case "diagnosticados":{
                                                    reader.beginArray();
                                                    int patientCounter = 0;
                                                    while (reader.hasNext()){
                                                        jtoken = reader.peek();
                                                        if(jtoken == JsonToken.END_OBJECT) {
                                                            break;
                                                        }

                                                        CubaPatientDetail patient = new CubaPatientDetail();
                                                        ++patientCounter;

                                                        reader.beginObject();
                                                        while (jtoken != JsonToken.END_OBJECT) {
                                                            jtoken = reader.peek();

                                                            if(jtoken == JsonToken.END_OBJECT) {
                                                                break;
                                                            }
                                                            String tokenPatientDetailName = reader.nextName();
                                                            switch (tokenPatientDetailName){
                                                                case "id":
                                                                    patient.setId(reader.nextString());
                                                                    break;
                                                                case "pais":
                                                                    patient.setPais(reader.nextString());
                                                                    break;
                                                                case "edad":
                                                                    patient.setEdad(reader.nextInt());
                                                                    break;
                                                                case "sexo":
                                                                    patient.setSexo(reader.nextString());
                                                                    break;
                                                                case "municipio_detección":
                                                                    patient.setMunicipio(reader.nextString());
                                                                    break;
                                                                case "provincia_detección":
                                                                    patient.setProvicia(reader.nextString());
                                                                    break;
                                                                default:
                                                                    reader.skipValue();
                                                            }
                                                        }
                                                        reader.endObject();
                                                        ++totalNewCases;
                                                        patient.setDate(lastDate);
                                                        patientList.add(patient);
                                                        jtoken = reader.peek();
                                                    }
                                                    reader.endArray();
                                                }
                                                    break;
                                                case "recuperados_numero":
                                                    totalRecoveredCases += reader.nextInt();
                                                    break;
                                                default:
                                                    reader.skipValue();
                                            }
                                        }
                                    reader.endObject();
                                    totalCases += totalNewCases;
                                    totalDeathCases += totalNewDeath;
                                }
                            reader.endObject();
                        }
                    reader.endObject();
                }
            reader.endObject();
            con.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        totalActiveCases = totalCases - totalDeathCases - totalRecoveredCases - totalEvacuados;
        populateCasesValues();
    }

    private void populateCasesValues(){
        Map<String, StateDetailCounter> stateCounter = new TreeMap<>();
        for (CubaPatientDetail itPatient : patientList) {

            boolean deathFound = false;
            if(!deathByState.isEmpty())
                deathFound = deathByState.remove(itPatient.getId());
            CountryLine state = countryDetail.get(itPatient.getProvicia());

            if(state == null){
                String cases = "1";
                String newCases = "1";
                String recovered = "0";
                String death = "0";
                String newDeath = "0";
                String prov = itPatient.getProvicia();
                state = new CountryLine(prov, cases, newCases, recovered, death, newDeath);
                countryDetail.put(prov, state);
            }
            else{
                StateDetailCounter stateDetailCounter = stateCounter.get(itPatient.getProvicia());
                if(stateDetailCounter == null) {
                    stateDetailCounter = new StateDetailCounter();
                    stateCounter.put(itPatient.getProvicia(), stateDetailCounter);
                }
                if(itPatient.getDate().equals(lastDate))
                    ++stateDetailCounter.newCase;

                ++stateDetailCounter.caseTotal;

                //int recovered = Integer.parseInt(state.getRecovered());
                //state.setRecovered(String.valueOf(recovered));
            }

            if(deathFound){
                int death = Integer.parseInt(state.getDeaths()) + 1;
                state.setDeaths(String.valueOf(death));
            }
        }

        ArrayList<CountryLine> countryLines = new ArrayList<>();
        for (Map.Entry<String, CountryLine> it : countryDetail.entrySet()) {
            StateDetailCounter stateDetailCounter = stateCounter.get(it.getKey());
            it.getValue().setNewCases(String.valueOf(stateDetailCounter == null ? 0 : stateDetailCounter.newCase));
            it.getValue().setCases(String.valueOf(stateDetailCounter == null ? 0 : stateDetailCounter.caseTotal));
            countryLines.add(it.getValue());
        }

        casesValues.getAllCountriesResults().add(countryLines);
        casesValues.getTotalCases().add(String.valueOf(totalCases));
        casesValues.getTotalActiveCases().add(String.valueOf(totalActiveCases));
        casesValues.getTotalRecoveredCases().add(String.valueOf(totalRecoveredCases));
        casesValues.getTotalDeath().add(String.valueOf(totalDeathCases));
        casesValues.getTotalNewDeath().add(String.valueOf(totalNewDeath));
        casesValues.getTotalNewCases().add(String.valueOf(totalNewCases));

        casesValues.getAllDates().add(lastDate);
        casesValues.getUrlList().add(url);
    }

}
