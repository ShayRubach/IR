package model;

public class QueryUtil {

    public static final String CREATE_INDEX_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "index_files (" +
                        "word 		varchar(20) NOT NULL," +
                        "doc_id 	int NOT NULL," +
                        "appears 	int NOT NULL," +
                        "FOREIGN KEY (doc_id) REFERENCES storage_files(id) ON DELETE CASCADE, " +
                        "PRIMARY KEY (word,doc_id))";


    public static final String CREATE_STORAGE_FILES_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "storage_files (" +
                    "id 	int NOT NULL AUTO_INCREMENT," +
                    "name 	varchar(128) NOT NULL," +
                    "link 	varchar(128) NOT NULL," +
                    "display varchar(1) NOT NULL," +
                    "PRIMARY KEY (id)" +
                    ");";

    //perform safe check for dups
    public static final String INSERT_FILE_TO_STORAGE =
            "INSERT INTO storage_files (" +
                "name, " +
                "link," +
                "display ) " +
            "VALUES(?,?,?) ";


    public static final String GET_AVAILABLE_STORAGE_FILES =
            "SELECT id,name,link " +
            "FROM storage_files " +
            "WHERE display=1";
}
