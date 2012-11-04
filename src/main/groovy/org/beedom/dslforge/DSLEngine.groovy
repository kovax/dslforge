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

import groovy.util.logging.Slf4j
import groovy.lang.MissingPropertyException

import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.util.regex.Pattern


/**
 * 
 * 
 * @author zs.myth
 */
@Slf4j
public class DSLEngine  {

    private String configFile = ""
    private String configEnv = ""
    private String scriptsHome = ""
    
    private ConfigObject   dslConfig = null
    private Binding        context = null
    private ReportRenderer reporter = null

    private GroovyScriptEngine gse = null
    private aliases = [:]
    
    private def injectedAliases = Collections.synchronizedMap([:])
    private def delegatesMap = Collections.synchronizedMap([:])

	/**
	 *
	 * @param map
	 */
	public DSLEngine() {
		log.debug "Constructor - DSLEngine()"
		init()
	}

	/**
     * 
     * @param map
     */
    public DSLEngine(Map map) {
		log.debug "Constructor - DSLEngine(Map)"
        if(map) {
            for (entry in map) {
                this."${entry.key}" = entry.value
            }
        }
        init()
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

        if (options.c) { confFile  = options.c }
        if (options.e) { confEnv   = options.e }
        if (options.d) { scriptDir = options.d }
        if (options.p) { pattern   = options.p }

        def arguments = options.arguments()
        def dsl = new DSLEngine( configFile: confFile, configEnv: confEnv, scriptsHome: scriptDir )

        if(pattern) {
            dsl.run( Pattern.compile(pattern) )
        }

        if(arguments) {
            arguments.each { file-> dsl.run(file) }
        }
    }


    /**
     * 
     */
    public void init() {
        if(!dslConfig) {
            if(!configFile) { configFile = "conf/DSLConfig.groovy" }
            if(!configEnv)  { configEnv  = "development" }

            log.debug("Loading config:$configFile for environment:$configEnv")
            dslConfig = new ConfigSlurper(configEnv).parse(new File(configFile).toURI().toURL())
        }

        log.debug("DSL config: "+dslConfig.dump())
        
        if(!context) {
            context = new Binding()
        }

        if(!scriptsHome) {
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

    def createImportConfigration() {
        def configuration = new CompilerConfiguration()
        def customizer = new ImportCustomizer()

        if(dslConfig.dsl.imports.imports) {
            customizer.addImports(dslConfig.dsl.imports.imports as String[])
        }
        if(dslConfig.dsl.imports.starImports) {
            customizer.addStarImports(dslConfig.dsl.imports.starImports as String[])
        }
        if(dslConfig.dsl.imports.staticStars) {
            customizer.addStaticStars(dslConfig.dsl.imports.staticStars as String[])
        }
        if(dslConfig.dsl.imports.aliasedImports) {
            dslConfig.dsl.imports.aliasedImports.each {
                customizer.addImport(it.key, it.value)
            }
        }
        if(dslConfig.dsl.imports.staticImports) {
            dslConfig.dsl.imports.staticImports.each {
                customizer.addStaticImport(it[0], it[1])
            }
        }
        if(dslConfig.dsl.imports.aliasedStaticImports) {
            dslConfig.dsl.imports.aliasedStaticImports.each {
                customizer.addStaticImport(it.key, it.value[0],it.value[1])
            }
        }

        configuration.addCompilationCustomizers(customizer)

        return configuration
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
     * @param scriptName the name of the script file to be enhanced and run
     * @return returns the Object(s) returned by the script
     */
    def run(String scriptName) {
        log.info("running script file: $scriptName")
        
        assert scriptsHome, "use config file or -d in command line to define the home of your scipts"
        def compConfig = new CompilerConfiguration()
        
        if(dslConfig.dsl.imports) {
            compConfig = createImportConfigration()
        }

        if(dslConfig.dsl.defaultDelegate) {
            Class clazz = null

            if( dslConfig.dsl.defaultDelegate instanceof Class ) {
                clazz = dslConfig.dsl.defaultDelegate
            }
            else if(dslConfig.dsl.defaultDelegate instanceof Map) {
                clazz = dslConfig.dsl.defaultDelegate[FilenameUtils.getExtension(scriptName)]
            }
            else {
                throw new RuntimeException("Type of $config must be Class or Map")
            }
            
            assert clazz, "Could not identify class for default delegate"

            String dslKey = getDelegateDslKey( clazz );
            
            log.info( "Try to enhance the script with the dslKey: '$dslKey'" )

            //TODO: modify original Script object using GroovyScriptEngine and GroovyClassLoader.parse() (how???)
            String scriptText = new File(scriptsHome+"/"+scriptName).text

            //wraps the script with a Closure containing the default dslKey
            return run( new GroovyShell(compConfig).evaluate( "return {->${dslKey} {${scriptText}}}" ) )
        }
        else {
            //last minute initialisation of GroovyScriptEngine
            if(!gse) {
                gse = new GroovyScriptEngine( scriptsHome )
            }
            gse.config = compConfig
            def script = gse.createScript(scriptName, context)

            script.metaClass = createEMC( script.class, getEMCClosure() )

            if(dslConfig.dsl.categories) {
                use(dslConfig.dsl.categories) { 
                    return script.run()
                }
            }
            else {
                return script.run()
            }
        }
    }


    /**
     * Run closure by enhancing it with categories and EMC instance
     * 
     * @param cl the Closure to be enhanced and run
     * @return returns the Object which is returned by the script
     */
    def run(Closure cl) {
        log.info "run(Closure cl) "

        cl.metaClass = createEMC( cl.class, getEMCClosure() )

        cl.delegate = context
        cl.resolveStrategy = Closure.DELEGATE_FIRST

        if(dslConfig.dsl.categories) {
            use(dslConfig.dsl.categories) { 
                return cl()
            }
        }
        else {
            return cl()
        }
    }


    /**
     * 
     * @param clazz Script or Closure to be enhanced by the EMC
     * @param cl Closure to configure the EMC
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
        else {
            throw new RuntimeException("Type of $config must be Class or Map")
        }
    }


    /**
     * Convention: Get the dslKey from class or configuration entry. If none exists
     * use the lower-case name of the class removing the Delegate from the end if needed,
     * and also inject the dslKey property
     * 
     * @param config the object retrieved from configuration object
     * @return the dslKey string
     */
    private String getDelegateDslKey(config) {
        Class clazz = getDelegateClazz(config)

        if(clazz.metaClass.properties.find{it.name=="dslKey"} && clazz.dslKey) {
            return clazz.dslKey
        }
        else {
            String key

            if(config instanceof Map) {
                key = config.dslKey
            }
            else if(clazz.simpleName.endsWith("Delegate")) {
                key = clazz.simpleName.substring(0,clazz.simpleName.indexOf("Delegate")).toLowerCase()
            }
            else {
                key = clazz.simpleName.toLowerCase()
            }

            //TODO: investigate why injecting dslKey crashes loads of tests
            //clazz.metaClass.getDslKey = {-> return key }
            return key
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
     * @param delegate
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
        //Convention: Inject 'context' property into delegate class
        clazz.metaClass.getContext = {-> return context }

        //Convention: Inject 'reporter' property into delegate class
        if(reporter) {
            clazz.metaClass.getReporter = {-> return reporter }
        }

        //Convention: Inject 'dslAlias' property into delegate class
        clazz.metaClass.getDslAlias = {-> return injectedAliases[delegate.class] }

        //Convention: Find and call real method for the missing ones using aliases
        clazz.metaClass.methodMissing = { String name, args ->
            def alias = findAlias(delegate, name)

            if(alias) {
                //collect types for method arguments & find method with the same signature
                def types = args.collect {it.class} as Object[]
                def methods = delegate.metaClass.respondsTo(delegate.class, alias, types)

                if(methods) {
                    assert 1 == methods.size(), "Ambiguous method list found for aliasing '${name}' to '${alias}'"

                    //dynamically register this alias method so next time no methodMissing is thrown
                    clazz.metaClass."$name" = { Object[] varArgs ->
                        log.info("Registered alias: $name ")

                        //Set the value of dslAlias property for the time of the method call only
                        injectedAliases[delegate.class] = name
                        def returns = methods[0].invoke(delegate, args)
                        injectedAliases[delegate.class] = null
                        return returns
                    }

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
     * @return closure to initialise the delegate instance
     */
    private Closure getProcessClosure(Class clazz) {
        return {  Object[] args ->
            assert args, "Arguments of closure in DSL must not be empty"
            def l = args.length

            assert (args[l-1] instanceof Closure), "Last argument of closure must be closure"
            Closure cl = (Closure)args[l-1]

            //Construct of the delegate class and call its processClosure() method
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
     * the missing methods to the delegate class. The closure contains instantiation of the delegate class.
     * 
     * @param clazz the delegate class
     * @param method the delegate method, used to support aliasing
     * @return closure to initialise the delegate instance
     */
    private Closure getDelegateClosure(Class clazz, String method) {
        return { Object[] args ->
            assert args, "Arguments of closure in DSL must not be empty"
            def l = args.length

            assert (args[l-1] instanceof Closure), "Last argument must be closure"
            Closure cl = (Closure)args[l-1]

            //if class has a sharedInstance property the delegate could be in the map
            def delegateInstance = delegatesMap[clazz]

            //getDSLAlias() uses this map
            injectedAliases[clazz] = method

            //sharedInstance already exists so call its init() method
            if(delegateInstance) {
                if(l==1) {
                    delegateInstance.initDelegate()
                }
                else {
                    delegateInstance.initDelegate(args[0..l-2])
                }
                cl.delegate = delegateInstance
            }
            //No sharedInstance exists so call the constructor of the delegate class
            else {
                if(l==1) {
                    cl.delegate = clazz.newInstance()
                }
                else {
                    cl.delegate = clazz.newInstance(args[0..l-2] as Object[])
                }

                //if class has a sharedInstance property add the class to the delegatesMap
                if(clazz.metaClass.properties.find{it.name=="sharedInstance"}) {
                    delegatesMap[clazz] = cl.delegate
                }
            }
            
            //getDSLAlias() uses this map
            injectedAliases[clazz] = null

            if(clazz.metaClass.properties.find{it.name=="delegateMethods"}) {
                cl.delegate.addDelegateMethods(clazz.delegateMethods)
            }

            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()

            if(clazz.metaClass.methods.find{it.name=="destroyDelegate"}) {
                injectedAliases[clazz] = method
                cl.delegate.destroyDelegate()
                injectedAliases[clazz] = null
            }

            return cl.delegate
        }
    }


    /**
     * Calls the appropriate method which creates the closure to implement the method
     * which initialise the delegate instance
     *
     * @param clazz the delegate class
     * @param method the delegate method, used to support aliasing
     * @return closure to initialise the delegate instance
     */
    private Closure getMethodClosure(Class clazz, String method) {
        if(clazz.metaClass.methods.find {it.name == "processClosure"}) {
            //Convention: delegate class have processClosure() method to processes the content of the closure
            return getProcessClosure(clazz)
        }
        else {
            //Convention: the closure is executed, and all unknown methods are delegated to the delegate instance
            return getDelegateClosure(clazz, method)
        }
    }
    

    /**
     * Creates the closure to configure the EMC
     *
     * @return closure to configure the EMC
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
                    emc."$method" = getMethodClosure(clazz, method)
                }
            }
        }
    }
}
