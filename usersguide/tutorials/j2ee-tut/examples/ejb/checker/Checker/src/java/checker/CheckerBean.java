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

package checker;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;


public class CheckerBean implements SessionBean, checker.CheckerRemoteBusiness {
    String customerName;

    public CheckerBean() {
    }

    public void ejbCreate(String person) {
        customerName = person;
    }

    public double applyDiscount(double amount) {
        try {
            double discount;

            Context initial = new InitialContext();
            Context environment = (Context) initial.lookup("java:comp/env");

            Double discountLevel =
                (Double) environment.lookup("Discount Level");
            Double discountPercent =
                (Double) environment.lookup("Discount Percent");

            if (amount >= discountLevel.doubleValue()) {
                discount = discountPercent.doubleValue();
            } else {
                discount = 0.00;
            }

            return amount * (1.00 - discount);
        } catch (NamingException ex) {
            throw new EJBException("NamingException: " + ex.getMessage());
        }
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext(SessionContext sc) {
    }
}