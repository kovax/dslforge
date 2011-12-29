
product = new org.beedom.dslforge.test.beans.Product(id: "100", name: "Noname RW/DL")
product.shall "not be", null


new File("src/test/data/address.csv").openCsvEachRow(headerRows:1) { address, i ->
    assert address.name
    assert address.postal
    assert address.email
}
