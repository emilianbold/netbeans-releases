/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
