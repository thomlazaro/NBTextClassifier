package com.example.nbtextclassifier.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Text Classification App that use Naive Bayes as its algorithm. The app will read the text on spreadsheet and identifies the Category of each Text");
    }

    public LiveData<String> getText() {
        return mText;
    }
}