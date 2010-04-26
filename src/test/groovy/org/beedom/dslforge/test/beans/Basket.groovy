package org.beedom.dslforge.test.beans


import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

/**
 * @author kovax
 *
 */
public class Basket extends Currency {

    String title = ""

    BigDecimal net
    BigDecimal vat
    BigDecimal total

    Shipping shipping

    List<Product> items = []
    List<Promotion> promos = []

    GiftCard giftcard


    def setUniformCurrency() {
        if (currency) {
            setUniformCurrency(currency)
        }
        else {
            fail("Basket must have currency set!")
        }
    }

    def setUniformCurrency(String c) {
        if (shipping)
            shipping.currency = c

        if (promos)
            promos.each { it.currency = c }

        if (items)
            items.each { it.currency = c }

        if (giftcard)
            giftcard.currency = c 
    }

    /**
     * Hook to initialise Basket which cannot be done by MetaBuilder
     */
    def initialise() {
        setUniformCurrency()
    }

    def print() {
        println "basket: net=${net} total=${total} ${currencySign}"

        if (shipping)
            println "    shipping: ${shipping.method} cost=${shipping.cost}"

        if (promos) {
            promos.each { promo ->
                println "    promo: ${promo.name} discount=${promo.discount} total=${promo.total}"
            }
        }

        items.each { product ->
            assert (product.name != null) | (product.id != null), "Either name or id must not be null for each products in basket"
            println "    product: ${product.name} qty=${product.qty} price=${product.price} total=${product.total}"
        }
    }

    def getListTotal(list) {
        def total = 0.00
        list.each { total += it.total }
        return total
    }
}