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

package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;

/**
 *
 * @author Kirill Sorokin
 */
public class CorrelationSetImpl implements CorrelationSet {
    
    private String myName;
    private long myId;
    private String myValue;
    
    private Property[] myProperties;
    
    public CorrelationSetImpl(
            final String name, 
            final long id,
            final String value,
            final QName[] propNames,
            final QName[] propTypes,
            final String[] propValues) {
        myName = name;
        myId = id;
        myValue = value;
        
        if (value != null) {
            myProperties = new Property[propNames.length];
            
            for (int i = 0; i < propNames.length; i++) {
                myProperties[i] = 
                        new Property(propNames[i], propTypes[i], propValues[i]);
            }
        } else {
            myProperties = new Property[0];
        }
    }
    
    public String getName() {
        return myName;
    }
    
    public long getId() {
        return myId;
    }
    
    public String getValue() {
        return myValue;
    }

    public Property[] getProperties() {
        return myProperties;
    }
}
