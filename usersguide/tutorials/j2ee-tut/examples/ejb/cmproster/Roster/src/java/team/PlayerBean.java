package team;
import java.util.Collection;
import javax.ejb.FinderException;

/**
 * This is the bean class for the PlayerBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class PlayerBean implements javax.ejb.EntityBean, team.PlayerLocalBusiness {
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
    
    public abstract java.lang.String getPlayerId();
    public abstract void setPlayerId(java.lang.String id);
    
    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    public abstract java.lang.String getPosition();
    public abstract void setPosition(java.lang.String position);
    
    public abstract java.lang.Double getSalary();
    public abstract void setSalary(java.lang.Double salary);
    
    // </editor-fold>
    
    public java.lang.String ejbCreate(java.lang.String playerId, java.lang.String name, java.lang.String position, java.lang.Double salary)  throws javax.ejb.CreateException {
        if (playerId == null) {
            throw new javax.ejb.CreateException("The field \"id\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setPlayerId(playerId);
        setName(name);
        setPosition(position);
        setSalary(salary);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String playerId, java.lang.String name, java.lang.String position, java.lang.Double salary) {
        // TODO populate relationships here if appropriate
        
    }
 // Business methods
    public Collection getLeagues() throws FinderException {
        PlayerLocal player = (team.PlayerLocal) context.getEJBLocalObject();

        return ejbSelectLeagues(player);
    }

    public Collection getSports() throws FinderException {
        PlayerLocal player = (team.PlayerLocal) context.getEJBLocalObject();

        return ejbSelectSports(player);
    }
    public abstract java.util.Collection getTeams();

    public abstract void setTeams(java.util.Collection teams);
    
    public abstract java.util.Collection ejbSelectLeagues(PlayerLocal p0) throws javax.ejb.FinderException;

    public abstract java.util.Collection ejbSelectSports(PlayerLocal p0) throws javax.ejb.FinderException;
}
