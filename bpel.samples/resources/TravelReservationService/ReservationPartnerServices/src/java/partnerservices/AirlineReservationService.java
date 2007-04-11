package partnerservices;

import javax.xml.rpc.*;

public interface AirlineReservationService extends javax.xml.rpc.Service {
    public partnerservices.AirlineReservationPortType getAirlineReservationPortTypePort() throws ServiceException;
}
