import java.util.List;

abstract class Delegate {
    def addDelegateMethods(List methods) {
        methods.each { method ->
            this.metaClass."$method" = { String desc ->
                println "1. $method $desc"
                return [:].withDefault { key -> return { cl -> return cl() } }
            }

            this.metaClass."$method" = { String desc, Closure cl ->
                println "2. $method $desc"
                return cl()
            }
        }
    }
}

class Scenario extends Delegate {
    def bddMethods = ["given", "when", "then", "and", "but"]
}


class Feature extends Delegate {
    def bddMethods = ["in_order", "as_a", "i_want"]
}


def methodMissing(String name, args) {
    println "methodMissing: $name $args"

    Closure cl = args[args.length-1]
    if (name == "scenario") {
        cl.delegate = new Scenario()
    }
    else {
        cl.delegate = new Feature()
    }
    cl.delegate.addDelegateMethods(cl.delegate.bddMethods)
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    return cl()
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