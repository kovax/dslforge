package org.beedom.dslforge.test

import org.beedom.dslforge.DSLEngine

import groovy.lang.Binding

import org.junit.Before


class TestBase {

	DSLEngine dsle
	private def context
    def timeStamp

    def configFile = ""
    def configEnv = "development"
    
    def file
    def writer
    def reporter

    @Before
	public void init() {
        timeStamp = System.currentTimeMillis()

        //if(!context) { context = new Binding(junit: "junit4") }
        context = new Binding(junit: "junit4")

        if(!dsle) {
            dsle = new DSLEngine(context: context, configFile: configFile, configEnv: configEnv, reporter: reporter)
        }
	}
}
