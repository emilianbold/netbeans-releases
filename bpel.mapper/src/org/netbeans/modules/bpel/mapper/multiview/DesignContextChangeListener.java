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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author nk160297
 * @author Vitaly Bychkov
 */
public class DesignContextChangeListener/* implements PropertyChangeListener */{

    private DesignContextController mController;
    
    /** Creates a new instance of DesignContextChangeListener */
    public DesignContextChangeListener(DesignContextController controller) {
        assert controller != null;
        mController = controller;
    }
    
//    public synchronized void propertyChange(PropertyChangeEvent evt) {
//        if (mController instanceof DesignContextControllerImpl 
//                && !((DesignContextControllerImpl)mController).isMapperTcShown()) 
//        {
//            return;
//        }
//        
//        String propertyName = evt.getPropertyName();
//        BpelDesignContext newBpelContext = null;
//        
//        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
//            newBpelContext = getActivatedContext();
//        } else {
//            // Other properties are not supported 
//            return;
//        }
//        //
//        if (mController != null && newBpelContext != null) {
//            mController.setContext(newBpelContext);
//        }
//    }
    
//    /**
//     * Don't change context in case non bpel entity were selected
//     * 
//     * @return new context, null if context should be changed, 
//     * bpel model changes are supported self by DesignContextController
//     */
//    public static BpelDesignContext getActivatedContext(BpelModel currentBpelModel) {
//        if (currentBpelModel == null) {
//            return null;
//        }
//        
//        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
//        if (nodes == null || nodes.length != 1) {
//            return null;
//        }
//        BpelEntity bpelEntity = null;
//        if (nodes[0] instanceof InstanceRef) {
//            Object entity = ((InstanceRef) nodes[0]).getReference();
//            if (entity instanceof BpelEntity 
//                    && currentBpelModel.equals(((BpelEntity)entity).getBpelModel())) 
//            {
//                bpelEntity = (BpelEntity)entity;
//            }
//        } else {
//            return null;
//        }
//        
//        Lookup lookup = nodes[0].getLookup();
//        BpelDesignContext bpelContext = 
//                new BpelDesignContext(bpelEntity, nodes[0], lookup);
//        return bpelContext;
//    }
}
