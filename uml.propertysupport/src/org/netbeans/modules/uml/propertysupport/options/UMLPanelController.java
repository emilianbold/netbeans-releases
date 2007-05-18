/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
 * MyPanelController.java
 *
 * Created on October 28, 2005, 3:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
/**
 *
 * @author krichard
 */
public class UMLPanelController extends OptionsPanelController {
    
    
    private boolean changed = false ;
    private static final boolean debug = false ;
    private DefaultOptionsPanel defaultPanel = null ;
    private MiscOptionsPanel misc = null ;
    private OptionsSupport support = null ;
    /**
     * Creates a new instance of UMLPanelController
     */
    public UMLPanelController() {
        if (debug) log("MyPanelController");
    }
    
    
    public void update() {
        changed = false ;
        if (debug) log("update");
        
        // (1) apply changes for "general" panel options
        defaultPanel.update();
        
        // (2..n) apply changes for registered panel options
        for (UMLOptionsPanel panel: support.getMainPanels()) {
            panel.update(); 
        }
        
        //((n+1)..m) apply changes for misc panels options
        for (UMLOptionsPanel panel: support.getMiscPanels()) {
            panel.update(); 
        }
    }
    
    public void applyChanges() {
        changed = true ;
        if (debug) log("applyChanges");
        
        // (1) apply changes for "general" panel options
        defaultPanel.applyChanges();
        
        // (2..n) apply changes for registered panel options
        for (UMLOptionsPanel panel: support.getMainPanels()) {
            panel.applyChanges(); 
        }
        
        //((n+1)..m) apply changes for misc panels options
        for (UMLOptionsPanel panel: support.getMiscPanels()) {
            panel.applyChanges(); 
        }
    }
    
    public void cancel() {
        if (debug) log("cancel");
    }
    
    public boolean isValid() {
        if (debug) log("isValid");
        return true ;
    }
    
    public boolean isChanged() {
        if (debug) log("isChanged"); 
        return changed ;
    }
    
    public JComponent getComponent(Lookup lookup) {
        
        if (debug) log("getComponent");
        
        // this is the main component that holds the various tabs. There are two
        // panels being added directly: General and Misc. The other tabs are found
        // from the lookup (via layer file). See OptionsSupport getMainPanels() and
        // getMiscPanels().
        JTabbedPane pane = new JTabbedPane() ;
        
        // (1) create the General panel and populate it.
        defaultPanel = new DefaultOptionsPanel() ;
        DefaultOptionsPanelForm defaultForm = (DefaultOptionsPanelForm) defaultPanel.create() ;
        
        pane.addTab(defaultPanel.getDisplayName(), defaultForm) ;
        
        // (2..n) add all panels that are registered via layer files
        support = new OptionsSupport() ;
        
        for (UMLOptionsPanel panel:support.getMainPanels()) {
            pane.addTab(panel.getDisplayName(), panel.create()) ;
        }
        
        // ((n+1)..m) create the Misc panel and populate it.
        misc = new MiscOptionsPanel() ;
        UMLMiscOptionsPanelForm miscPanel = (UMLMiscOptionsPanelForm) misc.create() ;
        
        pane.addTab(misc.getDisplayName(), miscPanel) ;
        
        for (UMLOptionsPanel panel:support.getMiscPanels()) {
            miscPanel.addTab(panel) ;
        }
        
        
        return pane ;
        
    }
    
    public HelpCtx getHelpCtx() {
        if (debug) log("getHelpCtx");
        return HelpCtx.DEFAULT_HELP ;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("addPropertyChangeListener::"+propertyChangeListener.toString());
        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("removePropertyChangeListener");
        
    }
    
    private static void log(String s) {
        System.out.println("MyPanelController::"+s);
    }
    
}
