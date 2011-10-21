/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classinfo;

import java.io.Serializable;
import java.util.ArrayList;
import sun.org.mozilla.javascript.Callable;
import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.Scriptable;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassInfoTest implements Serializable, Cloneable {
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
    
    public static class StaticInner extends ClassInfoTest {
        public void doit() {
            
        }
    }
    
    public class Inner {
        public void doit() {
            
        }
    }
    
    public static class CallableTest implements Callable {
        public Object call(Context cntxt, Scriptable s, Scriptable s1, Object[] os) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
