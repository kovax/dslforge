description = "Shopping Cart Management"

in_order "to use a webshop"
as_a "customer"
i_want "to make changes to a shopping cart"

scenario "Remove product from basket", {
    given "logged in customer1", {
    }
    and "the customer has checked out a basket with 1 item" 
    when "the customer removes that item from the basket"
    then "the basket is empty"
}
