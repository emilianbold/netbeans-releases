package org.netbeans.modules.editor;

import junit.framework.TestSuite;
import org.netbeans.editor.PlainDocumentCompatibilityRandomTest;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class RandomDocumentUnitTestSuite extends NbTestSuite {
      
    public RandomDocumentUnitTestSuite() {
        super("Document Implementation Randomized Tests");
        
        addTestSuite(PlainDocumentCompatibilityRandomTest.class);
    }
    

    public static NbTestSuite suite() {
        return new RandomDocumentUnitTestSuite();
    }
    
}
