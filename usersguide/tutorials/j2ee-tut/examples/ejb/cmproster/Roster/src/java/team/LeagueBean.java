package team;

import java.util.Collection;
import javax.ejb.*;
import util.Debug;

/**
 * This is the bean class for the LeagueBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class LeagueBean implements javax.ejb.EntityBean, team.LeagueLocalBusiness {
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
    
    public abstract java.lang.String getLeagueId();
    public abstract void setLeagueId(java.lang.String id);
    
    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    public abstract java.lang.String getSport();
    public abstract void setSport(java.lang.String sport);
    
    // </editor-fold>
    
    public java.lang.String ejbCreate(java.lang.String leagueId, java.lang.String name, java.lang.String sport)  throws javax.ejb.CreateException {
        if (leagueId == null) {
            throw new javax.ejb.CreateException("The field \"id\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setLeagueId(leagueId);
        setName(name);
        setSport(sport);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String leagueId, java.lang.String name, java.lang.String sport) {
        // TODO populate relationships here if appropriate
        
    }

    public abstract java.util.Collection getTeams();

    public abstract void setTeams(java.util.Collection teams);
    
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
