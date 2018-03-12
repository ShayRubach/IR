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

        //TODO: handle stop word with operators nearby!! remove operator???
        //TODO: handle quotation marks. calculate all words in quotations into 1 (most left) word and pass them all as operand, then cut them all from query


        queryMap = mapAllTerms(searchQuery,db);
        //printMap(queryMap);

        recordsList = deepSearch(queryMap,searchQuery,db);
        //printRecords(recordsList);

        return recordsList;


    }


    private ArrayList<String[]> deepSearch(HashMap<String, ArrayList<String[]>> queryMap, String searchQuery,DatabaseUtil db) throws SQLException {

        if(searchQuery.split(" ").length == 1 || queryMap.size() == 0){
            //return last record by the only remaining term in query
            System.out.println("deepSearch: \t no more terms, exiting.");
            System.out.println("SEARCH QUERY: " + searchQuery);
            return queryMap.get(searchQuery);
        }



        String opType;
        if(true == containsQuotes(searchQuery))
            opType = "\"";
        else
            opType = findFirstOperator(searchQuery);

        System.out.println("OP TYPE : " + opType);

        StringBuilder leftWord = new StringBuilder();
        StringBuilder rightWord = new StringBuilder();


        System.out.println("$$$$$$$$$   QUERY STRING: " + searchQuery);

        if(opType.equals("|")){

            //get both operands to the sides of |
            getOperands(leftWord,rightWord,searchQuery,"|");

            //TODO: call to a func that converts the !term to term with opposite records:
            //this function is not good:
            searchQuery = invertIfNotTerm(searchQuery,db,queryMap,leftWord,rightWord);

            //the union operation
            doLogic(queryMap,leftWord,rightWord,"|");

            //cut the treated part of the string: (leftWord_size + operator (1) + spaces (2)
            searchQuery = searchQuery.substring(leftWord.length()+3);


        }
        else if(opType.equals("&")){

            //get both operands to the sides of |
            getOperands(leftWord,rightWord,searchQuery,"&");

            //TODO: call to a func that converts the !term to term with opposite records:
            //this function is not good:
            searchQuery = invertIfNotTerm(searchQuery,db,queryMap,leftWord,rightWord);

            //the union operation
            doLogic(queryMap,leftWord,rightWord,"&");

            //cut the treated part of the string: (leftWord_size + operator (1) + spaces (2)
            searchQuery = searchQuery.substring(leftWord.length()+3);

        }
        else if(opType.equals("!")){

            handleOperatorNot(searchQuery,db,queryMap);
            System.out.println("INDEX OF FUCKIN (!) : " + searchQuery.indexOf("!"));
            searchQuery = searchQuery.substring(searchQuery.indexOf("!")+1);
            searchQuery = fixSpaces(searchQuery);
            System.out.println("AFTER SUBSTRING IN (!) : " + searchQuery);

        }

        // "shay and reut" -> shay
        else if(opType.equals("\"")){
            //TODO: return the results to the map or soemthing..
            searchQuery = handleOperatorQuotation(searchQuery,db,queryMap);

            //searchQuery = searchQuery.substring(searchQuery.indexOf("!")+1);
            searchQuery = fixSpaces(searchQuery);

            System.out.println("AFTER SUBSTRING IN (\") : " + searchQuery);

        }

        //TODO: put old handle on non-opertors -> return the exact record list. dont use recursive.


//        //TODO: handle "" operator
//        if(searchQuery.contains("\""))
//            return handleOperatorQuotation(searchQuery,db);

//        //TODO: handle () operator
//        if(searchQuery.contains("(") && searchQuery.contains(")"))
//            handleOperatorParentheses(searchQuery,db);

//        //TODO: handle ! operator
//        if(searchQuery.contains("!"))
//            return handleOperatorNot(searchQuery,db);
//







        leftWord.setLength(0);
        rightWord.setLength(0);
        return deepSearch(queryMap,searchQuery,db);
    }

    private boolean containsQuotes(String searchQuery) {
        boolean result = false;
        for (int i = 0 ; i < searchQuery.length() ; ++i){
            if(searchQuery.charAt(i) == '\"')
                result = true;
        }
        return result;
    }

    private String invertIfNotTerm(String searchQuery, DatabaseUtil db, HashMap<String,
                                 ArrayList<String[]>> queryMap, StringBuilder left,
                                   StringBuilder right) throws SQLException {

        String word = null;
        String[] tempQuery = searchQuery.split(" ");

        if(right.toString().equals("!")){
            for (int i = 0; i < tempQuery.length ; i++) {
                if(tempQuery[i].equals(("!")) && tempQuery[i+1] != null && i+1 < tempQuery.length){
                    word = tempQuery[i+1];
                }
            }

        }

        if(word == null){
            //we got not (!) operator, we can safely return to our deepSearch.
            return searchQuery;
        }

        handleOperatorNot("! "+word,db,queryMap);
        System.out.println("INDEX OF FUCKIN (!) : " + searchQuery.indexOf("!"));

        //TODO: cut the string as if it was in the middle of the query..
        StringBuilder sb = new StringBuilder(searchQuery);
        sb.deleteCharAt(searchQuery.indexOf("!"));
        searchQuery = sb.toString();
        searchQuery = fixSpaces(searchQuery);
        System.out.println("AFTER SUBSTRING IN (!) : " + searchQuery);

        //replace the right word (!) with the real word (operand)
        right.setLength(0);
        right.append(word);

        return searchQuery;

    }


    private String findFirstOperator(String searchQuery) {
        for (int i = 0 ; i < searchQuery.length() ; ++i){
            switch (searchQuery.charAt(i)){
                case '\"':
                    return "\"";
                case '&':
                    return "&";
                case '!':
                    return "!";
                case '|':
                    return "|";
                case '(':
                    return "(";
                default:
                    break;
            }
        }
        return null;
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

        String right = rightWord.toString();
        String left = leftWord.toString();

        if(op.equals("|")) {
            System.out.println("doLogic: case OR(|) ");

            if(queryMap.get(left).size() == 0 || queryMap.get(right).size() == 0){
                if (queryMap.get(right).size() == 0){
                    queryMap.remove(right);;
                }
                else{
                    queryMap.remove(left);
                }
                return;
            }

            //check if words are identical
            // TODO: fix yeah | yeah | yeah issue!
            if(!rightWord.toString().equals(leftWord.toString())) {
                //unite all records into the right-side key
                for (String[] leftLine : queryMap.get(leftWord.toString())) {
                    queryMap.get(rightWord.toString()).add(leftLine);
                }

            }
            //remove left term
            queryMap.remove(leftWord.toString());

        }
        else if(op.equals("&")) {
            System.out.println("doLogic: case AND(&) ");
            System.out.println("---------:LEFT:" + left);
            System.out.println("---------:RIGHT:" + right);

            if(true == listsAreIdentical(queryMap,left,right)){
                System.out.println("doLogic: lists are identical. ");
                return;
            }

            if(queryMap.get(left).size() == 0 || queryMap.get(right).size() == 0){
                if(queryMap.get(left).size() == 0 ){
                    queryMap.remove(right);
                }
                else{
                    queryMap.remove(left);
                }
                return;
            }

            //check if words are identical
            if(!right.equals(left)) {

                int size = queryMap.get(right).size();

                for (int i = 0; i < size ; i++) {
                    String rightDocId = queryMap.get(right).get(i)[1];
                    boolean delete = true;

                    for (int j = 0; j < queryMap.get(left).size() ; j++) {

                        System.out.println("i = " + i + "\t\tj = " + j);
                        printMap(queryMap);

                        String leftDocId = queryMap.get(left).get(j)[1];


                        String rightRecordName = queryMap.get(right).get(i)[0];
                        String leftRecordName = queryMap.get(left).get(j)[0];

                        System.out.println("queryMap.get(right).get(i)[0] = " + queryMap.get(right).get(i)[0] + "\t\ti="+i);
                        System.out.println("queryMap.get(left).get(j)[0] = " + queryMap.get(left).get(j)[0] + "\t\tj=" + j);

                        //if doc id is identical and this is not the same record (same word on both sides)
                        if(leftDocId.equals(rightDocId) && !rightRecordName.equals(leftRecordName)){
                            delete = false;

                            //add left record to the right records list
                            queryMap.get(right).add(hackFromTheMovies(queryMap.get(left).get(j)));

                        }
                    }


                    if(queryMap.get(left).isEmpty()){
                        delete = false;
                    }

                    //if the right term did not match any docId from the left terms, remove it.
                    if(true == delete){
                        queryMap.get(rightWord.toString()).remove(i);
                        if(!queryMap.get(right).isEmpty()){
                            i--;    //keeping the iterator in boundaries. a removal will decrease the list's size..
                        }else{
                            i = size;
                        }
                    }
                }
            }
        }

        //remove left term
        System.out.println((char)27 + "[31mAbout to remove left word from map!!" + (char)27 + "[0m");
        System.out.println((char)27 + "[31mMAP STATUS BEFORE REMOVING LEFT WORD:" + (char)27 + "[0m");
        printMap(queryMap);
        queryMap.remove(leftWord.toString());
        System.out.println((char)27 + "[31mLEFT WORD REMOVED.." + (char)27 + "[0m");
        System.out.println((char)27 + "[31mMAP STATUS AFTER REMOVING LEFT WORD:" + (char)27 + "[0m");
        printMap(queryMap);


    }

    private boolean listsAreIdentical(HashMap<String, ArrayList<String[]>> queryMap,String left, String right) {
        boolean result = false;
        int leftListSize = queryMap.get(left).size();
        int rightListSize = queryMap.get(right).size();


        if(leftListSize == rightListSize
                && queryMap.get(left).get(0)[0].equals(queryMap.get(right).get(0)[0])
                && queryMap.get(left).get(leftListSize-1)[0].equals(queryMap.get(right).get(rightListSize-1)[0])){
            result = true;
        }

        return  result;
    }

//    private void doLogic2(HashMap<String, ArrayList<String[]>> queryMap, StringBuilder leftWord, StringBuilder rightWord, String op) {
//
//        System.out.println("doLogic: called. \t operator="+op);
//        System.out.println("doLogic: map status:");
//        printMap(queryMap);
//
//        String right = rightWord.toString();
//        String left = leftWord.toString();
//
//        if(op.equals("|")) {
//            System.out.println("doLogic: case OR(|) ");
//
//            if(queryMap.get(left).size() == 0 || queryMap.get(right).size() == 0){
//                if (queryMap.get(right).size() == 0){
//                    queryMap.remove(right);;
//                }
//                else{
//                    queryMap.remove(left);
//                }
//                return;
//            }
//
//            //check if words are identical
//            // TODO: fix yeah | yeah | yeah issue!
//            if(!rightWord.toString().equals(leftWord.toString())) {
//                //unite all records into the right-side key
//                for (String[] leftLine : queryMap.get(leftWord.toString())) {
//                    queryMap.get(rightWord.toString()).add(leftLine);
//                }
//
//            }
//            //remove left term
//            queryMap.remove(leftWord.toString());
//
//        }
//        else if(op.equals("&")) {
//            System.out.println("doLogic: case AND(&) ");
//            System.out.println("---------:LEFT:" + left);
//            System.out.println("---------:RIGHT:" + right);
//
//            if(queryMap.get(left).size() == 0 || queryMap.get(right).size() == 0){
//                if(queryMap.get(left).size() == 0 ){
//                    queryMap.remove(right);
//                }
//                else{
//                    queryMap.remove(left);
//                }
//                return;
//            }
//
//            //check if words are identical
//            if(!right.equals(left)) {
//
//
//
//                for (int i = 0; i < queryMap.get(right).size() ; i++) {
//                    String rightDocId = queryMap.get(right).get(i)[1];
//                    boolean delete = true;
//
//                    for (int j = 0; j < queryMap.get(left).size() ; j++) {
//
//                        System.out.println("i = " + i + "\t\tj = " + j);
//                        printMap(queryMap);
//
//                        String leftDocId = queryMap.get(left).get(j)[1];
//
//                        if(leftDocId.equals(rightDocId) /* && the words are different! */ ){
//                            delete = false;
//
//                            //add left record to the right records list
//                            int len =queryMap.get(left).get(j).length;
//
//                            queryMap.get(right).add(hackFromTheMovies(queryMap.get(left).get(j)));
//                            queryMap.get(left).remove(j);
//
//                            //TODO: do we need to j-- ?
//                            j--;
//                        }
//
//                    }
//                    if(queryMap.get(left).isEmpty()){
//                        delete = false;
//                    }
//
//                    //if the right term did not match any docId from the left terms, remove it.
//                    if(true == delete){
//                        queryMap.get(rightWord.toString()).remove(i);
//                        i--;    //keeping the iterator in boundaries. a removal will decrease the list's size..
//                    }
//
//                }
//            }
//
//
//
//        }
//
//        //remove left term
//        System.out.println((char)27 + "[31mAbout to remove left word from map!!" + (char)27 + "[0m");
//        System.out.println((char)27 + "[31mMAP STATUS BEFORE REMOVING LEFT WORD:" + (char)27 + "[0m");
//        printMap(queryMap);
//        queryMap.remove(leftWord.toString());
//        System.out.println((char)27 + "[31mLEFT WORD REMOVED.." + (char)27 + "[0m");
//        System.out.println((char)27 + "[31mMAP STATUS AFTER REMOVING LEFT WORD:" + (char)27 + "[0m");
//        printMap(queryMap);
//
//
//    }

    private String[] hackFromTheMovies(String[] remove) {
        List<String> nonBlank = new ArrayList<>();
        for(String s: remove) {
            if (!s.trim().isEmpty()) {
                nonBlank.add(s);
            }
        }
        // nonBlank will have all the elements which contain some characters.
        return nonBlank.toArray( new String[nonBlank.size()] );
    }

    private void printMap(HashMap<String, ArrayList<String[]>> queryMap) {
        for(String key : queryMap.keySet()){
            System.out.println((char)27 + "[33m" + key + " : "+(char)27 + "[0m");
            System.out.println((char)27 + "[33m============= : "+(char)27 + "[0m");
            for(String[] list : queryMap.get(key)){
                for (String str : list){
                    System.out.print(str + "\t\t\t");
                }
                System.out.println("");
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

            map.put(word,new ArrayList<>(records));

            //System.out.println("mapAllTerms:  map=");
            //printMap(map);
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


//        //TODO: handle () operator
//        if(searchQuery.contains("(") && searchQuery.contains(")"))
//            return handleOperatorParentheses(searchQuery,db);
//
//        System.out.println(searchQuery);
//        //TODO: handle ! operator
//        if(searchQuery.contains("!"))
//            return handleOperatorNot(searchQuery,db);
//
//        //TODO: handle | operator
//        if(searchQuery.contains("|"))
//            return handleOperatorOr(searchQuery,db);
//
//        //TODO: handle & operator
//        if(searchQuery.contains("&"))
//            return handleOperatorAnd(searchQuery,db);
//
//        //TODO: handle "" operator
//        if(searchQuery.contains("\""))
//            return handleOperatorQuotation(searchQuery,db);


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

    private String handleOperatorQuotation(String searchQuery, DatabaseUtil db, HashMap<String, ArrayList<String[]>> queryMap) throws SQLException {

        ArrayList<Integer> posList = new ArrayList<>();
        ArrayList<String[]>  records = new ArrayList<>();
        ArrayList<String[]>  tempRecords = new ArrayList<>();

        String tempQuery = searchQuery;
        findSymbols(posList,searchQuery,'\"');

        System.out.println("handleOperatorQuotation: tempQuery = " + tempQuery);

        if(!posList.isEmpty()){
            for(int i=0; i < posList.size() ; i+=2){
                tempQuery = searchQuery.substring(posList.get(i)+2,posList.get(i+1)-1);
                String[] splitStr = tempQuery.split(" ");

                System.out.println("handleOperatorQuotation: AFTER TRIM->tempQuery = {" + tempQuery + "}");

                String term = splitStr[0];
                System.out.println("TERM: "+ term);

                db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM);
                db.pStmt.setString(1,term);
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
        else {
            System.out.println("handleOperatorQuotation:\t\t posList is empty.");
        }



        for(String[] record : records){
            try {
                Scanner itr = new Scanner(new File(record[4]));
                while(itr.hasNextLine()){
                    String line = itr.nextLine();
                    if(line.trim().contains(tempQuery)){
                        tempRecords.add(record);

                        // {"chi","chu"}
                        String[] words = tempQuery.split(" ");

                        System.out.println("LINE : " + line);
                        System.out.println("WORDS AFTER SPLIT");
                        for(String s : words){
                            System.out.println(s);
                        }


                        if(words.length > 1){

                            ///start from 2nd word. 1st is a header
                            for (int i = 1; i < words.length ; i++) {

                                //"chu"
                                db.pStmt = db.getConn().prepareStatement(QueryUtil.GET_DOCS_BY_TERM_AND_ID);
                                db.pStmt.setString(1,words[i]);         //our word friend
                                db.pStmt.setString(2,record[1]);        //id
                                db.rs = db.pStmt.executeQuery();
                                while(db.rs.next()){
                                    String[] anotherRecord = {
                                            db.rs.getString(1),
                                            String.valueOf(db.rs.getInt(2)),
                                            db.rs.getString(5),
                                            db.rs.getString(5),
                                            db.rs.getString(6)
                                    };
                                    tempRecords.add(anotherRecord);
                                }
                            }
                        }

                        //update map with word[0]
                        queryMap.put(words[0],new ArrayList<>(tempRecords));
                        System.out.println("tempRecords of : " + words[0]);
                        printRecords(tempRecords);

                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        System.out.println("TEMP RECORDS AFTERALL SHIT: ");
        printRecords(tempRecords);

        searchQuery = fixQuotedTerm(posList,searchQuery);

        System.out.println("searchQuery AFTER fixQuotedTerm = " + searchQuery);


        return searchQuery;
    }

    private String fixQuotedTerm(ArrayList<Integer> posList,String searchQuery) {

        String tempQuery;
        StringBuilder sb = new StringBuilder();
        System.out.println("fixQuotedTerm: called");
        System.out.println("fixQuotedTerm: searchQuery = {" + searchQuery + "}");


        for (int i = 0; i < posList.size() ; i+=2) {
            //get the " X Y Z " phrase
            tempQuery = searchQuery.substring(posList.get(i)+2,posList.get(i+1)-1);
            String[] splitStr = tempQuery.split(" ");

            System.out.println("split str:  ");
            for(String s : splitStr)
                System.out.println(s);

            sb.append(searchQuery.substring(0,posList.get(i)));     //take string upto the first "
            System.out.println("SB1 : {" + sb + "}");
            sb.append(splitStr[0]);                                 //append the head term (X) of " X Y Z "
            System.out.println("SB2 : {" + sb + "}");
            sb.append(searchQuery.substring(posList.get(i+1)+1));
            System.out.println("SB3 : {" + sb + "}");



        }
        return sb.toString();
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
    private void handleOperatorNot(String searchQuery, DatabaseUtil db,HashMap<String,ArrayList<String[]>> queryMap) throws SQLException {
        System.out.println("handleOperatorNot: called.\t searchQuery="+searchQuery);
        ArrayList<String[]> recordsList = new ArrayList<>();
        String[] tempQuery = searchQuery.split(" ");
        String word = null;

        for (int i = 0; i < tempQuery.length ; i++) {
            if(tempQuery[i].equals("!") && tempQuery[i+1] != null){
                System.out.println((char)27 + "[35m" +"DOING A NOT(!) ON THIS: " + tempQuery[i+1] +(char)27 + "[0m");
                System.out.println();
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

        queryMap.put(word,recordsList);

        System.out.println((char)27 + "[36  m" +"MAP AFTER NOT (!): " + (char)27 + "[0m");
        printMap(queryMap);

        return;

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
