package org.beedom.dslforge.test

import org.junit.Before
import org.junit.Test

import org.beedom.dslforge.DSLEngine
import org.beedom.dslforge.integrations.OpenCSVCategory

/**
 */
class OpenCSVTest {

    @Before
    public void before () {
    }

    @Test
    public void noHeaderValues() {
        def dsle = new DSLEngine("src/test/conf/DecoratorTestConfig.groovy", "development")

        dsle.run {
            new File("src/test/data/noHeaderAddress.csv").openCsvEachRow { address ->
                assert address[0]
                assert address[1]
                assert address[2]
            }
        }
    }

    @Test
    public void singleLineHeaderValues() {
        def dsle = new DSLEngine("src/test/conf/DecoratorTestConfig.groovy", "development")

        dsle.run {
            new File("src/test/data/address.csv").openCsvEachRow(headerRows:1) { address ->
                assert address.name
                assert address.postal
                assert address.email
            }
        }
    }

    @Test
    public void multiLineHeader() {
        
        use(OpenCSVCategory) {
            def header = new File("src/test/data/multiHeader.csv").openCsvHeader(headerRows:2)

            assert header

            assert header[0] == ["kind"]
            assert header[6] == ["sex"]

            assert header[7] == ["address","purpose"]
            assert header[14] == ["address","deliveryInfo"]

            assert header[15] == ["phone","purpose"]
            assert header[17] == ["phone","number"]

            assert header[18] == ["email","purpose"]
            assert header[19] == ["email","address"]
        }
    }

    @Test
    public void multiLineHeaderValues() {
        def dsle = new DSLEngine("src/test/conf/DecoratorTestConfig.groovy", "development")
        
        dsle.run {
            new File("src/test/data/multiHeader.csv").openCsvEachRow(headerRows:2) { user ->
                assert user.kind
                assert user.sex

                assert user.address
                assert user.address.purpose
                assert user.address.deliveryInfo

                assert user.phone
                assert user.phone.purpose
                assert user.phone.number

                assert user.email
                assert user.email.purpose
                assert user.email.address
            }
        }
    }

    @Test
    public void multiLineHeaderWithRepeatValues() {
        def dsle = new DSLEngine("src/test/conf/DecoratorTestConfig.groovy", "development")

        def i=0

        dsle.run {
            new File("src/test/data/multiHeaderWithRepeat.csv").openCsvEachRow(headerRows:3) { user ->
                assert user.kind
                assert user.sex

                assert user.contacts.address[0].purpose
                assert user.contacts.address[0].deliveryInfo

                assert user.contacts.phone[0].purpose
                assert user.contacts.phone[0].number

                assert user.contacts.email[0].purpose
                assert user.contacts.email[0].address

                if(i==0) {
                    assert user.contacts.email[1].purpose == "secondary"
                    assert user.contacts.email[1].address == "customer2@nowhere.com"
                }
                else {
                    assert user.contacts.email[1].purpose == ""
                    assert user.contacts.email[1].address == ""
                }

                i++
            }
        }
    }


    @Test
    public void multiLineHeaderWithRepeatValues1() {
        def dsle = new DSLEngine("src/test/conf/DecoratorTestConfig.groovy", "development")

        def i=0

        dsle.run {
            def header = new File("src/test/data/multiHeaderWithRepeat.csv").openCsvHeader(headerRows:3)
            
            new File("src/test/data/multiHeaderWithRepeat.csv").openCsvEachRow(header:header, skipRows:3) { user ->
                assert user.kind
                assert user.sex

                assert user.contacts.address[0].purpose
                assert user.contacts.address[0].deliveryInfo

                assert user.contacts.phone[0].purpose
                assert user.contacts.phone[0].number

                assert user.contacts.email[0].purpose
                assert user.contacts.email[0].address

                if(i==0) {
                    assert user.contacts.email[1].purpose == "secondary"
                    assert user.contacts.email[1].address == "customer2@nowhere.com"
                }
                else {
                    assert user.contacts.email[1].purpose == ""
                    assert user.contacts.email[1].address == ""
                }

                i++
            }
        }
    }
}
