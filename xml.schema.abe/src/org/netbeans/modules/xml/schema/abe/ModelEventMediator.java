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

/*
 * ModelEventMediator.java
 *
 * Created on September 15, 2006, 8:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 *
 * @author girix
 */
public abstract class ModelEventMediator implements PropertyChangeListener{
    
    ABEBaseDropPanel uiPeer;
    AXIComponent modelPeer;
    /** Creates a new instance of ModelEventMediator */
    protected ModelEventMediator(ABEBaseDropPanel uiPeer, AXIComponent modelPeer) {
        this.uiPeer = uiPeer;
        this.modelPeer = modelPeer;
        
        uiPeer.addPropertyChangeListener(ABEBaseDropPanel.PROP_COMPONENT_REMOVED,
                new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ABEBaseDropPanel.PROP_COMPONENT_REMOVED))
                    cleanUp();
            }
        });
    }
    
    public abstract void _propertyChange(PropertyChangeEvent pce);
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(hasPathToTC()){
            //if the UI peer has the top frame then only despatch the event
            _propertyChange(evt);
        }else{
            //Else remove myself
            cleanUp();
        }
    }
    
    protected boolean hasPathToTC() {
        if(uiPeer != null)
            return (SwingUtilities.getAncestorOfClass(InstanceDesignerPanel.class, uiPeer) != null);
        else{
            modelPeer = null;
            return false;
        }
    }
    
    protected void cleanUp() {
        if(!uiPeer.getContext().isShutdown() &&
            uiPeer instanceof ElementsContainerPanel) // &&!= uiPeer.getAXIComponent())
            return;
        
        if(modelPeer != null) {
            if(modelPeer.getModel() != null)
                modelPeer.removePropertyChangeListener(this);
        }
        modelPeer = null;
        uiPeer = null;
    }
    
}
