package model;


import java.io.File;
import java.io.FileNotFoundException;
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


        //convert the query holder to string[] in case we need to handle multiple terms:
        String[] multipleTermsQuery = {searchQuery};

        ArrayList<String[]> recordsList = new ArrayList<>();        //will holder all records we get as results

        //remove useless spaces:
        searchQuery = fixSpaces(searchQuery);
        searchQuery = eliminateStopWords(searchQuery,db);
        opHandler.countOperators(searchQuery);


        System.out.println(searchQuery);
        //TODO: handle ! operator
        if(searchQuery.contains("!"))
            return handleOperatorNot(searchQuery,db);

        //TODO: handle | operator
        if(searchQuery.contains("|"))
            return handleOperatorOr(searchQuery,db);

        //TODO: handle & operator
        if(searchQuery.contains("&"))
            return handleOperatorAnd(searchQuery,db);

        //TODO: handle () operator
        if(searchQuery.contains("(") && searchQuery.contains(")"))
            handleOperatorParentheses(searchQuery);












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

    private String fixSpaces(String searchQuery) {
        //makes equal spaces between words and operators, e.g:
        //turns->       word!   (another &"you")|phrase       into->       word ! (another & " you " ) | phrase
        String[] operators = {"\\(","\\)","\\&","\\|","\\!","\\\""};
        for(String op : operators)
            searchQuery = searchQuery.trim().replaceAll(op," " + op + " ");

        return removeUselessSpaces(searchQuery);
    }

    private String removeUselessSpaces(String searchQuery) {
        searchQuery  = searchQuery.trim().replaceAll(" +", " ");
        return searchQuery;
    }

    private void handleOperatorQuotation(String searchQuery,DatabaseUtil db) throws SQLException {

        ArrayList<Integer> posList = new ArrayList<>();
        ArrayList<String[]>  results = new ArrayList<>();
        String tempQuery = searchQuery;
        findSymbols(posList,searchQuery,'\"');
        //findQuotationMarks(posList,searchQuery);


        if(!posList.isEmpty()){
            for(int i=0; i < posList.size() ; i+=2){
                tempQuery = searchQuery.substring(posList.get(i)+2,posList.get(i+1)-1);
                String[] splitStr = tempQuery.split(" ");

                System.out.println(splitStr.length);
                for(int j=0; j < splitStr.length ; j++){
                    db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM);
                    db.pStmt.setString(1,splitStr[j]);
                    db.rs = db.pStmt.executeQuery();
                    while(db.rs.next()){
                        String[] record = {
                                db.rs.getString(1),
                                String.valueOf(db.rs.getInt(2)),
                                db.rs.getString(5),
                                db.rs.getString(5),
                                db.rs.getString(6)
                        };
                        results.add(record);
                    }
                }
            }
        }

        for(String[] sa : results){
            try {
                Scanner itr = new Scanner(new File(sa[3]));
                while(itr.hasNextLine()){
                    String line = itr.nextLine();
                    if(line.contains(tempQuery)){
                        System.out.println(sa[3]);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleOperatorParentheses(String searchQuery) {
    }

    private ArrayList<String[]> handleOperatorAnd(String searchQuery, DatabaseUtil db) throws SQLException {

        System.out.println("handleOperatorAnd: called.\t searchQuery="+searchQuery);

        ArrayList<String[]> recordsList = new ArrayList<>();
        StringBuilder leftWord= new StringBuilder();
        StringBuilder rightWord= new StringBuilder();

        getOperands(leftWord,rightWord,searchQuery);

        db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOC_BY_TERM_AND_TERM);
        db.pStmt.setString(1,leftWord.toString());
        db.pStmt.setString(2,rightWord.toString());
        db.pStmt.setString(3,leftWord.toString());
        db.pStmt.setString(4,rightWord.toString());
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

    private void getOperands(StringBuilder leftWord, StringBuilder rightWord, String searchQuery) {
        String[] tempQuery = searchQuery.split(" ");

        for (int i = 0; i < tempQuery.length ; i++) {
            if(tempQuery[i].equals("&") && tempQuery[i+1] != null){
                leftWord.append(tempQuery[i-1]);
                rightWord.append(tempQuery[i+1]);
                break;
            }
        }

    }

    private ArrayList<String[]> handleOperatorOr(String searchQuery, DatabaseUtil db) {

        System.out.println("handleOperatorOr: called.\t searchQuery="+searchQuery);

        ArrayList<String[]> recordsList = new ArrayList<>();
        StringBuilder leftWord= new StringBuilder();
        StringBuilder rightWord= new StringBuilder();

        getOperands(leftWord,rightWord,searchQuery);



        return null;
    }

    //restriction: the operator must be to the left of the operand
    private ArrayList<String[]> handleOperatorNot(String searchQuery, DatabaseUtil db) throws SQLException {
        System.out.println("handleOperatorNot: called.\t searchQuery="+searchQuery);
        ArrayList<String[]> recordsList = new ArrayList<>();
        String[] tempQuery = searchQuery.split(" ");
        String word = null;

        for (int i = 0; i < tempQuery.length ; i++) {
            if(tempQuery[i].equals("!") && tempQuery[i+1] != null){
                word = tempQuery[i+1];
                break;
            }
        }

        db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOC_BY_NOT_TERM);
        db.pStmt.setString(1,word);
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



    private String eliminateStopWords(String searchQuery, DatabaseUtil db) {

        searchQuery = searchQuery.toLowerCase();
        ArrayList<Integer> posList = new ArrayList<>();
        findSymbols(posList,searchQuery,'\"');


        StringBuilder sb = new StringBuilder(searchQuery);
        //encrypt all characters between q marks:
        for(int i=0; i < posList.size() ; i+=2) {
            for(int j=posList.get(i); j < posList.get(i+1)+1; j++){
                sb.setCharAt(j,'$');
            }
        }
        //remove any clear-text stop word that is left outside of q marks:
        sb = new StringBuilder(eliminate(sb.toString()));

        //decrypt back with the original characters:
        for(int i=0; i < posList.size() ; i+=2) {
            for(int j=posList.get(i); j < posList.get(i+1)+1; j++){
                sb.setCharAt(j,searchQuery.charAt(j));
            }
        }

        System.out.println(sb.toString().trim().replaceAll(" +"," "));
        return sb.toString().trim().replaceAll(" +"," ");
    }

    private void findSymbols(ArrayList<Integer> posList, String searchQuery, char symbol) {
        for(int j = 0 ; j < searchQuery.length() ; ++j) {
            if(searchQuery.charAt(j) == symbol ){
                posList.add(j);
            }
        }
    }


    private String eliminate(String searchQuery) {
        searchQuery = searchQuery.toLowerCase();
        String word,regexWord;

        try {
            //iterate over stop words file
            Scanner itr = new Scanner(new File(stopListPath));

            while(itr.hasNext()){
                word = itr.next();
                regexWord = "\\b" + word + "\\b";
                searchQuery = searchQuery.replaceAll(regexWord, fixedSpacesLength(word));
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("eliminateStopWords called. cant find the stop list file");
            e.printStackTrace();
        }

        return searchQuery;
    }


    private String fixedSpacesLength(String word) {
        StringBuilder sb = new StringBuilder();
        for(int  i=0 ; i < word.length() ; i++){
            sb.append(" ");
        }
        return sb.toString();
    }


    public void setStopListPath(String stopListPath) {
        this.stopListPath = stopListPath;
    }




    class OperatorHandler {
        public int opQuotations = 0;
        public int opParentheses = 0;
        public int opOr = 0;
        public int opAnd = 0;
        public int opNot = 0;


        // in simple terms: < <operatorType,OperatorsCount>, List<posStart,posEnd> >
        public HashMap<HashMap<String,Integer>,ArrayList<HashMap<Integer,Integer>>> ops = new HashMap<>();


        public OperatorHandler(){}


        public void countOperators(String searchQuery){
            for (int i = 0 ; i < searchQuery.length() ; ++i){
                switch (searchQuery.charAt(i)){
                    case '\"':
                        opQuotations++; break;
                    case '&':
                        opAnd++; break;
                    case '!':
                        opNot++; break;
                    case '|':
                        opOr++; break;
                    case '(':
                        opParentheses++; break;
                    case ')':
                        opParentheses++; break;
                    default:
                        break;
                }
            }
            opQuotations /= 2;
            opParentheses /= 2;
        }


        public void clean() {
            opQuotations=0;
            opParentheses=0;
            opOr=0;
            opAnd=0;
            opNot=0;
        }

    }









}
