package com.virtusa.gto.insight.nyql.engine.exceptions

import com.virtusa.gto.insight.nyql.exceptions.NyException
import groovy.transform.InheritConstructors

/**
 * @author IWEERARATHNA
 */
@InheritConstructors
class NyScriptParseException extends NyException {

    NyScriptParseException(String scriptId, File file, Throwable inner) {
        super("Query script cannot be parsed due to syntax errors! ['$scriptId', ${file.absolutePath}]", inner)
    }

}
