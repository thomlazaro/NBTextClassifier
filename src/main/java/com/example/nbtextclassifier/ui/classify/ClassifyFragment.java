package com.example.nbtextclassifier.ui.classify;

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
import com.example.nbtextclassifier.databinding.FragmentClassifyBinding;

public class ClassifyFragment extends Fragment {

    private FragmentClassifyBinding binding;
    private Spinner modelSpinP;
    private RadioGroup rgColumn;
    private RadioGroup rgSheetCls;
    private EditText edtTxtTextCls;
    private EditText edtSheetCls;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ClassifyViewModel classifyViewModel =
                new ViewModelProvider(this).get(ClassifyViewModel.class);

        binding = FragmentClassifyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //get view of needed component
        rgColumn = binding.rgColumn;//column radiogroup
        edtTxtTextCls = binding.edtTxtTextCls;

        rgSheetCls = binding.rgSheetCls;//sheet radiogroup
        edtSheetCls = binding.edtSheetCls;


        //rgColumn listener to enable and disable editTxt column for customization
        rgColumn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID){
                    case R.id.rbDefaultCls://disable editText for Columns
                        edtTxtTextCls.setEnabled(false);
                        break;
                    case R.id.rbCustomCls://enable editText for Columns
                        edtTxtTextCls.setEnabled(true);
                        break;
                }
            }
        });

        //rgSheet listener to enable and disable editTxt sheet for customization
        rgSheetCls.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID){
                    case R.id.rbDefaultSheetCls://disable editText for Sheet
                        edtSheetCls.setEnabled(false);

                        break;
                    case R.id.rbCustomSheetCls://enable editText for Sheet
                        edtSheetCls.setEnabled(true);

                        break;
                }
            }
        });

        final TextView textView = binding.textClassify;
        classifyViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //update modelSpinnerP
        modelSpinP = binding.spinModelP;

        //create array adapter. get list from MainActivity
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.appContext, android.R.layout.simple_spinner_item, MainActivity.list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinP.setAdapter(spinnerAdapter);//set list to modelSpinnerP
        spinnerAdapter.notifyDataSetChanged();//update spinner

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}