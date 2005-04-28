
package team;


/**
 * This is the business interface for League enterprise bean.
 */
public interface LeagueLocalBusiness {
    public abstract java.lang.String getLeagueId();

    public abstract void setLeagueId(java.lang.String id);

    public abstract java.lang.String getName();

    public abstract void setName(java.lang.String name);

    public abstract java.lang.String getSport();

    public abstract void setSport(java.lang.String sport);

    java.util.Collection getTeams();

    void setTeams(java.util.Collection teams);

    void addTeam(team.TeamLocal team);

    void dropTeam(team.TeamLocal team);
    
}
