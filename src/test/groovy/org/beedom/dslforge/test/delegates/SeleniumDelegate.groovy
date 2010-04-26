package org.beedom.dslforge.test.delegates

import com.thoughtworks.selenium.DefaultSelenium;

class SeleniumDelegate {
    
    @Delegate
    private DefaultSelenium selenium
    
    public SeleniumDelegate() {
        selenium = new DefaultSelenium("localhost", 4444, context.browser, context.url)
    }
    
    public SeleniumDelegate(String browser, String url) {
        selenium = new DefaultSelenium("localhost", 4444, browser, url)
    }
    
    def static dslKey = "seleniumD"


}
