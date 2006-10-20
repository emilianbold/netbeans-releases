/*
 * ModelSynchronizer.java
 *
 * Created on 28 Èþëü 2006 ã., 10:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.nodes.synchronizer;

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
