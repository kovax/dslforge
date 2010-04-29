package org.beedom.dslforge.test

import org.junit.Test

class ScriptTests extends TestBase {

	@Test
	public void scriptWithAllFunctionality() {
		dsle.run("AllFuntionalityScript.groovy")
		assert context.junit == "junit4"
	}
}
