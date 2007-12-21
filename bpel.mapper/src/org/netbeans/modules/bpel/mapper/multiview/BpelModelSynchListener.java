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

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;

/**
 * Listen to the changes in the BPEL model and synchronize the mapper view.
 * Now it actually only initialize full reload of the mapper.
 * 
 * @author nk160297
 */
public class BpelModelSynchListener implements ChangeEventListener {
    
    private DesignContextController mController;

    /** Creates a new instance of DesignContextSynchronizationListener */
    public BpelModelSynchListener(DesignContextController controller) {
        assert controller != null;
        mController = controller;
    }
    
    public void notifyPropertyUpdated(PropertyUpdateEvent event) {
        mController.reloadMapper(event);
    }
    
    public void notifyArrayUpdated(ArrayUpdateEvent event) {
    }
    
    public void notifyEntityInserted(EntityInsertEvent event) {
        mController.reloadMapper(event);
    }
    
    public void notifyEntityRemoved(EntityRemoveEvent event) {
        mController.reloadMapper(event);
    }
    
    public void notifyEntityUpdated(EntityUpdateEvent event) {
        mController.reloadMapper(event);
    }
    
    public void notifyPropertyRemoved(PropertyRemoveEvent event) {
        mController.reloadMapper(event);
    }
    
}
