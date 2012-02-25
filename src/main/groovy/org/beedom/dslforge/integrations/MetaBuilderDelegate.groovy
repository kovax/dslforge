/*
* Copyright 2003-2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.beedom.dslforge.integrations

import groovy.util.logging.Slf4j

import org.beedom.dslforge.BindingConvention;

import groovytools.builder.MetaBuilder


/**
 * 
 * @author zs.myth
 */
@Slf4j
class MetaBuilderDelegate {
	def initialised = false
	
	def processClosure(Closure cl) {

	    if(!initialised) {
	    	context.metaBuilder = new MetaBuilder()

            context.mbSchemaFiles.each { String fPath ->
                log.debug "MetaBuilder schema file: $fPath"
                new GroovyShell( context ).evaluate( new File(fPath) )
            }
	    	initialised = true
		}

	    def objs = context.metaBuilder.buildList(cl)
    	BindingConvention.bindObjectList( context, objs, context.mbObjectKeys )
	}
}
