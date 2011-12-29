DSLForge
========

*Groovy DSL on Rails*

The primary goal of the project is to help DSL development by providing a flexible framework for easy creation,
extension and modification of DSLs, and also supports integration of existing classes.

It is based on the approach described in the
[A Groovy DSL from scratch in 2 hours](http://groovy.dzone.com/news/groovy-dsl-scratch-2-hours) post by Steven Devijver,
in which each new term in the DSL are delegate to a method of a specific delegate class. I have found this the 
neat way of separating concerns, so I have generalised it a bit further, I have added some conventions inspired 
by Grails, and with the help of Groovy's fantastic MOP support, the project offers you the following features:


Configuration entry to specify Delegate classes
-----------------------------------------------

Add the `[dslKey: "selenium", clazz: com.thoughtworks.selenium.DefaultSelenium]` specification to the dsl.delegates 
entry (it expects `List<Class>` or `Map<String,Class>`) in the 
[DSL config file](https://github.com/kovax/dslforge/blob/master/src/test/conf/DelegateTestConfig.groovy),
and you can execute the following script:

    selenium "localhost", 4444, "*firefox", "http://www.google.com", {
        start
        open "/"
        type "q", "selenium rc"
        click "btnG"
        waitForPageToLoad "30000"
    }

[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
takes the value of dslKey and registers a method called selenium in the EMC class of the script. 
The method takes the 4 parameters, and calls the constructor of DefaultSelenum with all of them. In general, 
the parameters passed the EMC method are used to construct the delegate class. The dslKey can also be specified 
in the delegate class itself.

Check [SeleniumDelagateTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/SeleniumDelagateTests.groovy)
and [SeleniumDelegate.groovy](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/delegates/SeleniumDelegate.groovy)
for more details about the different ways of integrating existing classes in DSLForge.

Also check [DelegateTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/DelegateTests.groovy)
to see that Groovy handles nesting delegates very nicely.


The DSL can be executed as a Script or Closure
-----------------------------------------------------

[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
has 2 run() methods, `def run(String scriptName)` takes a name of the script file and `def run(Closure cl)` takes
a Closure. Both updates the instances with the enhanced EMC.


The execution context/binding of the DSL is injected into each delegate class
-----------------------------------------------------------------------------

The variable called context, instance of the Binding class, is injected into each delegate class. 
[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
can be initialised with an existing Binding instance, so you have all the possible ways to share data between 
different parts of the execution environment. As you very likely know Binding is used in the Script class by default, 
so to support context sharing in Closures the DSLEngine assign its context property to the delegate field of Closure
instance.
Check the [DSLContextTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/DSLContextTests.groovy)
from more details.


Configuration entry to specify Category classes
-----------------------------------------------

The dsl.categories entry in the config file can specify a list of Category classes, and the DLSEngine will execute
the Script or Closure within the closure of `use(categories)` method.
Check [DecoratorTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/DecoratorTests.groovy)
for more details.


Method aliases in delegate classes
----------------------------------

Each delegate class may have a static aliases property, which holds a map, in which you can specify the list of aliases
for any methods that the delegate is implemeneting. This way it is easy to implement the same DSL in different languages
or adjust it for different problem domains. 
Check [DelegateTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/DelegateTests.groovy)
for more details. 


EMC method calls processClosure(closure) of the delegate class
--------------------------------------------------------------

If you require to take the full control on how the closure is executed in your DSL implementation, you can define
the `processClosure(Closure)` method in your delegate class.
[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
looks for that methods in the delegate class,
and if it exists, it will call it instead of setting up the delegation pattern. It was used in my functional testing
project to setup fixture data in a very user friendly way. Consider the following DSL snippet from the AllFuntionalityScript.

    define {
        user {
            kind = "customer"
            userid = "test@somewhere.com"
            password = "hellobaby"
            firstName = "Test"
            lastName = "Customer1231"
            title = "Mr."
            sex = "M"
        }
    }
    assert customer

The define keyword is associated with the MetaBuilderDelegate class, which has the processClosure() method. 
MetaBuilder provides a very flexible solution to populate POJOs, and as it can use Closure it integrates very well 
with [DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy).
[MetaBuilderDelegate](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/delegates/MetaBuilderDelegate.groovy) 
initialise itself from a script containing the schema definition and a map called
objectKeys, and using the [BindingConvention](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/BindingConvention.groovy)
it adds the customer property of User class to the context.


Nested calls of the same DSL keyword can be handled by the same delegate instance
---------------------------------------------------------------------------------

    feature "Feature 1", {
        in_order "principle 1"
        feature "Feature 1.1", {
            in_order "principle 1.1"
        }
        in_order "principle 2"
    }

In this example the delegate class for the "feature" term will be constructed twice. If you need to avoid that,
the delegate class can define a static property called sharedInstance. In this case
[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
stores the instance of the delegate class, and retrieves it for the second "feature", and calls its `init(args)` 
method instead of calling its constructor.


DSLEngine has main(arg) to support execution from a command line
----------------------------------------------------------------

It is based on CliBuilder and this is the usage:

    usage: dslengine -[chedp] [file/directory name/pattern]
     -c,--config-file <confFile>   Configuration file
     -d,--script-dir <scriptDir>   Script root directory
     -e,--config-env <confEnv>     Configuration environment
     -h,--help                     Show usage information
     -p,--pattern <pattern>        File pattern


Default delegate can be specified in the config file
----------------------------------------------------

Add the entry like bellow to the 
[DSLConfig.groovy](https://github.com/kovax/dslforge/blob/master/src/test/conf/DefaultDelegateTestConfig.groovy) file:

    dsl.defaultDelegate = org.beedom.dslforge.test.delegates.FeatureDelegate

[DSLEngine](https://github.com/kovax/dslforge/blob/master/src/main/groovy/org/beedom/dslforge/DSLEngine.groovy)
will automatically extend the sript with the dslKey of the delegate class. This can be usefull as it makes
the DSL script more concise in case there is only one delegate class for the given DSL or the given DSL is more complex
having more delegates and one of them should always be used.
Check [DefaultDelegateTests](https://github.com/kovax/dslforge/blob/master/src/test/groovy/org/beedom/dslforge/test/DefaultDelegateTests.groovy)
and [DefaultDelegate.feature](https://github.com/kovax/dslforge/blob/master/src/test/scripts/DefaultDelegate.feature).

**IMPORTANT: Default delegate only works for Scripts i.e. NO support for Closures YET!!!**


Imports can be added to Scripts by the integration with ImportCostumizer 
----------------------------------------------------

Add the entries like bellow to the 
[DSLConfig.groovy](https://github.com/kovax/dslforge/blob/master/src/test/conf/ImputCustomizerTestConfig.groovy) file:

    imports {
        imports = [groovytools.builder.MetaBuilder.name]
        starImports = ['org.beedom.dslforge.test.beans']
        aliasedImports = [Customer: org.beedom.dslforge.test.beans.User.name, Cart: org.beedom.dslforge.test.beans.Basket.name]
        staticStars = [Math.name, org.hamcrest.MatcherAssert.name, org.hamcrest.Matchers.name]
        staticImports = [[Locale.name,'FRENCH']]
        aliasedStaticImports = [France:[Locale.name,'FRENCH']]
    }

Check the [ImputCustomizerTestScript.groovy](https://github.com/kovax/dslforge/blob/master/src/test/scripts/ImputCustomizerTestScript.groovy)
to see the import in 'actions'.


Future work
----------------------------------------------------------------

Well, it very much depends on the feedback I hope to get from all of you. I will certainly use it for my future
projects, so I am going to add features that I need. I think some nice DSL aware error handling could be very useful, 
specially with localisation support. Also I would be interested in developing an eclipse editor which can take
the content of delegate classes to provide code completion and syntax highlight. A Grails plugin can be done,
although a lot of testing is needed to check if many instances if the same DSL script can be run at the same time.
