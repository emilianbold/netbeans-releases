
package team;

import java.util.Collection;


/**
 * This is the business interface for League enterprise bean.
 */
public interface LeagueLocalBusiness {
    public abstract String getLeagueId();

    public abstract void setLeagueId(String id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getSport();

    public abstract void setSport(String sport);

    Collection getTeams();

    void setTeams(Collection teams);

    void addTeam(TeamLocal team);

    void dropTeam(TeamLocal team);
    
}
