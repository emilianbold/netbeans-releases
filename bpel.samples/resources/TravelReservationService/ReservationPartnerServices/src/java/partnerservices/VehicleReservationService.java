package partnerservices;

import javax.xml.rpc.*;

public interface VehicleReservationService extends javax.xml.rpc.Service {
    public partnerservices.VehicleReservationPortType getVehicleReservationPortTypePort() throws ServiceException;
}
