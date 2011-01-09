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

import java.util.regex.Pattern

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * @author zs.myth
 */
public class DSLEngine  {
    
    protected static Logger log = LoggerFactory.getLogger(DSLEngine.class.getName());
    
    private ConfigObject dslConfig
    private aliases = [:]
    private Binding context
    private String scriptsHome

    private GroovyScriptEngine gse

    private def injectedAliases = Collections.synchronizedMap([:])
    private def delegatesMap = Collections.synchronizedMap([:])

    /**
     * 
     */
    public DSLEngine() {
        init()
    }

    /**
     * 
     * @param context
     */
    public DSLEngine(Binding context) {
        this.context = context
        init()
    }

    /**
     * 
     * @param context
     * @param configFile
     * @param configEnv
     * @param scriptDir
     */
    public DSLEngine(Binding context, String configFile, String configEnv, String scriptDir) {
        this.context = context
        init(configFile,configEnv, scriptDir)
    }
 
    public DSLEngine(Binding context, String configFile, String configEnv) {
        this.context = context
        init(configFile,configEnv, null)
    }

    /**
     * 
     * @param configFile
     * @param configEnv
     * @param scriptDir
     */
    public DSLEngine(String configFile, String configEnv, String scriptDir) {
        init(configFile,configEnv, scriptDir)
    }
    

    public DSLEngine(String configFile, String configEnv) {
        init(configFile,configEnv, null)
    }
    
    
    /**
     * Run DSL script from a command line
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        def cli = new CliBuilder(usage: 'dslengine -[chedp] [file/directory name/pattern]')

        cli.with {
            h longOpt: 'help', 'Show usage information'
            c longOpt: 'config-file', args: 1, argName: 'confFile', 'Configuration file'
            e longOpt: 'config-env',  args: 1, argName: 'confEnv', 'Configuration environment'
            d longOpt: 'script-dir',  args: 1, argName: 'scriptDir', 'Script root directory'
            p longOpt: 'pattern',     args: 1, argName: 'pattern', 'File pattern'
        }

        def options = cli.parse(args)
        if (!options) {
            cli.usage()
            return
        }

        // Show usage text when -h or --help option is used.
        if (options.h) {
            cli.usage()
            return
        }
        
        def confFile
        def confEnv
        def scriptDir
        def pattern
        
        if (options.c) {
        	confFile = options.c
        }

        if (options.e) {
        	confEnv = options.e
        }
        
        if (options.d) {
            scriptDir = options.d
        }
        
        if (options.p) {
            pattern = options.p
        }
        
        def arguments = options.arguments()
        def dse = new DSLEngine( confFile, confEnv, scriptDir )
        
        if(pattern) {
            dse.run( Pattern.compile(pattern) )
        }

        if(arguments) {
            arguments.each { file-> dse.run(file) }
        }
    }
    
    /**
     * 
     */
    public void init() {
        init(null,null,null)
    }
    
    /**
     * 
     * @param configFile
     * @param configEnv
     */
    public void init(String configFile, String configEnv, String scriptDir) {
        if(!configEnv) {
            configEnv = "development"
        }
        
        if(!configFile) {
            configFile = "conf/DSLConfig.groovy"
        }
        
        if(!context) {
            context = new Binding()
        }
        
        dslConfig = new ConfigSlurper(configEnv).parse(new File(configFile).toURI().toURL())

        log.info("DSL config was loaded: "+dslConfig.dump())
        
        if(scriptDir) {
            scriptsHome = scriptDir
        }
        else {
            scriptsHome = dslConfig.dsl.scripts
        }
        
        //enable inheritance for ExpandoMetaClass
        if(dslConfig.dsl.emcInheritance) {
            ExpandoMetaClass.enableGlobally()
        }
        
        if( dslConfig.dsl.mbSchemaFiles ) {
            context.mbSchemaFiles = dslConfig.dsl.mbSchemaFiles
        }
    }
    
    /**
     * 
     * @param p
     * @return
     */
    def run( Pattern p ) {
        new File(scriptsHome).eachFileMatch(p) { File f ->
            run(f.name)
        }
    }

    /**
     * Run script by enhancing it with EMC instance
     * 
     * @param scriptName the name of the script file to be run
     * @return returns the Object which is returned by the script
     */
    def run(String scriptName) {
        assert scriptsHome, "use config file or -d in command line to define the home of your scipts"

        if(dslConfig.dsl.defaultDelegate) {
            String dslKey = getDelegateDslKey( dslConfig.dsl.defaultDelegate );

            log.info( "Try to enhance the script with the dslKey: '$dslKey'" )

            //TODO: modify original Script object using GroovyScriptEngine and GroovyClassLoader.parse()
            String t = new File(scriptsHome+"/"+scriptName).text

            //wraps the script with a Closure containing the default dslKey
            return run( new GroovyShell().evaluate( "{->${dslKey} {${t}}}" ) )
        }
        else {
            //last minute initialisation of GroovyScriptEngine
            if(!gse) {
                gse = new GroovyScriptEngine( scriptsHome, "conf" )
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

    }


    /**
     * Run closure by enhancing it with EMC instance
     * 
     * @param cl the Closure to be run
     * @return returns the Object which is returned by the script
     */
    def run(Closure cl) {
        log.info "run(Closure cl) "

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


    /**
     * 
     * @param clazz
     * @param cl
     * @return the ExpandoMetaClass 
     */
    private ExpandoMetaClass createEMC(Class clazz, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
        
        cl(emc)
        
        emc.initialize()
        return emc
    }

    /**
     * 
     * @param config
     * @return
     */
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
     * use the lower-case name of the class removing the Delegate from the end if needed
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
     * Converts user-friendly alias declaration to a format usable by this class
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
     * Finds real method to be called for the alias method name
     * 
     * @param dslKey key defined in delegate class
     * @param aliasName the alias name of the method
     * @return
     */
    private String findAlias(delegate, String aliasName) {
        if(aliases) {
            try {
                return aliases[delegate.dslKey+"-"+aliasName]
            } catch (MissingPropertyException e) {
                //TODO: make aliases work without the explicit declaration of dslKey in delegate class
            }
        }
        return null
    }
    

    /**
     * Injects a number of properties and methods into the delegate class
     * 
     * @param clazz the delegate class
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
                    
                    //TODO: dynamically register this alias method so next time no methodMissing is thrown

                    //Set the value of dslAlias property for the time of the method call only
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

    /**
     * Returns a closure which calls processClosure() method of the delegate class to execute
     * the closure defined in the DSL script
     * 
     * @param clazz the delegate class
     * @return the closure
     */
    private Closure getProcessClosure(Class clazz) {
        return {  Object[] args ->
            assert args, "Arguments of closure in DSL must not be empty"
            def l = args.length

            assert (args[l-1] instanceof Closure), "Last argument of closure must be closure"
            Closure cl = (Closure)args[l-1]

            if(l==1) {
                return clazz.newInstance().processClosure(cl)
            }
            else {
                return clazz.newInstance(args[0..l-2] as Object[]).processClosure(cl)
            }
        }
    }

    /**
     * Returns the closure which executes the closure defined in the DSL script by delegating
     * the missing methods to the delegate class
     * 
     * @param clazz the delegate class
     * @return the closure
     */
    private Closure getDelegateClosure(Class clazz) {
        return { Object[] args ->
            assert args, "Arguments of closure in DSL must not be empty"
            def l = args.length

            assert (args[l-1] instanceof Closure), "Last argument of closure must be closure"
            Closure cl = (Closure)args[l-1]

            //if sharedInstance was defined for class there could be already an instance
            def delegateInstance = delegatesMap[clazz]

            if(delegateInstance) {
                if(l==1) {
                    delegateInstance.init()
                }
                else {
                    delegateInstance.init(args[0..l-2])
                }
                cl.delegate = delegateInstance
            }
            else {
                if(l==1) {
                    cl.delegate = clazz.newInstance()
                }
                else {
                    cl.delegate = clazz.newInstance(args[0..l-2] as Object[])
                }
                
                if(clazz.metaClass.properties.find{it.name=="sharedInstance"}) {
                    delegatesMap[clazz] = cl.delegate
                }
            }
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            //cl.resolveStrategy = Closure.OWNER_FIRST
            cl()
            return cl.delegate
        }
    }
    
    /*
     * 
     */
    private Closure getMethodClosure(Class clazz) {
        if(clazz.metaClass.methods.find {it.name == "processClosure"}) {
            //Convention: delegate class have processClosure() method to processes the content of the closure
            return getProcessClosure(clazz)
        }
        else {
            //Convention: the closure is executed, and all unknown methods are delegated to the delegate instance
            return getDelegateClosure(clazz)
        }
    }

    /**
     * 
     * @return 
     */
    private Closure getEMCClosure() {
        return { ExpandoMetaClass emc ->
            //TODO: implement convention to discover classes by looking for Delegate in their names, or listing classes in delegates source directory
            if(!dslConfig.dsl.delegates) {
                log.warn("NO delegate class was specified in DSL Config file")
            }

            //Add these methods in case the DSL needs to support evaluate/include
            dslConfig.dsl?.evaluate.each { evalMethod ->
                
                log.info("Adding evaluate methods to ECM: $evalMethod")
                
                emc."$evalMethod" = { String file -> run(file) }
                emc."$evalMethod" = { Closure cl -> run(cl) }
            }
            
            dslConfig.dsl?.delegates.each { delegateConfig ->
                
                log.info("dslConfig.dsl.delegates.each: $delegateConfig")
                
                def clazz   = getDelegateClazz(delegateConfig)
                def dslKey  = getDelegateDslKey(delegateConfig)
                def methods = [dslKey]

                //If the delegate class has the aliases property, make this names available to missingMethod()
                if(clazz.metaClass.properties.find{it.name=="aliases"} && clazz.aliases) {
                    convertAliasDefinition( dslKey, clazz.aliases )
                }

                //add the alias method names to the list of EMC methods if any alias exists
                if(aliases[dslKey]) {
                    methods += aliases[dslKey]
                }
                
                log.info("ECM method names including aliases: $methods")
                
                enhanceDelegateByConvention(clazz)

                //Adds method to ECM which instantiates the delegates and runs the closure pseed as input parameter
                methods.each { method ->
                    emc."$method" = getMethodClosure(clazz)
                }
            }
        }
    }
}
