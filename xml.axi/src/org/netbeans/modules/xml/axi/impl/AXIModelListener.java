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
package org.netbeans.modules.xml.axi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 * Listener to listen to this model changes. Used by code generator.
 * 
 * @author Ayub
 */
public class AXIModelListener implements PropertyChangeListener {
    List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        //filter events if not intended for code generator.
        if(!validatePropertyChangeEvent(evt))
            return;
        
        //add event to the event queue.
        events.add(evt);
    }
    
    public List<PropertyChangeEvent> getEvents() {
        return events;
    }
    
    public void clearEvents() { events.clear();}
    
    /**
     * Checks the validity of this event. Certain events shouldn't go to
     * the code generator. For example events coming from proxy components.
     * There are certain other cases as well.
     */
    private boolean validatePropertyChangeEvent(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        //events coming from model are valid
        if( !(source instanceof AXIComponent) )
            return true;
                
        //ignore proxy related events
        if(proxyRelated(evt))
            return false;
                        
        
        return true;
    }
    
    private boolean proxyRelated(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof AXIComponentProxy)
            return true;
        
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        //proxy child added
        if( (newValue != null) && (oldValue == null) &&
            (newValue instanceof AXIComponentProxy) )
            return true;
        //proxy child removed
        if( (oldValue != null) && (newValue == null) &&
            (oldValue instanceof AXIComponentProxy) )
            return true;
        
        return false;
    }
    
}
