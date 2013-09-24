package org.beedom.dslforge.test.delegates

import org.beedom.dslforge.SimpleReportingDelegate;
import org.beedom.dslforge.test.beans.User 


/**
 * 
 * @author kovax
 *
 */
class WebShopDelegate extends SimpleReportingDelegate  {
	
    def static dslKey = "webshop"
    def static aliases = [login:  ["signIn",  "sign in"], 
                          logout: ["signOut", "sign out"]]

    def boolean loggedIn = false

	public WebShopDelegate() {
        initDelegate("")
	}

	def login(String id, String pwd) {
        writeMethod("login", "user id: $id, password: $pwd")
	    
		assert id
        assert pwd
        
        loggedIn = true
	}

	def login(User u) {
        assert u, "define user"

		if(u.kind == "customer") {
	        assert u == context.customer
		}
		login(u.userid, u.password)
	}
	
    def logout() {
        writeMethod("logout", "")

        assert loggedIn, "call login first"
        loggedIn = false
    }
	
	def logout(User u) {
        writeMethod("logout", "user: ${u.userid}")
        
        assert u, "define user"
        assert loggedIn, "call login first"

        if(u.kind == "customer") {
            assert u == context.customer
            context.customer.firstName = "logged out customer"
        }

        loggedIn = false
	}
}
