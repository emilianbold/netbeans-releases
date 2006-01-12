
package ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;


/**
 * This is the local-home interface for TestSession enterprise bean.
 */
public interface TestSessionLocalHome extends EJBLocalHome {
    
    TestSessionLocal create()  throws CreateException;
    
    
}
