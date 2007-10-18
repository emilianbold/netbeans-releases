package pulluppkg;

import java.io.Serializable;

public class PullUpClass extends PullUpSuperClass implements Serializable {
     
    public String field;
    
    public void method() {
        //method body
        System.out.println("Hello");
    }
    
    public class InnerClass {
        //class body
        public void method() {
            
        }
    }

    
    public void existing() {
        
    }
    
    private void localyReferenced() {
        
    }
    
    private void reference() {
        localyReferenced();
    }
}
