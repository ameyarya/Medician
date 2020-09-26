package com.medician.ui.covid19;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CovidViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CovidViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Covid fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}