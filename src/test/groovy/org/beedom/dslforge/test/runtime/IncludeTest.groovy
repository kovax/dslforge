package org.beedom.dslforge.test.runtime;

import org.beedom.dslforge.test.TestBase;
import org.junit.Test;

class IncludeTest extends TestBase {

    def defineCustomer = {
        define {
            user {
                kind = "customer"
                userid = "test@d${timeStamp}d.com"
                password = "hellobaby"
                firstName = "Test"
                lastName = "Customer_${timeStamp}"
                title = "Mr."
                sex = "M"
            }
        }
    }

    @Test
    public void scriptIncludeScript() {
        dsle.run("IncludeTestScript.groovy")
    }

    @Test
    public void closureIncludeClosure() {
        
        dsle.run {
            include defineCustomer
            assert customer
        }
        
        assert context.customer
    }

    @Test
    public void closureIncludeScript() {
        context.timeStamp = timeStamp
        dsle.run {
            include "DefineCustomerScript.groovy"
            assert customer
        }
        
        assert context.customer
    }
}
