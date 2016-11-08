package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QLogProfiler implements QProfiling {

    static final QLogProfiler INSTANCE = new QLogProfiler()

    private static final Logger LOGGER = LoggerFactory.getLogger(QLogProfiler)
    private static final String TAG = '[NyPROFILE]'
    private static final String TOOK = ' took ('
    private static final String TIME = ' ms) time.'

    private QLogProfiler() {}

    @Override
    void start(Map options) {
        LOGGER.info(TAG + ' Log profiler started.')
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        LOGGER.info(TAG + ' Script parsing ' + scriptId + TOOK + elapsed + TIME)
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        LOGGER.info(TAG + ' Script execution ' + script.id + TOOK + elapsed + TIME)
    }

    @Override
    void close() throws IOException {
        LOGGER.debug('Closing log profile.')
    }
}
