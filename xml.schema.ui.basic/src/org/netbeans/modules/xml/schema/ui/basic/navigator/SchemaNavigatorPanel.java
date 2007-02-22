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

package org.netbeans.modules.xml.schema.ui.basic.navigator;

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
 * An implementation of NavigatorPanel for XML Schema navigator.
 *
 * @author Marek Fukala
 * @author Nathan Fiedler
 */
public class SchemaNavigatorPanel implements LookupListener, NavigatorPanel {
    private SchemaNavigatorContent navigator;
    private Lookup.Result selection;

    /**
     * Public nullary constructor needed for system to instantiate the provider.
     */
    public SchemaNavigatorPanel() {
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(SchemaNavigatorPanel.class,
                "LBL_SchemaNavigatorPanel_Hint");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SchemaNavigatorPanel.class,
                "LBL_SchemaNavigatorPanel_Name");
    }

    public JComponent getComponent() {
	if (navigator == null) {
	    navigator = new SchemaNavigatorContent();
	}
	return navigator;
    }

    public Lookup getLookup() {
        return null;
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
    }

    public void panelDeactivated() {
		TopComponent.getRegistry().removePropertyChangeListener(navigator);
        selection.removeLookupListener(this);
        selection = null;
		// if we set navigator to null its parent tc ref goes away
        // navigator = null;
    }

    public void resultChanged(LookupEvent ev) {
        Collection selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = (DataObject) selected.iterator().next();
            navigator.navigate(dobj);
        }
    }
}
