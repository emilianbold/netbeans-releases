package org.netbeans.modules.editor.java;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
//import org.netbeans.modules.editor.java.JavaBracketCompletionUnitTest;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class JavaSmartBracketUnitTestSuite extends NbTestSuite {
      
    public JavaSmartBracketUnitTestSuite() {
        super("Java Smart Brackets");
        
        addTestSuite(JavaBracketCompletionUnitTest.class);
    }
    

    public static NbTestSuite suite() {
        return new JavaSmartBracketUnitTestSuite();
    }
    
}
