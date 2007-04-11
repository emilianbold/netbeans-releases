package partnerservices;

public interface VehicleReservationPortType extends java.rmi.Remote {
    public void reserveVehicle(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException;
    public boolean cancelVehicle(javax.xml.soap.SOAPElement itinerary) throws
            java.rmi.RemoteException;
}
