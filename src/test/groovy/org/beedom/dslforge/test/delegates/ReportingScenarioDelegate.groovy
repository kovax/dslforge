package org.beedom.dslforge.test.delegates


import org.beedom.dslforge.SimpleReportingDelegate;


class ReportingScenarioDelegate extends SimpleReportingDelegate {

    def static dslKey = "scenario"
    def static delegateMethods = ["given", "when", "then", "and", "but"]

    def static aliases = [ main:  ["szenárió", "process"],
                           given: ["adott",    "provided"],
                           and:   ["és",       "plus"],
                           when:  ["amikor",   "although"],
                           then:  ["akkor",    "consequently"]]

    public ReportingScenarioDelegate(String desc) {
        initDelegate(desc)
    }
}
