package model;


import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public void indexFile(int docId, String fileName, DatabaseUtil db) throws FileNotFoundException, SQLException {
        String line;
        String[] words;
        HashMap<String,Integer> appearances = new HashMap<>();
        Scanner itr = new Scanner(new File(db.getLocalStoragePath() + "/"+ fileName));

        //parse and create a unique hashmap:
        while(itr.hasNextLine()){
            line = itr.nextLine();
            //remove all punctuations but the ' character :
            line = line.replaceAll("(?=[^a-zA-Z0-9])([^'])", " ").toLowerCase();
            words = line.split(" ");
            mapWords(words,line,appearances);
        }

        //upload unique records to db:
        for (String key : appearances.keySet() ) {
            db.pStmt = db.getConn().prepareStatement(QueryUtil.INSERT_NEW_INDEX_FILE);
            db.pStmt.setString(1,key);
            db.pStmt.setInt(2,docId);
            db.pStmt.setInt(3,appearances.get(key));
            db.pStmt.execute();
        }

    }

    private void mapWords(String[] words, String line,HashMap<String,Integer> appearances) {
        for(String s : words){
            if(!appearances.containsKey(s))
                appearances.put(s,1);
            else
                appearances.put(s,appearances.get(s)+1);
        }
        appearances.remove("");
    }



    public ArrayList<String[]> search(String searchQuery, DatabaseUtil db) throws SQLException {
        //TODO: parse the query for operators and remove punctuations
        //String fixedLine = searchQuery.replaceAll("(?=[^a-zA-Z0-9])([^'])", " ").toLowerCase();
        //fixedLine = fixedLine.split(" ");
        //System.out.println(fixedLine);

        if(searchQuery.contains("\""));
            stackSearchQuery(searchQuery);

        ArrayList<String[]> recordsList = new ArrayList<>();
        eliminateStopWords(searchQuery);

        db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM);
        db.pStmt.setString(1,searchQuery);
        db.rs = db.pStmt.executeQuery();

        while(db.rs.next()){
            String[] record = {db.rs.getString(1),      //word
                    String.valueOf(db.rs.getInt(2)),    //id
                    String.valueOf(db.rs.getInt(3)),    //appearances
                    db.rs.getString(5),                 //name
                    db.rs.getString(6)};                //link


        recordsList.add(record);

        }


        return recordsList;
        //TODO: look in db with operator restrictions

    }

    //combine all words in the " " boundaries into 1 single phrase
    private void stackSearchQuery(String searchQuery) {

    }

    private void eliminateStopWords(String searchQuery) {
        int times = 0;
        for (int i=0 ; i < searchQuery.length() ;i++){
            if(searchQuery.charAt(i) == '"'){
                ++times;
            }
        }

    }


    public void setStopListPath(String stopListPath) {
        this.stopListPath = stopListPath;
    }












}
