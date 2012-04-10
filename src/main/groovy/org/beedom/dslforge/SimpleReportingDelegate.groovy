package org.beedom.dslforge

import groovy.util.logging.Slf4j;

import java.util.List;


/**
 * 
 * @author kovax
 *
 */
@Slf4j
abstract class SimpleReportingDelegate {

    String description = ""

    static def sharedInstance
    
 
    protected boolean checkReporter() {
        if( this.metaClass.properties.find{it.name=="reporter"} && reporter ) {
            return true
        }
        else {
            log.warn("reporter property does not exists or not initialised")
            return false
        }
    }

    /**
     * 
     * @param name
     */
    public void initDelegate(String desc) {
        description = desc
        
        if( checkReporter() ) {
            reporter.openContext(dslKey, dslAlias ?: dslKey, description)
        }
    }

    
    /**
     * 
     */
    public void destroyDelegate() {
        if(checkReporter()) { reporter.closeContext(dslKey, dslAlias ?: dslKey) }
    }


    /**
     * 
     * @param method
     * @param desc
     * @param level
     */
    protected void writeMethod(String method, String desc) {
        if(checkReporter()) { reporter.writeMethod(dslKey, dslAlias ?: method, desc) }
    }


    /**
     * 
     * @param clazz
     * @param methods
     */
    public void addDelegateMethods(List methods) {
        assert this.metaClass.properties.find{it.name=="reporter"}, "reporter shall be added dynamicaly"

        methods.each { method ->
            this.metaClass."$method" = { String desc ->
                writeMethod(method, desc)
                return [:].withDefault { key -> 
                    return { cl -> 
                        if(cl && !context.dryRun) { return cl() }
                    }
                }
            }

            this.metaClass."$method" = { String desc, Closure cl ->
                writeMethod(method, desc)
                if(cl && !context.dryRun) { return cl() }
            }
        }
    }
}
