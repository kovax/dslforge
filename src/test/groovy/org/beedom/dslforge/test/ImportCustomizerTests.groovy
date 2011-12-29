package org.beedom.dslforge.test;

import static org.junit.Assert.*;

import org.beedom.dslforge.DSLEngine;
import org.beedom.dslforge.test.beans.Product;
import org.junit.Before;
import org.junit.Test;

class ImportCustomizerTests {

    def context
    def dsle

    @Before
    public void before () {
        context = new Binding()
        dsle = new DSLEngine(context, "src/test/conf/ImportCustomizerTestConfig.groovy", "development")
    }

    @Test
    public void allImports() {
        dsle.run("ImportCustomizerTestScript.groovy")
     }

}
