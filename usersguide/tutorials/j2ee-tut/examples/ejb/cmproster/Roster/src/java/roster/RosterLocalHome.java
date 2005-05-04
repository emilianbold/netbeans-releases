
package roster;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;


/**
 * This is the local-home interface for Roster enterprise bean.
 */
public interface RosterLocalHome extends EJBLocalHome {
    
    RosterLocal create()  throws CreateException;
    
}
