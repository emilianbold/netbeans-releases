package lib;

import org.netbeans.junit.NbTestSuite;

/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
public class DummyTestSuite extends NbTestSuite {
    
    public DummyTestSuite() {
        super("Dummy Test (Project opening only)");
        
        addTestSuite(DummyTest.class);
    }
    
    
    public static NbTestSuite suite() {
        return new DummyTestSuite();
    }
    
}
