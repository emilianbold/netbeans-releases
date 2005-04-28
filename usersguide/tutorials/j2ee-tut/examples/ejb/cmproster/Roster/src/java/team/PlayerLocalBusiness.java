
package team;

import java.util.Collection;
import javax.ejb.FinderException;


/**
 * This is the business interface for Player enterprise bean.
 */
public interface PlayerLocalBusiness {
    public abstract java.lang.String getPlayerId();

    public abstract void setPlayerId(java.lang.String id);

    public abstract java.lang.String getName();

    public abstract void setName(java.lang.String name);

    public abstract java.lang.String getPosition();

    public abstract void setPosition(java.lang.String position);

    public abstract java.lang.Double getSalary();

    public abstract void setSalary(java.lang.Double salary);

    java.util.Collection getTeams();

    void setTeams(java.util.Collection teams);

    Collection getLeagues() throws FinderException;

    Collection getSports() throws FinderException;
    
}
