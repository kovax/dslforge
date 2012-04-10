package org.beedom.dslforge.test.delegates


import org.beedom.dslforge.SimpleReportingDelegate;


class ReportingFeatureDelegate extends SimpleReportingDelegate {

    def static dslKey = "feature"
    def static delegateMethods = ["in_order", "as_a", "i_want"]

    public ReportingFeatureDelegate(String desc) {
        initDelegate(desc)
    }
}
