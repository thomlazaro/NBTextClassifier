package com.example.nbtextclassifier.backend;



import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.util.Iterator;

public class ExcelToTextFile {

    public static void convertExcel(File inputFileloc,File outputFileloc,boolean train){

        boolean firstRead = true;//first read flag


        //to transfer excel spreadsheet content to text file
        try {
            String corpusData = "";//temp container for excel row before writing to text file

            FileWriter outputWriter = new FileWriter(outputFileloc);//create output writer
            FileInputStream fis = new FileInputStream(inputFileloc);   //obtaining bytes from the file

            //creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            //wb.getSheet("");//get by sheet name
            XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object
            Iterator<Row> itr = sheet.iterator();    //iterating over excel file
            byte clcount = 0;// 0 = Column A, 1 = Column B and so on

            //iterate on spreadsheet
            while (itr.hasNext())
            {
                Row row = itr.next();
                //skip if first row
                if(firstRead){
                    row = itr.next();
                    firstRead = false;//set first read flag to false
                }

                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    //check if column A
                    if(clcount == 0&&train){//train file conversion
                        corpusData = cell.getStringCellValue();//get category
                        clcount+=1;
                        //check if column B
                    } else if (clcount == 1) {//train file conversion
                        corpusData = corpusData +" "+cell.getStringCellValue()+"\n";//get description
                        clcount = 0;//reset column counter
                    }
                    //check if column A
                    else{//this is for predict file conversion
                        corpusData = cell.getStringCellValue()+"\n";//get description
                    }

                }
                //write excel row to textfile
                outputWriter.write(corpusData);
            }
            //close output writer object
            outputWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    //might need to revamp the default function to use this custom function instead
    public static void customConvertExcel(File inputFileloc,File outputFileloc,boolean train, int catCol, int textCol, String sheetname){

        boolean firstRead = true;//first read flag


        //to transfer excel spreadsheet content to text file
        try {
            String corpusData = "";//temp container for excel row before writing to text file
            String category = "";//temp container for category
            String text = "";//temp container for text/description

            FileWriter outputWriter = new FileWriter(outputFileloc);//create output writer
            FileInputStream fis = new FileInputStream(inputFileloc);   //obtaining bytes from the file

            //creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            Sheet sheet = wb.getSheet(sheetname);//get by sheet name

            Iterator<Row> itr = sheet.iterator();    //iterating over excel file

            //iterate on spreadsheet
            while (itr.hasNext())
            {

                Row row = itr.next();

                //skip if first row
                if(firstRead){
                    row = itr.next();//get next row
                    firstRead = false;//set first read flag to false
                }

                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column that is not blank
                while (cellIterator.hasNext())

                {
                    //get next column
                    Cell cell = cellIterator.next();

                    //check if current column index is equal to designated column from custom
                    if(cell.getColumnIndex() == catCol&&train){//train file conversion - if current columnIndex is equal to designated category column

                        category = cell.getStringCellValue();//get category
                        System.out.println(" Category Column "+category);

                        //check if column is for text/description from custom
                    } else if (cell.getColumnIndex() == textCol) {//train file conversion - if current columnIndex is equal to designated Text column
                        text = " "+cell.getStringCellValue()+"\n";//get description
                        System.out.println(" Text Column "+text);

                    }
                    //check if column is from classify custom
                    else if(!train&&cell.getColumnIndex() == textCol){//this is for predict file conversion
                        //System.out.println("predick");
                        text = cell.getStringCellValue()+"\n";//get description
                    }



                }
                corpusData = category+text;//combine category and text to 1 line

                //write excel row to textfile
                outputWriter.write(corpusData);
            }
            //close output writer object
            outputWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
