environments {
     development {
         dsl.emcInheritance = true
         dsl.scripts = 'src/test/scripts'
         dsl.delegates = [org.beedom.dslforge.test.delegates.SeleniumDelegate,
                          [dslKey: "selenium", clazz: com.thoughtworks.selenium.DefaultSelenium]]
     }
}
