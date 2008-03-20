println "Starting testcase"
new TestCase().method1(1)

class TestCase {
    int membervar1 = 2
    
    def method1 (int param1){
        int localvar1 = 3
        int localvar2 = 4
        
        def localvar3 = membervar1 + param1 + localvar1 + localvar2
        println "Result: " + localvar3
    }
    
}