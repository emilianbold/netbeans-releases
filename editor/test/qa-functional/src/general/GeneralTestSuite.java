package general;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
  public class GeneralTestSuite extends NbTestSuite {
      
    public GeneralTestSuite() {
        super("General Editing Tests");
        
        addTestSuite(GeneralTypingTest.class);
    }
    

    public static NbTestSuite suite() {
        return new GeneralTestSuite();
    }
    
}
