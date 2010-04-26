environments {
     development {
         dsl.emcInheritance = true
         dsl.scripts = 'src/test/scripts'
         dsl.delegates = [org.beedom.dslforge.test.delegates.MetaBuilderDelegate,
                          org.beedom.dslforge.test.delegates.WebShopDelegate,
                          org.beedom.dslforge.test.delegates.ScenarioDelegate,
                          org.beedom.dslforge.test.delegates.FeatureDelegate]
         dsl.categories = [org.beedom.dslforge.test.decorators.ShallCategory]
     }
}
