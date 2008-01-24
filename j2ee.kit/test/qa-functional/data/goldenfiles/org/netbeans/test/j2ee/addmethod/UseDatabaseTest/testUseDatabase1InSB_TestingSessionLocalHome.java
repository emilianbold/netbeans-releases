
package test;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;


/**
 * This is the local-home interface for TestingSession enterprise bean.
 */
public interface TestingSessionLocalHome extends EJBLocalHome {
    
    
    
    /**
     *
     */
    TestingSessionLocal create()  throws CreateException;
    
    
}
