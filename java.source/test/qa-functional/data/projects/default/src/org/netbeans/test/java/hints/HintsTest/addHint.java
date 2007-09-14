/* */
package org.netbeans.test.java.hints.HintsTest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class addHint {

    public addHint() {
        x = new String[]{"array"};
    }  
    
    /**
     *  test method
     * @param p1 
     * @param p2 
     */
    public void method(String p1, int p2) {
        a = 3;
        b = 3l;
        c = 'c';
        d = (byte) 2;
        e = 3.4;
        f = new Integer(1);
        g = new LinkedList<String>();                        
        h = "ssss";
        i = getMap();
    }    
    // test method 2    
    public void method2(double x, int ... y) {
        a = 3;                
    }
    
    
    public Map<String,List<String>> getMap() {
        
    }

    {        
        z = 2.3;
    }
            
}
