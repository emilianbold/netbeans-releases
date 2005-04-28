
package roster;

import java.util.ArrayList;
import util.LeagueDetails;
import util.PlayerDetails;
import util.TeamDetails;


/**
 * This is the business interface for RosterBean enterprise bean.
 */
public interface RosterRemoteBusiness {
    LeagueDetails getLeague(java.lang.String leagueId) throws java.rmi.RemoteException;

    void removeLeague(java.lang.String leagueId) throws java.rmi.RemoteException;

    void createLeague(util.LeagueDetails details) throws java.rmi.RemoteException;

    TeamDetails getTeam(java.lang.String teamId) throws java.rmi.RemoteException;

    void removeTeam(java.lang.String teamId) throws java.rmi.RemoteException;

    void createTeamInLeague(util.TeamDetails details, java.lang.String leagueId) throws java.rmi.RemoteException;

    ArrayList getSportsOfPlayer(java.lang.String playerId) throws java.rmi.RemoteException;

    ArrayList getLeaguesOfPlayer(java.lang.String playerId) throws java.rmi.RemoteException;

    ArrayList getPlayersByPositionAndName(java.lang.String position, java.lang.String name) throws java.rmi.RemoteException;

    ArrayList getPlayersNotOnTeam() throws java.rmi.RemoteException;

    ArrayList getAllPlayers() throws java.rmi.RemoteException;

    ArrayList getPlayersByCity(java.lang.String city) throws java.rmi.RemoteException;

    ArrayList getPlayersBySport(java.lang.String sport) throws java.rmi.RemoteException;

    ArrayList getPlayersByLeagueId(java.lang.String leagueId) throws java.rmi.RemoteException;

    ArrayList getPlayersBySalaryRange(double low, double high) throws java.rmi.RemoteException;

    ArrayList getPlayersByHigherSalary(java.lang.String name) throws java.rmi.RemoteException;

    ArrayList getPlayersByPosition(java.lang.String position) throws java.rmi.RemoteException;

    ArrayList getTeamsOfLeague(java.lang.String leagueId) throws java.rmi.RemoteException;

    ArrayList getPlayersOfTeam(java.lang.String teamId) throws java.rmi.RemoteException;

    PlayerDetails getPlayer(java.lang.String playerId) throws java.rmi.RemoteException;

    void dropPlayer(java.lang.String playerId, java.lang.String teamId) throws java.rmi.RemoteException;

    void removePlayer(java.lang.String playerId) throws java.rmi.RemoteException;

    void addPlayer(java.lang.String playerId, java.lang.String teamId) throws java.rmi.RemoteException;

    void createPlayer(util.PlayerDetails details) throws java.rmi.RemoteException;

    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    ArrayList testFinder(java.lang.String parm1, java.lang.String parm2, java.lang.String parm3) throws java.rmi.RemoteException;

    ArrayList getPlayersOfTeamCopy(java.lang.String teamId) throws java.rmi.RemoteException;
    
}
