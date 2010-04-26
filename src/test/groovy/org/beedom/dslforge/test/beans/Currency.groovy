package org.beedom.dslforge.test.beans


/**
 * Base class for all beans which holds price/cost
 * User: zsolt.kovacs
 */
class Currency {
	
	String currency = ""

    /**
     * java.util.Currency can only work with system default locale or with a specific instance
     * but for testing purposes these currency signs could be essential
     */
    public static String getCurrencySign(String c) {
        //return java.util.Currency.getInstance( c ).getSymbol(new Locale("fr","FR"))
        if(c == "EUR") {
            return "\u20ac" //€
        }
        else if(c == "GBP") {
            return "\u00A3" //£
        }
        else if(c == "USD") {
            return "\$" //needs to be escaped as it is a groovy special character
        }
        else {
            return c
        }
    }

    def getCurrencySign() {
        getCurrencySign(currency)
    }
}
