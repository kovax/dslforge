def bdd = 0
def given = { c1 -> c1(); [when: { c2 -> c2(); [then: { c3 -> c3() }] }]}

given { bdd++ }
when { bdd++ }
then { bdd++ }

assert bdd == 3