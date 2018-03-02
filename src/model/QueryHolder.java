package model;

public class QueryHolder {

    public static final String CREATE_POSTING_TABLE =
            "CREATE TABLE       posting_table (" +
                    "word 		VARCHAR(20) NOT NULL," +
                    "document 	INT NOT NULL," +
                    "PRIMARY KEY (word));";

    public static final String INSERT_POSTING_FILE =
            "INSERT INTO posting_table (" +
                    "word, " +
                    "document)" +
                    "VALUES (?,?);";
}
