package com.example.nutrilife.ui.cerrar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CerrarViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CerrarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is cerrar fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}