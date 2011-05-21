
product = new org.beedom.dslforge.test.beans.Product(id: "100", name: "Noname RW/DL")
product.shall "not be", null


new File("src/test/data/address.csv").openCsvEachRow(1) { address ->
    assert address.name
    assert address.postal
    assert address.email
}
