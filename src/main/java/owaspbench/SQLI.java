package owaspbench;

import GraphProcess.Util;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

/**
 * @ Author     ：wxkong
 */
public class SQLI {
    static Connection conn;
    static PreparedStatement ps;
    static CallableStatement cs;
    static JdbcTemplate jt = new JdbcTemplate();

    public static List<String> ExecuteMethodsSQL = Arrays.asList("execute", "executeQuery", "executeUpdate",
            "executeLargeUpdate", "executeBatch", "executeLargeBatch");
    public static List<String> ExecuteMethodsJDBCTemplate = Arrays.asList("update", "batchUpdate",
            "query", "queryForList", "queryForMap", "queryForObject", "queryForRowSet");
    public static List<String> ExecuteMethods = Arrays.asList("execute", "executeQuery", "executeUpdate",
            "executeLargeUpdate", "executeBatch", "executeLargeBatch", "update", "batchUpdate",
            "query", "queryForList", "queryForMap", "queryForObject", "queryForRowSet");

    public static void main(String[] args) {
        String dir = "../benchmark/sqli/true/";
        File file=new File(dir);
        for(File temp:file.listFiles()){
            if(temp.isFile()){
                boolean contains = false;
                for (String m : ExecuteMethods) {
                    if (String.join("\n", Util.readFileToArrayList(dir + temp.getName())).contains(m)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    System.out.println(temp.toString());
                }
            }
        }
    }
}
