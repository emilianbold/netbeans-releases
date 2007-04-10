package partnerservices;

public interface AirlineReservationPortType extends java.rmi.Remote {
    public void reserveAirline(javax.xml.soap.SOAPElement itinerary) throws 
         java.rmi.RemoteException;
    public boolean cancelAirline(javax.xml.soap.SOAPElement itinerary) throws 
         java.rmi.RemoteException;
}
