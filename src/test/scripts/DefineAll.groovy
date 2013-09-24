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
