package org.beedom.dslforge.test.delegates

import org.beedom.dslforge.test.beans.User 


/**
 * 
 * @author kovax
 *
 */
class WebShopDelegate {
	
    def static dslKey = "webshop"
    def static aliases = [login: ["signIn", "sign in"], logout: ["signOut"]]

    def boolean loggedIn = false

	public WebShopDelegate() {
	}
	
	def login(String id, String pwd) {
        if(dslAlias) println "$dslAlias/login - id:$id pwd:$pwd"
        else println "login - id:$id pwd:$pwd"
	    
		assert id
        assert pwd
        
        loggedIn = true
	}
	
	def login(User u) {
        assert u, "define user"
        
        if(dslAlias) println "$dslAlias/login - $u.kind"
        else println "login - $u.kind"

		if(u.kind == "customer") {
	        assert u == context.customer
		}
		login(u.userid, u.password)
	}
	
    def logout() {
        assert loggedIn, "call login first"
        loggedIn = false
    }
	
	def logout(User u) {
        assert u, "define user"
        
        if(dslAlias) println "$dslAlias/logout - $u.kind"
        else println "logout - $u.kind"
        
        assert loggedIn, "call login first"
        if(u.kind == "customer") {
            assert u == context.customer
            context.customer.firstName = "logged out customer"
        }
        loggedIn = false
	}
}
