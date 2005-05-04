/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */


import javax.xml.soap.*;
import java.net.*;
import java.util.*;
import java.io.*;


public class UddiPing {
    public static void main(String[] args) {
        try {
            // two arguments are passed in from build.xml
            if (args.length != 2) {
                System.err.println("Usage: asant run " +
                    "-Dbusiness-name=<name>");
                System.exit(1);
            }

            // Retrieve settings from uddi.properties file,
            // add to system properties
            Properties myprops = new Properties();
            myprops.load(new FileInputStream(args[0]));

            Properties props = System.getProperties();

            Enumeration propNames = myprops.propertyNames();
            while (propNames.hasMoreElements()) {
                String s = (String) propNames.nextElement();
                props.setProperty(s, myprops.getProperty(s));
            }

            // Create connection, message factory, and
            // SOAP factory
            SOAPConnectionFactory soapConnectionFactory =
                SOAPConnectionFactory.newInstance();
            SOAPConnection connection =
                soapConnectionFactory.createConnection();
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();

            // Create a message
            SOAPMessage message = messageFactory.createMessage();

            // Get the SOAP header from the message and remove it
            SOAPHeader header = message.getSOAPHeader();
            header.detachNode();

            // Get the SOAP body from the message
            SOAPBody body = message.getSOAPBody();

            // Create a UDDI v2 find_business body element
            SOAPBodyElement findBusiness =
                body.addBodyElement(soapFactory.createName("find_business", 
                        "",
                        "urn:uddi-org:api_v2"));
            findBusiness.addAttribute(soapFactory.createName("generic"), "2.0");
            findBusiness.addAttribute(soapFactory.createName("maxRows"), "100");

            SOAPElement businessName =
                findBusiness.addChildElement(soapFactory.createName("name"));
            businessName.addTextNode(args[1]);

            message.saveChanges();

            // Display the message you are sending
            System.out.println("\n----- Request Message ----\n");
            message.writeTo(System.out);

            // Retrieve the endpoint from system properties
            URL endpoint = new URL(System.getProperties().getProperty("URL"));

            // Send message and get reply
            SOAPMessage reply = connection.call(message, endpoint);

            System.out.println("\n\nReceived reply from: " + endpoint);
            System.out.println("\n------ Reply Message -----\n");

            // Display the reply
            reply.writeTo(System.out);

            // Now parse the reply
            SOAPBody replyBody = reply.getSOAPBody();

            /*
             * The response to a find_business query is a
             * businessList conformant to the UDDI V2 Data
             * Structure specifications. For further details,
             * see
             * http://uddi.org/pubs/DataStructure-V2.03-Published-20020719.htm#_Toc25130802
             */
            System.out.println("\n\nContent extracted from " +
                "the reply message:\n");

            Iterator businessListIterator =
                replyBody.getChildElements(soapFactory.createName(
                        "businessList",
                        "",
                        "urn:uddi-org:api_v2"));

            /*
             * businessList is a mandatory element in a
             * successful response and appears only once.
             */
            SOAPBodyElement businessList =
                (SOAPBodyElement) businessListIterator.next();

            /*
             * Get the businessInfos element. This contains
             * the business entries retrieved that match the
             * criteria specified in the find_business
             * request.
             */
            Iterator businessInfosIterator =
                businessList.getChildElements(soapFactory.createName(
                        "businessInfos",
                        "",
                        "urn:uddi-org:api_v2"));

            /*
             * businessInfos, too is a mandatory element in
             * a successful response and appears only once.
             */
            SOAPElement businessInfos =
                (SOAPElement) businessInfosIterator.next();

            /*
             * The businessInfos contains zero or more
             * businessInfo structures. Each businessInfo
             * structure contains the company name and
             * optional description data.
             */
            Iterator businessInfoIterator =
                businessInfos.getChildElements(soapFactory.createName(
                        "businessInfo",
                        "",
                        "urn:uddi-org:api_v2"));

            if (!businessInfoIterator.hasNext()) {
                System.out.println("No businesses found " +
                    "matching the name \"" + args[1] + "\".");
            } else {
                while (businessInfoIterator.hasNext()) {
                    SOAPElement businessInfo =
                        (SOAPElement) businessInfoIterator.next();

                    // Extract name and description from the 
                    // businessInfo
                    Iterator nameIterator =
                        businessInfo.getChildElements(soapFactory.createName(
                                "name",
                                "",
                                "urn:uddi-org:api_v2"));

                    while (nameIterator.hasNext()) {
                        businessName = (SOAPElement) nameIterator.next();
                        System.out.println("Company name: " +
                            businessName.getValue());
                    }

                    Iterator descriptionIterator =
                        businessInfo.getChildElements(soapFactory.createName(
                                "description",
                                "",
                                "urn:uddi-org:api_v2"));

                    while (descriptionIterator.hasNext()) {
                        SOAPElement businessDescription =
                            (SOAPElement) descriptionIterator.next();
                        System.out.println("Description: " +
                            businessDescription.getValue());
                    }

                    System.out.println("");
                }
            }

            // Close the connection
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
