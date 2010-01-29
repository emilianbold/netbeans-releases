/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.project.libraries.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryCustomizerContext;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

public final class LibrariesCustomizer extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(LibrariesCustomizer.class.getName());

    private ExplorerManager manager;
    private LibrariesModel model;
    private BeanTreeView libraries;
    private LibraryStorageArea libraryStorageArea;

    public LibrariesCustomizer (LibraryStorageArea libraryStorageArea) {
        this.model = new LibrariesModel ();
        this.libraryStorageArea = (libraryStorageArea != null ? libraryStorageArea : LibrariesModel.GLOBAL_AREA);
        initComponents();
        postInitComponents ();
        expandTree();
    }
    
    private void expandTree() {
        // get first library node
        Node[] n1 = getExplorerManager().getRootContext().getChildren().getNodes();
        if (n1.length != 0) { //#130730 in case there are no LibraryTypeProviders in LibraryTypeRegistry.getDefault().getLibraryTypeProviders()
            Node[] n = n1[0].getChildren().getNodes();
            if (n.length != 0) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{n[0]});
                } catch (PropertyVetoException ex) {
                    // OK to ignore - it is just selection initialization
                }
            }
        }
    }
    
    public void setLibraryStorageArea(LibraryStorageArea libraryStorageArea) {
        this.libraryStorageArea = (libraryStorageArea != null ? libraryStorageArea : LibrariesModel.GLOBAL_AREA);
        forceTreeRecreation();
        expandTree();
    }
    
    public LibrariesModel getModel() {
        return model;
    }
    
    public void hideLibrariesList() {
        libsPanel.setVisible(false);
        jLabel2.setVisible(false);
        createButton.setVisible(false);
        deleteButton.setVisible(false);
        jLabel3.setVisible(true);
        libraryLocation.setVisible(true);
    }
    
    /**
     * Force nodes recreation after LibrariesModel change. The nodes listen on
     * model and eventually refresh themselves but usually it is too late.
     * So forcing recreation makes sure that any subsequent call to 
     * NodeOp.findPath is successful and selects just created library node.
     */
    public void forceTreeRecreation() {
        getExplorerManager().setRootContext(buildTree());
    }

    public void setSelectedLibrary (LibraryImplementation library) {
        if (library == null)
            return;
        ExplorerManager currentManager = this.getExplorerManager();
        Node root = currentManager.getRootContext();        
        String[] path = {library.getType(), library.getName()};
        try {
            Node node = NodeOp.findPath(root, path);
            if (node != null) {
                currentManager.setSelectedNodes(new Node[] {node});
            }
        } catch (NodeNotFoundException e) {
            //Ignore it
        }
        catch (PropertyVetoException e) {
            //Ignore it
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( LibrariesCustomizer.class );
    }
    
    public boolean apply () {
        try {
            this.model.apply();
            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    public void addNotify() {
        super.addNotify();
        expandAllNodes(this.libraries,this.getExplorerManager().getRootContext());
        //Select first library if nothing selected
        if (this.getExplorerManager().getSelectedNodes().length == 0) {
            SELECTED: for (Node areaNode : getExplorerManager().getRootContext().getChildren().getNodes(true)) {
                for (Node typeNode : areaNode.getChildren().getNodes(true)) {
                    for (Node libNode : typeNode.getChildren().getNodes(true)) {
                        try {
                            getExplorerManager().setSelectedNodes(new Node[] {libNode});
                        } catch (PropertyVetoException e) {
                            //Ignore it
                        }
                        break SELECTED;
                    }
                }
            }
        }
        this.libraries.requestFocus();
    }    
    
    public ExplorerManager getExplorerManager () {
        if (this.manager == null) {
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener (new PropertyChangeListener() {
                public void propertyChange (PropertyChangeEvent event) {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue ();
                        selectLibrary(nodes);                            
                        libraries.requestFocus();
                    }                    
                }
            });
            this.manager.addVetoableChangeListener(new VetoableChangeListener() {
                public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue();
                        if (nodes.length <=1) {
                            return;
                        }
                        else {
                            throw new PropertyVetoException ("Invalid length", event);  //NOI18N
                        }
                    }
                }
            });            
            manager.setRootContext(buildTree());
        }
        return this.manager;
    }

    private void postInitComponents () {
        this.libraries = new LibrariesView ();        
        this.libsPanel.setLayout(new BorderLayout());
        this.libsPanel.add(this.libraries);
        this.libraries.setPreferredSize(new Dimension (200,334));
        this.libraryName.setColumns(25);
        this.libraryName.setEnabled(false);
        this.libraryName.addActionListener(
                new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        nameChanged();
                    }
                });                        
        jLabel3.setVisible(false);
        libraryLocation.setVisible(false);
        createButton.setEnabled(LibraryTypeRegistry.getDefault().getLibraryTypeProviders().length>0);
    }

    private void nameChanged () {
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            LibraryImplementation lib = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (lib == null) {
                return;
            }
            String newName = this.libraryName.getText();
            if (newName.equals(lib.getName())) {
                return;
            }
            if (newName.length () == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_InvalidName"),
                        NotifyDescriptor.ERROR_MESSAGE));
            } else if (isValidName(model, newName, model.getArea(lib))) {
                lib.setName(newName);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_ExistingName", newName),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }                        
    }

    private void selectLibrary (Node[] nodes) {
        int tabCount = this.properties.getTabCount();
        for (int i=0; i<tabCount; i++) {
            this.properties.removeTabAt(0);
        }
        this.libraryName.setEnabled(false);
        this.libraryName.setText("");   //NOI18N
        this.jLabel1.setVisible(false);
        this.libraryName.setVisible(false);
        this.properties.setVisible(false);
        this.deleteButton.setEnabled(false);        
        if (nodes.length != 1) {
            return;
        }
        LibraryImplementation impl = nodes[0].getLookup().lookup(LibraryImplementation.class);
        if (impl == null) {
            return;
        }
        this.jLabel1.setVisible(true);
        this.libraryName.setVisible(true);
        this.properties.setVisible(true);
        boolean editable = model.isLibraryEditable (impl);
        this.libraryName.setEnabled(editable);
        this.deleteButton.setEnabled(editable);
        this.libraryName.setText (getLocalizedName(impl));
        LibraryTypeProvider provider = nodes[0].getLookup().lookup(LibraryTypeProvider.class);
        if (provider == null)
            return;
        LibraryCustomizerContextWrapper customizerContext;
        LibraryStorageArea area = nodes[0].getLookup().lookup(LibraryStorageArea.class);
        if (area != null && area != LibrariesModel.GLOBAL_AREA) {
            customizerContext = new LibraryCustomizerContextWrapper(impl, area);
            File f = new File(URI.create(area.getLocation().toExternalForm()));
            this.libraryLocation.setText(f.getPath());
        } else {
            customizerContext = new LibraryCustomizerContextWrapper(impl, null);
            this.libraryLocation.setText(NbBundle.getMessage(LibrariesCustomizer.class,"LABEL_Global_Libraries"));
        }

        String[] volumeTypes = provider.getSupportedVolumeTypes();
        for (int i=0; i< volumeTypes.length; i++) {
            Customizer c = provider.getCustomizer (volumeTypes[i]);
            if (c instanceof JComponent) {
                c.setObject (customizerContext);
                JComponent component = (JComponent) c;
                component.setEnabled (editable);
                String tabName = component.getName();
                if (tabName == null) {
                    tabName = volumeTypes[i];
                }
                this.properties.addTab(tabName, component);
            }
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        libraryName = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        libsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        properties = new javax.swing.JTabbedPane();
        jLabel3 = new javax.swing.JLabel();
        libraryLocation = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(642, 395));

        jLabel1.setLabelFor(libraryName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_CustomizerLibraryName")); // NOI18N

        libraryName.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, bundle.getString("CTL_NewLibrary")); // NOI18N
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibrary(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, bundle.getString("CTL_DeleteLibrary")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibrary(evt);
            }
        });

        libsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout libsPanelLayout = new javax.swing.GroupLayout(libsPanel);
        libsPanel.setLayout(libsPanelLayout);
        libsPanelLayout.setHorizontalGroup(
            libsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        libsPanelLayout.setVerticalGroup(
            libsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 365, Short.MAX_VALUE)
        );

        jLabel2.setLabelFor(libsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("TXT_LibrariesPanel")); // NOI18N

        properties.setPreferredSize(new java.awt.Dimension(400, 300));

        jLabel3.setLabelFor(libraryLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_CustomizerLibraryLocationName")); // NOI18N

        libraryLocation.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(libsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(libraryLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                                    .addComponent(libraryName, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)))
                            .addComponent(properties, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(libraryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(libraryLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(properties, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                    .addComponent(libsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createButton)
                    .addComponent(deleteButton)))
        );

        libraryName.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryName")); // NOI18N
        createButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_NewLibrary")); // NOI18N
        deleteButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_DeleteLibrary")); // NOI18N
        libsPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_libsPanel")); // NOI18N
        properties.getAccessibleContext().setAccessibleName(bundle.getString("AN_LibrariesCustomizerProperties")); // NOI18N
        properties.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizerProperties")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription("Edit Library");
        libraryLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryLocation")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void deleteLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrary
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            LibraryImplementation library = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (library == null) {
                return;
            }
            Node[] sib = nodes[0].getParentNode().getChildren().getNodes(true);            
            Node selNode = null;
            for (int i=0; i < sib.length; i++) {
                if (nodes[0].equals(sib[i])) {
                    if (i>0) {
                        selNode = sib[i-1];
                    }
                    else if (i<sib.length-1){
                        selNode = sib[i+1];
                    }
                }
            }            
            model.removeLibrary(library);
            try {
                if (selNode != null) {
                    this.getExplorerManager().setSelectedNodes(new Node[] {selNode});            
                }
            } catch (PropertyVetoException e) {
                //Ignore it
            }
            this.libraries.requestFocus();
        }
    }//GEN-LAST:event_deleteLibrary

    private void createLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibrary
        Dialog dlg = null;
        try {
            String preselectedLibraryType = null;
            LibraryStorageArea area = null;
            Node[] preselectedNodes = this.getExplorerManager().getSelectedNodes();
            if (preselectedNodes.length == 1) {
                LibraryTypeProvider provider = preselectedNodes[0].getLookup().lookup(LibraryTypeProvider.class);
                if (provider != null) {
                    preselectedLibraryType = provider.getLibraryType();
                }
                area = preselectedNodes[0].getLookup().lookup(LibraryStorageArea.class);
            }
            if (area == null) {
                area = LibrariesModel.GLOBAL_AREA;
            }
            NewLibraryPanel p = new NewLibraryPanel(model, preselectedLibraryType, area);
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(LibrariesCustomizer.class,"CTL_CreateLibrary"),
                    true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
            p.setDialogDescriptor(dd);
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                String libraryType = p.getLibraryType();
                String currentLibraryName = p.getLibraryName();
                LibraryImplementation impl;
                if (area != LibrariesModel.GLOBAL_AREA) {
                    impl = model.createArealLibrary(libraryType, currentLibraryName, area);
                } else {
                    LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(libraryType);
                    if (provider == null) {
                        return;
                    }
                    impl = provider.createLibrary();
                    impl.setName(currentLibraryName);
                }
                model.addLibrary (impl);                
                forceTreeRecreation();
                String[] path = {impl.getType(), impl.getName()};
                ExplorerManager mgr = this.getExplorerManager();
                try {
                    Node node = NodeOp.findPath(mgr.getRootContext(),path);
                    if (node != null) {
                        mgr.setSelectedNodes(new Node[] {node});
                    }
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
                catch (NodeNotFoundException e) {
                    //Ignore it
                }
                this.libraryName.requestFocus();
                this.libraryName.selectAll();
            }
            else {
                this.libraries.requestFocus();
            }
        }
        finally {
            if (dlg != null)
                dlg.dispose();
        }
    }//GEN-LAST:event_createLibrary

    static boolean isValidName(LibrariesModel model, String name, LibraryStorageArea area) {
        for (LibraryImplementation lib : model.getLibraries()) {
            if (lib.getName().equals(name) && Utilities.compareObjects(model.getArea(lib), area)) {
                return false;
            }
        }
        return true;
    }

    private static Map<LibraryImplementation,FileObject> sources = new WeakHashMap<LibraryImplementation,FileObject>();
    public static void registerSource(LibraryImplementation impl, FileObject descriptorFile) {
        sources.put(impl, descriptorFile);
    }


    public static String getLocalizedName(LibraryImplementation impl) {
        FileObject src = sources.get(impl);
        if (src != null) {
            Object obj = src.getAttribute("displayName"); // NOI18N
            if (obj instanceof String) {
                return (String)obj;
            }
        }
        if (impl instanceof ProxyLibraryImplementation) {
            String proxiedName = getLocalizedName(((ProxyLibraryImplementation)impl).getOriginal());
            if (proxiedName != null) {
                return proxiedName;
            }
        }

        return getLocalizedString(impl.getLocalizingBundle(), impl.getName());
    }

    static String getLocalizedString (String bundleResourceName, String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            LOG.log(Level.INFO, "Wrong resource bundle", mre);      //NOI18N
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField libraryLocation;
    private javax.swing.JTextField libraryName;
    private javax.swing.JPanel libsPanel;
    private javax.swing.JTabbedPane properties;
    // End of variables declaration//GEN-END:variables

    private static void expandAllNodes (BeanTreeView btv, Node node) {
        btv.expandNode (node);
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {            
            return;
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i]);
        }

    }
    
    private static class LibrariesView extends BeanTreeView {
        
        public LibrariesView () {
            super ();
            this.setRootVisible(false);
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.tree.setEditable (false);
            this.tree.setShowsRootHandles (false);
            this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LibrariesCustomizer.class, "AD_Libraries"));
        }
        
    }
    
    private class AreaChildren extends Children.Keys<LibraryStorageArea> implements ChangeListener {

        @Override
        protected void addNotify() {
            super.addNotify();
            model.addChangeListener(this);
            computeKeys();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            model.removeChangeListener(this);
            setKeys(Collections.<LibraryStorageArea>emptySet());
        }

        private void computeKeys() {
            setKeys(getSortedAreas(model));
        }

        protected Node[] createNodes(LibraryStorageArea area) {
            return new Node[] {new AreaNode(area)};
        }

        public void stateChanged(ChangeEvent e) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    computeKeys();
                }
            });
        }

    }

    static Collection<? extends LibraryStorageArea> getSortedAreas(LibrariesModel model) {
        List<LibraryStorageArea> areas = new ArrayList<LibraryStorageArea>(model.getAreas());
        Collections.sort(areas,new Comparator<LibraryStorageArea>() {
            Collator COLL = Collator.getInstance();
            public int compare(LibraryStorageArea a1, LibraryStorageArea a2) {
                return COLL.compare(a1.getDisplayName(), a2.getDisplayName());
            }
        });
        areas.add(0, LibrariesModel.GLOBAL_AREA);
        assert !areas.contains(null);
        return areas;
    }

    private final class AreaNode extends AbstractNode {

        private final LibraryStorageArea area;

        AreaNode(LibraryStorageArea area) {
            super(new TypeChildren(area), Lookups.singleton(area));
            this.area = area;
        }

        @Override
        public String getName() {
            return getDisplayName();
        }

        @Override
        public String getDisplayName() {
            return area.getDisplayName();
        }

        private Node delegate() {
            return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        public Image getIcon(int type) {
            return delegate().getIcon(type);
        }

        public Image getOpenedIcon(int type) {
            return delegate().getOpenedIcon(type);
        }

    }

    private class TypeChildren extends Children.Keys<LibraryTypeProvider> {

        private final LibraryStorageArea area;

        TypeChildren(LibraryStorageArea area) {
            this.area = area;
        }

        @Override
        public void addNotify () {
            // Could also filter by area (would then need to listen to model too)
            this.setKeys(LibraryTypeRegistry.getDefault().getLibraryTypeProviders());
        }
        
        @Override
        public void removeNotify () {
            this.setKeys(new LibraryTypeProvider[0]);
        }
        
        protected Node[] createNodes(LibraryTypeProvider provider) {
            return new Node[] {new CategoryNode(provider, area)};
        }
        
    }
    
    private class CategoryNode extends AbstractNode {
        
        private LibraryTypeProvider provider;
        private Node iconDelegate;
                
        public CategoryNode(LibraryTypeProvider provider, LibraryStorageArea area) {
            super(new CategoryChildren(provider, area), Lookups.fixed(provider, area));
            this.provider = provider;       
            this.iconDelegate = DataFolder.findFolder (FileUtil.getConfigRoot()).getNodeDelegate();
        }
        
        public String getName () {
            return provider.getLibraryType ();
        }
        
        public String getDisplayName() {
            return this.provider.getDisplayName();
        }
        
        public Image getIcon(int type) {            
            return this.iconDelegate.getIcon (type);
        }        
        
        public Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon (type);
        }        
                        
    }    

    private class CategoryChildren extends Children.Keys<LibraryImplementation> implements ChangeListener {
        
        private LibraryTypeProvider provider;
        private final LibraryStorageArea area;
        
        public CategoryChildren(LibraryTypeProvider provider, LibraryStorageArea area) {
            this.provider = provider;
            this.area = area;
            model.addChangeListener(this);
        }
        
        public void addNotify () {
            Collection<LibraryImplementation> keys = new ArrayList<LibraryImplementation>();
            for (LibraryImplementation impl : model.getLibraries()) {
                if (provider.getLibraryType().equals(impl.getType()) && model.getArea(impl).equals(area)) {
                    keys.add (impl);
                }
            }
            this.setKeys(keys);
        }
        
        public void removeNotify () {
            this.setKeys(new LibraryImplementation[0]);
        }
        
        protected Node[] createNodes(LibraryImplementation impl) {
            return new Node[] {new LibraryNode(impl, provider, area)};
        }
        
        public void stateChanged(ChangeEvent e) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    addNotify();
                }
            });
        }
        
    }
    
    private static class LibraryNode extends AbstractNode {
        
        private static final String ICON = "org/netbeans/modules/project/libraries/resources/libraries.gif";  //NOI18N
        
        private LibraryImplementation lib;
        private LibraryTypeProvider provider;
        
        public LibraryNode(LibraryImplementation lib, LibraryTypeProvider provider, LibraryStorageArea area) {
            super(Children.LEAF, Lookups.fixed(lib, provider, area));
            this.lib = lib;
            this.provider = provider;
            this.setIconBaseWithExtension(ICON);
        }
        
        public String getName () {            
            return this.lib.getName ();
        }
        
        public String getDisplayName () {
            return getLocalizedName(this.lib);
        }
        
    }
    
    private Node buildTree() {
        return new AbstractNode(new TypeChildren(libraryStorageArea));
    }
    
    /**
     * This is backward compatible wrapper which can be passed to libraries customizer
     * via JComponent.setObject and which provides to customizer both LibraryImplementation
     * (old contract) and LibraryCustomizerContext (new contract).
     */
    private static class LibraryCustomizerContextWrapper extends LibraryCustomizerContext implements LibraryImplementation {
        
        public LibraryCustomizerContextWrapper(LibraryImplementation lib, LibraryStorageArea area) {
            super(lib, area);
        }

        public String getType() {
            return getLibraryImplementation().getType();
        }

        public String getName() {
            return getLibraryImplementation().getName();
        }

        public String getDescription() {
            return getLibraryImplementation().getDescription();
        }

        public String getLocalizingBundle() {
            return getLibraryImplementation().getLocalizingBundle();
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            return getLibraryImplementation().getContent(volumeType);
        }

        public void setName(String name) {
            getLibraryImplementation().setName(name);
        }

        public void setDescription(String text) {
            getLibraryImplementation().setDescription(text);
        }

        public void setLocalizingBundle(String resourceName) {
            getLibraryImplementation().setLocalizingBundle(resourceName);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            getLibraryImplementation().addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            getLibraryImplementation().removePropertyChangeListener(l);
        }

        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            getLibraryImplementation().setContent(volumeType, path);
        }
    }
    
}
