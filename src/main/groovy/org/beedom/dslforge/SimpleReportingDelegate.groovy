package org.beedom.dslforge

import groovy.util.logging.Slf4j;

import java.util.List;


/**
 * 
 * @author kovax
 *
 */
@Slf4j
public class SimpleReportingDelegate {

    String description = ""

    static def sharedInstance

 
    /**
     * 
     * @return
     */
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
     * @param desc
     */
    public void initDelegate(String desc) {
        description = desc
        
        if(checkReporter()) { reporter.openContext(dslKey, dslAlias, description) }
    }

    
    /**
     * 
     */
    public void destroyDelegate() {
        if(checkReporter()) { reporter.closeContext(dslKey, dslAlias) }
    }


    /**
     * 
     * @param method
     * @param desc
     */
    protected void writeMethod(String method, String desc) {
        if(checkReporter()) { reporter.writeMethod(dslKey, method, dslAlias, desc) }
    }


    /**
     * 
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
