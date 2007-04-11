package partnerservices;

import javax.ejb.SessionBean;
import partnerservices.callback.ReservationCallbackProviderBean;

public class HotelReservationPortType_Impl implements partnerservices.HotelReservationPortType, SessionBean {
    
    javax.ejb.SessionContext context;
    public void reserveHotel(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException {
        String uniqueID = PartnerUtils.getUniqueID(itinerary);
        PartnerUtils.sendJMSMessageToReservationCallbackProviderDestination(
                ReservationCallbackProviderBean.HOTEL_RESERVATION, uniqueID);
    }
    
    public boolean cancelHotel(javax.xml.soap.SOAPElement itinerary) throws
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
