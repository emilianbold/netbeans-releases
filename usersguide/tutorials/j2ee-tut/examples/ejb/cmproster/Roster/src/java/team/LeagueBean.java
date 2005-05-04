package team;

import java.util.Collection;
import javax.ejb.*;
import util.Debug;

/**
 * This is the bean class for the LeagueBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class LeagueBean implements EntityBean, LeagueLocalBusiness {
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
    
    public abstract String getLeagueId();
    public abstract void setLeagueId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);
    
    public abstract String getSport();
    public abstract void setSport(String sport);
    
    // </editor-fold>
    
    public String ejbCreate(String leagueId, String name, String sport)  throws CreateException {
        if (leagueId == null) {
            throw new CreateException("The field \"id\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setLeagueId(leagueId);
        setName(name);
        setSport(sport);
        
        return null;
    }
    
    public void ejbPostCreate(String leagueId, String name, String sport) {
        // TODO populate relationships here if appropriate
        
    }

    public abstract Collection getTeams();

    public abstract void setTeams(Collection teams);
    
        // Business methods
    public void addTeam(TeamLocal team) {
        Debug.print("TeamBean addTeam");

        try {
            Collection teams = getTeams();

            teams.add(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropTeam(TeamLocal team) {
        Debug.print("TeamBean dropTeam");

        try {
            Collection teams = getTeams();

            teams.remove(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
    
}
