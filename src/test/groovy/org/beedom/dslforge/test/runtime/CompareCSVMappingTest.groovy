package org.beedom.dslforge.test.runtime

import org.beedom.dslforge.DSLEngine
import org.beedom.dslforge.test.TestBase;
import org.junit.Before
import org.junit.Test


/**
 * 
 * @author kovax
 *
 */
class CompareCSVMappingTest extends TestBase {

	def csv = "src/test/data/multiHeaderWithRepeat.csv"
	def csv2 = "src/test/data/multiHeaderWithRepeatv2.csv"

    @Before
    public void init () {
        configFile = "src/test/conf/CompareTestConfig.groovy"
        super.init()
    }

    @Test
    public void useGroovyAssert() {
        dsle.run {
            CSVCompare(name: 'compareUseGroovyAssert') {
                expectedDatasource = [file: csv,  options:[headerRows:3]]
                actualDatasource   = [file: csv2, options:[headerRows:3]]

                mappingFields {
					field(name: 'userid', type: 'String', isKey: true, isIgnored: false)
					field(name: 'kind')
					field(name: 'sex')
					field(name: 'age')
					field(name: 'title')
					field(name: 'firstName')
					field(name: 'lastName')
					field(name: 'password')

					field(name: 'contacts.address[].purpose')
					field(name: 'contacts.address[].country')
					field(name: 'contacts.address[].countryCode')
					field(name: 'contacts.address[].province')
					field(name: 'contacts.address[].address1')
					field(name: 'contacts.address[].city')
					field(name: 'contacts.address[].postalCode')
					field(name: 'contacts.address[].deliveryInfo')

					field(name: 'contacts.phone[].purpose')
					field(name: 'contacts.phone[].areaCode')
					field(name: 'contacts.phone[].number')

					field(name: 'contacts.email[].purpose')
					field(name: 'contacts.email[].address')
                }
            }

			compareUseGroovyAssert.run()
        }
    }
}
