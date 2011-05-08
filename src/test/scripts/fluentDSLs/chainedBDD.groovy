def bdd = 0
def given = { c1 -> c1(); [when: { c2 -> c2(); [then: { c3 -> c3() }] }]}

given {
    bdd++
    println "given"
} when {
    bdd++
    println "when"
} then {
    bdd++
    println "than"
}

assert bdd == 3

given( {
    bdd++
    println "given"
} ).when( {
    bdd++
    println "when"
} ).then( {
    bdd++
    println "than"
} )

assert bdd == 6
