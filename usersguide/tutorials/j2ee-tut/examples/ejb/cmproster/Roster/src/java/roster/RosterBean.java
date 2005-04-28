package roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.*;
import javax.naming.NamingException;
import team.*;
import util.Debug;
import util.LeagueDetails;
import util.PlayerDetails;
import util.TeamDetails;

/**
 * This is the bean class for the RosterBean enterprise bean.
 * Created Mar 23, 2005 1:49:15 PM
 * @author honza
 */
public class RosterBean implements javax.ejb.SessionBean, roster.RosterLocalBusiness, roster.RosterRemoteBusiness {
    private javax.ejb.SessionContext context;
    private PlayerLocalHome playerHome = null;
    private TeamLocalHome teamHome = null;
    private LeagueLocalHome leagueHome = null;

    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context = aContext;
    }
    
    public void ejbCreate() throws CreateException {
        Debug.print("RosterBean ejbCreate");

        
            playerHome = lookupPlayerBean();
            teamHome = lookupTeamBean();
            leagueHome = lookupLeagueBean();
        
    }

    public void ejbActivate() {
        Debug.print("RosterBean ejbActivate");

        
            playerHome = lookupPlayerBean();
            teamHome = lookupTeamBean();
            leagueHome = lookupLeagueBean();
        
    }

    public void ejbPassivate() {
        playerHome = null;
        teamHome = null;
        leagueHome = null;
    }
    
    
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
 
    
    
       // Player business methods
    public ArrayList testFinder(String parm1, String parm2, String parm3) {
        Debug.print("RosterBean testFinder");

        Collection players = null;

        try {
            players = playerHome.findByTest(parm1, parm2, parm3);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }

    public void createPlayer(PlayerDetails details) {
        Debug.print("RosterBean createPlayer");

        try {
            PlayerLocal player = playerHome.create(details.getId(), details.getName(),
                    details.getPosition(), new Double(details.getSalary()));
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void addPlayer(String playerId, String teamId) {
        Debug.print("RosterBean addPlayer");

        try {
            TeamLocal team = teamHome.findByPrimaryKey(teamId);
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);

            team.addPlayer(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removePlayer(String playerId) {
        Debug.print("RosterBean removePlayer");

        try {
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);

            player.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropPlayer(String playerId, String teamId) {
        Debug.print("RosterBean dropPlayer");

        try {
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);
            TeamLocal team = teamHome.findByPrimaryKey(teamId);

            team.dropPlayer(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public PlayerDetails getPlayer(String playerId) {
        Debug.print("RosterBean getPlayer");

        PlayerDetails playerDetails = null;

        try {
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);

            playerDetails =
                new PlayerDetails(playerId, player.getName(),
                    player.getPosition(), player.getSalary().doubleValue());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return playerDetails;
    }
     // getPlayer

    public ArrayList getPlayersOfTeam(String teamId) {
        Debug.print("RosterBean getPlayersOfTeam");

        Collection players = null;

        try {
            TeamLocal team = teamHome.findByPrimaryKey(teamId);

            players = team.getPlayers();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersOfTeam

    public ArrayList getPlayersOfTeamCopy(String teamId) {
        Debug.print("RosterBean getPlayersOfTeamCopy");

        ArrayList playersList = null;

        try {
            TeamLocal team = teamHome.findByPrimaryKey(teamId);

            playersList = team.getCopyOfPlayers();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return playersList;
    }
     // getPlayersOfTeamCopy

    public ArrayList getTeamsOfLeague(String leagueId) {
        Debug.print("RosterBean getTeamsOfLeague");

        ArrayList detailsList = new ArrayList();
        Collection teams = null;

        try {
            LeagueLocal league = leagueHome.findByPrimaryKey(leagueId);

            teams = league.getTeams();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        Iterator i = teams.iterator();

        while (i.hasNext()) {
            TeamLocal team = (TeamLocal) i.next();
            TeamDetails details =
                new TeamDetails(team.getTeamId(), team.getName(), team.getCity());

            detailsList.add(details);
        }

        return detailsList;
    }
     // getTeamsOfLeague

    public ArrayList getPlayersByPosition(String position) {
        Debug.print("RosterBean getPlayersByPosition");

        Collection players = null;

        try {
            players = playerHome.findByPosition(position);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersByPosition

    public ArrayList getPlayersByHigherSalary(String name) {
        Debug.print("RosterBean getPlayersByByHigherSalary");

        Collection players = null;

        try {
            players = playerHome.findByHigherSalary(name);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersByHigherSalary

    public ArrayList getPlayersBySalaryRange(double low, double high) {
        Debug.print("RosterBean getPlayersBySalaryRange");

        Collection players = null;

        try {
            players = playerHome.findBySalaryRange(low, high);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersBySalaryRange

    public ArrayList getPlayersByLeagueId(String leagueId) {
        Debug.print("RosterBean getPlayersByLeagueId");

        Collection players = null;

        try {
            LeagueLocal league = leagueHome.findByPrimaryKey(leagueId);

            players = playerHome.findByLeague(league);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersByLeagueId

    public ArrayList getPlayersBySport(String sport) {
        Debug.print("RosterBean getPlayersBySport");

        Collection players = null;

        try {
            players = playerHome.findBySport(sport);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersBySport

    public ArrayList getPlayersByCity(String city) {
        Debug.print("RosterBean getPlayersByCity");

        Collection players = null;

        try {
            players = playerHome.findByCity(city);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersByCity

    public ArrayList getAllPlayers() {
        Debug.print("RosterBean getAllPlayers");

        Collection players = null;

        try {
            players = playerHome.findAll();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getAllPlayers

    public ArrayList getPlayersNotOnTeam() {
        Debug.print("RosterBean getPlayersNotOnTeam");

        Collection players = null;

        try {
            players = playerHome.findNotOnTeam();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersNotOnTeam

    public ArrayList getPlayersByPositionAndName(String position, String name) {
        Debug.print("RosterBean getPlayersByPositionAndName");

        Collection players = null;

        try {
            players = playerHome.findByPositionAndName(position, name);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }
     // getPlayersByPositionAndName

    public ArrayList getLeaguesOfPlayer(String playerId) {
        Debug.print("RosterBean getLeaguesOfPlayer");

        ArrayList detailsList = new ArrayList();
        Collection leagues = null;

        try {
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);

            leagues = player.getLeagues();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        Iterator i = leagues.iterator();

        while (i.hasNext()) {
            LeagueLocal league = (LeagueLocal) i.next();
            LeagueDetails details =
                new LeagueDetails(league.getLeagueId(), league.getName(),
                    league.getSport());

            detailsList.add(details);
        }

        return detailsList;
    }
     // getLeaguesOfPlayer

    public ArrayList getSportsOfPlayer(String playerId) {
        Debug.print("RosterBean getSportsOfPlayer");

        ArrayList sportsList = new ArrayList();
        Collection sports = null;

        try {
            PlayerLocal player = playerHome.findByPrimaryKey(playerId);

            sports = player.getSports();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        Iterator i = sports.iterator();

        while (i.hasNext()) {
            String sport = (String) i.next();

            sportsList.add(sport);
        }

        return sportsList;
    }
     // getSportsOfPlayer

    // Team business methods
    public void createTeamInLeague(TeamDetails details, String leagueId) {
        Debug.print("RosterBean createTeamInLeague");

        try {
            LeagueLocal league = leagueHome.findByPrimaryKey(leagueId);
            TeamLocal team = teamHome.create(details.getId(), details.getName(), details.getCity());

            league.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removeTeam(String teamId) {
        Debug.print("RosterBean removeTeam");

        try {
            TeamLocal team = teamHome.findByPrimaryKey(teamId);

            team.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public TeamDetails getTeam(String teamId) {
        Debug.print("RosterBean getTeam");

        TeamDetails teamDetails = null;

        try {
            TeamLocal team = teamHome.findByPrimaryKey(teamId);

            teamDetails =
                new TeamDetails(teamId, team.getName(), team.getCity());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return teamDetails;
    }

    // League business methods
    public void createLeague(LeagueDetails details) {
        Debug.print("RosterBean createLeague");

        try {
            LeagueLocal league =
                leagueHome.create(details.getId(), details.getName(),
                    details.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removeLeague(String leagueId) {
        Debug.print("RosterBean removeLeague");

        try {
            LeagueLocal league = leagueHome.findByPrimaryKey(leagueId);

            league.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public LeagueDetails getLeague(String leagueId) {
        Debug.print("RosterBean getLeague");

        LeagueDetails leagueDetails = null;

        try {
            LeagueLocal league = leagueHome.findByPrimaryKey(leagueId);

            leagueDetails =
                new LeagueDetails(leagueId, league.getName(), league.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return leagueDetails;
    }

 private ArrayList copyPlayersToDetails(Collection players) {
        ArrayList detailsList = new ArrayList();
        Iterator i = players.iterator();

        while (i.hasNext()) {
            PlayerLocal player = (PlayerLocal) i.next();
            PlayerDetails details =
                new PlayerDetails(player.getPlayerId(), player.getName(),
                    player.getPosition(), player.getSalary().doubleValue());

            detailsList.add(details);
        }

        return detailsList;
    }
    
    
    // Enter business methods below. (Right-click in editor and choose
    // EJB Methods > Add Business Method)

    private team.LeagueLocalHome lookupLeagueBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            team.LeagueLocalHome rv = (team.LeagueLocalHome) c.lookup("java:comp/env/ejb/LeagueBean");
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private team.PlayerLocalHome lookupPlayerBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            team.PlayerLocalHome rv = (team.PlayerLocalHome) c.lookup("java:comp/env/ejb/PlayerBean");
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private team.TeamLocalHome lookupTeamBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            team.TeamLocalHome rv = (team.TeamLocalHome) c.lookup("java:comp/env/ejb/TeamBean");
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    
    
}
