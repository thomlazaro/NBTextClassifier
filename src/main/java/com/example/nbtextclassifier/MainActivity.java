package com.example.nbtextclassifier;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.nbtextclassifier.backend.DocParser;
import com.example.nbtextclassifier.backend.NLP;
import com.example.nbtextclassifier.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    //Help function can be related to the instruction on using the app and describing the functionality of customization
    //Category = feature - or the classification of 1 item,
    //Text = Description - the component of item. this is where the text classification identifies the category of an item
    //TODO Last Date Modified - 6/19/2022 (negative testing in progress)
    //TODO Implement Settings (No Idea what to put yet)
    //TODO Implement Help (On how to use the app show by image and text)
    //TODO implement material design on the app and make the app presentable
    //TODO Negative Testing for Train and Classify - in progress
    //TODO Implement Customization on Train column selection can be by column name or header name(header name not priority for now)
    //TODO Implement other Customization on Classify
    //and what column to put the Category, to include probability, write only the classification or copy the entire spreadsheet - good to have
    //TODO clear all or selected model file slot(this feature is not neccessary)
    //POI-ON-ANDROID setup
    //Necessary System-Properties
    //In order to work around problems with finding a suitable XML Parser,
    // currently the following system properties need to be set manually during
    //startup of your application
    static {
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Spinner modelSpin;
    //Train Fragment components start
    private RadioButton rbDefault;
    private RadioButton rbCustom;
    private RadioButton rbDefaultSheet;
    private RadioButton rbCustomSheet;
    private EditText edtTxtCatCol;
    private EditText edtTxtTextCol;
    private EditText edtSheetName;
    //Train Fragment components end
    //Classify Fragment Components start
    private RadioButton rbDefaultCls;
    private RadioButton rbCustomCls;
    private RadioButton rbDefaultSheetCls;
    private RadioButton rbCustomSheetCls;
    private EditText edtTxtTextCls;
    private EditText edtSheetCls;
    //Classify Fragment Components end
    private boolean modelReplace = false;//flag if model should be overwritten or not
    public static Context appContext;//set variable for app context to pass to fragments
    public static List<String> list = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getApplicationContext();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_train, R.id.nav_classify)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //create modelname.txt if it doesnt exist
        File modelTxt = createModelNameTxt();
        //read modelname.txt and rename content of spinner
        readModelName(modelTxt);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //Train fragment function and listeners start here!!!

    //open alert dialog and check if there is an existing model
    public void trainOpenFileDialog(View view){

        rbDefault = findViewById(R.id.rbDefault);
        rbCustom = findViewById(R.id.rbCustom);
        rbDefaultSheet = findViewById(R.id.rbDefaultSheet);
        rbCustomSheet = findViewById(R.id.rbCustomSheet);
        edtTxtCatCol = findViewById(R.id.edtTxtCatCol);
        edtTxtTextCol = findViewById(R.id.edtTxtTextCol);
        edtSheetName = findViewById(R.id.edtSheetName);
        boolean columnCatEmpty = edtTxtCatCol.getText().toString().isEmpty();
        boolean columnTxtEmpty = edtTxtTextCol.getText().toString().isEmpty();
        boolean columnSheetEmpty = edtSheetName.getText().toString().isEmpty();
        boolean formValid = true;

        if(rbDefault.isChecked()){
            //do nothing
        }
        else if (rbCustom.isChecked()&&!(columnCatEmpty)&&!(columnTxtEmpty)){
            //do nothing
        }
        else{
            formValid = false;//set flag to stop process
        }

        if(rbDefaultSheet.isChecked()){
            //do nothing
        }
        else if (rbCustomSheet.isChecked()&&!columnSheetEmpty){
            //do nothing
        }
        else{
            formValid = false;//set flag to stop process
        }


        modelSpin = findViewById(R.id.spinModel);
        Context context = MainActivity.appContext;
        File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
        File modelFile = new File(mydir, "Model"+Integer.toString(modelSpin.getSelectedItemPosition())+".bin"); //Creating model file for future use.
        //if model exist ask user if they want to replace
        if(modelFile.exists()&&formValid){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("There is already an existing model for this slot. Do you want to replace it?");
            alert.setPositiveButton("Yes", dialogClickListener);
            alert.setNegativeButton("No",dialogClickListener);
            AlertDialog alertDialog = alert.create();
            alertDialog.show();
        }
        else if(!formValid){//if form is invalid
            if(columnCatEmpty){
                Toast.makeText(context, "Category column is empty!", Toast.LENGTH_SHORT).show();
            }
            else if (columnTxtEmpty){
                Toast.makeText(context, "Text column is empty!", Toast.LENGTH_SHORT).show();
            }
            else if(columnSheetEmpty){
                Toast.makeText(context, "Sheetname is empty", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "Unknown Error", Toast.LENGTH_SHORT).show();
            }
        }
        else{//if there is no existing model directly call file picker
            dispFilePickerTrain();
            modelReplace = true;
        }

    }

    //btnTrain dialog interface if there is an existing model on slot
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    dispFilePickerTrain();//call file picker for training function if yes
                    modelReplace = true;//set flag to replace model to true
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    modelReplace = false;//set flag to replace model to false
                    break;
            }
        }
    };

    public void dispFilePickerTrain(){
        //make file picker to only choose excel files
        String[] mimeTypes =
                {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xls & .xlsx
                };

        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        data.addCategory(Intent.CATEGORY_OPENABLE);
        //add file types to choose for file picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            data.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                data.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            data.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }

        //call file chooser
        data = Intent.createChooser(data,"Choose a File for training");
        //to wait for user to choose then execute sActivityResultLauncher
        sActivityResultLauncher.launch(data);
    }

    //function starts here
    //File Picker Activity for btnTrain
    //This is where you put the code on what to do after picking a file
    ActivityResultLauncher<Intent> sActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        System.out.println("UUURRRIIII"+uri);
                        modelSpin = findViewById(R.id.spinModel);
                        rbDefault = findViewById(R.id.rbDefault);
                        rbCustom = findViewById(R.id.rbCustom);
                        rbDefaultSheet = findViewById(R.id.rbDefaultSheet);
                        rbCustomSheet = findViewById(R.id.rbCustomSheet);
                        edtTxtCatCol = findViewById(R.id.edtTxtCatCol);
                        edtTxtTextCol = findViewById(R.id.edtTxtTextCol);
                        edtSheetName = findViewById(R.id.edtSheetName);
                        String categoryCol = "A";//set default values
                        String textCol = "B";//set default values
                        String sheetname = "Sheet1";//set default values
                        int categoryColNum = 0;//set default values
                        int textColNum = 1;//set default values
                        boolean customFlag = false;
                        boolean nlpResult = false;
                        boolean columnCatEmpty = edtTxtCatCol.getText().toString().isEmpty();
                        boolean columnTxtEmpty = edtTxtTextCol.getText().toString().isEmpty();
                        boolean columnSheetEmpty = edtSheetName.getText().toString().isEmpty();


                        if(rbDefault.isChecked()){
                            //do nothing
                        }
                        else if (rbCustom.isChecked()&&!(columnCatEmpty)&&!(columnTxtEmpty)){
                            categoryCol = edtTxtCatCol.getText().toString();
                            textCol = edtTxtTextCol.getText().toString();
                            categoryColNum = convertColumn(categoryCol);//convert column letter to index no
                            textColNum = convertColumn(textCol);//convert column letter to index no
                            customFlag = true;
                        }
                        else{
                            modelReplace = false;//set flag to not replace model to cancel process
                        }

                        if(rbDefaultSheet.isChecked()){
                            //do nothing
                        }
                        else if (rbCustomSheet.isChecked()&&!columnSheetEmpty){
                            sheetname = edtSheetName.getText().toString();
                            customFlag = true;
                        }
                        else{
                            modelReplace = false;//set flag to not replace model to cancel process
                        }

                        Context context = MainActivity.appContext;
                        File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
                        File outputFile = new File(mydir, "corpus.train"); //creating corpus file for training.
                        File modelFile = new File(mydir, "Model"+Integer.toString(modelSpin.getSelectedItemPosition())+".bin"); //Creating model file for future use.


                        //if user choose to replace existing model or model does not exist yet
                        if(modelReplace){
                            try {
                                FileOutputStream modelStream = new FileOutputStream(modelFile); //create model file as outputstream.
                                System.out.println("URI:"+uri);
                                String fullPath = DocParser.getPath(context, uri);//get proper path of the file the user chose
                                File inputFile = new File(fullPath);//create user excel file as file
                                System.out.println("docparser:"+fullPath);
                                System.out.println("File:"+inputFile);


                                if(!customFlag){//if default
                                    nlpResult = NLP.trainNLP(inputFile,outputFile,modelStream);//call NLP.java function trainNLP
                                }
                                else{//if custom
                                    nlpResult = NLP.customTrainNLP(inputFile,outputFile,modelStream,categoryColNum,textColNum,sheetname);// call NLP.java function customTrainNLP
                                }

                                //check if prediction process is successful
                                if (nlpResult){
                                    Toast.makeText(context, "Training complete!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(context, "Excel file in invalid format!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            if(columnCatEmpty){
                                Toast.makeText(context, "Category column is empty!", Toast.LENGTH_SHORT).show();
                            }
                            else if (columnTxtEmpty){
                                Toast.makeText(context, "Text column is empty!", Toast.LENGTH_SHORT).show();
                            }
                            else if(columnSheetEmpty){
                                Toast.makeText(context, "Sheetname is empty", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(context, "Model Training cancelled!", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                }
            }
    );

    //btnRename function for renaming models
    public void renameOpenDialog(View view){
        modelSpin = findViewById(R.id.spinModel);//get Model Spinner
        Context context = MainActivity.appContext;// get app context
        File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
        File modelFile = new File(mydir, "Model"+Integer.toString(modelSpin.getSelectedItemPosition())+".bin"); //Creating model file for future use.



        //build new Alert Dialog with Edittext for user to put new name
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("New Name for Model"); //Title of Alert Dialog
        alert.setMessage("Enter new Model Name"); // message of alert dialog
        final EditText input = new EditText(MainActivity.this);//creating the editText
        input.setHint("Model Name");//setting placeholder of edittext
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(//layout of the alert dialog
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);//setting the layout to alertdialog
        alert.setView(input);//setting the edittext to alertdialog
        //setting the onclick listener for Save button of alert dialog
        alert.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();//get new name from edittext
                        if(!name.isEmpty()){//check if edittext is not empty
                            int spinSelectPos = modelSpin.getSelectedItemPosition();
                            list.set(spinSelectPos,name);//set the new name to the designated index on model name list

                            editModelNameTxt();//call function to recreate modelname.txt

                            //reload list to model spinner
                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.appContext, android.R.layout.simple_spinner_item, MainActivity.list);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            modelSpin.setAdapter(spinnerAdapter);//set list to modelSpinner
                            spinnerAdapter.notifyDataSetChanged();//update spinner

                            modelSpin.setSelection(spinSelectPos);//set spinner to the previous selected item
                            //show toast after successful renaming
                            Toast.makeText(getApplicationContext(),
                                    "New Model Name saved!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //show toast if new name is blank
                            Toast.makeText(getApplicationContext(),
                                    "New Model Name is blank!", Toast.LENGTH_SHORT).show();
                        }


                    }

                });
        //setting the onclick listener for Cancel button of alert dialog
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();//to cancel the dialog
                    }
                });

        alert.show();//show build alert dialog


    }

    //btnTrain dialog interface if there is an existing model on slot
    DialogInterface.OnClickListener renameClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Save button clicked
                    dispFilePickerTrain();//call file picker for training function if yes

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //Cancel button clicked

                    break;
            }
        }
    };


    //Train fragment function and listeners ends here!!!

    //Classify fragment function and listeners start here!!!

    //open File Dialog function. executes when btnPredict is clicked
    public void predictOpenFileDialog(View view){
        rbDefaultCls = findViewById(R.id.rbDefaultCls);
        rbCustomCls = findViewById(R.id.rbCustomCls);
        rbDefaultSheetCls = findViewById(R.id.rbDefaultSheetCls);
        rbCustomSheetCls = findViewById(R.id.rbCustomSheetCls);
        edtTxtTextCls = findViewById(R.id.edtTxtTextCls);
        edtSheetCls = findViewById(R.id.edtSheetCls);
        boolean columnTxtEmpty = edtTxtTextCls.getText().toString().isEmpty();
        boolean columnSheetEmpty = edtSheetCls.getText().toString().isEmpty();
        boolean formValid = false;

        if(rbDefaultCls.isChecked()){
            //do nothing
            formValid = true;
        }
        else if (rbCustomCls.isChecked()&&!columnTxtEmpty){
            formValid = true;
        }
        else{
            formValid = false;
        }

        if(rbDefaultSheetCls.isChecked()){
            //do nothing
            formValid = true;
        }
        else if (rbCustomSheetCls.isChecked()&&!columnSheetEmpty){
            formValid = true;
        }
        else{
            formValid = false;
        }

        modelSpin = findViewById(R.id.spinModelP);
        Context context = MainActivity.appContext;
        File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
        File modelFile = new File(mydir, "Model"+Integer.toString(modelSpin.getSelectedItemPosition())+".bin"); //Creating model file for future use.
        //if model does not exist prompt user to train model slot first
        if(!modelFile.exists()){
            Toast.makeText(context, "No Model found for this slot. Please train it first!", Toast.LENGTH_SHORT).show();
        }
        else if (!formValid){//if form is invalid
            if(columnTxtEmpty){
                Toast.makeText(context, "Text Column is empty!", Toast.LENGTH_SHORT).show();
            }
            else if (columnSheetEmpty){
                Toast.makeText(context, "Sheet name is empty!", Toast.LENGTH_SHORT).show();
            }
            else if (!modelFile.exists()){
                Toast.makeText(context, "No Model found for selected slot. Train model for selected slot first!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "Unknown Error!", Toast.LENGTH_SHORT).show();
            }
        }
        else{//if there is a existing model directly call file picker
            dispFilePickerPredict();
        }
        //System.out.println("btnPredict ends!");
    }

    public void dispFilePickerPredict(){
        //make file picker to only choose excel files
        String[] mimeTypes =
                {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xls & .xlsx
                };

        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        data.addCategory(Intent.CATEGORY_OPENABLE);
        //add file types to choose for file picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            data.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                data.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            data.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }

        //call file chooser
        data = Intent.createChooser(data,"Choose a File to classify");
        //to wait for user to choose then execute sActivityResultLauncher
        pActivityResultLauncher.launch(data);
    }

    //File Picker Activity for btnPredict
    //This is where you put the code on what to do after picking a file
    ActivityResultLauncher<Intent> pActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        modelSpin = findViewById(R.id.spinModelP);
                        rbDefaultCls = findViewById(R.id.rbDefaultCls);
                        rbCustomCls = findViewById(R.id.rbCustomCls);
                        rbDefaultSheetCls = findViewById(R.id.rbDefaultSheetCls);
                        rbCustomSheetCls = findViewById(R.id.rbCustomSheetCls);
                        edtTxtTextCls = findViewById(R.id.edtTxtTextCls);
                        edtSheetCls = findViewById(R.id.edtSheetCls);
                        String categoryCol = "A";//set default values
                        String textCol = "B";//set default values
                        String sheetname = "Sheet1";//set default values
                        int categoryColNum = 1;//set default values
                        int textColNum = 0;//set default values
                        boolean customFlag = false;
                        boolean nlpResult = false;
                        boolean columnTxtEmpty = edtTxtTextCls.getText().toString().isEmpty();
                        boolean columnSheetEmpty = edtSheetCls.getText().toString().isEmpty();
                        boolean formValid = false;

                        if(rbDefaultCls.isChecked()){
                            //do nothing
                            formValid = true;
                        }
                        else if (rbCustomCls.isChecked()&&!columnTxtEmpty){
                            textCol = edtTxtTextCls.getText().toString();
                            categoryColNum = convertColumn(categoryCol);//convert column letter to index no
                            textColNum = convertColumn(textCol);//convert column letter to index no
                            customFlag = true;
                            formValid = true;
                        }
                        else{
                            formValid = false;
                        }

                        if(rbDefaultSheetCls.isChecked()){
                            //do nothing
                            formValid = true;
                        }
                        else if (rbCustomSheetCls.isChecked()&&!columnSheetEmpty){
                            sheetname = edtSheetCls.getText().toString();
                            customFlag = true;
                            formValid = true;
                        }
                        else{
                            formValid = false;
                        }


                        Context context = getApplicationContext();
                        File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
                        File outputFile = new File(mydir, "corpus.train"); //creating corpus file to classify.
                        File modelFile = new File(mydir, "Model"+Integer.toString(modelSpin.getSelectedItemPosition())+".bin"); //locating model file to use by spinner position.

                        //check if model exist, if exist continue with process else stop
                        if (modelFile.exists()&&formValid){
                            try {
                                //new File("classify"+File.separator+intObj.getName().replace(".xlsx","_classified")+".xlsx")
                                FileInputStream modelStream = new FileInputStream(modelFile); //create model file as input stream.
                                String fullPath = DocParser.getPath(context, uri);//get proper path of the file the user chose
                                File inputFile = new File(fullPath);//create user excel file as file
                                File predictFile = new File(fullPath.replace(".xlsx","_classified")+".xlsx");//create output excel file for classified text

                                if(!customFlag) {
                                    nlpResult = NLP.predictNLP(inputFile, outputFile, predictFile, modelStream);//call NLP.java function predictNLP
                                }
                                else{
                                    nlpResult = NLP.customPredictNLP(inputFile, outputFile, predictFile, modelStream,categoryColNum,textColNum,sheetname);//call NLP.java function customPredictNLP
                                }

                                //check if prediction process is successful
                                if (nlpResult){
                                    Toast.makeText(context, "Classification complete!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(context, "Excel file in invalid format!", Toast.LENGTH_SHORT).show();
                                }


                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            if(columnTxtEmpty){
                                Toast.makeText(context, "Text Column is empty!", Toast.LENGTH_SHORT).show();
                            }
                            else if (columnSheetEmpty){
                                Toast.makeText(context, "Sheet name is empty!", Toast.LENGTH_SHORT).show();
                            }
                            else if (!modelFile.exists()){
                                Toast.makeText(context, "No Model found for selected slot. Train model for selected slot first!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(context, "Unknown Error!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }
    );

    //Classify fragment function and listeners ends here!!!

    //create modelname.txt to contain model names
    private File createModelNameTxt(){
        try {
            Context context = getApplicationContext();
            File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
            File modelTxt = new File(mydir, "modelname.txt"); //creating modelname file
            if (!modelTxt.exists()){//if modelname.txt does not exist, create it
                modelTxt.delete();
                if (modelTxt.createNewFile()) {
                    System.out.println("File created: " + modelTxt.getName());
                    FileWriter myWriter = new FileWriter(modelTxt.getPath().toString());
                    int modelCount = 0;
                    for(modelCount=0;modelCount<5;modelCount++){
                        myWriter.write("Model Name "+(Integer.toString(modelCount+1))+"\n");
                    }
                    myWriter.close();
                    System.out.println("Successfully wrote to the file.");

                } else {
                    System.out.println("File already exists.");

                }
            }
            else{
                //do nothing
            }


            return modelTxt;//return modelname.txt as File
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }
    //read modelname.txt content and save to list
    public static void readModelName(File myObj){
        try {

            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {//add modelname.txt content to list
                String data = myReader.nextLine();
                list.add(data);
                System.out.println(data);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //edit modelname.txt content and save to list
    private void editModelNameTxt(){
        try {
            Context context = getApplicationContext();
            File mydir = context.getDir("Text Classfier", Context.MODE_PRIVATE); //Creating an internal dir;
            File modelTxt = new File(mydir, "modelname.txt"); //creating modelname file
            if (modelTxt.exists()){//delete existing txt file
                modelTxt.delete();
            }
            if (modelTxt.createNewFile()) {//create new txt file
                System.out.println("File created: " + modelTxt.getName());
                FileWriter myWriter = new FileWriter(modelTxt.getPath().toString());
                int modelCount = 0;
                for(modelCount=0;modelCount<5;modelCount++){
                    myWriter.write(list.get(modelCount)+"\n");//rewrite modelname.txt according to list model name
                }
                myWriter.close();
                System.out.println("Successfully wrote to the file.");

            } else {
                System.out.println("File already exists.");

            }

            //return modelTxt;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //convert excel column to index number
    public int convertColumn(String column){

        int index = 0;
        int max = column.length();
        int counter = 0;
        int reverse = 0;

        if(max==1){
            //do nothing if column letter is only 1 char
        }
        else{
            reverse = column.length()-1;
        }

        //get 1st char value
        if(column.substring(reverse).equalsIgnoreCase("A")){
            index = 0;
        }
        else if(column.substring(reverse).equalsIgnoreCase("B")){
            index = 1;
        }
        else if(column.substring(reverse).equalsIgnoreCase("C")){
            index = 2;
        }
        else if(column.substring(reverse).equalsIgnoreCase("D")){
            index = 3;
        }
        else if(column.substring(reverse).equalsIgnoreCase("E")){
            index = 4;
        }
        else if(column.substring(reverse).equalsIgnoreCase("F")){
            index = 5;
        }
        else if(column.substring(reverse).equalsIgnoreCase("G")){
            index = 6;
        }
        else if(column.substring(reverse).equalsIgnoreCase("H")){
            index = 7;
        }
        else if(column.substring(reverse).equalsIgnoreCase("I")){
            index = 8;
        }
        else if(column.substring(reverse).equalsIgnoreCase("J")){
            index = 9;
        }
        else if(column.substring(reverse).equalsIgnoreCase("K")){
            index = 10;
        }
        else if(column.substring(reverse).equalsIgnoreCase("L")){
            index = 11;
        }
        else if(column.substring(reverse).equalsIgnoreCase("M")){
            index = 12;
        }
        else if(column.substring(reverse).equalsIgnoreCase("N")){
            index = 13;
        }
        else if(column.substring(reverse).equalsIgnoreCase("O")){
            index = 14;
        }
        else if(column.substring(reverse).equalsIgnoreCase("P")){
            index = 15;
        }
        else if(column.substring(reverse).equalsIgnoreCase("Q")){
            index = 16;
        }
        else if(column.substring(reverse).equalsIgnoreCase("R")){
            index = 17;
        }
        else if(column.substring(reverse).equalsIgnoreCase("S")){
            index = 18;
        }
        else if(column.substring(reverse).equalsIgnoreCase("T")){
            index = 19;
        }
        else if(column.substring(reverse).equalsIgnoreCase("U")){
            index = 20;
        }
        else if(column.substring(reverse).equalsIgnoreCase("V")){
            index = 21;
        }
        else if(column.substring(reverse).equalsIgnoreCase("W")){
            index = 22;
        }
        else if(column.substring(reverse).equalsIgnoreCase("X")){
            index = 23;
        }
        else if(column.substring(reverse).equalsIgnoreCase("Y")){
            index = 24;
        }
        else if(column.substring(reverse).equalsIgnoreCase("Z")){
            index = 25;
        }
        reverse = reverse - 1;

        if(reverse<0){
            //do nothing
        }
        else{
            //get 2nd char value
            if(column.substring(reverse,1).equalsIgnoreCase("A")){
                index = index+(26*1);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("B")){
                index = index+(26*2);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("C")){
                index = index+(26*3);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("D")){
                index = index+(26*4);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("E")){
                index = index+(26*5);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("F")){
                index = index+(26*6);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("G")){
                index = index+(26*7);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("H")){
                index = index+(26*8);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("I")){
                index = index+(26*9);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("J")){
                index = index+(26*10);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("K")){
                index = index+(26*11);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("L")){
                index = index+(26*12);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("M")){
                index = index+(26*13);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("N")){
                index = index+(26*14);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("O")){
                index = index+(26*15);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("P")){
                index = index+(26*16);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("Q")){
                index = index+(26*17);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("R")){
                index = index+(26*18);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("S")){
                index = index+(26*19);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("T")){
                index = index+(26*20);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("U")){
                index = index+(26*21);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("V")){
                index = index+(26*22);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("W")){
                index = index+(26*23);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("X")){
                index = index+(26*24);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("Y")){
                index = index+(26*25);
            }
            else if(column.substring(reverse,1).equalsIgnoreCase("Z")){
                index = index+(26*26);
            }
        }

        return index;
    }

}