package org.beedom.dslforge.test.beans


/**
 * @author kovax
 *
 */
public class User {
    String kind      = ""
    String firstName = ""
    String lastName  = ""
    String userid    = ""
    String password  = ""
    String title     = ""
    String sex       = ""
    String birthdate = ""
    String comment   = ""

    List<Contact> contacts = []
    List<Payment> payments = []

    /**
    * Returns a contact of specific type and purpose, assumes only one has such purpose
    * If no purpose is given it returns the first contact of the type
    */
    public Contact getContactByPupose( Class claz, String purp ) {
        for (Contact c in contacts) {
            if (c.class == claz && (c.purpose.contains(purp) || !purp))
                return c
        }
        return null
    }

    /**
    * Returns the list of contacts of specific type
    */
    public List<Contact> getContactList(Class claz) {
        List<Contact> l = []
        for (c in contacts) {
            if (c.class == claz )
                l.add(c)
        }
        return l
    }
    
    /**
    * Returns a address of specific country, assumes only one has such country
    */
    public Address getAddressByCountry( String countryCode ) {
        def l = getContactList(Address.class)

        for (Address a in l) {
            if( a.countryCode == countryCode )
                return a
        }
        return null
    }

    /**
     * Searches addresses by country and purpose
     */
    public Address findAddress( String key ) {
        def addr = getAddressByCountry(key)

        if(!addr) {
            addr = (Address) getContactByPupose(Address.class, key)
        }

        return addr
    }


    public CreditCard getCardPayment() {
        for (p in payments) {
            if (p instanceof CreditCard)
                return p
        }
        return null
    }

    public DirectDebit getBankPayment() {
        for (p in payments) {
            if (p instanceof DirectDebit)
                return p
        }
        return null
    }

    public PayPal getPayPalPayment() {
        for (p in payments) {
            if (p instanceof PayPal)
                return p
        }
        return null
    }

    public GiftCard getGiftCardPayment() {
        for (p in payments) {
            if (p instanceof GiftCard)
                return p
        }
        return null
    }
}