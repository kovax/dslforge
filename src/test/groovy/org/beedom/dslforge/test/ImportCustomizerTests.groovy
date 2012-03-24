package org.beedom.dslforge.test;

import static org.junit.Assert.*

import org.beedom.dslforge.DSLEngine
import org.junit.Before
import org.junit.Test

class ImportCustomizerTests extends TestBase {

    @Before
    public void init () {
        configFile = "src/test/conf/ImportCustomizerTestConfig.groovy"
        super.init()
    }

    @Test
    public void allImports() {
        dsle.run("ImportCustomizerTestScript.groovy")
     }

}
