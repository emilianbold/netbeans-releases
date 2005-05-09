/*
 * Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright (c) 2005 Sun Microsystems, Inc. Tous droits reserves.
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

package orderclient;


import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import order.LineItem;
import order.OrderRemote;
import order.OrderRemoteHome;


public class OrderClient {
    public static void main(String[] args) {
        try {
            ArrayList lineItems = new ArrayList();

            lineItems.add(new LineItem("p23", 13, 12.00, 1, "123"));
            lineItems.add(new LineItem("p67", 47, 89.00, 2, "123"));
            lineItems.add(new LineItem("p11", 28, 41.00, 3, "123"));

            Context initial = new InitialContext();
            Object objref = initial.lookup("ejb/SimpleOrder");

            OrderRemoteHome home =
                (OrderRemoteHome) PortableRemoteObject.narrow(objref, OrderRemoteHome.class);

            OrderRemote duke =
                home.create("123", "c44", "open", totalItems(lineItems),
                    lineItems);

            displayItems(duke.getLineItems());
            System.out.println();

            Collection c = home.findByProductId("p67");
            Iterator i = c.iterator();

            while (i.hasNext()) {
                OrderRemote order = (OrderRemote) i.next();
                String id = (String) order.getPrimaryKey();

                System.out.println(id);
            }

            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an exception.");
            ex.printStackTrace();
        }
    }

    static double totalItems(ArrayList lineItems) {
        double total = 0.00;
        ListIterator iterator = lineItems.listIterator(0);

        while (iterator.hasNext()) {
            LineItem item = (LineItem) iterator.next();

            total += item.getUnitPrice();
        }

        return total;
    }

    static void displayItems(ArrayList lineItems) {
        ListIterator iterator = lineItems.listIterator(0);

        while (iterator.hasNext()) {
            LineItem item = (LineItem) iterator.next();

            System.out.println(item.getOrderId() + " " + item.getItemNo() +
                " " + item.getProductId() + " " + item.getUnitPrice());
        }
    }
}
