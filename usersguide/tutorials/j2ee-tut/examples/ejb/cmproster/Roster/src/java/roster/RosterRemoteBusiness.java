
package roster;

import java.rmi.RemoteException;
import java.util.ArrayList;
import util.LeagueDetails;
import util.PlayerDetails;
import util.TeamDetails;


/**
 * This is the business interface for RosterBean enterprise bean.
 */
public interface RosterRemoteBusiness {
    LeagueDetails getLeague(String leagueId) throws RemoteException;

    void removeLeague(String leagueId) throws RemoteException;

    void createLeague(LeagueDetails details) throws RemoteException;

    TeamDetails getTeam(String teamId) throws RemoteException;

    void removeTeam(String teamId) throws RemoteException;

    void createTeamInLeague(TeamDetails details, String leagueId) throws RemoteException;

    ArrayList getSportsOfPlayer(String playerId) throws RemoteException;

    ArrayList getLeaguesOfPlayer(String playerId) throws RemoteException;

    ArrayList getPlayersByPositionAndName(String position, String name) throws RemoteException;

    ArrayList getPlayersNotOnTeam() throws RemoteException;

    ArrayList getAllPlayers() throws RemoteException;

    ArrayList getPlayersByCity(String city) throws RemoteException;

    ArrayList getPlayersBySport(String sport) throws RemoteException;

    ArrayList getPlayersByLeagueId(String leagueId) throws RemoteException;

    ArrayList getPlayersBySalaryRange(double low, double high) throws RemoteException;

    ArrayList getPlayersByHigherSalary(String name) throws RemoteException;

    ArrayList getPlayersByPosition(String position) throws RemoteException;

    ArrayList getTeamsOfLeague(String leagueId) throws RemoteException;

    ArrayList getPlayersOfTeam(String teamId) throws RemoteException;

    PlayerDetails getPlayer(String playerId) throws RemoteException;

    void dropPlayer(String playerId, String teamId) throws RemoteException;

    void removePlayer(String playerId) throws RemoteException;

    void addPlayer(String playerId, String teamId) throws RemoteException;

    void createPlayer(PlayerDetails details) throws RemoteException;

    ArrayList testFinder(String parm1, String parm2, String parm3) throws RemoteException;

    ArrayList getPlayersOfTeamCopy(String teamId) throws RemoteException;
    
}
