package org.netbeans.modules.editor.java;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class JavaFormatterUnitTestSuite extends NbTestSuite {
      
    public JavaFormatterUnitTestSuite() {
        super("Java Formatter");
        
        addTestSuite(JavaFormatterUnitTest.class);
    }
    

    public static NbTestSuite suite() {
        return new JavaFormatterUnitTestSuite();
    }
    
}
