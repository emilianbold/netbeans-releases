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
package org.netbeans.modules.bpel.properties.editors.controls.filter;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.properties.Constants;

/**
 * This filter is intended to work with Fault Name Nodes.
 * Preferred Fault Names can be shown specific way to user be able 
 * to distint them from others.
 *
 * @author nk160297
 */
public class PreferredFaultFilter {
    
    private Collection<QName> myPreferredFaults;
    private boolean isShowPreferredFaultsOnly = false;
    
    public PreferredFaultFilter(Collection<QName> preferredFaults) {
        myPreferredFaults = preferredFaults;
    }
    
    public boolean isFaultPreferred(QName faultName) {
        boolean isFound = false;
        for (QName qName : myPreferredFaults) {
            if (qName.equals(faultName)) {
                isFound = true;
                break;
            }
        }
        return isFound;
    }
    
    public boolean isShowPreferredFaultsOnly() {
        return isShowPreferredFaultsOnly;
    }
    
    public void setShowPreferredFaultsOnly(boolean newValue) {
        isShowPreferredFaultsOnly = newValue;
    }
}
