package com.virtusa.gto.insight.nyql.model.impl

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession

/**
 * @author IWEERARATHNA
 */
class QNoProfiling implements QProfiling {

    public static final QNoProfiling INSTANCE = new QNoProfiling()

    private QNoProfiling() { }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {

    }

    @Override
    void doneExecuting(QScript script, long elapsed) {

    }

    @Override
    void close() throws IOException {

    }
}
