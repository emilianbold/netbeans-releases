package org.netbeans.modules.editor;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
//import org.netbeans.modules.editor.java.JavaFormatterUnitTest;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class AnnotationsUnitTestSuite extends NbTestSuite {
      
    public AnnotationsUnitTestSuite() {
        super("Annotations Unit Tests");
        
        addTestSuite(AnnotationsTest.class);
    }
    

    public static NbTestSuite suite() {
        return new AnnotationsUnitTestSuite();
    }
    
}
