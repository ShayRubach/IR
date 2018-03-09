package model;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ParserUtil {

    private ArrayList<String> stopList = new ArrayList<>();
    private String stopListPath = null;
    private OperatorHandler opHandler = new OperatorHandler();

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
        //TODO: remove punctuations

        //convert the query holder to string[] in case we need to handle multiple terms:
        String[] multipleTermsQuery = {searchQuery};

        ArrayList<String[]> recordsList = new ArrayList<>();        //will holder all records we get as results

        //remove useless spaces:
        searchQuery = removeUselessSpaces(searchQuery);
        //searchQuery = searchQuery.trim().replaceAll(" ","");
        //searchQuery  = searchQuery.trim().replaceAll(" +", " ");

        //TODO:eliminate stop words which aren't between quotation marks
        searchQuery = eliminateStopWords(searchQuery,db);


        opHandler.countOperators(searchQuery);



//        if(!searchQuery.contains("\"")) {
//            searchQuery = eliminateStopWords(searchQuery,db);
//            System.out.println(searchQuery);
//        }
//        else {
//            searchQuery = handleOperatorQuotation(searchQuery);
//            multipleTermsQuery = searchQuery.split(" ");
//        }
//
//
//        //TODO: handle | operator
//        if(searchQuery.contains("!"))
//            handleOperatorOr(searchQuery);
//
//        //TODO: handle | operator
//        if(searchQuery.contains("|"))
//            handleOperatorOr(searchQuery);
//
//        //TODO: handle & operator
//        if(searchQuery.contains("&"))
//            handleOperatorAnd(searchQuery);
//
//        //TODO: handle () operator
//        if(searchQuery.contains("(") && searchQuery.contains(")"))
//            handleOperatorParanthesis(searchQuery);







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


    }

    private String removeUselessSpaces(String searchQuery) {
        searchQuery  = searchQuery.trim().replaceAll(" +", " ");
        return searchQuery;
    }

    //combine all words in the " " boundaries into 1 single phrase
    private String handleOperatorQuotation(String searchQuery) {
        int posStart = searchQuery.indexOf("\"");
        int posEnd = searchQuery.substring(posStart+1).indexOf("\"");
        return searchQuery.substring(posStart+1,posEnd+1);
    }

    private void handleOperatorParanthesis(String searchQuery) {
    }

    private void handleOperatorAnd(String searchQuery) {
    }

    private void handleOperatorOr(String searchQuery) {
    }

    private String eliminateStopWords(String searchQuery, DatabaseUtil db) {

        ArrayList<Integer> posList = new ArrayList<>();

        //look for quotation marks
        for(int j = 0 ; j < searchQuery.length() ; ++j) {
            if(searchQuery.charAt(j) == '\"'){
                posList.add(j);
            }
        }

        System.out.println(searchQuery);

        //odd number of parenthesis, illegal.
        if(!posList.isEmpty() && posList.size()%2 != 0){
            return searchQuery;
        }

        try {
            Scanner itr = new Scanner(new File(stopListPath));


            while(itr.hasNext()){
                String word = itr.next();

                if(posList.isEmpty()){
                    searchQuery = searchQuery.replaceAll(word,"");
                    break;
                }


                int wordPos = searchQuery.indexOf(word);
                if(wordPos != -1){
                    for(int i=0 ; i < posList.size() - 1 ; i+=2 ){
                        if(wordPos > posList.get(i) && wordPos < posList.get(i+1))
                            searchQuery = searchQuery.replaceAll(word,"");
                    }

                }
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("eliminateStopWords called. cant find the stop list file");
            e.printStackTrace();
        }


        return searchQuery;
    }


    private String eliminateStopWords2(String searchQuery, DatabaseUtil db) {

        String[] split = searchQuery.split(" ");
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> stopList = new ArrayList<>();


        try {
            Scanner itr = new Scanner(new File(stopListPath));

            //convert to list
            for(String s : split)
                list.add(s);

            while(itr.hasNext()){
                stopList.add(itr.next().toString());
            }

            //remove all stop words:
            for(int i = 0 ; i < list.size() ; ++i) {
                for(int j = 0 ; j < stopList.size() ; ++j)
                    list.remove(stopList.get(j));
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("eliminateStopWords called. cant find the stop list file");
            e.printStackTrace();
        }

        //build new string without stop words
        StringBuilder sb = new StringBuilder();
        for(String s: list)
            sb.append(s + " ");

        return sb.substring(0,sb.length()-1);  //cut last space

    }


    public void setStopListPath(String stopListPath) {
        this.stopListPath = stopListPath;
    }




    class OperatorHandler {
        public int opQuotations = 0;
        public int opParenthasis = 0;
        public int opOr = 0;
        public int opAnd = 0;

        // in simple terms: < <operatorType,OperatorsCount>, List<posStart,posEnd> >
        public HashMap<HashMap<String,Integer>,ArrayList<HashMap<Integer,Integer>>> ops = new HashMap<>();


        public OperatorHandler(){}


        public void countOperators(String searchQuery){
            for (int i = 0 ; i < searchQuery.length() ; ++i){
                ;
            }
        }


        public void clean() {
            opQuotations=0;
            opParenthasis=0;
            opOr=0;
            opAnd=0;
        }

    }









}
