environments {
     development {
         dsl.emcInheritance = true
         dsl.scripts = 'src/test/scripts'
         dsl.categories = [org.beedom.dslforge.test.decorators.ShallCategory]
     }
}
