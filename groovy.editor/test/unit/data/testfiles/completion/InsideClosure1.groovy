boolean test() {
    (1..3).any {println }

    [3,4,5].each {println }

    (1..3).any {a,b -> println }

    [3,4,5].each {x,y,z -> println }

    def t1 = {println }

    def t2 = {x,y -> println }

    "TestString".eachLine {String line -> println }

}