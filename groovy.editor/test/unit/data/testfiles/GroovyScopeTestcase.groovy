/*
   This is to demonstrate missing OccurencyFinder features:
   
 1) method usage (method1()      : MISSING
 2) Class usage (TestCase)       : MISSING
 3) Local vars (localvar[1|2])   : MISSING
 4) Member vars (membervar1)     : OK
 5) Parameter (param1)           : ERROR, shifted
 */

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
    
    def method2(){
        new TestCase()
    }
    
}