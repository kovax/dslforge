environments {
    development {
        dsl {
            emcInheritance = true
            scripts = 'src/test/scripts'
            evaluate = ["evaluate", "include"]
            delegates = [org.beedom.dslforge.integrations.CompareCSV]
            categories = [org.beedom.dslforge.integrations.OpenCSVCategory]
        }
    }
}
