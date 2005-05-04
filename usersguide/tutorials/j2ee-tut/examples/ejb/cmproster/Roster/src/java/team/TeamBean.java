package team;

import javax.ejb.*;
import util.*;
import java.util.*;

/**
 * This is the bean class for the TeamBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class TeamBean implements EntityBean, TeamLocalBusiness {
    private EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    // <editor-fold desc="CMP fields and relationships.">
    
    public abstract String getTeamId();
    public abstract void setTeamId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);
    
    public abstract String getCity();
    public abstract void setCity(String city);
    
    // </editor-fold>
    
    public String ejbCreate(String teamId, String name, String city)  throws CreateException {
        if (teamId == null) {
            throw new CreateException("The field \"id\" must not be null");
        }
       // if (leagueId == null) {
       //     throw new CreateException("The field \"leagueId\" must not be null");
        //}
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setTeamId(teamId);
        setName(name);
        setCity(city);
        
        return null;
    }
    
    public void ejbPostCreate(String teamId, String name, String city) {
        // TODO populate relationships here if appropriate
        //setLeagueId(leagueId);
        
    }
    // Business methods
    public ArrayList getCopyOfPlayers() {
        Debug.print("TeamBean getCopyOfPlayers");

        ArrayList playerList = new ArrayList();
        Collection players = getPlayers();

        Iterator i = players.iterator();

        while (i.hasNext()) {
            PlayerLocal player = (PlayerLocal) i.next();
            PlayerDetails details =
                new PlayerDetails(player.getPlayerId(), player.getName(), player.getPosition(), 0.0);

            playerList.add(details);
        }

        return playerList;
    }

    public void addPlayer(PlayerLocal player) {
        Debug.print("TeamBean addPlayer");

        try {
            Collection players = getPlayers();

            players.add(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropPlayer(PlayerLocal player) {
        Debug.print("TeamBean dropPlayer");

        try {
            Collection players = getPlayers();

            players.remove(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public abstract Collection getPlayers();

    public abstract void setPlayers(Collection players);

    public abstract LeagueLocal getLeague();

    public abstract void setLeague(LeagueLocal league);



    
    
}
