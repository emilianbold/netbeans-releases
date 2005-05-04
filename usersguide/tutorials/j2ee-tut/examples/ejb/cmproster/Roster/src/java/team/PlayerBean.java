package team;

import java.util.Collection;
import javax.ejb.*;

/**
 * This is the bean class for the PlayerBean enterprise bean.
 * Created Mar 23, 2005 1:48:50 PM
 * @author honza
 */
public abstract class PlayerBean implements EntityBean, PlayerLocalBusiness {
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
    
    public abstract String getPlayerId();
    public abstract void setPlayerId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);
    
    public abstract String getPosition();
    public abstract void setPosition(String position);
    
    public abstract Double getSalary();
    public abstract void setSalary(Double salary);
    
    // </editor-fold>
    
    public String ejbCreate(String playerId, String name, String position, Double salary)  throws CreateException {
        if (playerId == null) {
            throw new CreateException("The field \"id\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setPlayerId(playerId);
        setName(name);
        setPosition(position);
        setSalary(salary);
        
        return null;
    }
    
    public void ejbPostCreate(String playerId, String name, String position, Double salary) {
        // TODO populate relationships here if appropriate
        
    }
 // Business methods
    public Collection getLeagues() throws FinderException {
        PlayerLocal player = (PlayerLocal) context.getEJBLocalObject();

        return ejbSelectLeagues(player);
    }

    public Collection getSports() throws FinderException {
        PlayerLocal player = (PlayerLocal) context.getEJBLocalObject();

        return ejbSelectSports(player);
    }
    public abstract Collection getTeams();

    public abstract void setTeams(Collection teams);
    
    public abstract Collection ejbSelectLeagues(PlayerLocal p0) throws FinderException;

    public abstract Collection ejbSelectSports(PlayerLocal p0) throws FinderException;
}
