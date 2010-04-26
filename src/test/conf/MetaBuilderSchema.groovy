objectKeys = [(org.beedom.dslforge.test.beans.User)  : "kind",
              (org.beedom.dslforge.test.beans.Basket): {b-> "basket${b.title}"}]

def contactSchema = metaBuilder.define {
    contact(factory: org.beedom.dslforge.test.beans.Contact) {
        properties { purpose()
        }
    }
}

def addressSchema = metaBuilder.define {
    address(schema: contactSchema, factory: org.beedom.dslforge.test.beans.Address) {
        properties {
            toName()
            attnName()
            country()
            countryCode()
            province()
            provinceCode()
            address1()
            address2()
            city()
            postalCode()
            deliveryInfo()
            allowSolicitation()
        }
    }
}

def phoneSchema = metaBuilder.define {
    phone(schema: contactSchema, factory: org.beedom.dslforge.test.beans.Phone) {
        properties {
            countryCode()
            areaCode()
            number()
            extension()
        }
    }
}

def emailSchema = metaBuilder.define {
    email(schema: contactSchema, factory: org.beedom.dslforge.test.beans.Email) {
        properties {
            address()
        }
    }
}

def webSchema = metaBuilder.define {
    web(schema: contactSchema, factory: org.beedom.dslforge.test.beans.Web) {
        properties {
            url()
        }
    }
}

def currencySchema = metaBuilder.define {
    currency(factory: org.beedom.dslforge.test.beans.Currency) {
        properties {
            currency()
        }
    }
}

def paymentSchema = metaBuilder.define {
    payment(schema: currencySchema, factory: org.beedom.dslforge.test.beans.Payment) {
        properties {
            kind(check: ['cc', 'dd', 'pp', 'gc'], def: 'cc')
        }
    }
}

def cardSchema = metaBuilder.define {
    creditcard(schema: paymentSchema, factory: org.beedom.dslforge.test.beans.CreditCard) {
        properties {
            type(check: ['Visa','Mastercard', 'Amex'], def: 'Visa')
            number()
            expYear()
            expMonth()
            firstName()
            lastName()
            securityCode()
        }
    }
}

def debitSchema = metaBuilder.define {
    directdebit(schema: paymentSchema, factory: org.beedom.dslforge.test.beans.DirectDebit) {
        properties {
            bankName()
            bankAddress()
            bankPostcode()
            accountOwner()
            accountNumber()
            branchShortCode()
            startDate()
            frequency()
        }
    }
}

def paypalSchema = metaBuilder.define {
    paypal(schema: paymentSchema, factory: org.beedom.dslforge.test.beans.PayPal) {
        properties {
            userId()
            password()
        }
    }
}

def giftCardSchema = metaBuilder.define {
    giftcard(schema: paymentSchema, factory: org.beedom.dslforge.test.beans.GiftCard) {
        properties {
            name()
            amount()
            balance()
        }
    }
}

def userSchema = metaBuilder.define {
    user(factory: org.beedom.dslforge.test.beans.User) {
        properties {
            kind(check: ['admin', 'customer', 'agent'], def: 'customer')
            firstName()
            lastName()
            userid()
            password()
            title()
            sex()
            birthdate()
            comment()
        }
        collections {
            contacts {
                address(schema: addressSchema)
                phone(schema: phoneSchema)
                email(schema: emailSchema)
                web(schema: webSchema)
            }
            payments {
                creditcard(schema: cardSchema)
                directdebit(schema: debitSchema)
                paypal(schema: paypalSchema)
                giftcard( schema: giftCardSchema )
            }
        }
    }
}

def productSchema = metaBuilder.define {
    product( schema: currencySchema, factory: org.beedom.dslforge.test.beans.Product ) {
        properties {
            categories()
            id()
            name()
            qty()
            price()
            vat()
            total()
        }
    }
}

def promoSchema = metaBuilder.define {
    promotion( schema: currencySchema, factory: org.beedom.dslforge.test.beans.Promotion ) {
        properties {
            name()
            discount()
            total()
        }
    }
}

def shippingSchema = metaBuilder.define {
    shipping( schema: currencySchema, factory: org.beedom.dslforge.test.beans.Shipping ) {
        properties {
            method()
            cost()
        }
    }
}

def basketSchema = metaBuilder.define {
    basket(schema: currencySchema, factory: org.beedom.dslforge.test.beans.Basket) {
        properties {
            title()
            
            net()
            vat()
            total()
            
            shipping(schema: shippingSchema)
            giftcard(schema: giftCardSchema)
        }
        collections {
            items {
                product(schema: productSchema)
            }
            promos {
                promotion(schema: promoSchema)
            }
        }
    }
}
