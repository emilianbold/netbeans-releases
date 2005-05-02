/*
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
import java.util.*;


public class SOAPFaultTest {
    public static void main(String[] args) {
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            SOAPMessage message = messageFactory.createMessage();
            SOAPBody body = message.getSOAPBody();
            SOAPFault fault = body.addFault();

            Name faultName =
                soapFactory.createName("Client", "",
                    SOAPConstants.URI_NS_SOAP_ENVELOPE);
            fault.setFaultCode(faultName);

            fault.setFaultString("Message does not have necessary info");
            fault.setFaultActor("http://gizmos.com/order");

            Detail detail = fault.addDetail();

            Name entryName =
                soapFactory.createName("order", "PO",
                    "http://gizmos.com/orders/");
            DetailEntry entry = detail.addDetailEntry(entryName);
            entry.addTextNode("Quantity element does not have a value");

            Name entryName2 =
                soapFactory.createName("confirmation", "PO",
                    "http://gizmos.com/confirm");
            DetailEntry entry2 = detail.addDetailEntry(entryName2);
            entry2.addTextNode("Incomplete address: " + "no zip code");

            message.saveChanges();

            System.out.println("Here is what the XML message looks like:");
            message.writeTo(System.out);
            System.out.println();
            System.out.println();

            // Now retrieve the SOAPFault object and
            // its contents, after checking to see that 
            // there is one
            if (body.hasFault()) {
                SOAPFault newFault = body.getFault();

                // Get the qualified name of the fault code
                Name code = newFault.getFaultCodeAsName();

                String string = newFault.getFaultString();
                String actor = newFault.getFaultActor();

                System.out.println("SOAP fault contains: ");
                System.out.println("  Fault code = " + code.getQualifiedName());
                System.out.println("  Local name = " + code.getLocalName());
                System.out.println("  Namespace prefix = " + code.getPrefix() +
                    ", bound to " + code.getURI());
                System.out.println("  Fault string = " + string);

                if (actor != null) {
                    System.out.println("  Fault actor = " + actor);
                }

                Detail newDetail = newFault.getDetail();

                if (newDetail != null) {
                    Iterator entries = newDetail.getDetailEntries();

                    while (entries.hasNext()) {
                        DetailEntry newEntry = (DetailEntry) entries.next();
                        String value = newEntry.getValue();
                        System.out.println("  Detail entry = " + value);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
