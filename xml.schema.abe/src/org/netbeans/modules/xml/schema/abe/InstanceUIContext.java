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

package org.netbeans.modules.xml.schema.abe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import javax.swing.JScrollPane;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.spi.palette.PaletteController;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Todd
 */
public class InstanceUIContext {
    ////////////////////////////////////////////////////////////////////////////
    // Instance members
    ////////////////////////////////////////////////////////////////////////////
    
    protected InstanceDesignerPanel instanceDesignerPanel;
    protected PaletteController paletteController;
    private boolean userInducedEventMode;
    private JScrollPane instanceDesignerScrollPane;
    private TopComponent topComponent;
    private DataObject schemaDataObject;
    private ComponentSelectionManager componentSelectionManager;
    private MultiComponentActionManager mcaManager;
    private boolean shutdown = false;
    
    protected InstanceUIContext(){
        componentSelectionManager = new ComponentSelectionManager(this);
        mcaManager = new MultiComponentActionManager(this);
    }
        
    public InstanceDesignerPanel getInstanceDesignerPanel() {
        return instanceDesignerPanel;
    }    
        
    public SchemaModel getSchemaModel() {
        return getInstanceDesignerPanel().getSchemaModel();
    }
    
    public ComponentSelectionManager getComponentSelectionManager() {
        return componentSelectionManager;
    }
    
    public MultiComponentActionManager getMultiComponentActionManager(){
        return mcaManager;
    }
    
    public boolean isUserInducedEventMode() {
        return this.userInducedEventMode;
    }
    
    public void setUserInducedEventMode(boolean userInducedEventMode) {
        this.userInducedEventMode = userInducedEventMode;
        if(!userInducedEventMode)
            this.userActedComponent = null;
    }
    
    ABEBaseDropPanel userActedComponent;
    public void setUserInducedEventMode(boolean eventMode, ABEBaseDropPanel userActedComponent){
        setUserInducedEventMode(eventMode);
        this.userActedComponent = userActedComponent;
    }
    
    public ABEBaseDropPanel getUserActedComponent(){
        return userActedComponent;
    }
    
    public void resetUserActedComponent() {
        this.userActedComponent = null;
    }
    
    public JScrollPane getInstanceDesignerScrollPane() {
        return instanceDesignerScrollPane;
    }
    
    public void setInstanceDesignerScrollPane(JScrollPane instanceDesignerScrollPane) {
        this.instanceDesignerScrollPane = instanceDesignerScrollPane;
    }
        
    public TopComponent getTopComponent(){
        return topComponent;
    }
        
    public Lookup getLookup() {
        return getSchemaDataObject().getNodeDelegate().getLookup();
    }
    
    public DataObject getSchemaDataObject() {
        return schemaDataObject;
    }
        
    void showPopupMenu() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public AXIModel getAXIModel(){
        return instanceDesignerPanel.getAXIModel();
    }
    
    public NamespacePanel getNamespacePanel(){
        return getInstanceDesignerPanel().getNamespacePanel();
    }
    
    private FocusTraversalManager focusTraversalManager;
    public void setFocusTraversalManager(FocusTraversalManager focusTraversalManager) {
        this.focusTraversalManager = focusTraversalManager;
    }
    
    public FocusTraversalManager getFocusTraversalManager() {
        return this.focusTraversalManager;
    }

    PropertyChangeSupport pcs;
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        if(pcs == null)
            pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        if(pcs == null)
            return;
        pcs.removePropertyChangeListener(pcl);
    }
    
    public void shutdown() {
        this.shutdown = true;
        pcs.firePropertyChange(InstanceDesignConstants.PROP_SHUTDOWN, null, true);
        this.componentSelectionManager = null;
        this.mcaManager = null;
        this.pcs = null;
        this.userActedComponent = null;
        this.focusTraversalManager = null;
    }
    
    public boolean isShutdown() {
        return shutdown;
    }

    void initialize(TopComponent tc, DataObject schemaDataObject,
            InstanceDesignerPanel instanceDesignerPanel, PaletteController paletteController) {
        this.topComponent = tc;
        this.schemaDataObject = schemaDataObject;
        this.paletteController = paletteController;
        this.instanceDesignerPanel = instanceDesignerPanel;        
    }
}
