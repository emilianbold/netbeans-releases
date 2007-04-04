/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package partnerservices;

import javax.ejb.SessionBean;
import partnerservices.callback.ReservationCallbackProviderBean;

/**
 * This is the implementation bean class for the AirlineReservationService web service.
 * Created Aug 31, 2006 2:25:58 PM
 * @author Praveen
 */
public class AirlineReservationPortType_Impl implements partnerservices.AirlineReservationPortType, SessionBean {
    
    javax.ejb.SessionContext context;
    public void reserveAirline(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException {
        
//        System.err.println("Enter AirlineReservationPortType_Impl.reserveAirline");
//        System.err.println("Itinerary as SOAPElement: " + itinerary.toString() );
        
        String uniqueID = PartnerUtils.getUniqueID(itinerary);
        
        PartnerUtils.sendJMSMessageToReservationCallbackProviderDestination(
                ReservationCallbackProviderBean.AIRLINE_RESERVATION, uniqueID);
        
//        System.err.println("AirlineReservationPortType_Impl.reserveAirline after sendJMS");
        
    }
    
    
    public boolean cancelAirline(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException {
        
        boolean _retVal = false;
        return _retVal;
    }
    
    
    
    
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
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
    }
}
