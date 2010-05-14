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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import javax.swing.Action;
import org.netbeans.modules.soa.ui.nodes.synchronizer.ModelSynchronizer;
import org.netbeans.modules.soa.ui.nodes.synchronizer.SynchronisationListener;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public abstract class BpelWSDLNode<T extends BPELExtensibilityComponent> 
        extends BpelNode<T> implements SynchronisationListener 
{

    private ModelSynchronizer synchronizer;
    
    public BpelWSDLNode(T component, Children children, Lookup lookup) {
        super(component, children, lookup);
        suscribeWSDLSynchronizer(component);
    }
    
    public BpelWSDLNode(T component, Lookup lookup) {
        super(component, lookup);
        suscribeWSDLSynchronizer(component);
    }
    
    protected void suscribeWSDLSynchronizer(T component) {
        if (component == null) {
            return;
        }
        
        WSDLModel model = component.getModel();
        if (model != null) {
            synchronizer = new ModelSynchronizer(this);
            synchronizer.subscribe(model);
        }
    }
    
    public Action[] getActions(boolean b) {
        Action[] actions = null;
        Object ref = getReference();
        if (ref instanceof BPELExtensibilityComponent) {
            WSDLModel model = ((BPELExtensibilityComponent)ref).getModel();
            if (model != null && WSDLModel.State.VALID.equals(model.getState())) 
            {
                actions = createActionsArray();
            }
        }
        return actions == null ? super.getActions(b) : actions;
    }
    
    public void componentUpdated(org.netbeans.modules.xml.xam.Component component) {
        if (component.equals(getReference())) {
            updateName();
            updateAllProperties();
        }
    }

    public void childrenUpdated(org.netbeans.modules.xml.xam.Component component) {
        // do nothing
    }
}
