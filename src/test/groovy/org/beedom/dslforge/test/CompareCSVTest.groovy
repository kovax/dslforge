package org.beedom.dslforge.test

import org.beedom.dslforge.DSLEngine
import org.junit.Before
import org.junit.Test
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;



/**
 * 
 * @author kovax
 *
 */
class CompareCSVTest extends TestBase {

	def dsle
	def csv = "src/test/data/multiHeaderWithRepeat.csv"
	def csv2 = "src/test/data/multiHeaderWithRepeatv2.csv"

	def assertClosure = { left, right, i ->
		assert left.kind      == right.kind
		assert left.sex       == right.sex
		assert left.title     == right.title
		assert left.firstName == right.firstName
		assert left.lastName  == right.lastName
		assert left.userid    == right.userid
		assert left.password  == right.password

		assert left.contacts.address[0].purpose      == right.contacts.address[0].purpose
		assert left.contacts.address[0].country      == right.contacts.address[0].country
		assert left.contacts.address[0].countryCode  == right.contacts.address[0].countryCode
		assert left.contacts.address[0].province     == right.contacts.address[0].province
		assert left.contacts.address[0].address1     == right.contacts.address[0].address1
		assert left.contacts.address[0].city         == right.contacts.address[0].city
		assert left.contacts.address[0].postalCode   == right.contacts.address[0].postalCode
		assert left.contacts.address[0].deliveryInfo == right.contacts.address[0].deliveryInfo

		assert left.contacts.phone[0].purpose  == right.contacts.phone[0].purpose
		assert left.contacts.phone[0].areaCode == right.contacts.phone[0].areaCode
		assert left.contacts.phone[0].number   == right.contacts.phone[0].number

		assert left.contacts.email[0].purpose == right.contacts.email[0].purpose
		assert left.contacts.email[0].address == right.contacts.email[0].address

		assert left.contacts.email[1].purpose == right.contacts.email[1].purpose
		assert left.contacts.email[1].address == right.contacts.email[1].address
	}

	@Before
	public void before () {
		dsle = new DSLEngine(new Binding(), "src/test/conf/CompareTestConfig.groovy", "development")
	}

	@Test
	public void compareSameFile() {
		dsle.run {
			csvCompare (failFast:true) {
				leftDatasource  = [file: csv, options:[headerRows:3]]
				rightDatasource = [file: csv, options:[headerRows:3]]

				map (assertClosure)
			}

			csvCompare (failFast:true) {
				leftDatasource  = [file: csv, options:[headerRows:3]]
				rightDatasource = [file: csv, options:[headerRows:3]]

				map { left, right, i ->
					assert right == left
				}
			}

			csvCompare (failFast:true) {
				leftDatasource  = [file: csv, options:[headerRows:3]]
				rightDatasource = [file: csv, options:[headerRows:3]]

				map { left, right, i ->
					assertThat right, equalTo(left)
				}
			}
		}
	}

	@Test
	public void compareFilesWithDiffs() {
		dsle.run {
			csvCompare {
				leftDatasource  = [file: csv,  options:[headerRows:3]]
				rightDatasource = [file: csv2, options:[headerRows:3]]

				map assertClosure
				
				assert false, "Result file is not implemented"
			}
		}
	}

	@Test(expected=AssertionError)
	public void compareFilesWithDiffsFailfast() {
		dsle.run {
			csvCompare (failFast:true) {
				leftDatasource  = [file: csv,  options:[headerRows:3]]
				rightDatasource = [file: csv2, options:[headerRows:3]]

				map assertClosure
			}
		}
	}

	@Test
	public void compareFilesWithDiffsHarmcrest() {
		dsle.run {
			csvCompare {
				leftDatasource  = [file: csv,  options:[headerRows:3]]
				rightDatasource = [file: csv2, options:[headerRows:3]]

				map { left, right, i ->
					assertThat right equalTo left
				}
			}
		}
	}

}
