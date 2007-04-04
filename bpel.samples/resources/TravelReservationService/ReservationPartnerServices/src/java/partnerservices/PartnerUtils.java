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

import java.rmi.RemoteException;
import javax.jms.MapMessage;
import javax.xml.soap.SOAPElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import partnerservices.callback.ReservationCallbackProviderBean;

/**
 *
 * @author Praveen
 */
public class PartnerUtils {
    
    /** Creates a new instance of PartnerUtils */
    public PartnerUtils() {
    }
    
    
    public static javax.jms.Message createJMSMessageForReservationCallbackProviderDestination(
            javax.jms.Session session, java.lang.String messsageType, String uniqueID)
            throws javax.jms.JMSException {
        
        // TODO create and populate message to send        
        MapMessage mapMessage = session.createMapMessage();
        mapMessage.setString(ReservationCallbackProviderBean.MESSAGE_TYPE, messsageType);
        mapMessage.setString(ReservationCallbackProviderBean.UNIQUE_ID, uniqueID);
        
        return mapMessage;
    }
    
    public static void sendJMSMessageToReservationCallbackProviderDestination(
            String messageType, String uniqueID)
            throws RemoteException {
        javax.jms.Connection conn = null;
        javax.jms.Session s = null;
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            javax.jms.ConnectionFactory cf = (javax.jms.ConnectionFactory) c.lookup("java:comp/env/jms/ReservationCallbackProviderDestinationFactory");
            
            conn = cf.createConnection();
            s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);
            javax.jms.Destination destination = (javax.jms.Destination) c.lookup("java:comp/env/jms/ReservationCallbackProviderDestination");
            javax.jms.MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForReservationCallbackProviderDestination(s,messageType, uniqueID));
            
            if (s != null) {
                s.close();
            }
            
            if (conn != null) {
                conn.close();
            }
            
        } catch( javax.naming.NamingException ne ) {
            throw new java.rmi.RemoteException("NamingException", ne);
        } catch( javax.jms.JMSException  jmse) {
            throw new java.rmi.RemoteException("JMSException", jmse);
        }
    }
    
    
    /** 
     *  Parses the input itinerary and returns the uniqueID element value.
     */
    public static String getUniqueID(SOAPElement itinerary) {
        String uniqueID = "";
        
        NodeList nodeList = itinerary.getChildNodes();
        try {
            for(int index1=0; index1<nodeList.getLength() ; index1++) {
                Node node1 = nodeList.item(index1);
                if(node1.getNodeName().endsWith("ItineraryRef")) {
                    for (int index2=0 ; index2<node1.getChildNodes().getLength() ; index2++) {
                        Node node2 = node1.getChildNodes().item(index2);
                        if(node2.getNodeName().endsWith("UniqueID")) {
                            uniqueID = node2.getTextContent();
                            break;
                        }
                    }
                }
            }        
        } catch (Exception ex) {
//            System.out.println("Error in PartnerServices. Input itinerary is not +" +
//                    " as expected. Error retrieving the UniqueID" + ex);  // NOI18N
        }
        return uniqueID;
        
    }
    
}
