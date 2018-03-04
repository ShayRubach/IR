package model;

public class QueryHolder {

    public static final String CREATE_INDEX_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "index_table (" +
                        "word 		VARCHAR(20) NOT NULL," +
                        "doc_id 	INT NOT NULL," +
                        "appears 	INT NOT NULL," +
                        "PRIMARY KEY (word,doc_id)" +
                    ");";

    public static final String CREATE_FILE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "file_table (" +
                        "id 	INT NOT NULL," +
                        "name 	VARCHAR(32) NOT NULL," +
                        "link 	VARCHAR(64) NOT NULL," +
                        "PRIMARY KEY (id)" +
                    ");";

    public static final String INSERT_POSTING_FILE =
            "INSERT INTO posting_table (" +
                    "word, " +
                    "document)" +
                    "VALUES (?,?);";

}
