package org.beedom.dslforge.test.compiletime;

import org.beedom.dslforge.ast.local.UseRuntimeDSL
import org.junit.Test


/**
 * 
 * @author kovax
 *
 */
class MethodASTDelegateTests {

    @Test
    @UseRuntimeDSL
    public void nestedDelegates() {
        dryRunScenario = false
        timeStamp = System.currentTimeMillis()
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

		assert customer.firstName == "Test"

        feature "Shopping Cart Management", {
            in_order "to use a webshop"
            as_a "customer"
            i_want "to make changes to a shopping cart"

            webshop {
                scenario "Remove product from basket", {
                    given "logged in customer", { login customer }
                    and "the customer has checked out a basket with 1 item"
                    when "the customer removes that item from the basket"
                    then "the basket is empty", { logout customer }
                }
            }
        }

        assert customer.firstName == "logged out customer"
    }
}
