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


public class HeaderExample {
    public static void main(String[] args) {
        try {
            // two arguments are passed in from build.xml
            if (args.length != 0) {
                System.err.println("Usage: asant run");
                System.exit(1);
            }

            // Create message factory and SOAP factory
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();

            // Create a message
            SOAPMessage message = messageFactory.createMessage();

            // Get the SOAP header from the message and 
            //  add headers to it
            SOAPHeader header = message.getSOAPHeader();

            String nameSpace = "ns";
            String nameSpaceURI = "http://gizmos.com/NSURI";

            Name order =
                soapFactory.createName("orderDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement orderHeader = header.addHeaderElement(order);
            orderHeader.setActor("http://gizmos.com/orders");

            Name shipping =
                soapFactory.createName("shippingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement shippingHeader =
                header.addHeaderElement(shipping);
            shippingHeader.setActor("http://gizmos.com/shipping");

            Name confirmation =
                soapFactory.createName("confirmationDesk", nameSpace,
                    nameSpaceURI);
            SOAPHeaderElement confirmationHeader =
                header.addHeaderElement(confirmation);
            confirmationHeader.setActor("http://gizmos.com/confirmations");

            Name billing =
                soapFactory.createName("billingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement billingHeader = header.addHeaderElement(billing);
            billingHeader.setActor("http://gizmos.com/billing");

            // Add header with mustUnderstand attribute
            Name tName =
                soapFactory.createName("Transaction", "t",
                    "http://gizmos.com/orders");

            SOAPHeaderElement transaction = header.addHeaderElement(tName);
            transaction.setMustUnderstand(true);
            transaction.addTextNode("5");

            // Get the SOAP body from the message but leave
            // it empty
            SOAPBody body = message.getSOAPBody();

            message.saveChanges();

            // Display the message that would be sent
            System.out.println("\n----- Request Message ----\n");
            message.writeTo(System.out);

            // Look at the headers
            Iterator allHeaders = header.examineAllHeaderElements();

            while (allHeaders.hasNext()) {
                SOAPHeaderElement headerElement =
                    (SOAPHeaderElement) allHeaders.next();
                Name headerName = headerElement.getElementName();
                System.out.println("\nHeader name is " +
                    headerName.getQualifiedName());
                System.out.println("Actor is " + headerElement.getActor());
                System.out.println("mustUnderstand is " +
                    headerElement.getMustUnderstand());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
