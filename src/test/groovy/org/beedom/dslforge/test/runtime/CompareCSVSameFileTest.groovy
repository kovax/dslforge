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
class CompareCSVSameFileTest extends TestBase {

	def csv = "src/test/data/multiHeaderWithRepeat.csv"

	@Before
	public void init () {
        configFile = "src/test/conf/CompareTestConfig.groovy"
        super.init()
	}

	@Test
	public void useGroovyAssert() {
		dsle.run {
			csvCompare (failFast:true) {
				actualDatasource  = [file: csv, options:[headerRows:3]]
				expectedDatasource = [file: csv, options:[headerRows:3]]

				map { actual, expected, i ->
                    assert actual.kind      == expected.kind
                    assert actual.sex       == expected.sex
                    assert actual.title     == expected.title
                    assert actual.firstName == expected.firstName
                    assert actual.lastName  == expected.lastName
                    assert actual.userid    == expected.userid
                    assert actual.password  == expected.password
            
                    assert actual.contacts.address[0].purpose      == expected.contacts.address[0].purpose
                    assert actual.contacts.address[0].country      == expected.contacts.address[0].country
                    assert actual.contacts.address[0].countryCode  == expected.contacts.address[0].countryCode
                    assert actual.contacts.address[0].province     == expected.contacts.address[0].province
                    assert actual.contacts.address[0].address1     == expected.contacts.address[0].address1
                    assert actual.contacts.address[0].city         == expected.contacts.address[0].city
                    assert actual.contacts.address[0].postalCode   == expected.contacts.address[0].postalCode
                    assert actual.contacts.address[0].deliveryInfo == expected.contacts.address[0].deliveryInfo
            
                    assert actual.contacts.phone[0].purpose  == expected.contacts.phone[0].purpose
                    assert actual.contacts.phone[0].areaCode == expected.contacts.phone[0].areaCode
                    assert actual.contacts.phone[0].number   == expected.contacts.phone[0].number
            
                    assert actual.contacts.email[0].purpose == expected.contacts.email[0].purpose
                    assert actual.contacts.email[0].address == expected.contacts.email[0].address
            
                    assert actual.contacts.email[1].purpose == expected.contacts.email[1].purpose
                    assert actual.contacts.email[1].address == expected.contacts.email[1].address
                }

                assert !difference
			}
		}
	}

    @Test
    public void useHamcrestAssertThat() {
        dsle.run {
            csvCompare (failFast:true) {
                actualDatasource  = [file: csv, options:[headerRows:3]]
                expectedDatasource = [file: csv, options:[headerRows:3]]

                map { actual, expected, i ->
                    assertThat actual.kind      equalTo expected.kind
                    assertThat actual.sex       equalTo expected.sex
                    assertThat actual.title     equalTo expected.title
                    assertThat actual.firstName equalTo expected.firstName
                    assertThat actual.lastName  equalTo expected.lastName
                    assertThat actual.userid    equalTo expected.userid
                    assertThat actual.password  equalTo expected.password

                    assertThat actual.contacts.address[0].purpose      equalTo expected.contacts.address[0].purpose
                    assertThat actual.contacts.address[0].country      equalTo expected.contacts.address[0].country
                    assertThat actual.contacts.address[0].countryCode  equalTo expected.contacts.address[0].countryCode
                    assertThat actual.contacts.address[0].province     equalTo expected.contacts.address[0].province
                    assertThat actual.contacts.address[0].address1     equalTo expected.contacts.address[0].address1
                    assertThat actual.contacts.address[0].city         equalTo expected.contacts.address[0].city
                    assertThat actual.contacts.address[0].postalCode   equalTo expected.contacts.address[0].postalCode
                    assertThat actual.contacts.address[0].deliveryInfo equalTo expected.contacts.address[0].deliveryInfo

                    assertThat actual.contacts.phone[0].purpose  equalTo expected.contacts.phone[0].purpose
                    assertThat actual.contacts.phone[0].areaCode equalTo expected.contacts.phone[0].areaCode
                    assertThat actual.contacts.phone[0].number   equalTo expected.contacts.phone[0].number

                    assertThat actual.contacts.email[0].purpose equalTo expected.contacts.email[0].purpose
                    assertThat actual.contacts.email[0].address equalTo expected.contacts.email[0].address

                    assertThat actual.contacts.email[1].purpose equalTo expected.contacts.email[1].purpose
                    assertThat actual.contacts.email[1].address equalTo expected.contacts.email[1].address
                }
                
                assert !difference
            }
        }
    }

    @Test
    public void useGroovyAssertRow() {
        dsle.run {
			csvCompare (failFast:true) {
				actualDatasource  = [file: csv, options:[headerRows:3]]
				expectedDatasource = [file: csv, options:[headerRows:3]]

				map { actual, expected, i ->
					assert actual == expected
				}

                assert !difference
			}
        }
    }

    @Test
    public void useHamcrestAssertThatRow() {
        dsle.run {
			csvCompare (failFast:true) {
				actualDatasource  = [file: csv, options:[headerRows:3]]
				expectedDatasource = [file: csv, options:[headerRows:3]]

				map { actual, expected, i ->
					assertThat actual equalTo expected
				}

                assert !difference
			}
		}
	}
}
