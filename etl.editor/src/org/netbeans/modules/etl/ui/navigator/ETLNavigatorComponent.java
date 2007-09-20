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
package org.netbeans.modules.etl.ui.navigator;


import org.netbeans.modules.etl.ui.*;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


public class ETLNavigatorComponent implements NavigatorPanel, NavigatorLookupHint {
    
    /** holds UI of this panel */
    private JComponent panelUI;
    
    /** template for finding data in given context.
     * Object used as example, replace with your own data source, for example JavaDataObject etc */
    @SuppressWarnings("unchecked")
    private static final Lookup.Template ETL_DATA = new Lookup.Template(ETLDataObject.class);
    /** current context to work on */
    private Lookup.Result curContext;
    /** listener to context changes */
    private LookupListener contextL;
    
    /** public no arg constructor needed for system to instantiate provider well */
    public ETLNavigatorComponent() {
    }
    
    public String getDisplayHint() {
        return null;//"Basic dummy implementation of NavigatorPanel interface";
    }
    
    public String getDisplayName() {
        return null;//"Dummy View";
    }
    
    public JComponent getComponent() {
        if(panelUI == null) {
            panelUI = new JPanel();
            //panelUI.setBorder(javax.swing.BorderFactory.createEmptyBorder(50,50,50,50));//, left, bottom, right)createTitledBorder("Navigator Panel Title"));
            //panelUI.setSize(75,75);
            //panelUI = new JLabel("Dummy label");            
        }
        return panelUI; 
    }
    
    @SuppressWarnings("unchecked")
    public void panelActivated(Lookup context) {
        // lookup context and listen to result to get notified about context changes
        curContext = context.lookup(ETL_DATA);
        curContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection data = curContext.allInstances();
        setNewContent(data);
    }
    
    public void panelDeactivated() {
        curContext.removeLookupListener(getContextListener());
        curContext = null;
        panelUI = null;
    }
    
    public Lookup getLookup() {
        // go with default activated Node strategy
        return null;
    }
    
    /************* non - public part ************/
    
    private void setNewContent(Collection newData) {
        java.util.logging.Logger.getLogger(ETLNavigatorComponent.class.getName()).info("_____________________ setNewContent ");
        // put your code here that grabs information you need from given
        // collection of data, recompute UI of your panel and show it.
        // Note - be sure to compute the content OUTSIDE event dispatch thread,
        // just final repainting of UI should be done in event dispatch thread.
        // Please use RequestProcessor and Swing.invokeLater to achieve this.
        Iterator it = newData.iterator();
        while(it.hasNext()) {
            ETLDataObject dObj = (ETLDataObject) it.next();
            if(panelUI == null) {
                panelUI = new JPanel();
                panelUI.setBorder(javax.swing.BorderFactory.createEmptyBorder(50,50,50,50));//, left, bottom, right)createTitledBorder("Navigator Panel Title"));
                panelUI.setSize(150,150);
            }
            //panelUI.removeAll();
            while(true) {
                try {                                      
                   //panelUI = dObj.getETLEditorTC().getSatelliteView();
                    //panelUI.add((JComponent)dObj.getETLEditorTC().getGraphView());
                    //dObj.getETLEditorTC().getGraphView().getObserved();
                    panelUI.add(dObj.getETLEditorTC().getSatelliteView());
                    panelUI.updateUI();
                    panelUI.requestFocus();
                    break;
                } catch (Exception exception) {
                    // wait till scene is loaded
                }
            }            
            panelUI.revalidate();
            break;
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
            Collection data = ((Lookup.Result)ev.getSource()).allInstances();
            setNewContent(data);
        }
        
    } // end of ContextListener
    
    public String getContentType() {
        return "x-etl+xml";
    }
}
