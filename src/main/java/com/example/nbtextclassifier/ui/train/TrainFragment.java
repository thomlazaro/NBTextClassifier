package com.example.nbtextclassifier.ui.train;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.nbtextclassifier.MainActivity;
import com.example.nbtextclassifier.R;
import com.example.nbtextclassifier.databinding.FragmentTrainBinding;




public class TrainFragment extends Fragment {

    private FragmentTrainBinding binding;
    private Spinner modelSpin;
    private RadioGroup rgColumn;
    private RadioGroup rgSheet;
    private EditText edtTxtCatCol;
    private EditText edtTxtTextCol;
    private EditText edtSheetName;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TrainViewModel trainViewModel =
                new ViewModelProvider(this).get(TrainViewModel.class);

        binding = FragmentTrainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //get view of needed component
        rgColumn = binding.rgColumn;//column radiogroup
        edtTxtCatCol = binding.edtTxtCatCol;
        edtTxtTextCol = binding.edtTxtTextCol;

        rgSheet = binding.rgSheet;//sheet radiogroup
        edtSheetName = binding.edtSheetName;


        //rgColumn listener to enable and disable editTxt column for customization
        rgColumn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID){
                    case R.id.rbDefault://disable editText for Columns
                        edtTxtCatCol.setEnabled(false);
                        edtTxtTextCol.setEnabled(false);
                        break;
                    case R.id.rbCustom://enable editText for Columns
                        edtTxtCatCol.setEnabled(true);
                        edtTxtTextCol.setEnabled(true);
                        break;
                }
            }
        });

        //rgSheet listener to enable and disable editTxt sheet for customization
        rgSheet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID){
                    case R.id.rbDefaultSheet://disable editText for Sheet
                        edtSheetName.setEnabled(false);

                        break;
                    case R.id.rbCustomSheet://enable editText for Sheet
                        edtSheetName.setEnabled(true);

                        break;
                }
            }
        });

        final TextView textView = binding.textTrain;
        trainViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //update modelSpinner
        modelSpin = binding.spinModel;

        //create array adapter. get list from MainActivity
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.appContext, android.R.layout.simple_spinner_item, MainActivity.list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpin.setAdapter(spinnerAdapter);//set list to modelSpinner
        spinnerAdapter.notifyDataSetChanged();//update spinner

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}