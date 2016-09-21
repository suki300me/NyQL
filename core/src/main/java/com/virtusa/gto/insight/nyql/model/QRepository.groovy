package com.virtusa.gto.insight.nyql.model

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.exceptions.NyException

/**
 * @author IWEERARATHNA
 */
trait QRepository {

    abstract void clearCache(int level)

    abstract QScript parse(String scriptId, QSession session) throws NyException

    QScript parse(QResultProxy resultProxy, QSession session = null) throws NyException {
        return new QScript(proxy: resultProxy, qSession: session)
    }

}