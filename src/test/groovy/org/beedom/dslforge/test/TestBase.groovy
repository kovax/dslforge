package org.beedom.dslforge.test

import org.beedom.dslforge.DSLEngine

import groovy.lang.Binding

import org.junit.Before

class TestBase {

	def dsle
	def context
    def timeStamp

	@Before
	public void init() {
        timeStamp = System.currentTimeMillis()
		context = new Binding(junit: "junit4")
        
        if(!dsle) {
            dsle = new DSLEngine(context)
        }
	}
}
