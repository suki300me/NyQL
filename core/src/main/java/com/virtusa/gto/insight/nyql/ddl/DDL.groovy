package com.virtusa.gto.insight.nyql.ddl

import com.virtusa.gto.insight.nyql.DSLContext
import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.model.QSession
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
@ToString(includes = ["tables"])
class DDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDL.class)

    final QSession session
    final DSLContext dslContext

    Map<String, DTable> tables = [:]
    List<DTable> tablesToDrop = []

    public DDL(QSession theSession) {
        session = theSession
        dslContext = session.dslContext
    }

    DDL TEMP_TABLE(String name, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: true)
        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)

        // invoke table creation
        callCreateTable(rTable)

        return this
    }

    DDL TABLE(String name, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable()
        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)
        return this
    }

    DDL DROP_TEMP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name, temporary: true)
        }
        rTable.temporary = true
        tablesToDrop.add(rTable)

        callDropTable(rTable)
        return this
    }

    DDL DROP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name)
        }
        tablesToDrop.add(rTable)

        callDropTable(rTable)
        return this
    }

    private void callCreateTable(DTable dTable) {
        LOGGER.debug("Executing table creation command...")
        QResultProxy proxy = session.dslContext.qTranslator.___ddls().___createTable(dTable)
        session.executor.execute(session.scriptRepo.parse(proxy, session))
    }

    private void callDropTable(DTable dTable) {
        LOGGER.debug("Executing table drop command...")
        QResultProxy proxy = session.dslContext.qTranslator.___ddls().___dropTable(dTable)
        session.executor.execute(session.scriptRepo.parse(proxy, session))
    }
}
