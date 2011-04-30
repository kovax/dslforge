
// ================= DSL =================

take chocolate
take tea with sugar
take tea with sugar and milk
take coffee with sugar, milk and liquor

// =======================================

println """You took:\n${allBreaks.collect { " - $it" }.join('\n')}"""

// out beverages and ingredients
enum Beverage { tea, coffee, chocolate }
enum Ingredient { sugar, milk, liquor }

// trick #1: you have to "import static" the enum values for using them as constants
// trick #2: in a script, you can actually put imports anywhere, not just at the beginning!

import static Beverage.*
import static Ingredient.*

// our holder for beverage and ingredients
class AfternoonBreak {
    Beverage beverage = null
    List<Ingredient> ingredients = []

    String toString() {
        "a $beverage ${ingredients ? "with ${ingredients.join(', ')}" : ''}"
    }
}

// our DSL method

def take(Beverage drink) {
// our current working beverage
    def ab = new AfternoonBreak()
    ab.beverage = drink
// trick #3: store all the drinks in the binding
    if (!binding.variables.allBreaks) allBreaks = []
    allBreaks << ab
// trick #4: do the method chaining with nested maps and closures
    return [with: { Ingredient... ings ->

        ab.ingredients.addAll ings

        return [and: { Ingredient ing ->
            ab.ingredients << ing
        }]
    }]
}