package org.netbeans.test.editor.suites.focus.data.testfiles.FocusTestPerformer;

import java.util.Arrays;

/*
 * SysProps.java
 * a1
 * Created on February 3, 2003, 2:26 PM
 */

/**
 * a2
 * @author  eh103527
 */
public class Test2 {
    
    /** Creates a new instance of SysProps */
    public Test2() {  //a3
    }
    
    /**
     * a4
     * @param args the command line arguments
     */
    public static void main(String[] args) {  //a5
        Object keys[]=System.getProperties().keySet().toArray(); //a6
        Arrays.sort(keys); //a7
        for (int i=0;i < keys.length;i++) {  //a8
            System.out.println(keys[i]+"="+System.getProperty((String)keys[i]));  //a9
        }
    }
    
}
