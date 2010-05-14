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
package org.netbeans.modules.soa.ui.nodes.synchronizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.WeakListeners;

/**
 * Helper class to create a weak listeners to any XAM model.
 * Dispatches the events from model in AWT thread
 * 
 * @author Alexey
 */
public class ModelSynchronizer implements ComponentListener, PropertyChangeListener {
    
    
    private PropertyChangeListener weakModelListener;
    private ComponentListener weakComponentListener;
    private Model currentModel;
    private SynchronisationListener listener;
    
    public ModelSynchronizer(SynchronisationListener listener){
        this.listener = listener;
        
    }
    
    public void subscribe(Model model){
        if (currentModel != model){
            
            unsubscribe();
            
            if (model != null){
                weakModelListener = WeakListeners.propertyChange(this, model);
                
                model.addPropertyChangeListener(weakModelListener);
                
                weakComponentListener = (ComponentListener) WeakListeners.create(
                        ComponentListener.class, this, model);
                
                model.addComponentListener(weakComponentListener);
            }
            currentModel = model;
        }
    }
    public void unsubscribe(){
        if ( currentModel != null){
            currentModel.removePropertyChangeListener(weakModelListener);
            currentModel.removeComponentListener(weakComponentListener);
        }
        
        currentModel = null;
    }
    public void propertyChange(final PropertyChangeEvent evt) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(evt.getSource() instanceof Component) {
                    listener.componentUpdated((Component)evt.getSource());
                }
            }
        });
        
    }
    public void childrenAdded(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
    public void childrenDeleted(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
    public void valueChanged(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
}
