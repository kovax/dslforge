package org.beedom.dslforge.test

import groovy.lang.Binding;

import org.beedom.dslforge.BindingConvention;

import org.beedom.dslforge.test.beans.Basket;
import org.beedom.dslforge.test.beans.Product;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


class BindingConventionTests {
    static def product
    static def basket
    
    @BeforeClass
    public static void init() {
        product = new Product(id: "100", name: "Noname RW/DL")
        basket  = new Basket (title: "No Shiping Cost")
    }

    @Test
    public void propertyNameConversion() {
        assertEquals( "withShipingCost", BindingConvention.convertToPropertyName("With.Shiping / Cost:./ , :?\$"))
    }

    @Test
    public void autoPropertyName() {
        assertEquals( "product100", BindingConvention.getDefaultPropName(product) )
        assertEquals( null,         BindingConvention.getDefaultPropName(basket) )
    }

    @Test
    public void propertyNameByString() {
        assertEquals( "noShipingCost", BindingConvention.getUserDefinedPropName("title", basket))
    }
    
    @Test
    public void propertyNameByStringException() {
        try {
            BindingConvention.getUserDefinedPropName("title", product)
            fail("MissingPropertyException shall be thrown")
        }
        catch(MissingPropertyException e) {
        }
    }
    
    @Test
    public void propertyNameByClosure() {
        assertEquals( "basketNoShipingCost", BindingConvention.getUserDefinedPropName({b-> "basket${b.title}"}, basket))
    }
    
    @Test
    public void bidingIndividualObject() {
        def binding = new Binding()
        
        BindingConvention.bindObject(binding, product, null)
        BindingConvention.bindObject(binding, basket, {b-> "basket${b.title}"})

        assertNotNull( binding.product100 )
        assertEquals("Noname RW/DL",binding.product100.name)
        assertNotNull( binding.basketNoShipingCost )
        assertEquals("No Shiping Cost",binding.basketNoShipingCost.title)
    }

    @Test
    public void bidingObjectList() {
        def binding = new Binding()
        
        BindingConvention.bindObjectList(binding, [product,basket], [(org.beedom.dslforge.test.beans.Basket): {b-> "basket${b.title}"}])
        
        assertNotNull( binding.product100 )
        assertEquals("Noname RW/DL",binding.product100.name)
        assertNotNull( binding.basketNoShipingCost )
        assertEquals("No Shiping Cost",binding.basketNoShipingCost.title)
    }
}
