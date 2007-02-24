
/*
 * File       : AllConfigStringFrameworkTests.java
 * Created on : Nov 3, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.configstringframework;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Aztec
 */

public class AllConfigStringFrameworkTests
{

    public static Test suite()
    {
       TestSuite suite = new TestSuite("ConfigStringFramework Tests");
      
       //$JUnit-BEGIN$
       suite.addTest(new TestSuite(ConfigStringTranslatorTestCase.class));
       suite.addTest(new TestSuite(ConfigStringHelperTestCase.class));
       //$JUnit-END$
       return suite;
    }
    
    public static void main(String args[]) 
    {
        junit.textui.TestRunner.run(suite());
    }

}

