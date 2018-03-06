package model;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ParserUtil {

    private ArrayList<String> stopList = new ArrayList<>();
    private String stopListPath = null;

    public ParserUtil(){}
    public ParserUtil(String stopListPath) throws FileNotFoundException {
        setStopListPath(stopListPath);
        createStopList();
    }

    public void createStopList() throws FileNotFoundException {
        Scanner itr = new Scanner(new File(stopListPath));
        while(itr.hasNextLine()){
            stopList.add(itr.next());
        }

    }

    public void indexFile(int docId, String fileName, DatabaseUtil db) throws FileNotFoundException {
        //TODO: use the db to upload the index table


        //Scanner itr = new Scanner(new File(db.getSourceFilesPath() +"/"+fileName));

    }










    public void setStopListPath(String stopListPath) {
        this.stopListPath = stopListPath;
    }

}
