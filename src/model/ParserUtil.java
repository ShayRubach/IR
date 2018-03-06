package model;


import java.util.ArrayList;

public class ParserUtil {



    public ParserUtil(){}


    /*
    parse this string: { 1 $ Shay - A Song $ C:/Shay/Shay - A Song.mp3 $ 0 }
    doc id = 1
    doc name = Shay - A Song
    doc link = C:/Shay/Shay - A Song.mp3
    display = 0 (false, do not display)
    $ = delimiter
     */
    public String parseSourceNameAndId(String unfixedString, String delim){

        int delimPos;
        String docId,docName;

        delimPos = unfixedString.indexOf(delim);
        docId = unfixedString.substring(0,delimPos);
        unfixedString = unfixedString.substring(delimPos+1);

        delimPos = unfixedString.indexOf(delim);
        docName = unfixedString.substring(0,delimPos);


        return docName + "("+docId+")";
    }





}
