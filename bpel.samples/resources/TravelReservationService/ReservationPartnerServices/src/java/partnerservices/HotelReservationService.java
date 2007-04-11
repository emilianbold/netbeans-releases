package partnerservices;

import javax.xml.rpc.*;

public interface HotelReservationService extends javax.xml.rpc.Service {
    public partnerservices.HotelReservationPortType getHotelReservationPortTypePort() throws ServiceException;
}
