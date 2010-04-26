

import org.beedom.dslforge.test.beans.Product

product = new Product(id: "100", name: "Noname RW/DL")
product.shall "not be", null
