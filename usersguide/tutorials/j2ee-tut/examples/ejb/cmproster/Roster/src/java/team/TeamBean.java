package team;

import javax.ejb.*;
import util.*;
import java.util.*;

/**
 * This is the bean class for the TeamBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class TeamBean implements javax.ejb.EntityBean, team.TeamLocalBusiness {
    private javax.ejb.EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    // <editor-fold desc="CMP fields and relationships.">
    
    public abstract java.lang.String getTeamId();
    public abstract void setTeamId(java.lang.String id);
    
    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    public abstract java.lang.String getCity();
    public abstract void setCity(java.lang.String city);
    
    // </editor-fold>
    
    public java.lang.String ejbCreate(java.lang.String teamId, java.lang.String name, java.lang.String city)  throws javax.ejb.CreateException {
        if (teamId == null) {
            throw new javax.ejb.CreateException("The field \"id\" must not be null");
        }
       // if (leagueId == null) {
       //     throw new javax.ejb.CreateException("The field \"leagueId\" must not be null");
        //}
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setTeamId(teamId);
        setName(name);
        setCity(city);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String teamId, java.lang.String name, java.lang.String city) {
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

    public abstract java.util.Collection getPlayers();

    public abstract void setPlayers(java.util.Collection players);

    public abstract team.LeagueLocal getLeague();

    public abstract void setLeague(team.LeagueLocal league);



    
    
}
