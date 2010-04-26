def timeStamp = System.currentTimeMillis()
assert junit == "junit4"

define {
    user {
        kind = "customer"
        userid = "test@d${timeStamp}d.com"
        password = "hellobaby"
        firstName = "Test"
        lastName = "Customer_${timeStamp}"
        title = "Mr."
        sex = "M"
        
        contacts {
            address {
                purpose = "shiping,billing"
                country = "Netherlands"
                countryCode = "NLD"
                province = ""
                address1 = "Dutch Test ${timeStamp}"
                city = "Delft"
                postalCode = "NL23212"
                deliveryInfo = "NLD Delivery Info:${timeStamp}"
                allowSolicitation = true
            }
            phone(purpose: "home", areaCode: "+33-1", number: "${timeStamp}")
            email(purpose: "primary", address: "test@d${timeStamp}d.com")
        }
        payments {
            creditcard {
                kind = "cc"
                type = "Visa"
                number = "4444333322221111"
                expYear = "2011"
                expMonth = "01"
                firstName = "Test"
                lastName = "Customer_${timeStamp}"
                securityCode = "123"
            }
        }
    }
    
    basket(title: "No Shiping Cost", net: 15.45, total: 18.16, currency: "EUR") {
        promos {
            promotion(name: "5% First Order Discount", discount: 0.05, total: 0.81)
        }
        items {
            product {
                categories = "Media_DVD"
                id = "100"
                name = "Noname RW/DL"
                qty = 3
                price = 5.42
                total = 16.26
            }
        }
    }
    
    basket(title: "With Shiping Cost:./ , :?\$", net: 20.35, vat: 3.56, total: 23.91, currency: "EUR") {
        shipping(method: "1st Class (3-5 days)", cost: 4.90)
        promos {
            promotion(name: "5% First Order Discount", discount: 0.05, total: 0.81)
        }
        items {
            product {
                categories = "Media_DVD"
                id = "100"
                name = "Noname RW/DL"
                qty = 3
                price = 5.42
                total = 16.26
            }
        }
    }
}

//This is added by MetaBuilderDelegate during the define block
assert metaBuilder

webshop {
	assert context
	
	define {
        product(categories: "Media_DVD", id: "100", name: "Siverblade RW/DL")
	}
	
    assert product100
	assert product100.name == "Siverblade RW/DL"
	assert customer
	assert basketNoShipingCost
	assert context.customer
    assert customer == context.customer
    assert customer.is(context.customer)
    
    //This is a byproduct of injecting the context variable into delegates
    customer.shall "be equal", context.customer

    login customer
    logout customer
    
    'sign in' customer
    logout customer
    
    assert customer.firstName == "logged out customer"
}

feature "Shopping Cart Management", {
	assert context
	dryRunScenario = false
	
    in_order "to use a webshop"
    as_a "customer"
    i_want "to make changes to a shopping cart"
    as_a "admin"
    i_want "to forbid changes to a shopping cart"
    
    scenario "Remove product from basket", {
        given "logged in customer1"
        provided "logged in customer2"
        provided "logged in customer3", {
            assert dslAlias == "provided"
        }
        and "the customer has checked out a basket with 1 item"
        és "a vásárló a bevásárló kocsit véglegesítettete"
        when "the customer removes that item from the basket"
        then "the basket is empty", {
        	basketWithShipingCost.shall "not be", null
        }
    }
	
    //scenario in hungarian
    szenárió "Termék törlése a kosárból", {
        adott "egy vásárló, aki azonosította magát"
        és "a vásárló a kosarat véglegesítettete egy termékkel"
        amikor "a vásárló törölte a terméket a kosárból"
        akkor "a kosár üres lesz"
    }
}

webshop {
	assert context
    assert basketWithShipingCost
}
