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
package partnerservices.callback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.ejb.*;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This is the bean class for the ReservationCallbackProviderBean enterprise bean.
 * Created Aug 31, 2006 2:24:21 PM
 * @author Praveen
 */
public class ReservationCallbackProviderBean implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    
    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(MessageDrivenContext aContext) {
        context = aContext;
    }
    
    /**
     * See section 15.4.4 of the EJB 2.0 specification
     * See section 15.7.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    }
    
    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() {
        // TODO release any resource acquired in ejbCreate.
        // The code here should handle the possibility of not getting invoked
        // See section 15.7.3 of the EJB 2.1 specification
    }
    
    // </editor-fold>
    
    public void onMessage(Message aMessage) {
        // typical implementation will delegate to session bean or application service
        try {
            String messageType, uniqueID;
            
            MapMessage mapMessage = (MapMessage) aMessage;
            messageType = mapMessage.getString(MESSAGE_TYPE);
            uniqueID = mapMessage.getString(UNIQUE_ID);
            
//            System.out.println("Enter ReservationCallbackProviderBean.onMessage "  +
//                    messageType + "   uniqueID=" + uniqueID);
            
            String soapStr = "";
            URL cbURL = null;
            
            
            
            // Initialise to defaults.
            String airlineCallbackURL = "http://localhost:18181/TravelReservationService/airlineReserved";
            String vehicleCallbackURL = "http://localhost:18181/TravelReservationService/vehicleReserved";
            String hotelCallbackURL = "http://localhost:18181/TravelReservationService/hotelReserved";
            
            // Lookup URL's defined in the deployment descriptor.
            try {
                InitialContext ic = new InitialContext();
                airlineCallbackURL = (String) ic.lookup("java:comp/env/AirlineCallbackURL");
                vehicleCallbackURL = (String) ic.lookup("java:comp/env/VehicleCallbackURL");
                hotelCallbackURL = (String) ic.lookup("java:comp/env/HotelCallbackURL");
            } catch (NamingException ne) {
//                System.out.println("Error getting callback URL's. Using defaults. Error:" + ne);
            }
            
            
            if(messageType.equals(AIRLINE_RESERVATION)  ) {
                cbURL = new URL(airlineCallbackURL);
                soapStr = createSOAPString(AIRLINE_RESERVATION_SIM_FILE);
                // cb.airlineReserved(createSOAPElementFor(AIRLINE_RESERVATION_SIM_FILE));
                
            } else if(messageType.equals(VEHICLE_RESERVATION)  ) {
                cbURL = new URL(vehicleCallbackURL);
                soapStr = createSOAPString(VEHICLE_RESERVATION_SIM_FILE);
                // cb.vehicleReserved(createSOAPElementFor(VEHICLE_RESERVATION_SIM_FILE));
                
            } else if(messageType.equals(HOTEL_RESERVATION)  ) {
                cbURL = new URL(hotelCallbackURL);
                soapStr = createSOAPString(HOTEL_RESERVATION_SIM_FILE);
                // cb.hotelReserved(createSOAPElementFor(HOTEL_RESERVATION_SIM_FILE));
                
            } else {
                
//                System.out.println("In ReservationCallbackProviderBean.onMessage "  +
//                        messageType + " UNEXPECTED");
                return;
            }
            
            
            soapStr = soapStr.replaceAll("ENTER_ID_HERE", uniqueID);
//            System.out.println("  --------------  soap string sent back ---------------");
//            System.out.println(soapStr);
//            System.out.println("  --------------  soap string sent back ---------------");
            
            sendSOAPMsg(cbURL, soapStr);
//            System.out.println("Exit ReservationCallbackProviderBean.onMessage ");
            
            
        } catch( javax.jms.JMSException  jmse) {
            
//            System.err.println("JMSException in ReservationCallbackProviderBean.onMessage"  +
//                    jmse);
            jmse.printStackTrace();
        }
        
        catch( SOAPException  soapE) {
            
//            System.err.println("SOAPException in ReservationCallbackProviderBean.onMessage"  +
//                    soapE);
            soapE.printStackTrace();
        } catch( RemoteException  re) {
            
//            System.err.println("RemoteException in ReservationCallbackProviderBean.onMessage"  +
//                    re);
            re.printStackTrace();
        } catch( IOException  ioe) {
            
//            System.err.println("IOException in ReservationCallbackProviderBean.onMessage"  +
//                    ioe);
            ioe.printStackTrace();
        } catch( ParserConfigurationException  pce) {
            
//            System.err.println("ParserConfigurationException in ReservationCallbackProviderBean.onMessage"  +
//                    pce);
            pce.printStackTrace();
        } catch( SAXException  saxe) {
            
//            System.err.println("SAXException in ReservationCallbackProviderBean.onMessage"  +
//                    saxe);
            saxe.printStackTrace();
        } catch( TransformerConfigurationException  tce) {
            
//            System.err.println("TransformerConfigurationException in ReservationCallbackProviderBean.onMessage"  +
//                    tce);
            tce.printStackTrace();
        } catch( TransformerException  te) {
            
//            System.err.println("TransformerException in ReservationCallbackProviderBean.onMessage"  +
//                    te);
            te.printStackTrace();
        }
    }
    
    
    
    private String createSOAPString(String simulationMsgFileName)
    throws SOAPException, IOException, ParserConfigurationException, SAXException,
            TransformerConfigurationException, TransformerException{
        
        
        BufferedInputStream source = null;
        StringBuffer sb = new StringBuffer();
        
        try {
            
            source = new BufferedInputStream(
                    getClass().getResourceAsStream(simulationMsgFileName));
            
            
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(source));
            String s;
            while((s = reader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            if (source != null) source.close();
        }
        
    }
    
    
    private void sendSOAPMsg(URL dest, String msg )  throws IOException {
        HttpURLConnection urlC  = null;
        OutputStream os = null;
        try {
            
            urlC = (HttpURLConnection) dest.openConnection();
            urlC.setDoOutput(true);
            
            urlC.setRequestProperty("Content-Type", "text/xml");
            urlC.setRequestMethod("POST");
            os = urlC.getOutputStream();
//            System.out.println("Callback TRACE - in sendSOAPMsg msg =[" + msg + "]");
            os.write(msg.getBytes());
            os.flush();
            urlC.getResponseMessage();
            
            
        }
        
        catch(MalformedURLException e) {
//            System.err.println("MalformedURLException in ReservationCallbackProviderBean.sendSOAPMsg"  +
//                    e);
            e.printStackTrace();
            
        } finally {
            try {
                if(os != null)
                    os.close();
            } catch(Exception e) {
//                System.err.println("sendSOAPMsg "  +  e);
            }
        }
    }
    
    
    
    
    public static final String AIRLINE_RESERVATION = "airline";
    public static final String VEHICLE_RESERVATION = "vehicle";
    public static final String HOTEL_RESERVATION = "hotel";
    public static final String AIRLINE_RESERVATION_SIM_FILE = "ItineraryPlusAirline.xml";
    public static final String VEHICLE_RESERVATION_SIM_FILE = "ItineraryPlusVehicle.xml";
    public static final String HOTEL_RESERVATION_SIM_FILE = "ItineraryPlusHotel.xml";
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    public static final String UNIQUE_ID = "UNIQUE_ID";
    
    public static final String BPEL_PROC_NS =
            "http://www.sun.com/javaone/05/ItineraryReservationService";
    
    public static final String OTA_NS =
            "http://www.opentravel.org/OTA/2003/05";
    
    
    
    
}
