package com.example.nbtextclassifier.ui.classify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClassifyViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ClassifyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Choose the model to use for classification below");
    }

    public LiveData<String> getText() {
        return mText;
    }
}