package com.r2bd.covid19Helper.Country.Cuba;

import com.google.gson.annotations.Expose;

public class CubaPatientDetail {

    public static class Evento{

        @Expose private String id;
        @Expose private String identificador;
        private String municipio;
        private String provincia;
        private double dpacode_municipio;
        private double dpacode_provincia;
        @Expose private double lon;
        @Expose private double lat;
        @Expose private boolean abierto;

        public Evento(){}
        public String getMunicipio() {
            return municipio;
        }

        public void setMunicipio(String municipio) {
            this.municipio = municipio;
        }

        public String getProvincia() {
            return provincia;
        }

        public void setProvincia(String provincia) {
            this.provincia = provincia;
        }

        public double getDpacode_municipio() {
            return dpacode_municipio;
        }

        public void setDpacode_municipio(double dpacode_municipio) {
            this.dpacode_municipio = dpacode_municipio;
        }

        public double getDpacode_provincia() {
            return dpacode_provincia;
        }

        public void setDpacode_provincia(double dpacode_provincia) {
            this.dpacode_provincia = dpacode_provincia;
        }
    }

    @Expose private String schema_version;
    @Expose private String note_ext;

    private String id;
    private String pais;
    private int edad;
    private String sexo;
    private String provicia;
    private String municipio;
    private String date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CubaPatientDetail(){}
    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getProvicia() {
        return provicia;
    }

    public void setProvicia(String provicia) {
        this.provicia = provicia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public class CentrosAislamientos{
        @Expose private String id;
        @Expose private String nombre;
        @Expose private String provincia;
        @Expose private String dpacode_provincia;
    }
    public class Casos{
        public class Dias{
            @Expose private String fecha;
        }
    }
}
