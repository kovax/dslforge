environments {
    development {
        dsl {
            emcInheritance = true
            scripts = 'src/test/scripts'
            evaluate = ["evaluate", "include"]
            mbSchemaFiles = ["src/test/conf/CompareCSVSchema.groovy"]
            delegates = [org.beedom.dslforge.integrations.CompareCSV]
            categories = [org.beedom.dslforge.integrations.OpenCSVCategory]
        }
    }
}
