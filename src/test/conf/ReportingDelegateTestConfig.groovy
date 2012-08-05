environments {
    development {
        dsl {
            emcInheritance = true
            scripts = 'src/test/scripts'
            evaluate = ["evaluate", "include"]
            mbSchemaFiles = ["src/test/conf/MetaBuilderSchema.groovy"]
            delegates = [[dslKey: "define", clazz: org.beedom.dslforge.integrations.MetaBuilderDelegate],
                         org.beedom.dslforge.test.delegates.WebShopDelegate,
                         org.beedom.dslforge.test.delegates.ReportingScenarioDelegate,
                         org.beedom.dslforge.test.delegates.ReportingFeatureDelegate]
            categories = [org.beedom.dslforge.test.decorators.ShallCategory]
        }
    }
}
