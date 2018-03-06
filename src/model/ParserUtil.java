package model;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtil {

    private ArrayList<String> stopList = new ArrayList<>();
    private String stopListPath = null;

    //private static final Pattern UNWANTED_SYMBOLS = Pattern.compile("(?:--|[\\[\\]{}(),.+/\\\\])");
    private static final Pattern UNWANTED_SYMBOLS = Pattern.compile("/[^A-Za-z0-9]/g");


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
        String line;

        Scanner itr = new Scanner(new File(db.getLocalStoragePath() + "/"+ fileName));
        while(itr.hasNextLine()){
            line = itr.nextLine();

            //remove all punctuations but the ' character :
            line = line.replaceAll("(?=[^a-zA-Z0-9])([^'])", " ").toLowerCase();


        }

    }

    //cleans string from any punctuation symbols




    public void setStopListPath(String stopListPath) {
        this.stopListPath = stopListPath;
    }

}
