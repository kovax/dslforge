package org.beedom.dslforge.test.runtime;

import static org.junit.Assert.*;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import org.beedom.dslforge.DSLEngine;
import org.beedom.dslforge.SimpleRenderer;
import org.beedom.dslforge.test.TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

class ReportingDelegateTests extends TestBase {
    
    @Rule public TestName name = new TestName();

    @Before
    public void init() {
        def reportType = SimpleRenderer.ReportType.XML
        configFile = "src/test/conf/ReportingDelegateTestConfig.groovy"

        new File("build/reports/beedriven").mkdirs()

        file = new File("build/reports/beedriven/ReportingDelegateTests.${name.getMethodName()}.${SimpleRenderer.getFileExt(reportType)}")
        file.createNewFile()
        writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))
        reporter = new SimpleRenderer(type: reportType, writer: writer)
        
        context = new Binding(dryRun: false)

        super.init()
    }

    @After
    public void tearDown() {
        writer.close()
    }

    @Test
    public void nestedDelegates() {
        dsle.run {
            define {
                user {
                    kind = "customer"
                    userid = "test@d${timeStamp}d.com"
                    password = "hellobaby"
                    firstName = "Test"
                    lastName = "Customer_${timeStamp}"
                    title = "Mr."
                    sex = "M"
                }
            }

            feature "Shopping Cart Management", {
                in_order "to use a webshop", {}
                as_a "customer"
                i_want "to make changes to a shopping cart"

                webshop {
                    scenario "Remove product from basket", {
                        given "logged in customer" go {
                            login customer
                        }
                        and "the customer has checked out a basket with 1 item"
                        when "the customer removes that item from the basket"
                        then "the basket is empty", {
                            logout customer
                            assert customer.firstName == "logged out customer"
                        }
                    }
                }
            }
        }//End of dsle.run
    }

    @Test
    public void nestedFeature() {
        dsle.run {
            feature "Feature 1", {
            	in_order "principle 1"
                feature "Feature 1.1", {
                	in_order "principle 1.1"
                    feature "Feature 1.1.1", {
                        in_order "principle 1.1.1"
                    }
                }
            	as_a "principle 1"
                i_want "principle 1"
            }
        }
    }

    @Test
    public void useAlias() {
        dsle.run {
            process "Remove product from basket", {
                given "logged in customer"
                and "the customer has checked out a basket with 1 item"
                when "the customer removes that item from the basket"
                then "the basket is empty"
            }

            //The above translated to hungarian
            szenárió "Termék törlése a kosárból", {
                adott "egy vásárló, aki azonosította magát"
                és "a vásárló a kosarat véglegesítettete egy termékkel"
                amikor "a vásárló törölte a terméket a kosárból"
                akkor "a kosár üres lesz"
            }

            //The above translated to hungarian one more time to test registered methods
            szenárió "Termék törlése a kosárból", {
                adott "egy vásárló, aki azonosította magát"
                és "a vásárló a kosarat véglegesítettete egy termékkel"
                amikor "a vásárló törölte a terméket a kosárból"
                és "a vásárló a kosarat véglegesítettete"
                akkor "a kosár üres lesz"
            }

            define {
                user {
                    kind = "customer"
                    userid = "test@d${timeStamp}d.com"
                    password = "hellobaby"
                    firstName = "Test"
                    lastName = "Customer_${timeStamp}"
                    title = "Mr."
                    sex = "M"
                }
            }
            
            webshop {
                signIn customer
                signOut
            }
        }
    }
}
