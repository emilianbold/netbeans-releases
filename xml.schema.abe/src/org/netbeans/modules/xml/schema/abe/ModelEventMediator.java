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
    }
    
    public abstract void _propertyChange(PropertyChangeEvent pce);
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(hasPathToTC()){
            //if the UI peer has the top frame then only despatch the event
            _propertyChange(evt);
        }else{
            //Else remove myself
            if(modelPeer != null)
                modelPeer.removePropertyChangeListener(this);
            else{
                uiPeer = null;
            }
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
    
}
