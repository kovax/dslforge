package org.beedom.dslforge.test.beans


/**
 * A general purpose Bean to be used in many scenarios when a Product data is needed,
 * like Basket, Catalog, Product definition, Order etc
 * 
 * @author kovax
 *
 */
public class Product extends Currency {
	private static final String token ="_"

	String     categories
	String     id
	String     name
	Integer    qty
	BigDecimal price
	BigDecimal vat
	BigDecimal total

	public static List<String> getCategoryList( catPath ) {
		if(catPath)
			return catPath.tokenize(token)
		else
			return null
	}


	def List<String> getCategoryList( ) {
		return getCategoryList(categories)
	}
}