/*
 * WSDesignNavigatorPanel.java
 *
 * Created on April 9, 2007, 5:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author rico
 */
public class WSDesignNavigatorPanel implements NavigatorPanel, LookupListener{
    
    private WSDesignViewNavigatorContent navigator;
    private Lookup.Result selection;
    
    /** Creates a new instance of WSDesignNavigatorPanel */
    public WSDesignNavigatorPanel() {
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Name");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Hint");
    }
    
    public JComponent getComponent() {
        if(navigator == null){
            navigator = new WSDesignViewNavigatorContent();
        }
        return navigator;
    }
    
    public void panelActivated(Lookup context) {
        getComponent();
        TopComponent.getRegistry().addPropertyChangeListener(navigator);
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(this);
        resultChanged(null);
        // hack to init selection if any
        navigator.propertyChange(new PropertyChangeEvent(this,
                TopComponent.getRegistry().PROP_ACTIVATED_NODES,false,true));
        navigator.navigate();
    }
    
    public void panelDeactivated() {
        TopComponent.getRegistry().removePropertyChangeListener(navigator);
        selection.removeLookupListener(this);
        selection = null;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void resultChanged(LookupEvent ev) {
        navigator.navigate();
    }
    
}
