
package team;

import java.util.Collection;
import javax.ejb.FinderException;


/**
 * This is the business interface for Player enterprise bean.
 */
public interface PlayerLocalBusiness {
    
    public abstract String getPlayerId();

    public abstract void setPlayerId(String id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getPosition();

    public abstract void setPosition(String position);

    public abstract Double getSalary();

    public abstract void setSalary(Double salary);

    Collection getTeams();

    void setTeams(Collection teams);

    Collection getLeagues() throws FinderException;

    Collection getSports() throws FinderException;
    
}
