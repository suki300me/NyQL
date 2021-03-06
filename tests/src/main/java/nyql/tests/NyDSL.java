package nyql.tests;

import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QScript;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author IWEERARATHNA
 */
public class NyDSL {

    private static final String QNAME = "::q::";
    private static final char NL = '\n';

    private Map<String, StringBuilder> queries = new HashMap<>();

    private LinkedList<String> qOrder = new LinkedList<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    public StringBuilder newInnerQuery() {
        return newInnerQuery("innQ" + seq.incrementAndGet());
    }

    public static void main(String[] args) throws NyException {
        try {
            NyQL.configure(new File("./configs/nyql.json"));

            QScript aaa = NyQL.parse("aaa", new HashMap<>());
            System.out.println(aaa);

        } finally {
            NyQL.shutdown();
        }
    }

    private StringBuilder newInnerQuery(String name) {
        qOrder.addFirst(name);
        StringBuilder builder = new StringBuilder();
        queries.put(name, builder);
        return builder;
    }

    public StringBuilder newQuery() {
        qOrder.addLast(QNAME);
        StringBuilder builder = new StringBuilder();
        queries.put(QNAME, builder);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder query = new StringBuilder();
        for (String q : qOrder) {
            query.append(NL).append(NL);
            if (!q.equals(QNAME)) {
                query.append("def ").append(q).append(" = ");
            }
            query.append(queries.get(q).toString());
        }
        return query.toString();
    }
}
