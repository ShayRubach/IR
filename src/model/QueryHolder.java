package model;

public class QueryHolder {

    public static final String CREATE_INDEX_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "index_files (" +
                        "word 		varchar(20) NOT NULL," +
                        "doc_id 	int NOT NULL," +
                        "appears 	int NOT NULL," +
                        "FOREIGN KEY (doc_id) REFERENCES source_files(id) ON DELETE CASCADE, " +
                        "PRIMARY KEY (word,doc_id))";

    public static final String CREATE_FILE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "source_files (" +
                        "id 	int NOT NULL AUTO_INCREMENT," +
                        "name 	varchar(128) NOT NULL," +
                        "link 	varchar(128) NOT NULL," +
                        "PRIMARY KEY (id)" +
                    ");";

    public static final String REMOVE_FILE_FROM_SOURCE =
            "DELETE FROM file_table " +
            "WHERE id=?";

    //perform safe check for dups
    public static final String INSERT_SOURCE_FILE =
            "INSERT INTO source_files (" +
                "name, " +
                "link) " +
            "VALUES(?,?) ";

    public static final String IS_SOURCE_FILE_EXISTS =
            "SELECT COUNT(id) " +
            "FROM source_files " +
            "WHERE name=? AND link=?";




}
