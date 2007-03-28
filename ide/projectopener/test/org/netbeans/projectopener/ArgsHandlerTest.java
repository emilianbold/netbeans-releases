/*
 * ArgsHandlerTest.java
 * JUnit based test
 *
 * Created on February 9, 2007, 4:16 PM
 */

package org.netbeans.projectopener;

import junit.framework.TestCase;
import junit.framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Milan Kubec
 */
public class ArgsHandlerTest extends TestCase {
    
    public ArgsHandlerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of getArgValue method, of class org.netbeans.projectopener.ArgsHandler.
     */
    public void testGetArgValue() {
        
        System.out.println("getArgValue");
        
        String prjURLString = "http://www.someurl.com/TheProject.zip";
        String nbVersionString = "1.2.3";
        String mainPrjNameString = "TheMainProject";
        String args[] = new String[] { "-projecturl", prjURLString, 
                                       "-minversion", nbVersionString, 
                                       "-mainproject", mainPrjNameString, 
                                       "-showgui", "-otherarg" };
        List<String> list = new ArrayList<String>();
        list.add("showgui");
        list.add("otherarg");
        
        ArgsHandler handler = new ArgsHandler(args);
        assertEquals(prjURLString, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(mainPrjNameString, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
        String args2[] = new String[] { "-projecturl", 
                                        "-minversion", nbVersionString, 
                                        "-showgui" };
        list.clear();
        list.add("showgui");
        
        handler = new ArgsHandler(args2);
        assertEquals(null, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(null, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
    }

    /**
     * Test of getAdditionalArgs method, of class org.netbeans.projectopener.ArgsHandler.
     */
    // public void testGetAdditionalArgs() { }
    
}
