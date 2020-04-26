package com.r2bd.covid19Helper.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<CasesValues> casesData = new MutableLiveData<>();

    public void setCasesData(CasesValues casesData) {
        this.casesData.postValue(casesData);
    }

    public LiveData<CasesValues> getCasesData(){
        return casesData;
    }

    public void clear(){
        this.casesData.removeObserver(new Observer<CasesValues>() {
            @Override
            public void onChanged(CasesValues casesValues) {
                casesData = null;
            }
        });
    }
}
