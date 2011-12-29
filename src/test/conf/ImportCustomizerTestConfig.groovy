environments {
    development {
        dsl {
            emcInheritance = true
            scripts = 'src/test/scripts'
            imports {
                imports = [groovytools.builder.MetaBuilder.name]
                starImports = ['org.beedom.dslforge.test.beans']
                aliasedImports = [Customer: org.beedom.dslforge.test.beans.User.name, Cart: org.beedom.dslforge.test.beans.Basket.name]
                staticStars = [Math.name,org.hamcrest.MatcherAssert.name, org.hamcrest.Matchers.name]
                staticImports = [[Locale.name,'FRENCH']]
                aliasedStaticImports = [France:[Locale.name,'FRENCH']]
            }
        }
    }
}
