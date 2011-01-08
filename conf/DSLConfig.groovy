environments {
    development {
        dsl {
            emcInheritance = true
            scripts = 'src/test/scripts'
            evaluate = ["evaluate", "include"]
            mbSchemaFiles = ["src/test/conf/MetaBuilderSchema.groovy"]
            delegates = [[dslKey: "define", clazz: org.beedom.dslforge.integrations.MetaBuilderDelegate],
                         [dslKey: "selenium", clazz: com.thoughtworks.selenium.DefaultSelenium],
                         org.beedom.dslforge.test.delegates.WebShopDelegate,
                         org.beedom.dslforge.test.delegates.ScenarioDelegate,
                         org.beedom.dslforge.test.delegates.FeatureDelegate]
            categories = [org.beedom.dslforge.test.decorators.ShallCategory]
        }
    }
}
