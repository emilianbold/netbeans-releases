boolean test() {
    (1..3).any {println }

    [3,4,5].each {println i}

    (1..3).any {aa,ab -> println a}

    [3,4,5].each {x1,x2,x3 -> println x}

    def t1 = {println i}

    def t2 = {test1,test2,test3 -> println test}

    "TestString".eachLine {String line -> println i}

    "TestString".eachLine {String line -> println lin}
}