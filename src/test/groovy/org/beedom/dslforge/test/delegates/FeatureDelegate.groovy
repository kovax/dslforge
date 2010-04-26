package org.beedom.dslforge.test.delegates


import org.beedom.dslforge.BindingConvention


class FeatureDelegate {
	
    def static dslKey = "feature"
    
	public FeatureDelegate() {
    }

    public FeatureDelegate(String desc) {
        println "FeatureDelegate: $desc"
    }

    def doThisAlways(String method, String desc, Closure cl ) {
        if(dslAlias) println "$dslAlias($method) $desc"
        else println "$method $desc"

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
