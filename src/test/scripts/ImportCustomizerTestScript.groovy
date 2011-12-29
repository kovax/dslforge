def pi3 = cos PI/3
assert Locale.FRENCH == FRENCH

def mb = new MetaBuilder()
def b = new Basket()
def b1 = new Cart()
def c = new Customer()

assert b
assert b1
assert c

assertThat(FRENCH, equalTo(Locale.FRENCH))
assertThat(France, equalTo(FRENCH))
