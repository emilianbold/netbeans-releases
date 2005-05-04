/*
 * Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package cart;

import exception.BookException;
import java.util.Vector;
import javax.ejb.*;
import util.IdVerifier;

public class CartBean implements SessionBean, CartRemoteBusiness, CartLocalBusiness {
    private SessionContext context;
    String customerName;
    String customerId;
    Vector contents;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods
    /**
     * @see SessionBean#setSessionContext(SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see SessionBean#ejbRemove()
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

    public void ejbCreate(String person) throws CreateException {
        //TODO implement 
        if(person == null) {
            throw new CreateException("Null person not allowed.");
        }else{
            customerName = person;
        }
        customerId = "0";
        contents = new Vector();
    }

    public void ejbCreate(String person, String id) throws CreateException {
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

    public void addBook(String title) {
        //TODO implement addBook
        contents.add(title);
    }

    public void removeBook(String title) throws BookException {
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
