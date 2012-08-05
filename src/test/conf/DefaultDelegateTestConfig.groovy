environments {
     development {
         dsl {
             emcInheritance = true
             scripts = 'src/test/scripts'
             defaultDelegate = [feature: org.beedom.dslforge.test.delegates.FeatureDelegate,
                                scenario: org.beedom.dslforge.test.delegates.ScenarioDelegate]
             delegates = [org.beedom.dslforge.test.delegates.ScenarioDelegate,
                          org.beedom.dslforge.test.delegates.FeatureDelegate]
         }
     }
}
