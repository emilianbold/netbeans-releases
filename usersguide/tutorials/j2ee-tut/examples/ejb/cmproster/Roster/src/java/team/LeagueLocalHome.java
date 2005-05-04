
package team;

import javax.ejb.*;


/**
 * This is the local-home interface for League enterprise bean.
 */
public interface LeagueLocalHome extends EJBLocalHome {
    
    team.LeagueLocal findByPrimaryKey(String key)  throws FinderException;

    public team.LeagueLocal create(String id, String name, String sport) throws CreateException;

    java.util.Collection findByName(String name) throws FinderException;

    java.util.Collection findBySport(String sport) throws FinderException;
    
    
}
