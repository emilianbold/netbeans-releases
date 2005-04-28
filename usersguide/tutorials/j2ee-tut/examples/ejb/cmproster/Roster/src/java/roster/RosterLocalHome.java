
package roster;


/**
 * This is the local-home interface for Roster enterprise bean.
 */
public interface RosterLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    roster.RosterLocal create()  throws javax.ejb.CreateException;
    
    
}
