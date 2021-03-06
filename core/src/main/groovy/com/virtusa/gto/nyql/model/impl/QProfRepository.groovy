package com.virtusa.gto.nyql.model.impl

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.QRepository
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptResult
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class QProfRepository implements QRepository {

    private final QRepository repository
    private final Configurations configurations

    QProfRepository(Configurations theConfigs, QRepository theRepository) {
        configurations = theConfigs
        repository = theRepository
    }

    @Override
    void clearCache(int level) {
        repository.clearCache(level)
    }

    @Override
    QScript parse(String scriptId, QSession session) throws NyException {
        long s = System.currentTimeMillis()
        def result = repository.parse(scriptId, session)
        long e = System.currentTimeMillis()

        if (result instanceof QScriptResult) {
            configurations.profiler.doneExecuting(result, (e - s))
        } else {
            configurations.profiler.doneParsing(scriptId, (e - s), session)
        }
        result
    }

    @Override
    void close() throws IOException {
        repository.close()
    }
}
