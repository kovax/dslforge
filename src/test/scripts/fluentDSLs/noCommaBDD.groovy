def bdd = 0

def scenario = { String desc ->
    println desc;
    return [body: { cl -> cl() }]
}

def given = { String desc ->
    println desc;
    return [body: { cl -> cl() }]
}

/*
def then = { String desc ->
    println desc;
    return [body: { cl -> cl() }]
}
*/

def when = { String desc ->
    println desc;
    return [body: { cl -> cl() }]
}

def then = { String desc, Closure cl ->
    println desc;
    cl()
}

scenario "do some dummy stuff" body {

    given "a dummy context" body {
        bdd++
    }

    when "a dummy event" body {
        bdd++
    }

    then "ensure it remains dummy", {
        bdd++
    }

}

assert bdd == 3

