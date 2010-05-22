/*
 * Copyright 2003-2010 the original author or authors.
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
package org.beedom.dslforge

import groovy.lang.MissingPropertyException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author zs.myth
 */
public class DSLEngine  {
    
    protected static Logger log = Logger.getLogger(DSLEngine.class.getName());
    
    private ConfigObject dslConfig
    private aliases = [:]
    private Binding context

    private GroovyScriptEngine gse
    
    private def injectedAliases = Collections.synchronizedMap([:])
    
    public DSLEngine() {
        init()
    }
    
    public DSLEngine(Binding context) {
        this.context = context
        init()
    }
    
    public DSLEngine(Binding context, String configFile, String configEnv) {
        this.context = context
        init(configFile,configEnv)
    }
 
    public DSLEngine(String configFile, String configEnv) {
        init(configFile,configEnv)
    }
    
    
    /**
     * Run DSL script from a command line
     * 
     * @param args
     */
    public static void main(String[] args) {
    	
    	def cli = new CliBuilder(usage: 'dslengine -[chfe] [file/directory name/pattern]')

        cli.with {
            h longOpt: 'help', 'Show usage information'
            c longOpt: 'config-file', args: 1, argName: 'confFile', 'Configuration file'
            e longOpt: 'config-env',  args: 1, argName: 'confEnv', 'Configuration environment'
            //p longOpt: 'paralell', 'Paralell execution'
        }

        def options = cli.parse(args)
        if (!options) {
            return
        }

        // Show usage text when -h or --help option is used.
        if (options.h) {
            cli.usage()
            return
        }
        
        def confFile
        def confEnv
        
        if (options.c) {
        	confFile = options.c
        }

        if (options.e) {
        	confEnv = options.e
        }
        
        def arguments = options.arguments()
        def dse = new DSLEngine( confFile, confEnv )
        
        options.arguments().each{ dse.run(it) }
    }
    
    
    public void init() {
        init(null,null)
    }
    
    
    public void init(String configFile, String configEnv) {
        if(!configEnv) {
            configEnv = "development"
        }
        
        if(!configFile) {
            configFile = "conf/DSLConfig.groovy"
        }
        
        if(!context) {
            context = new Binding()
        }
        
        dslConfig = new ConfigSlurper(configEnv).parse(new File(configFile).toURL())
        
        //enable inheritance for ExpandoMetaClass
        if(dslConfig.dsl.emcInheritance) {
            ExpandoMetaClass.enableGlobally()
        }
    }


    def run(String scriptName) {
        if(!gse) {
            gse = new GroovyScriptEngine( dslConfig.dsl.scripts, "conf" )
        }
        
        def script = gse.createScript(scriptName, context)
        script.metaClass = createEMC( script.class, getEMCClosure() )
        if(dslConfig.dsl.categories) {
            use(dslConfig.dsl.categories) { script.run() }
        }
        else {
            script.run()
        }
    }
    
    
    def run(Closure cl) {
        cl.metaClass = createEMC( cl.class, getEMCClosure() )

        cl.delegate = context
        cl.resolveStrategy = Closure.DELEGATE_FIRST

        if(dslConfig.dsl.categories) {
            use(dslConfig.dsl.categories) { cl() }
        }
        else {
            cl()
        }
    }
 
    
    private ExpandoMetaClass createEMC(Class clazz, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
        
        cl(emc)
        
        emc.initialize()
        return emc
    }

    
    private Class getDelegateClazz(config) {
        if( config instanceof Class ) {
            return config
        }
        else if(config instanceof Map) {
            return config.clazz
        }
    }

    /**
     * Convention: Get the dslKey from class or configuration entry. If none exists
     * use the lowercase name of the class removing the Delegate from the end if needed
     * 
     * @param config the object retrieved from configuration object
     * @return the dslKey string
     */
    private String getDelegateDslKey(config) {
        Class clazz = getDelegateClazz(config)
        String dslKeyFromConfig 
        
        if(config instanceof Map) {
            dslKeyFromConfig = config.dslKey
        }

        if(clazz.metaClass.properties.find{it.name=="dslKey"} && clazz.dslKey) {
            return clazz.dslKey
        }
        else if( dslKeyFromConfig ) {
            return dslKeyFromConfig
        }
        else if(clazz.simpleName.endsWith("Delegate")) {
            return clazz.simpleName.substring(0,clazz.simpleName.indexOf("Delegate")).toLowerCase()
        }
        else {
            return clazz.simpleName.toLowerCase()
        }
    }
    
    
    /**
     * Converts alias declaration to a more usable format
     * 
     * @param dslKey
     * @param aliasMap
     */
    private void convertAliasDefinition(String dslKey, Map aliasMap) {
        aliasMap.each { k, list ->
            if(k=="main") {
                aliases[dslKey] = list
            }
            else {
                list.each { alias ->
                    aliases[dslKey+"-"+alias] = k
                }
            }
        }
    }
    
    /**
     * Finds real method to be called using dslKey and method name 
     * 
     * @param dslKey key defined in delegate class
     * @param name the name of the method
     * @return
     */
    private String findAlias(delegate, String name) {
        if(aliases) {
            try {
                return aliases[delegate.dslKey+"-"+name]
            } catch (MissingPropertyException e) {
                //TODO: make aliases work without the explicit declaration of dslKey in delegate class
            }
        }
        return null
    }
    

    /**
     * 
     * @param clazz
     */
    private void enhanceDelegateByConvention(Class clazz) {
        //Convention: Inject 'context' property in delegate class
        clazz.metaClass.getContext = {-> context }
        
        //Convention: Inject 'dslAlias' property in delegate class
        clazz.metaClass.getDslAlias = {-> injectedAliases[delegate.class] }
        
        //Convention: Find and call real method for the missing ones using aliases
        clazz.metaClass.methodMissing = { String name, args ->
            def alias = findAlias(delegate, name)
            
            if(alias) {
                def types = args.collect {it.class} as Object[]
                def methods = delegate.metaClass.respondsTo(delegate.class, alias, types)
                
                if(methods) {
                    assert 1 == methods.size(), "Ambiguous method list found for aliasing '${name}' to '${alias}'"
                    
                    //TODO: dynamically register this alias method, but the code bellow has glitches
                    //delegate.metaClass."$name" = { Object[] varArgs ->
                    //    methods[0].invoke(delegate, args)
                    //}
                    
                    //Set the value of dslAlias property for the time of the method call
                    injectedAliases[delegate.class] = name
                    def returns = methods[0].invoke(delegate, args)
                    injectedAliases[delegate.class] = null
                    return returns
                }
            }
            throw new MissingMethodException(name, delegate.class, args)
        }

        //Convention: For the missing property check if there is a method with no arguments, and call it
        clazz.metaClass.propertyMissing = { String name -> 
            def methods = delegate.metaClass.respondsTo(delegate.class, name, null)
            if(!methods) {
                def alias = findAlias(delegate, name)
                
                if(alias) {
                    methods = delegate.metaClass.respondsTo(delegate.class, alias, null)
                }
            }

            if(methods) {
                return methods[0].invoke(delegate, null)
            }

            throw new MissingPropertyException(name, delegate.class)
        }
    }
    
    //TODO: implement closure declaration with variable argument list if possible
    private Closure getProcessClosure0Args(Class clazz) {
        return { Closure cl ->
            return clazz.newInstance().processClosure(cl)
        }
    }
    private Closure getProcessClosure1Args(Class clazz) {
        return { arg1, Closure cl ->
            return clazz.newInstance(arg1).processClosure(cl)
        }
    }
    private Closure getProcessClosure2Args(Class clazz) {
        return { arg1, arg2, Closure cl ->
            return clazz.newInstance(arg1, arg2).processClosure(cl)
        }
    }
    private Closure getProcessClosure3Args(Class clazz) {
        return { arg1, arg2, arg3, Closure cl ->
            return clazz.newInstance(arg1, arg2, arg3).processClosure(cl)
        }
    }
    private Closure getProcessClosure4Args(Class clazz) {
        return { arg1, arg2, arg3, arg4, Closure cl ->
            return clazz.newInstance(arg1, arg2, arg3, arg4).processClosure(cl)
        }
    }
    
    //TODO: implement closure declaration with variable argument list if possible
    private Closure getDelegateClosure0Args(Class clazz) {
        return { Closure cl ->
            cl.delegate = clazz.newInstance()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }
    }
    private Closure getDelegateClosure1Args(Class clazz) {
        return { arg1, Closure cl ->
            cl.delegate = clazz.newInstance(arg1)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }
    }
    private Closure getDelegateClosure2Args(Class clazz) {
        return { arg1, arg2, Closure cl ->
            cl.delegate = clazz.newInstance(arg1, arg2)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }
    }
    private Closure getDelegateClosure3Args(Class clazz) {
        return { arg1, arg2, arg3, Closure cl ->
            cl.delegate = clazz.newInstance(arg1, arg2, arg3)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }
    }
    private Closure getDelegateClosure4Args(Class clazz) {
        return { arg1, arg2, arg3, arg4, Closure cl ->
            cl.delegate = clazz.newInstance(arg1, arg2, arg3, arg4)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            return cl.delegate
        }
    }


    /**
     * 
     * @return 
     */
    private Closure getEMCClosure() {
        return { ExpandoMetaClass emc ->
            //TODO: implement convention to discover classes by looking for Delegate in their names, 
            //or listing classes in delegates source directory
            if(!dslConfig.dsl.delegates) {
                log.warning("NO delegate class was specified in DSL Config file")
            }

            //Add these methods in case the DSL needs to support evaluate/include
            dslConfig.dsl?.evaluate.each { evalMethod ->
                log.fine("Adding evaluate methods to ECM: $evalMethod")
                
            	emc."$evalMethod" = { String file -> run(file) }
            	emc."$evalMethod" = { Closure cl -> run(cl) }
            }
            
            dslConfig.dsl?.delegates.each { delegateConfig ->
                def clazz   = getDelegateClazz(delegateConfig)
                def dslKey  = getDelegateDslKey(delegateConfig)
                def methods = [dslKey]
                
                if(clazz.metaClass.properties.find{it.name=="aliases"} && clazz.aliases) {
                    convertAliasDefinition( dslKey, clazz.aliases )
                }

                //add the alias method names to the list of EMC methods if any alias exists
                if(aliases[dslKey]) {
                    methods += aliases[dslKey]
                }
                
                log.fine("ECM method names including aliases: $methods")
                
                enhanceDelegateByConvention(clazz)

                if(clazz.metaClass.methods.find {it.name == "processClosure"}) {
                    //Convention: delegate class have processClosure() method to processes the content of the closure
                    methods.each { method ->
                        emc."$method" = getProcessClosure0Args(clazz) 
                        emc."$method" = getProcessClosure1Args(clazz) 
                        emc."$method" = getProcessClosure2Args(clazz) 
                        emc."$method" = getProcessClosure3Args(clazz) 
                        emc."$method" = getProcessClosure4Args(clazz) 
                    }
                }
                else {
                    //Convention: the closure is executed, and all unknown methods are delegated to the object of clazz
                    methods.each { method ->
                        emc."$method" = getDelegateClosure0Args(clazz)
                        emc."$method" = getDelegateClosure1Args(clazz)
                        emc."$method" = getDelegateClosure2Args(clazz)
                        emc."$method" = getDelegateClosure3Args(clazz)
                        emc."$method" = getDelegateClosure4Args(clazz)
                    }
                }
            }
        }
    }
}
