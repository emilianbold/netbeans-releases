package org.netbeans.modules.editor;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.java.JavaBracketCompletionUnitTest;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class SmartBracketUnitTestSuite extends NbTestSuite {
      
    public SmartBracketUnitTestSuite() {
        super("Smart Brackets");
        
        addTestSuite(JavaBracketCompletionUnitTest.class);
    }
    

    public static NbTestSuite suite() {
        return new SmartBracketUnitTestSuite();
    }
    
}
