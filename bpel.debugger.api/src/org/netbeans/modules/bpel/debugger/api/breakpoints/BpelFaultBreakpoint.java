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

package org.netbeans.modules.bpel.debugger.api.breakpoints;

import javax.xml.namespace.QName;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelFaultBreakpoint extends BpelBreakpoint implements Comparable {

    /** Property name constant. */
    public static final String PROP_PROCESS_QNAME = "processQName"; // NOI18N
    /** Property name constant. */
    public static final String PROP_FAULT_QNAME = "faultQName"; // NOI18N

    private QName myProcessQName;
    private QName myFaultQName;
    
    private BpelFaultBreakpoint() {
    }

    public static BpelFaultBreakpoint create(
            QName processQName,
            QName faultQName)
    {
        BpelFaultBreakpoint b = new BpelFaultBreakpoint();
        b.setProcessQName(processQName);
        b.setFaultQName(faultQName);
        return b;
    }

    public QName getProcessQName() {
        return myProcessQName;
    }

    public void setProcessQName(QName newProcessQName) {
        QName oldProcessQName = myProcessQName;
        myProcessQName = newProcessQName;
        firePropertyChange(PROP_PROCESS_QNAME, oldProcessQName, newProcessQName);
    }

    public QName getFaultQName() {
        return myFaultQName;
    }

    public void setFaultQName(QName newFaultQName) {
        QName oldFaultQName = myFaultQName;
        myFaultQName = newFaultQName;
        firePropertyChange(PROP_FAULT_QNAME, oldFaultQName, newFaultQName);
    }

    public void touch() {
        firePropertyChange(null, null, null);
    }

    public String toString() {
        return "BpelFaultBreakpoint " + myProcessQName + " : " + myFaultQName; // NOI18N
    }

    public int compareTo(Object o) {
        if (o instanceof BpelFaultBreakpoint) {
            //TODO:implement!!!
            return o.hashCode() > hashCode() ? 1 : -1;
//            BpelFaultBreakpoint bp = (BpelFaultBreakpoint)o;
//            
//            int uc = getProcessQName().getURL().compareTo(lb.getURL());
//            if (uc != 0) {
//                return uc;
//            } else {
//                return getXpath().compareTo(lb.getXpath());
//            }
        } else {
            //TODO: does it comply with Comparable interface contract?
            //shouldn't we guarantee that a>b => b<a ?
            return -1;
        }
    }
}
