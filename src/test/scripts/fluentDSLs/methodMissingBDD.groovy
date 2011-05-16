def methodMissing(String name, args) {
    def bddMethods = ["feature","in_order","as_a","i_want","scenario", "given", "when", "then", "and", "but"]

    if (bddMethods.contains(name)) {
        print "$name ${args[0]}"

        if (args.length == 1) {
            println " (map)"
            //Makes possible to use any word for calling a closure
            return [:].withDefault { key -> { cl -> cl() } }
        }
        else if (args.length == 2) {
            println " (call)"
            return args[1]()
        }
        else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    throw new MissingMethodException(name, this.class, args)
}


feature "The Simplest BDD implementation ever" body {

    def bdd = 0

    in_order "to provide better SW quality"
    as_a "Test Automation Engineer"
    i_want "to use BDD"

    scenario "do some dummy stuff" body {

        given "a dummy context" body {
            bdd++
        }

        when "a dummy event" actions {
            bdd++
        }

        and "another dummy event"

        and { //interesting that this works
            bdd++
        }

        then "ensure it remains dummy" actions {
            bdd++
        }

        but "it shall stay groovy"
    }

    assert bdd == 4

    scenario "do some dummy stuff with comma", {

        given "a dummy context", {
            bdd++
        }

        when "a dummy event", {
            bdd++
        }

        then "ensure it remains dummy", {
            bdd++
        }
    }

    assert bdd == 7

}