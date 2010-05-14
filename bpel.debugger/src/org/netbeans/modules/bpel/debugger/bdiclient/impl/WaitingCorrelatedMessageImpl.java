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
import org.netbeans.modules.bpel.debugger.api.WaitingCorrelatedMessage;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.WaitingCorrelatedEvent;

/**
 *
 * @author ksorokin
 */
public class WaitingCorrelatedMessageImpl implements WaitingCorrelatedMessage {
    
    private WaitingCorrelatedEvent myEvent;
    private BpelProcessImpl myProcess;
    private CorrelationSet[] myCorrelationSets;
    
    public WaitingCorrelatedMessageImpl(
            final BpelProcessImpl process, 
            final WaitingCorrelatedEvent event) {
        myEvent = event;
        myProcess = process;
        
        String[] setNames = myEvent.getCorrelationSetNames();
        
        myCorrelationSets = new CorrelationSet[setNames.length];
        
        for (int i = 0; i < setNames.length; i++) {
            final String setName = setNames[i];
            
            String[] names = myEvent.getCorrelationSetPropertyNames(setName);
            String[] types = myEvent.getCorrelationSetPropertyTypes(setName);
            
            QName[] realNames = new QName[names.length];
            QName[] realTypes = new QName[names.length];
            
            for (int j = 0; j < names.length; j++) {
                String[] temp;
                
                temp = names[j].split("\n");
                realNames[j] = new QName(temp[0], temp[2], temp[1]);
                
                temp = types[j].split("\n");
                realTypes[j] = new QName(temp[0], temp[2], temp[1]);
            }
            
            
            myCorrelationSets[i] = new CorrelationSetImpl(
                    setName, 
                    myProcess.getProcessRef().getCorrelationSetId(setName), 
                    myEvent.getCorrelationSetValue(setName), 
                    realNames,
                    realTypes,
                    myEvent.getCorrelationSetPropertyValues(setName));
        }
    }
    
    public String getName() {
        return myEvent.getPartnerLinkName() + "(#" + myEvent.getId() + ")";
    }

    public CorrelationSet[] getCorrelationSets() {
        return myCorrelationSets;
    }
}
