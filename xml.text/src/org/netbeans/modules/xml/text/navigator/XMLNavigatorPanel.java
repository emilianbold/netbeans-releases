/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/** An implementation of NavigatorPanel for XML navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class XMLNavigatorPanel implements NavigatorPanel {
    
    private NavigatorContent navigator = NavigatorContent.getDefault();
    
    private Lookup.Result selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            navigate(selection.allInstances());
        }
    };
    
    /** public no arg constructor needed for system to instantiate the provider. */
    public XMLNavigatorPanel() {
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(XMLNavigatorPanel.class, "XML_files_navigator");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(XMLNavigatorPanel.class, "XML_View");
    }
    
    public JComponent getComponent() {
        return navigator;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void panelActivated(Lookup context) {
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        selection.removeLookupListener(selectionListener);
        selection = null;
        navigator.release(); //hide the UI
    }
    
    public void navigate(Collection/*<DataObject>*/ selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            navigator.navigate(d);
        }
    }
    
}
