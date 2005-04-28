package cart;
import exception.BookException;
import java.util.Vector;
import javax.ejb.CreateException;
import util.IdVerifier;

/**
 * This is the bean class for the CartBean enterprise bean.
 * Created Mar 25, 2005 9:37:42 AM
 * @author blaha
 */
public class CartBean implements javax.ejb.SessionBean, cart.CartRemoteBusiness, cart.CartLocalBusiness {
    private javax.ejb.SessionContext context;
    String customerName;
    String customerId;
    Vector contents;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
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
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }
    
    
    
    
    
    // Enter business methods below. (Right-click in editor and choose
    // EJB Methods > Add Business Method)

    public void ejbCreate(java.lang.String person) throws javax.ejb.CreateException {
        //TODO implement 
        if(person == null) {
            throw new CreateException("Null person not allowed.");
        }else{
            customerName = person;
        }
        customerId = "0";
        contents = new Vector();
    }

    public void ejbCreate(java.lang.String person, java.lang.String id) throws javax.ejb.CreateException {
        //TODO implement 
        if(person == null) {
            throw new CreateException("Null person not allowed.");
        }else{
            customerName = person;
        }
        
        IdVerifier idChecker = new IdVerifier();
        if(idChecker.validate(id)){
            customerId = id;
        }else{
            throw new CreateException("Invalid id: " + id);
        }
        contents = new Vector();
    }

    public void addBook(java.lang.String title) {
        //TODO implement addBook
        contents.add(title);
    }

    public void removeBook(java.lang.String title) throws BookException {
        //TODO implement removeBook
        boolean result = contents.remove(title);
        if(result == false)
            throw new BookException(title + " not in cart.");
    } 

    public Vector getContents() {
        //TODO implement getContents
        return contents;
    }
    
}
