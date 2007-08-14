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
package org.netbeans.modules.bpel.design.decoration.providers;

import java.awt.Color;
import java.util.ArrayList;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.StripeDescriptor;
import org.netbeans.modules.bpel.design.decoration.StrokeDescriptor;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 * @author aa160298
 */
public class SelectionDecorationProvider extends DecorationProvider implements DiagramSelectionListener {
    
    public SelectionDecorationProvider() {}
    
    public SelectionDecorationProvider(DesignView designView) {
        super(designView);
        designView.getSelectionModel().addSelectionListener(this);
    }
    
    public Decoration getDecoration(BpelEntity entity) {
        UniqueId entityID = entity.getUID();
        UniqueId selectedEntityID = getDesignView().getSelectionModel().getSelectedID();
        
        if (entityID!= null && entityID.equals(selectedEntityID)) {
            return new Decoration(new Descriptor[]{STROKE_DESCRIPTOR, 
                    StripeDescriptor.createSelection() });
        }
        return null;
    }
    
    public void selectionChanged(BpelEntity oldSelection, BpelEntity newSelection) {
        if (newSelection != null) {
            fireDecorationChanged(newSelection);
        } 
        
        if (oldSelection != null) {
            fireDecorationChanged(oldSelection);
        }
    }
    
    private ArrayList<UniqueId> linkedEntities = new ArrayList<UniqueId>();
    private static final Descriptor STROKE_DESCRIPTOR = new StrokeDescriptor(new Color(0x5D985C), 2);
}
