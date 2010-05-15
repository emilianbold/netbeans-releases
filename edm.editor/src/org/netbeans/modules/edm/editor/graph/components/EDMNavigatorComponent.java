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
package org.netbeans.modules.edm.editor.graph.components;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class EDMNavigatorComponent extends JPanel implements NavigatorPanel {

    private static final Logger mLogger = Logger.getLogger(EDMNavigatorComponent.class.getName());
    /** template for finding data in given context.
     * Object used as example, replace with your own data source, for example JavaDataObject etc */
    private static final Lookup.Template EDM_DATA = new Lookup.Template(MashupDataObject.class);
    /** current context to work on */
    private Lookup.Result curContext;
    /** listener to context changes */
    private LookupListener contextL;
    private static EDMNavigatorComponent instance = new EDMNavigatorComponent();

    public static EDMNavigatorComponent getInstance() {
        return instance;
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(EDMNavigatorComponent.class, "TITLE_EDM_Editor_Navigator");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EDMNavigatorComponent.class, "TITLE_EDM_Editor_Navigator");
    }

    public JComponent getComponent() {
        return instance;
    }

    public void panelActivated(Lookup context) {
        // lookup context and listen to result to get notified about context changes
        curContext = context.lookup(EDM_DATA);
        curContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection data = curContext.allInstances();
        //setNewContent(data);
    }

    public void panelDeactivated() {
        curContext.removeLookupListener(getContextListener());
        curContext = null;
    }

    public Lookup getLookup() {
        // go with default activated Node strategy
        return null;
    }


    /************* non - public part ************/
    public void setNewContent(MashupDataObject dObj) {
        getComponent().removeAll();
        try {
            getComponent().add(dObj.getGraphManager().getSatelliteView());
            revalidate();
        } catch (Exception ex) {            
        }
    }   

    /** Accessor for listener to context */
    private LookupListener getContextListener() {
        if (contextL == null) {
            contextL = new ContextListener();
        }
        return contextL;
    }

    /** Listens to changes of context and triggers proper action */
    private class ContextListener implements LookupListener {

        public void resultChanged(LookupEvent ev) {
            Collection data = ((Lookup.Result) ev.getSource()).allInstances();
            //setNewContent(data);
        }
    } // end of ContextListener
}
