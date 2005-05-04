
package team;

import java.util.ArrayList;
import java.util.Collection;


/**
 * This is the business interface for Team enterprise bean.
 */
public interface TeamLocalBusiness {
    
    public abstract String getTeamId();

    public abstract void setTeamId(String id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getCity();

    public abstract void setCity(String city);

    void dropPlayer(PlayerLocal player);

    Collection getPlayers();

    void setPlayers(Collection players);

    void setLeague(LeagueLocal league);

    LeagueLocal getLeague();

    void addPlayer(PlayerLocal player);

    ArrayList getCopyOfPlayers();
    
}
