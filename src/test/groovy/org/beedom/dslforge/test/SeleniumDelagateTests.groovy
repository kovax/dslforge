package org.beedom.dslforge.test

import org.beedom.dslforge.DSLEngine;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

class SeleniumDelagateTests {

    def context
    def dsle
    
    def static selServer


    @BeforeClass
    public static void beforeClass() {
        selServer = org.openqa.selenium.server.SeleniumServer.main(new String[0])
    }


    @Before
    public void before () {
        dsle = new DSLEngine("src/test/conf/SeleniumTestConfig.groovy", "development")
    }

    @Test
    public void defaultSelenium() {
        dsle.run {
            selenium "localhost", 4444, "*firefox", "http://www.google.com/", {
                start
                open "/"
                type "q", "selenium rc"
                click "btnG"
                waitForPageToLoad "30000"
            }
        }
    }

    @Test
    public void defaultSeleniumVariable() {
        dsle.run {
            sel = selenium( "localhost", 4444, "*firefox", "http://www.google.com/", { start() } )
            
            sel.open "/"
            sel.type "q", "selenium rc"
            sel.click "btnG"
            sel.waitForPageToLoad "30000"
        }
    }

    @Test
    public void seleniumDelegate() {
        dsle.run {
            seleniumD "*firefox", "http://www.google.com/", {
                start
                open "/"
                type "q", "selenium rc"
                click "btnG"
                waitForPageToLoad "30000"
            }
        }
    }

    @Test
    public void seleniumDelegate2() {
        dsle.run {
            //Setting context variables used by SeleniumDelegate
            browser = "*firefox"
            url = "http://www.google.com/"
                
            seleniumD {
                start
                open "/"
                type "q", "selenium rc"
                click "btnG"
                waitForPageToLoad "30000"
            }
        }
    }
}
