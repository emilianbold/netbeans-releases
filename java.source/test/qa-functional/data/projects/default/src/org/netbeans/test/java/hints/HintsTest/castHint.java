
package org.netbeans.test.java.hints.HintsTest;

import java.io.File;
import java.util.List;

public class castHint {

    String s = new Object();
        
    public castHint(List l) {
        File i = l.get(1);        
    }
    
    public void method(List<Number> nums) {
        Integer i = nums.get(1);
    }
    

}
