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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
import java.util.Collection;
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
    private Lookup.Result<DataObject> selection;
    
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
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(this);
        resultChanged(null);
        // hack to init selection if any
        navigator.propertyChange(new PropertyChangeEvent(this,
                TopComponent.getRegistry().PROP_ACTIVATED_NODES,false,true));
        
        //temporarily display root node
        navigator.navigate(null);
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
        Collection<? extends DataObject> selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = selected.iterator().next();
            navigator.navigate(dobj);
        }
    }
    
}
