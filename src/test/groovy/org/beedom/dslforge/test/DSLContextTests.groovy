package org.beedom.dslforge.test;

import org.beedom.dslforge.DSLEngine;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

class DSLContextTests {
    
    def timeStamp
    def context
    def dsle

    @Before
    public void before () {
        timeStamp = System.currentTimeMillis()
        context = new Binding()
        dsle = new DSLEngine(context)
    }


	@Test
	public void testContextShare() {
		context.junit = "junit"

		dsle.run {
		    assert junit == "junit"
		    var = "kovax"
		}
		
		try {
	        assert var
	        fail("Shall throw MissingPropertyException")
		}
		catch(MissingPropertyException mpe) {
		}
		
		assert context.var == "kovax"
	}

	@Test
    public void testContextUpdateByDSL() {
        dsle.run {
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
            assert customer
        }
        assert context.customer.lastName  == "Customer_${timeStamp}"
    }

    @Test
    public void testContextChangeByOtherDSL() {
        dsle.run {
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
                login customer
                
                //Logout implementation updates the customer.firstName
                logout customer
            }
        }
        
        assert context.customer.firstName == "logged out customer"
    }
}
