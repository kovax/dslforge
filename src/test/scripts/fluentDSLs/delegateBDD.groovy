class Scenario {
    def bddMethods = ["given", "when", "then", "and", "but"]

    def methodMissing(String name, args) {
        if (bddMethods.contains(name)) {
            println "S $name $args"
            if (args.length == 1) {
                if(args[0] instanceof Closure) {
                    return args[0]()
                }
                else {
                    return [:].withDefault { key -> { cl -> cl() } }
                }
            }
            else if (args.length == 2) {
                return args[1]()
            }
        }

        throw new MissingMethodException(name, this.class, args)
    }
}


class Feature {
    def bddMethods = ["in_order", "as_a", "i_want"]

    def methodMissing(String name, args) {
        println "F $name $args"

        if (bddMethods.contains(name)) {
            if (args.length == 1) {
                return [:].withDefault { key -> { cl -> cl() } }
            }
            else if (args.length == 2) {
                return args[1]()
            }
        }
        else {
            Closure cl
            if( name == "scenario" ) {
                cl = args[1]
            }
            else {
                //enable Spock like methods
                println "F assume it is scenario: $name"
                cl = args[0]
            }

            cl.delegate = new Scenario()
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            return cl()
        }

        throw new MissingMethodException(name, this.class, args)
    }
}


def methodMissing(String name, args) {
    println "$name $args"

    if (name == "feature") {
        Closure cl = args[1]
        cl.delegate = new Feature()
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        return cl()
    }

    throw new MissingMethodException(name, this.class, args)
}


feature "The Simplest BDD implementation ever", {

    def bdd = 0

    in_order "to provide better SW quality"
    as_a "Test Automation Engineer"
    i_want "to use BDD"

    scenario "do some dummy stuff", {

        given "a dummy context" body {
            bdd++
        }

        when "a dummy event" actions {
            bdd++
        }

        and "another dummy event"

        and {
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

    "do the dummiest stuff ever" {

        given {
            bdd++
        }
        when {
            bdd++
        }
        then {
            bdd++
        }
    }

    assert bdd == 10
}