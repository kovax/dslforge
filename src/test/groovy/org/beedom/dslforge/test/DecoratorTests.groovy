package org.beedom.dslforge.test;

import static org.junit.Assert.*;

import org.beedom.dslforge.DSLEngine;
import org.beedom.dslforge.test.beans.Product;
import org.junit.Before;
import org.junit.Test;

import com.sun.security.auth.login.ConfigFile;

class DecoratorTests extends TestBase {

    @Before
    public void init () {
        configFile =  "src/test/conf/DecoratorTestConfig.groovy"
        super.init()
    }
    
    @Test
    public void decoratorInClosure () {
        dsle.run {
            product = new Product(id: "100", name: "Noname RW/DL")
            product.shall "not be", null
        }
        assert context.product
    }
    
    @Test
    public void decoratorInScript() {
        dsle.run("DecoratorTestScript.groovy")
        assert context.product
    }

}
