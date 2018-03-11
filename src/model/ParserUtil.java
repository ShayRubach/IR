package model;


import annotations.A;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;
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

    @A.Indexing
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

        // <word , resulted records from db>
        HashMap<String,ArrayList<String[]>> queryMap;
        ArrayList<String[]> recordsList = new ArrayList<>();        //will holder all records we get as results


        //remove useless spaces:
        searchQuery = fixSpaces(searchQuery);
        searchQuery = eliminateStopWords(searchQuery,db);



        queryMap = mapAllTerms(searchQuery,db);
        printMap(queryMap);

        recordsList = deepSearch(queryMap,searchQuery);
        printRecords(recordsList);

        return recordsList;


    }


    private ArrayList<String[]> deepSearch(HashMap<String, ArrayList<String[]>> queryMap, String searchQuery) throws SQLException {

        if(queryMap.size() == 1 || searchQuery.split(" ").length == 1){
            //return last record by the only remaining term in query
            System.out.println("deepSearch: \t no more terms, exiting.");
            System.out.println("SEARCH QUERY: " + searchQuery);
            return queryMap.get(searchQuery);
        }

        StringBuilder leftWord = new StringBuilder();
        StringBuilder rightWord = new StringBuilder();


//        reut & ! ( shay & idan | gal ) & elaya = searchQuery
//        reut & shay & idan | gal  & elaya


//        //TODO: handle () operator
//        if(searchQuery.contains("(") && searchQuery.contains(")"))
//            handleOperatorParentheses(searchQuery,db);

//        //TODO: handle ! operator
//        if(searchQuery.contains("!"))
//            return handleOperatorNot(searchQuery,db);
//

        if(searchQuery.contains("|")){

            //get both operands to the sides of |
            getOperands(leftWord,rightWord,searchQuery,"|");

            //the union operation
            doLogic(queryMap,leftWord,rightWord,"|");

            //cut the treated part of the string: (leftWord_size + operator (1) + spaces (2)
            searchQuery = searchQuery.substring(leftWord.length()+3);

        }

        if(searchQuery.contains("&")){

            //get both operands to the sides of |
            getOperands(leftWord,rightWord,searchQuery,"&");

            //the union operation
            doLogic(queryMap,leftWord,rightWord,"&");

            //cut the treated part of the string: (leftWord_size + operator (1) + spaces (2)
            searchQuery = searchQuery.substring(leftWord.length()+3);

        }




//        //TODO: handle "" operator
//        if(searchQuery.contains("\""))
//            return handleOperatorQuotation(searchQuery,db);










        return deepSearch(queryMap,searchQuery);
    }

    private void printRecords(ArrayList<String[]> records) {
        for(String[] arr : records){
            System.out.print("{ ");
            for(String s : arr){
                System.out.print(s + " , ");
            }
            System.out.println(" }");
        }
    }

    private void doLogic(HashMap<String, ArrayList<String[]>> queryMap, StringBuilder leftWord, StringBuilder rightWord, String op) {

        System.out.println("doLogic: called. \t operator="+op);
        System.out.println("doLogic: map status:");
        printMap(queryMap);


        if(op.equals("|")) {
            System.out.println("doLogic: case OR(|) ");


            //check if words are identical
            // TODO: fix yeah | yeah | yeah issue!
            if(!rightWord.toString().equals(leftWord.toString())) {
                //unite all records into the right-side key
                for (String[] left : queryMap.get(leftWord.toString())) {
                    queryMap.get(rightWord.toString()).add(left);
                }

            }
            //remove left term
            queryMap.remove(leftWord.toString());
        }




        if(op.equals("&")) {
            System.out.println("doLogic: case AND(&) ");

            String right = rightWord.toString();
            String left = leftWord.toString();
            int size = queryMap.get(right).size();


            //check if words are identical
            if(!right.equals(left)) {





                for (int i = 0; i < size ; i++) {
                    String rightDocId = queryMap.get(right).get(i)[1];
                    boolean delete = true;

                    for (int j = 0; j < queryMap.get(left).size() ; j++) {
                        String leftDocId = queryMap.get(left).get(j)[1];

                        if(leftDocId.equals(rightDocId)){
                            delete = false;
                            //add left record to the right records list
                            queryMap.get(right).add(queryMap.get(left).get(j));


                            System.out.println("RIGHT:");
                            for(String[] STR : queryMap.get(right)){
                                for(String sTr : STR)
                                    System.out.println(sTr);
                            }

                            System.out.println("LEFT:");
                            for(String[] STR : queryMap.get(left)){
                                for(String sTr : STR)
                                    System.out.println(sTr);
                            }

                        }

                    }

                    //if the right term did not match any docId from the left terms, remove it.
                    if(true == delete){
                        queryMap.get(rightWord.toString()).remove(i);
                        i--;    //keeping the iterator in boundaries. a removal will decrease the list's size..
                    }

                }
            }
            //remove left term
            queryMap.remove(leftWord.toString());
        }







    }

    private void printMap(HashMap<String, ArrayList<String[]>> queryMap) {
        for(String key : queryMap.keySet()){
            System.out.print(key + " : ");
            for(String[] list : queryMap.get(key)){
                for (String str : list){
                    System.out.print(str + "\t");
                }
            }
            System.out.println("");
        }
    }

    private HashMap<String,ArrayList<String[]>> mapAllTerms(String searchQuery, DatabaseUtil db) throws SQLException {
        HashMap<String,ArrayList<String[]>> map = new HashMap<>();
        //iterate and map:

        String[] words = searchQuery.split(" ");

        for(String word : words){
            ArrayList<String[]> records = new ArrayList<>();

            if(true == isOperator(word))
                continue;
            db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM);
            db.pStmt.setString(1,word);
            db.rs = db.pStmt.executeQuery();

            while(db.rs.next()){
                String[] record = {db.rs.getString(1),      //word
                        String.valueOf(db.rs.getInt(2)),    //id
                        String.valueOf(db.rs.getInt(3)),    //appearances
                        db.rs.getString(5),                 //name
                        db.rs.getString(6)};                //link

                records.add(record);
            }
            //TODO: remove clear, it ends up as null
            map.put(word,new ArrayList<>(records));

            System.out.println("mapAllTerms:  map=");
            printMap(map);
        }

        return map;

    }

    private boolean isOperator(String word) {
        boolean result = false;
        if(word.equals("\"") || word.equals("&")
                || word.equals("|") || word.equals("(")
                || word.equals(")") || word.equals("!") )
            result = true;

        return result;
    }


    public ArrayList<String[]> search2(String searchQuery, DatabaseUtil db) throws SQLException {

        ArrayList<String[]> recordsList = new ArrayList<>();        //will holder all records we get as results

        //remove useless spaces:
        searchQuery = fixSpaces(searchQuery);
        searchQuery = eliminateStopWords(searchQuery,db);
        opHandler.countOperators(searchQuery);


        //TODO: handle () operator
        if(searchQuery.contains("(") && searchQuery.contains(")"))
            return handleOperatorParentheses(searchQuery,db);

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

        //TODO: handle "" operator
        if(searchQuery.contains("\""))
            return handleOperatorQuotation(searchQuery,db);


        String words[] = removeDups(searchQuery.split(" "));

        for(String word : words){
            System.out.println("word: " + word);
            db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM);
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
        }


        return recordsList;


    }

    private String[] removeDups(String[] words) {
        words = new HashSet<String>(Arrays.asList(words)).toArray(new String[0]);
        return words;
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

    private ArrayList<String[]> handleOperatorQuotation(String searchQuery, DatabaseUtil db) throws SQLException {

        ArrayList<Integer> posList = new ArrayList<>();
        ArrayList<String[]>  records = new ArrayList<>();
        ArrayList<String[]>  tempRecords = new ArrayList<>();

        String tempQuery = searchQuery;
        findSymbols(posList,searchQuery,'\"');

        System.out.print("handleOperatorQuotation: tempQuery = ");


        if(!posList.isEmpty()){
            for(int i=0; i < posList.size() ; i+=2){
                tempQuery = searchQuery.substring(posList.get(i)+2,posList.get(i+1)-1);
                String[] splitStr = tempQuery.split(" ");


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
                        records.add(record);
                    }
                }
            }
        }

        for(String[] record : records){
            try {
                Scanner itr = new Scanner(new File(record[4]));
                while(itr.hasNextLine()){
                    String line = itr.nextLine();
                    if(line.trim().contains(tempQuery)){
                        tempRecords.add(record);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }




        //TODO: fix empty return val here. "something" is not working for some reason while "it" "a" and more works.
        return new ArrayList<>(tempRecords);
    }

    private ArrayList<String[]> handleOperatorParentheses(String searchQuery, DatabaseUtil db) {
        ArrayList<String[]> recordsList = new ArrayList<>();


        return recordsList;
    }



    private void getOperands(StringBuilder leftWord, StringBuilder rightWord, String searchQuery,String operator) {
        String[] tempQuery = searchQuery.split(" ");

//        System.out.println("getOperands: called.\ttempQuery = ");
//
//        for(String s : tempQuery)
//            System.out.print(s + "  ,  ");
//        System.out.println("");


        for (int i = 0; i < tempQuery.length ; i++) {
            if(tempQuery[i].equals(operator) && tempQuery[i+1] != null){
                leftWord.append(tempQuery[i-1]);
                rightWord.append(tempQuery[i+1]);
                break;
            }
        }

    }

    private ArrayList<String[]> handleOperatorAnd(String searchQuery, DatabaseUtil db) throws SQLException {

        System.out.println("handleOperatorAnd: called.\t searchQuery="+searchQuery);

        ArrayList<String[]> recordsList = new ArrayList<>();
        StringBuilder leftWord= new StringBuilder();
        StringBuilder rightWord= new StringBuilder();

        getOperands(leftWord,rightWord,searchQuery,"&");
        System.out.println("left: " + leftWord + "\tright: " + rightWord);

        db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOC_BY_TERM_AND_TERM);
        db.pStmt.setString(1,leftWord.toString());
        db.pStmt.setString(2,rightWord.toString());
        db.rs = db.pStmt.executeQuery();

        while(db.rs.next()){
            if(db.rs.getString(1).equals(rightWord.toString()) || db.rs.getString(1).equals(leftWord.toString())) {
                String[] record = {db.rs.getString(1),      //word
                        String.valueOf(db.rs.getInt(2)),    //id
                        String.valueOf(db.rs.getInt(3)),    //appearances
                        db.rs.getString(5),                 //name
                        db.rs.getString(6)};                //link
                recordsList.add(record);
            }
        }

        return recordsList;
    }

    private ArrayList<String[]> handleOperatorOr(String searchQuery, DatabaseUtil db) throws SQLException {

        System.out.println("handleOperatorOr: called.\t searchQuery="+searchQuery);

        ArrayList<String[]> recordsList = new ArrayList<>();
        StringBuilder leftWord= new StringBuilder();
        StringBuilder rightWord= new StringBuilder();

        getOperands(leftWord,rightWord,searchQuery,"|");

        System.out.println("left: " + leftWord + "right: " + rightWord);
        db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOC_BY_TERM_OR_TERM);
        db.pStmt.setString(1,leftWord.toString());
        db.pStmt.setString(2,rightWord.toString());
        db.rs = db.pStmt.executeQuery();

        while(db.rs.next()){
            if(db.rs.getString(1).equals(rightWord.toString()) || db.rs.getString(1).equals(leftWord.toString())) {
                String[] record = {db.rs.getString(1),      //word
                        String.valueOf(db.rs.getInt(2)),    //id
                        String.valueOf(db.rs.getInt(3)),    //appearances
                        db.rs.getString(5),                 //name
                        db.rs.getString(6)};                //link
                recordsList.add(record);
            }
        }

        return recordsList;
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
