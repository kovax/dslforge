package org.beedom.dslforge.test.runtime;

import static org.junit.Assert.*

import org.beedom.dslforge.DSLEngine
import org.beedom.dslforge.test.TestBase;
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
