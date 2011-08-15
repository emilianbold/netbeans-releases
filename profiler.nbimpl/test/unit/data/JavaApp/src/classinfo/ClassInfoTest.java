/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classinfo;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassInfoTest {
    private Runnable anonymous = new Runnable() {

        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    public ClassInfoTest() {
        
    }
    
    private ClassInfoTest(int x) {
        
    }
    
    public static void staticMethod() {
        
    }
    
    public int retMethod() {
        return 10;
    }
    
    private int calc(int x, int y) {
        return x+y;
    }
    
    public static class StaticInner {
        public void doit() {
            
        }
    }
    
    public class Inner {
        public void doit() {
            
        }
    }
}
