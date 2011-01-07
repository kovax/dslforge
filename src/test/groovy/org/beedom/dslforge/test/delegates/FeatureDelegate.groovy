package org.beedom.dslforge.test.delegates


import org.beedom.dslforge.BindingConvention


class FeatureDelegate {
	
    def static dslKey = "feature"

    static sharedInstance
    def nesting = 0

    String name = ""
    String description = ""
    
	public FeatureDelegate() {
        init(null)
    }

    public FeatureDelegate(String desc) {
        println "FeatureDelegate()"
        
        init(desc)
    }
    
    def init(String desc) {
        if(desc) {
            description = desc
        }
        nesting++
        println "FeatureDelegate.init(): $description $nesting."
    }
    
    def destroy() {
        nesting--
    }

    def doThisAlways(String method, String desc, Closure cl ) {
        if(dslAlias) println "Feature '$description': $dslAlias($method) - $desc"
        else println "Feature '$description': $method - $desc"

        if(cl && !context.dryRunScenario) {
            cl()
        }
    }
    
    def in_order(String desc) {
        in_order(desc, null)
    }

    def in_order(String desc, Closure cl) {
        doThisAlways("in_order", desc, cl)
    }
    
    def as_a(String desc) {
        as_a(desc,null)
    }

    def as_a(String desc, Closure cl) {
        doThisAlways("as_a", desc, cl)
    }
    
    def i_want(String desc) {
        i_want(desc,null)
    }

    def i_want(String desc, Closure cl) {
        doThisAlways("i_want", desc, cl)
    }
}
