
package team;

import java.util.Collection;
import javax.ejb.*;


/**
 * This is the local-home interface for Player enterprise bean.
 */
public interface PlayerLocalHome extends EJBLocalHome {
    
    PlayerLocal findByPrimaryKey(String key)  throws FinderException;
    
    public PlayerLocal create(String id, String name, String position, Double salary) throws CreateException;
    
    Collection findByName(String name) throws FinderException;
    
    Collection findByPosition(String position) throws FinderException;
    
    Collection findBySalary(Double salary) throws FinderException;
    
    Collection findAll() throws FinderException;
    
    Collection findByHigherSalary(String name) throws FinderException;
    
    Collection findByPositionAndName(String position, String name) throws FinderException;
    
    Collection findBySalaryRange(double low, double high) throws FinderException;
    
    Collection findBySport(String sport) throws FinderException;
    
    Collection findByTest(String param1, String param2, String param3) throws FinderException;
    
    Collection findNotOnTeam() throws FinderException;
    
    Collection findByCity(String city) throws FinderException;
    
    Collection findByLeague(LeagueLocal league) throws FinderException;
    
    
    
}
