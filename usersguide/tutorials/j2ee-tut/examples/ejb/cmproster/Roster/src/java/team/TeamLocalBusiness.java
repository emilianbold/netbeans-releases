
package team;

import java.util.ArrayList;


/**
 * This is the business interface for Team enterprise bean.
 */
public interface TeamLocalBusiness {
    public abstract java.lang.String getTeamId();

    public abstract void setTeamId(java.lang.String id);

    public abstract java.lang.String getName();

    public abstract void setName(java.lang.String name);

    public abstract java.lang.String getCity();

    public abstract void setCity(java.lang.String city);

    void dropPlayer(PlayerLocal player);

    java.util.Collection getPlayers();

    void setPlayers(java.util.Collection players);

    void setLeague(team.LeagueLocal league);

    team.LeagueLocal getLeague();

    void addPlayer(team.PlayerLocal player);

    ArrayList getCopyOfPlayers();
    
}
