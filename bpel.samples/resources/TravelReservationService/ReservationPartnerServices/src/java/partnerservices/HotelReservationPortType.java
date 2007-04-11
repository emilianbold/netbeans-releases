package partnerservices;

public interface HotelReservationPortType extends java.rmi.Remote {
    public void reserveHotel(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException;
    public boolean cancelHotel(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException;
}
