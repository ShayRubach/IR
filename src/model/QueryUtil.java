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

    //perform safe check for dups
    public static final String INSERT_NEW_INDEX_FILE =
            "INSERT INTO index_files (" +
                "word, " +
                "doc_id," +
                "appears ) " +
            "VALUES(?,?,?) ";

    public static final String REMOVE_FILE_FROM_STORAGE =
            "UPDATE storage_files " +
                    "SET display=0 " +
                    "WHERE id=?";

    public static final String GET_DOC_ID_BY_LINK =
            "SELECT id FROM storage_files " +
                    "WHERE link=?";

    public static final String GET_LOCAL_STORAGE_FILES =
            "SELECT id,name " +
            "FROM storage_files " +
            "WHERE display=1";

    public static final String GET_DOCS_BY_TERM =
            "SELECT * FROM index_files,storage_files " +
                    "WHERE word=? " +
                    "AND doc_id=id " +
                    "AND display=1 " +
                    "ORDER BY appears DESC";
    }

//    public static final String GET_DOCS_BY_TERM =
//            "SELECT *,name " +
//                    "FROM index_files " +
//                    "JOIN storage_files " +
//                    "WHERE word=? " +
//                    "AND doc_id=id " +
//                    "AND display=1 " +
//                    "ORDER BY appears DESC";
//}