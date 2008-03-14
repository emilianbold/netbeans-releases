package foo;

class Hello1 {

    int field1 = 1;
    def name = 'World'

    Hello1(int inputval) {
	field1 = inputval
    }

    static void main(args) {
        String s = 'aaa'
        println 'Hello, world'
    }

    void dynamicmethod() {
        field1 = 2
        this.field1 = 77
    }

    def greeting = {
        println "Hello, ${name}!"
    }

}

class  SecondTestClass {

    SecondTestClass (int f) {
    }

    SecondTestClass (String str) {
    }
}

class ThirdTestClass {

    ThirdTestClass (int f) {
    }

    ThirdTestClass (String str) {
    }
}

Hello hello = new Hello()
hello.field1 = 9

println "End."

