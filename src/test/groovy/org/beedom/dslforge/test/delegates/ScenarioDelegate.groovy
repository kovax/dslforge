package org.beedom.dslforge.test.delegates


import org.beedom.dslforge.BindingConvention;


class ScenarioDelegate {

    public ScenarioDelegate(String desc) {
        println "ScenarioDelegate: $desc"
    }


    def static dslKey = "scenario"

    /**
     * Alias list for methods. The current values are Hungarian translation
     * and not-so-good English alternatives. The 'main' keyword denotes the
     * aliases used for dslKey
     */
    def static aliases = [ main:  ["szenárió", "process"],    
                           given: ["adott", "provided"],
                           and:   ["és", "plus"],
                           when:  ["amikor", "although"],
                           then:  ["akkor", "consequently"]]

    public ScenarioDelegate() {
    }

    def doThisAlways(String method, String desc, Closure cl ) {
        if(dslAlias) println "$bdslAlias($method) $desc"
        else println "$method $desc"

        if(cl && (!context.hasProperty("dryRunScenario") || (context.hasProperty("dryRunScenario") && !context?.dryRunScenario))) {
            cl()
        }
    }
    
    def given(String desc) {
    	given(desc,null)
    }

    def given(String desc, Closure cl) {
    	doThisAlways("given", desc, cl)
    }
    
    def and(String desc) {
    	and(desc,null)
    }

    def and(String desc, Closure cl ) {
    	doThisAlways("and", desc, cl)
    }

    def when(String desc) {
    	when(desc,null)
    }
    
    def when(String desc, Closure cl ) {
    	doThisAlways("when", desc, cl)
    }

    def then(String desc) {
    	then(desc,null)
    }

    def then(String desc, Closure cl ) {
    	doThisAlways("then", desc, cl)
    }
}
