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

package org.netbeans.modules.xml.text.navigator.base;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 * A base implementation of NavigatorPanel for all XML navigators.
 *
 * @author Samaresh
 * @version 1.0
 */
public abstract class AbstractXMLNavigatorPanel implements NavigatorPanel {

    protected AbstractXMLNavigatorContent navigator;
    protected Lookup.Result selection;
    protected final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            if(selection == null)
                return;
            navigate(selection.allInstances());
        }
    };
    
    /**
     * 
     */
    public AbstractXMLNavigatorPanel() {
        getNavigatorUI();
    }
    
    public abstract String getDisplayHint();
    
    public abstract String getDisplayName();
    
    /**
     * 
     * @return AbstractXMLNavigatorContent
     */
    protected abstract AbstractXMLNavigatorContent getNavigatorUI();
    
    public JComponent getComponent() {
        return getNavigatorUI();
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        if(navigator != null)
            navigator.release(); //hide the UI
    }
    
    /**
     * 
     * @param selectedFiles 
     */
    public void navigate(Collection/*<DataObject>*/ selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            navigator.navigate(d);           
        }
    }
    
   
}
