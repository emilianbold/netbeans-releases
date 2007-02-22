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

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaModelListener implements PropertyChangeListener {
        
    /**
     * Creates a new instance of SchemaModelListener
     */
    public SchemaModelListener(AXIModelImpl model) {
        this.model = model;
    }
    
    /**
     * Returns true if the event pool is not empty,
     * false otherwise.
     */
    boolean needsSync() {
        return !events.isEmpty();
    }
    
    void syncCompleted() {
        events.clear();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        assert(model != null);
        
        //do not add events if we mutated the model
        if(model.isIntransaction())
            return;
        
        events.add(evt);
        ((ModelAccessImpl)model.getAccess()).setDirty();
    }
    
    private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    private AXIModelImpl model;
}
