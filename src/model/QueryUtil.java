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

    public static final String GET_AVAILABLE_STORAGE_FILES =
            "SELECT id,name " +
                    "FROM storage_files " +
                    "WHERE display=0";


    public static final String GET_DOCS_BY_TERM =
            "SELECT DISTINCT * FROM index_files,storage_files " +
                    "WHERE word=? " +
                    "AND doc_id=id " +
                    "AND display=1 " +
                    "ORDER BY appears DESC";

    public static final String SET_DOC_AVAILABLE =
            "UPDATE storage_files " +
                    "SET display=1 " +
                    "WHERE id=?";

    public static final String GET_DOC_BY_NOT_TERM =
            "SELECT word,doc_id,appears,id,name,link " +
                    "FROM index_files "+
                    "JOIN storage_files ON storage_files.id=index_files.doc_id " +
                    "WHERE doc_id NOT IN " +
                    "   (SELECT doc_id " +
                    "   FROM index_files " +
                    "   WHERE word=?) " +
                    "AND doc_id=id AND display=1" ;


    //bad logic
    public static final String GET_DOC_BY_TERM_AND_TERM =
            "SELECT word,doc_id,appears,id,name,link FROM index_files " +
            "JOIN storage_files ON storage_files.id=index_files.doc_id AND display=1 " +
            "WHERE " +
            "doc_id IN (SELECT doc_id FROM index_files WHERE word=?) " +
            "AND " +
            "doc_id IN (SELECT doc_id FROM index_files WHERE word=?)";


    //bad logic
    public static final String GET_DOC_BY_TERM_OR_TERM =
            "SELECT word,doc_id,appears,id,name,link FROM index_files " +
                    "JOIN storage_files ON storage_files.id=index_files.doc_id AND display=1 " +
                    "WHERE " +
                    "doc_id IN (SELECT doc_id FROM index_files WHERE word=?) " +
                    "OR " +
                    "doc_id IN (SELECT doc_id FROM index_files WHERE word=?)";


    public static final String RESET_DB =
            "DROP TABLE IF EXISTS index_files,storage_files";


    public static final String GET_DOCS_BY_TERM_AND_ID =
            "SELECT DISTINCT * FROM index_files,storage_files " +
                    "WHERE word=? " +
                    "AND id=? " +
                    "AND display=1 " +
                    "ORDER BY appears DESC";


    //SELECT * FROM (SELECT * FROM index_files WHERE word='did') as result WHERE result.doc_id IN (SELECT doc_id FROM index_files WHERE word='i')
}
