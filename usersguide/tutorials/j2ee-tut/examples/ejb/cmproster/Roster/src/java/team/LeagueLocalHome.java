
package team;


/**
 * This is the local-home interface for League enterprise bean.
 */
public interface LeagueLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    team.LeagueLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public team.LeagueLocal create(java.lang.String id, java.lang.String name, java.lang.String sport) throws javax.ejb.CreateException;

    java.util.Collection findByName(java.lang.String name) throws javax.ejb.FinderException;

    java.util.Collection findBySport(java.lang.String sport) throws javax.ejb.FinderException;
    
    
}
