package nyql.tests;

import com.virtusa.gto.nyql.configs.Configurations;
import com.virtusa.gto.nyql.configs.NyConfig;
import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.model.QScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author IWEERARATHNA
 */
public class DBExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBExecutor.class);

    public static void main(String[] args) throws Exception {
        Configurations configurations = NyConfig.withDefaults()
                .forDatabase("mysql")
                .scriptFolder(new File("./examples"))
                .jdbcOptions("jdbc:mysql://localhost/sakila", "root", "root", "com.mysql.cj.jdbc.Driver")
                .build();

        NyQLInstance nyQLInstance = null;
        try {
            Map<String, Object> data = new HashMap<>();
            List<Integer> teams = asList(1410, 1234);
            List<Integer> modules = asList(97389, 97390, 97391);

            Map<String, Object> deeper = new HashMap<>();
            deeper.put("cid", 100);

            Map<String, Object> inners = new HashMap<>();
            inners.put("abc", "Dsadsads");
            inners.put("cids", deeper);

            data.put("teamIDs", teams);
            data.put("moduleIDs", modules);
            data.put("filmId", 250);
            data.put("start", 100);
            data.put("cost", 200);
            data.put("minRentals", 5);

            data.put("amap", inners);

            //data.put("hello", inners);

            nyQLInstance = NyQLInstance.create(configurations);

            //QScript result = NyQL.parse("insight/unmapped_users", data);
            Object result = nyQLInstance.execute("temp_table_test", data);
            System.out.println(result);

        } finally {
            if (nyQLInstance != null) {
                nyQLInstance.shutdown();
            }
        }
        //NyQL.execute("")
        //Quickly.configOnce();
        //parse();
    }

    private static void parse() throws Exception {
        Map<String, Object> data = new HashMap<>();
        List<Integer> teams = asList(1410, 1411);
        List<Integer> modules = asList(97389, 97390, 97391);

        data.put("teamIDs", teams);
        data.put("moduleIDs", modules);
        data.put("filmId", 250);

        QScript result = NyQL.parse("select", data);
        System.out.println(result);
    }

    private static void execute() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("minRentals", 25);
        data.put("customerId", 2);
        data.put("filmId", 250);

        Object result = NyQL.execute("top_customers", data);
        if (result instanceof List) {
            for (Object row : (List)result) {
                LOGGER.debug(row.toString());
            }
        }
    }

    @SafeVarargs
    private static <T> List<T> asList(T... items) {
        List<T> list = new LinkedList<T>();
        Collections.addAll(list, items);
        return list;
    }

}
