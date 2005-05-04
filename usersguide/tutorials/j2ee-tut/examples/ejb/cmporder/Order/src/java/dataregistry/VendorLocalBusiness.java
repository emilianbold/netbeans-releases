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

package dataregistry;


/**
 * This is the business interface for Vendor enterprise bean.
 */
public interface VendorLocalBusiness {
    public abstract int getVendorId();

    public abstract java.lang.String getName();

    public abstract void setName(java.lang.String name);

    public abstract java.lang.String getAddress();

    public abstract void setAddress(java.lang.String address);

    public abstract java.lang.String getContact();

    public abstract void setContact(java.lang.String contact);

    public abstract java.lang.String getPhone();

    public abstract void setPhone(java.lang.String phone);

    public abstract java.util.Collection getVendorPartBean();

    public abstract void setVendorPartBean(java.util.Collection vendorPartBean);
    
}
