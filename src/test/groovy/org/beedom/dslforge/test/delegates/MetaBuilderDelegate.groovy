package org.beedom.dslforge.test.delegates


import java.util.logging.Logger;

import org.beedom.dslforge.BindingConvention;

import groovytools.builder.MetaBuilder


/**
 * 
 * @author zs.myth
 */
class MetaBuilderDelegate {

    protected static Logger log = Logger.getLogger(MetaBuilderDelegate.class.getName());
    
	def initialised = false
	
	def static dslKey = "define"

	def processClosure(Closure cl) {

	    if(!initialised) {
	    	context.metaBuilder = new MetaBuilder()
	    	//TODO: make file name configurable, it could be in the context
	    	new GroovyShell( context ).evaluate(new File("src/test/conf/MetaBuilderSchema.groovy"))
	    	initialised = true
		}

	    def objs = context.metaBuilder.buildList(cl)
    	BindingConvention.bindObjectList( context, objs, context.objectKeys )
	}
}
