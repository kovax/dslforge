environments {
     development {
         dsl.emcInheritance = true
         dsl.scripts = 'src/test/scripts'
         dsl.defaultDelegate = org.beedom.dslforge.test.delegates.FeatureDelegate
         dsl.delegates = [org.beedom.dslforge.test.delegates.ScenarioDelegate,
                          org.beedom.dslforge.test.delegates.FeatureDelegate]
     }
}
