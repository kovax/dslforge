package org.beedom.dslforge.test.runtime

import org.beedom.dslforge.DSLEngine;
import org.beedom.dslforge.test.TestBase;
import org.junit.Before;
import org.junit.Test


class DefaultDelegateTests extends TestBase {

    @Before
    public void init() {
        configFile = "src/test/conf/DefaultDelegateTestConfig.groovy"
        super.init()
    }
    
    @Test
    public void defaultDelegateScript() {
        dsle.run("DefaultDelegate.feature")
    }

    @Test
    public void defaultDelegateClosure() {
        dsle.run {
            description = "Shopping Cart Management"
            
            in_order "to use a webshop"
            as_a "customer"
            i_want "to make changes to a shopping cart"
            
            scenario "Remove product from basket", {
                given "logged in customer1"
                and "the customer has checked out a basket with 1 item"
                when "the customer removes that item from the basket"
                then "the basket is empty"
            }
        }
    }
}
