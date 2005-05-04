
package team;

import java.util.Collection;
import javax.ejb.*;


/**
 * This is the local-home interface for Team enterprise bean.
 */
public interface TeamLocalHome extends EJBLocalHome {

    public TeamLocal create(String id, String name, String city) throws CreateException;

    TeamLocal findByPrimaryKey(String key)  throws FinderException;

    Collection findByName(String name) throws FinderException;

    Collection findByCity(String city) throws FinderException;
    
}
