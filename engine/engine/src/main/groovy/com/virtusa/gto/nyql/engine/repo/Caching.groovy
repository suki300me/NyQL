package com.virtusa.gto.nyql.engine.repo

import com.virtusa.gto.nyql.configs.Configurations
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.NyBaseScript
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import com.virtusa.gto.nyql.model.QSource
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap

/**
 * @author IWEERARATHNA
 */
@CompileStatic
class Caching implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Caching)

    private final Map<String, QScript> cache = new ConcurrentHashMap<>()

    private CompilerConfiguration compilerConfigurations
    private final GroovyClassLoader gcl
    private final Configurations configurations

    Caching(Configurations theConfigs) {
        configurations = theConfigs

        gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, makeCompilerConfigs())
    }

    void compileAllScripts(Collection<QSource> sources) throws NyException {
        if (configurations.cacheRawScripts()) {
            int n = sources.size()
            int len = String.valueOf(n).length()
            int curr = 1

            LOGGER.info("Compiling all ${n} dsl script(s)...")
            for (QSource qSource : sources) {
                String id = qSource.id
                try {
                    LOGGER.debug('  Compiling [' + padLeft(len, curr++) + '/' + n + ']: ' + id)
                    gcl.parseClass(qSource.codeSource, true)
                } catch (CompilationFailedException ex) {
                    LOGGER.error("Compilation error in script '$id'", ex)
                    throw new NyException("Compilation error in script '$id'!", ex)
                }
            }
            LOGGER.info('Compilation successful!')
        }
    }

    private String padLeft(int len, int number) {
        ' '*(len - String.valueOf(number).length()) + number
    }

    boolean hasGeneratedQuery(String scriptId) {
        cache.containsKey(scriptId)
    }

    QScript getGeneratedQuery(String scriptId, QSession session) {
        QScript qScript = cache.get(scriptId)
        if (qScript != null) {
            return new QScript(id: qScript.id, proxy: qScript.proxy, qSession: session)
        }
        qScript
    }

    /**
     * Spawn a new script instance from already cached instance of script.
     *
     * @param src source script to make a clone.
     * @return new instance of script.
     */
    private static QScript spawnScriptFrom(QScript src) {
        src.spawn()
    }

    /**
     * Add a generated query to the cache.
     *
     * @param scriptId script id.
     * @param script generated query instance with result.
     * @return the added script instance.
     */
    QScript addGeneratedQuery(String scriptId, QScript script) {
        cache.put(scriptId, spawnScriptFrom(script))
        script
    }

    /**
     * Returns a new instance of compiled script from the cache.
     *
     * @param sourceScript corresponding source of the script.
     * @param session session instance.
     * @return newly created script instance.
     */
    Script getCompiledScript(QSource sourceScript, QSession session) {
        Binding binding = new Binding(session?.sessionVariables ?: [:])
        if (configurations.cacheRawScripts()) {
            Class<?> clazz = gcl.parseClass(sourceScript.codeSource, true)
            NyBaseScript scr = clazz.newInstance() as NyBaseScript
            scr.setBinding(binding)
            scr.setSession(session)
            scr
        } else {
            GroovyShell shell = new GroovyShell(Thread.currentThread().contextClassLoader, binding, makeCompilerConfigs())
            Script parsedScript = sourceScript.parseIn(shell)
            parsedScript.setSession(session)
            parsedScript
        }
    }

    /**
     * CLear the generated query cache or class loader cache.
     *
     * @param level level of cache to clean.
     */
    void clearGeneratedCache(int level) {
        if (level >= 0) {
            cache.clear()
        }
        if (level > 1) {
            gcl.clearCache()
        }
    }

    /**
     * Create a set of configurations requires for script initial compilation.
     *
     * @return compiler configuration instance newly created or already created.
     */
    CompilerConfiguration makeCompilerConfigs() {
        if (compilerConfigurations != null) {
            return compilerConfigurations
        }

        compilerConfigurations = new CompilerConfiguration()

        compilerConfigurations.scriptBaseClass = NyBaseScript.name
        ASTTransformationCustomizer astStatic = new ASTTransformationCustomizer(CompileStatic)
        SourceAwareCustomizer sac = new SourceAwareCustomizer(astStatic)
        sac.extensionValidator = { ext -> ext == 'sgroovy' }
        compilerConfigurations.addCompilationCustomizers(sac)

        String[] defImports = configurations.defaultImports()
        if (defImports != null) {
            ImportCustomizer importCustomizer = new ImportCustomizer()
            importCustomizer.addImports(defImports)
            compilerConfigurations.addCompilationCustomizers(importCustomizer)
        }
        compilerConfigurations
    }

    @Override
    void close() throws IOException {
        if (gcl != null) {
            gcl.close()
        }
    }
}
