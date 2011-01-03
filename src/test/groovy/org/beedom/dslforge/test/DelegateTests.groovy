package org.beedom.dslforge.test;

import static org.junit.Assert.*;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import org.beedom.dslforge.DSLEngine;
import org.junit.Before;
import org.junit.Test;

class DelegateTests extends TestBase {

    @Before
    public void init() {
        dsle = new DSLEngine((Binding)context, "src/test/conf/DelegateTestConfig.groovy", "development")
    	super.init()
    }
    
    @Test
    public void nestedDelegates() {
        dsle.run {
            dryRunScenario = false
            
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

            feature "Shopping Cart Management", {
                in_order "to use a webshop"
                as_a "customer"
                i_want "to make changes to a shopping cart"

                webshop {
                    scenario "Remove product from basket", {
                        given "logged in customer", {
                            login customer
                        }
                        and "the customer has checked out a basket with 1 item"
                        when "the customer removes that item from the basket"
                        then "the basket is empty", {
                            logout customer
                        }
                    }
                }
            }

            assert customer.firstName == "logged out customer"
        }//End of dsle.run
    }

    @Test
    public void nestedFeature() {
        dsle.run {
            dryRunScenario = false
            feature "Feature 1", {
            	in_order "principle 1"
                feature "Feature 1.1", {
                	in_order "principle 1.1"
                }
            	in_order "principle 2"
            }
        }
    }

    @Test
    public void useAlias() {
        dsle.run {
            dryRunScenario = false
            
            scenario "Remove product from basket", {
                given "logged in customer"
                and "the customer has checked out a basket with 1 item"
                when "the customer removes that item from the basket"
                then "the basket is empty"
            }

            //The above translated to hungarian
            szenárió "Termék törlése a kosárból", {
                adott "egy vásárló, aki azonosította magát"
                és "a vásárló a kosarat véglegesítettete egy termékkel"
                amikor "a vásárló törölte a terméket a kosárból"
                akkor "a kosár üres lesz"
            }

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
            
            webshop {
                signIn customer
                signOut
            }
        }
    }
}
