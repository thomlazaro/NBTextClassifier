package com.example.nbtextclassifier.backend;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.ml.AbstractTrainer;
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;


public class NLP {

    //function for training
    public static boolean trainNLP(File inputFile, File outputFile, FileOutputStream modelFile) {
        try {


            //convert train excel to corpus
            System.out.println("Converting Excel to text file..");
            //call ExcelToTextFile to convert excel to training corpus text file
            ExcelToTextFile.convertExcel(inputFile,outputFile,true);

            // read the training data
            InputStreamFactory dataIn = new MarkableFileInputStreamFactory(outputFile);

            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);

            // define the training parameters
            TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ITERATIONS_PARAM, 10+"");
            params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
            params.put(AbstractTrainer.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);

            // create a model from traning data
            DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory());
            System.out.println("\nModel is successfully trained. "+modelFile.toString());

            // save the model to local
            BufferedOutputStream modelOut = new BufferedOutputStream(modelFile);
            model.serialize(modelOut);

            System.out.println("\nTrained Model is saved locally");

            // test the model file by subjecting it to prediction
            DocumentCategorizer doccat = new DocumentCategorizerME(model);
            //String[] docWords = "Afterwards Stuart and Charlie notice Kate in the photos Stuart took at Leopolds ball and realise that her destiny must be to go back and be with Leopold That night while Kate is accepting her promotion at a company banquet he and Charlie race to meet her and show her the pictures Kate initially rejects their overtures and goes on to give her acceptance speech but it is there that she sees Stuarts picture and realises that she truly wants to be with Leopold".replaceAll("[^A-Za-z]", " ").split(" ");
            String[] docWords = "Pikachu".split(" ");
            double[] aProbs = doccat.categorize(docWords);

            // print the probabilities of the categories
            System.out.println("\n---------------------------------\nCategory : Probability\n---------------------------------");
            for(int i=0;i<doccat.getNumberOfCategories();i++){
                System.out.println(doccat.getCategory(i)+" : "+aProbs[i]);
            }
            System.out.println("---------------------------------");

            System.out.println("\n"+doccat.getBestCategory(aProbs)+" : is the predicted category for the given sentence.");
            return true;//return true to confirm that the training process is successful

        }
        catch (IOException e) {
            System.out.println("An exception in reading the training file. Please check.");
            e.printStackTrace();
            return false;//return false to confirm that there is an exception thrown on training process
        }
    }

    //custom function for training
    public static boolean customTrainNLP(File inputFile, File outputFile, FileOutputStream modelFile, int catCol, int textCol, String sheetName) {
        try {


            //convert train excel to corpus
            System.out.println("Converting Excel to text file..");
            //call ExcelToTextFile to convert excel to training corpus text file
            ExcelToTextFile.customConvertExcel(inputFile,outputFile,true,catCol,textCol,sheetName);//the only line that was changed from original function

            // read the training data
            InputStreamFactory dataIn = new MarkableFileInputStreamFactory(outputFile);

            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);

            // define the training parameters
            TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ITERATIONS_PARAM, 10+"");
            params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
            params.put(AbstractTrainer.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);

            // create a model from traning data
            DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory());
            System.out.println("\nModel is successfully trained. "+modelFile.toString());

            // save the model to local
            BufferedOutputStream modelOut = new BufferedOutputStream(modelFile);
            model.serialize(modelOut);

            System.out.println("\nTrained Model is saved locally");

            // test the model file by subjecting it to prediction
            DocumentCategorizer doccat = new DocumentCategorizerME(model);
            //String[] docWords = "Afterwards Stuart and Charlie notice Kate in the photos Stuart took at Leopolds ball and realise that her destiny must be to go back and be with Leopold That night while Kate is accepting her promotion at a company banquet he and Charlie race to meet her and show her the pictures Kate initially rejects their overtures and goes on to give her acceptance speech but it is there that she sees Stuarts picture and realises that she truly wants to be with Leopold".replaceAll("[^A-Za-z]", " ").split(" ");
            String[] docWords = "Pikachu".split(" ");
            double[] aProbs = doccat.categorize(docWords);

            // print the probabilities of the categories
            System.out.println("\n---------------------------------\nCategory : Probability\n---------------------------------");
            for(int i=0;i<doccat.getNumberOfCategories();i++){
                System.out.println(doccat.getCategory(i)+" : "+aProbs[i]);
            }
            System.out.println("---------------------------------");

            System.out.println("\n"+doccat.getBestCategory(aProbs)+" : is the predicted category for the given sentence.");
            return true;//return true to confirm that the training process is successful

        }
        catch (IOException e) {
            System.out.println("An exception in reading the training file. Please check.");
            e.printStackTrace();
            return false;//return false to confirm that there is an exception thrown on training process
        }
    }


    //function for predicting
    public static boolean predictNLP(File inputFile, File outputFile, File predictFile, FileInputStream modelFile){
        try {

            //convert input to corpus
            ExcelToTextFile.convertExcel(inputFile,outputFile,false);

            //read input corpus file
            //File intObj = inputFile;//get input spreadsheet location
            File outObj = outputFile;//get input corpus location
            Scanner readCorpus = new Scanner(outObj);//set input corpus file scanner

            // create workbook object
            XSSFWorkbook workbook = new XSSFWorkbook();

            // spreadsheet object, designate sheet name
            XSSFSheet spreadsheet
                    = workbook.createSheet(" Classification Result");

            // creating a row object
            XSSFRow row;
            //set key index
            int keyIndex = 1;
            // This data needs to be written (Object[]), treemap for insterting to excel later
            Map<String, Object[]> predictData
                    = new TreeMap<String, Object[]>();
            //Put header
            predictData.put(
                    Integer.toString(keyIndex),
                    new Object[] { "Text", "Category", "Probability" });

            //reload saved model
            DoccatModel model = new DoccatModel(modelFile);
            // test the model file by subjecting it to prediction
            DocumentCategorizer doccat = new DocumentCategorizerME(model);

            //read corpus file line by line and predict
            while (readCorpus.hasNextLine()) {
                String data = readCorpus.nextLine();
                //String[] docWords = "Afterwards Stuart and Charlie notice Kate in the photos Stuart took at Leopolds ball and realise that her destiny must be to go back and be with Leopold That night while Kate is accepting her promotion at a company banquet he and Charlie race to meet her and show her the pictures Kate initially rejects their overtures and goes on to give her acceptance speech but it is there that she sees Stuarts picture and realises that she truly wants to be with Leopold".replaceAll("[^A-Za-z]", " ").split(" ");
                String[] docWords = data.split(" ");
                double[] aProbs = doccat.categorize(docWords);//predict the line

                //get the highest category with highest probability
                double max = Arrays.stream(aProbs).max().getAsDouble()*100;//probability
                String predictedCategory = doccat.getBestCategory(aProbs);//category name
                //put result to tree map
                keyIndex++;//iterate tree map key index
                predictData.put(
                        Integer.toString(keyIndex),
                        new Object[] { data, predictedCategory, Double.toString(max) });

            }
            readCorpus.close();//close input corpus file

            //write to excel start

            Set<String> keyid = predictData.keySet();

            int rowid = 0;

            // writing the data into the sheets...

            for (String key : keyid) {

                row = spreadsheet.createRow(rowid++);
                Object[] objectArr = predictData.get(key);
                int cellid = 0;

                for (Object obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    cell.setCellValue((String)obj);
                }
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            FileOutputStream out = new FileOutputStream(predictFile);//save new excel file with _classify on file name

            workbook.write(out);
            out.close();
            return true;//return true as a sign that prediction process is successful
            //write to excel end

        }
        catch (IOException e) {
            System.out.println("An exception in reading the corpus file. Please check.");
            e.printStackTrace();
            return false;//return false as a sign that there is a problem in prediction process
        }
    }

    //custom function for predicting
    public static boolean customPredictNLP(File inputFile, File outputFile, File predictFile, FileInputStream modelFile, int catCol, int textCol, String sheetName){
        try {

            //convert input to corpus
            ExcelToTextFile.customConvertExcel(inputFile,outputFile,false,catCol,textCol,sheetName);//the only line that was changed from original function

            //read input corpus file
            //File intObj = inputFile;//get input spreadsheet location
            File outObj = outputFile;//get input corpus location
            Scanner readCorpus = new Scanner(outObj);//set input corpus file scanner

            // create workbook object
            XSSFWorkbook workbook = new XSSFWorkbook();

            // spreadsheet object, designate sheet name
            XSSFSheet spreadsheet
                    = workbook.createSheet(" Classification Result");

            // creating a row object
            XSSFRow row;
            //set key index
            int keyIndex = 100;
            // This data needs to be written (Object[]), treemap for insterting to excel later
            Map<String, Object[]> predictData
                    = new TreeMap<String, Object[]>();
            //Put header
            predictData.put(
                    Integer.toString(keyIndex),
                    new Object[] { "Text", "Category", "Probability" });

            //reload saved model
            DoccatModel model = new DoccatModel(modelFile);
            // test the model file by subjecting it to prediction
            DocumentCategorizer doccat = new DocumentCategorizerME(model);

            //read corpus file line by line and predict
            while (readCorpus.hasNextLine()) {
                String data = readCorpus.nextLine();
                //String[] docWords = "Afterwards Stuart and Charlie notice Kate in the photos Stuart took at Leopolds ball and realise that her destiny must be to go back and be with Leopold That night while Kate is accepting her promotion at a company banquet he and Charlie race to meet her and show her the pictures Kate initially rejects their overtures and goes on to give her acceptance speech but it is there that she sees Stuarts picture and realises that she truly wants to be with Leopold".replaceAll("[^A-Za-z]", " ").split(" ");
                String[] docWords = data.split(" ");
                double[] aProbs = doccat.categorize(docWords);//predict the line

                //get the highest category with highest probability
                double max = Arrays.stream(aProbs).max().getAsDouble()*100;//probability
                String predictedCategory = doccat.getBestCategory(aProbs);//category name
                //put result to tree map
                keyIndex++;//iterate tree map key index
                predictData.put(
                        Integer.toString(keyIndex),
                        new Object[] { data, predictedCategory, Double.toString(max) });

            }
            readCorpus.close();//close input corpus file

            //write to excel start

            Set<String> keyid = predictData.keySet();

            int rowid = 0;

            // writing the data into the sheets...

            for (String key : keyid) {
                System.out.println(key);
                row = spreadsheet.createRow(rowid++);
                Object[] objectArr = predictData.get(key);
                int cellid = 0;

                for (Object obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    cell.setCellValue((String)obj);
                }
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            FileOutputStream out = new FileOutputStream(predictFile);//save new excel file with _classify on file name

            workbook.write(out);
            out.close();
            return true;//return true as a sign that prediction process is successful
            //write to excel end

        }
        catch (IOException e) {
            System.out.println("An exception in reading the corpus file. Please check.");
            e.printStackTrace();
            return false;//return false as a sign that there is a problem in prediction process
        }
    }



}

