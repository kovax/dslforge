package org.beedom.dslforge.test

import groovy.lang.Binding;

import org.beedom.dslforge.DSLEngine

import org.junit.After
import org.junit.Before
import org.junit.Test

class ScriptTests {

	def dse
	def context
	
	@Before
	public void init() {
		context = new Binding(junit: "junit4")
		dse = new DSLEngine(context)
	}
	
	@Test
	public void scriptWithAllFunctionality() {
		dse.run("AllFuntionalityScript.groovy")
		assert context.junit == "junit4"
	}

	@After
	public void tearOff() {
		def dse = null
	}
}
