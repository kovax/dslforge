package org.beedom.dslforge.test

import org.beedom.dslforge.DSLEngine;

import org.junit.Before;
import org.junit.Test


class ScriptTests extends TestBase {

	@Test
	public void scriptWithAllFunctionality() {
		dsle.run("AllFuntionalityScript.groovy")
		assert context.junit == "junit4"
	}
}
