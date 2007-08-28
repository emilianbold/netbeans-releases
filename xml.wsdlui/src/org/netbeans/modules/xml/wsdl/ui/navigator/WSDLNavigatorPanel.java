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

package org.netbeans.modules.xml.wsdl.ui.navigator;

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
import org.openide.windows.TopComponent.Registry;

/**
 * An implementation of NavigatorPanel for WSDL navigator.
 *
 * @author Marek Fukala
 * @author Nathan Fiedler
 */
public class WSDLNavigatorPanel implements LookupListener, NavigatorPanel {
    private Lookup.Result selection;

    /**
     * Public nullary constructor needed for system to instantiate the provider.
     */
    public WSDLNavigatorPanel() {
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(WSDLNavigatorPanel.class,
                "LBL_WSDLNavigatorPanel_Hint");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WSDLNavigatorPanel.class,
                "LBL_WSDLNavigatorPanel_Name");
    }

    public JComponent getComponent() {
        return WSDLNavigatorContent.getDefault();
    }

    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        TopComponent.getRegistry().removePropertyChangeListener(WSDLNavigatorContent.getDefault());
        TopComponent.getRegistry().addPropertyChangeListener(WSDLNavigatorContent.getDefault());
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(this);
        resultChanged(null);
        // hack to init selection if any
        WSDLNavigatorContent.getDefault().propertyChange(new PropertyChangeEvent(this,
                Registry.PROP_ACTIVATED_NODES, false, true));
    }

    public void panelDeactivated() {
        TopComponent.getRegistry().removePropertyChangeListener(WSDLNavigatorContent.getDefault());
        if (selection != null) {
            selection.removeLookupListener(this);
            selection = null;
        }
        // If we set navigator to null its parent tc ref goes away.
        //navigator = null;
        if(WSDLNavigatorContent.getDefault() != null)
            WSDLNavigatorContent.getDefault().release(); //hide the UI
    }

    public void resultChanged(LookupEvent ev) {
        if (selection == null) return;
        Collection selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = (DataObject) selected.iterator().next();
            WSDLNavigatorContent.getDefault().navigate(dobj);
        }
    }
}
