package org.netbeans.modules.editor;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
//import org.netbeans.modules.editor.java.JavaFormatterUnitTest;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class DocumentUnitTestSuite extends NbTestSuite {
      
    public DocumentUnitTestSuite() {
        super("Document Unit Tests");
        
        addTestSuite(DocumentUndoTest.class);
    }
    

    public static NbTestSuite suite() {
        return new DocumentUnitTestSuite();
    }
    
}
