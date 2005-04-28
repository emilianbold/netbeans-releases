
package team;


/**
 * This is the local-home interface for Team enterprise bean.
 */
public interface TeamLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    team.TeamLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public team.TeamLocal create(java.lang.String id, java.lang.String name, java.lang.String city) throws javax.ejb.CreateException;

    java.util.Collection findByName(java.lang.String name) throws javax.ejb.FinderException;

    java.util.Collection findByCity(java.lang.String city) throws javax.ejb.FinderException;
    
    
}
