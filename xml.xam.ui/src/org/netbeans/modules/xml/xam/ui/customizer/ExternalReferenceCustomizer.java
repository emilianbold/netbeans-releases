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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.explorer.view.BeanTreeView;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Base class for external reference customizers.
 *
 * @author  Ajit Bhate
 * @author  Nathan Fiedler
 */
public abstract class ExternalReferenceCustomizer<T extends Component>
        extends AbstractReferenceCustomizer<T>
        implements DocumentListener, ExplorerManager.Provider,
        PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** If true, the prefix was generated and not edited by the user. */
    private transient boolean prefixGenerated;
    /** The file being modified (where the import will be added). */
    private transient FileObject sourceFO;
    /** The file selected by the user. */
    private transient FileObject referencedFO;
    /** Used to deal with project catalogs. */
    private transient DefaultProjectCatalogSupport catalogSupport;
    
    /**
     * Creates new form ExternalReferenceCustomizer
     *
     * @param  component  external reference to customize.
     */
    public ExternalReferenceCustomizer(T component, Model model) {
        super(component);
        initComponents();
        sourceFO = (FileObject) component.getModel().getModelSource().
                getLookup().lookup(FileObject.class);
        catalogSupport = DefaultProjectCatalogSupport.getInstance(sourceFO);
        init(component, model);
        initializeUI();
        // View for selecting an external reference.
        BeanTreeView locationView = new BeanTreeView();
        locationView.setPopupAllowed(false);
        locationView.setDefaultActionAllowed(false);
        locationView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        locationView.setRootVisible(false);
        locationView.getAccessibleContext().setAccessibleName(locationLabel.getToolTipText());
        locationView.getAccessibleContext().setAccessibleDescription(locationLabel.getToolTipText());
        locationPanel.add(locationView, BorderLayout.CENTER);
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        explorerManager.setRootContext(createRootNode());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Force the Ok button to be disabled initially.
        firePropertyChange(PROP_ACTION_APPLY, true, false);
    }

    public void applyChanges() throws IOException {
        if (mustNamespaceDiffer() && isPrefixChanged()) {
            prefixTextField.setEditable(false);
        }
        if (isLocationChanged() && referencedFO != null &&
                catalogSupport.needsCatalogEntry(sourceFO, referencedFO)) {
            try {
                catalogSupport.createCatalogEntry(sourceFO, referencedFO);
            } catch (IOException ioe) {
            } catch (CatalogModelException cme) {
            }
        }
    }

    /**
     * Retrieves the location value from the interface.
     *
     * @return  new location value.
     */
    protected String getEditedLocation() {
        if (referencedFO == null) {
            return getReferenceLocation();
        }
        try {
            return catalogSupport.getReferenceURI(sourceFO, referencedFO).toString();
        } catch (URISyntaxException ex) {
        }
        return null;
    }
    
    /**
     * Retrieves the namespace value from the interface.
     *
     * @return  new namespace value, sans leading and trailing whitespace.
     */
    protected String getEditedNamespace() {
        return namespaceTextField.getText().trim();
    }
    
    /**
     * Retrieves the prefix value from the interface.
     *
     * @return  new prefix value, sans leading and trailing whitespace.
     */
    protected String getEditedPrefix() {
        return prefixTextField.getText().trim();
    }
    
    /**
     * Returns the location value from the original component.
     *
     * @return  original location value.
     */
    protected abstract String getReferenceLocation();
    
    /**
     * Returns the namespace value from the original component.
     *
     * @return  original namespace value.
     */
    protected abstract String getNamespace();
    
    /**
     * Returns the prefix value from the original component.
     *
     * @return  original prefix value.
     */
    protected abstract String getPrefix();
    
    /**
     * Generate a unique prefix value (e.g. "ns1") for the component.
     *
     * @return  unique prefix value.
     */
    protected abstract String generatePrefix();
    
    /**
     * Indicates if the location value was changed in the interface.
     *
     * @return  true if location was changed.
     */
    protected boolean isLocationChanged() {
        String rl = getReferenceLocation();
        String el = getEditedLocation();
        if (rl == null) {
            return el != null;
        }
        return !rl.equals(el);
    }
    
    /**
     * Indicates if the namespace value was changed in the interface.
     *
     * @return  true if namespace was changed.
     */
    protected boolean isNamespaceChanged() {
        if (!mustNamespaceDiffer()) {
            return false;
        }
        String ns = getNamespace();
        String ens = getEditedNamespace();
        if (ns == null) {
            return ens.length() > 0;
        }
        return !ns.equals(ens);
    }
    
    /**
     * Indicates if the prefix value was changed in the interface.
     *
     * @return  true if prefix was changed.
     */
    protected boolean isPrefixChanged() {
        if (!mustNamespaceDiffer()) {
            return false;
        }
        if (prefixGenerated) {
            // User has not yet modified the prefix value.
            return false;
        }
        String p = getPrefix();
        String ep = getEditedPrefix();
        if (p == null) {
            return ep.length() > 0;
        }
        return !p.equals(ep);
    }
    
    /**
     * Called from constructor, after the interface components have been
     * constructed, but before they have been initialized. Gives subclasses
     * a chance to perform initialization based on the given component.
     *
     * @param  component  the reference to be customized.
     * @param  model      the model passed to the constructor (may be null).
     */
    protected void init(T component, Model model) {
        // Note, do not place any code here, as there is no guarantee
        // that the subclasses will delegate to this method at all.
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        prefixGenerated = false;
        validateInput();
    }

    public void removeUpdate(DocumentEvent e) {
        prefixGenerated = false;
        validateInput();
    }
    
    protected void initializeUI() {
        // TODO select in panel
        if (mustNamespaceDiffer()) {
            namespaceTextField.setText(getNamespace());
            prefixTextField.getDocument().removeDocumentListener(this);
            String prefix = getPrefix();
            if (prefix != null) {
                prefixTextField.setText(prefix);
                prefixTextField.setEditable(false);
                prefixGenerated = false;
            } else {
                prefix = generatePrefix();
                prefixGenerated = true;
                prefixTextField.setText(prefix);
                prefixTextField.getDocument().addDocumentListener(this);
            }
        } else {
            namespaceLabel.setVisible(false);
            namespaceTextField.setVisible(false);
            prefixLabel.setVisible(false);
            prefixTextField.setVisible(false);
        }
    }
    
    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return new ExternalReferenceDataNode(original, getNodeDecorator());
    }
    
    /**
     * Determine if the user's input is valid or not. This will enable
     * or disable the save/reset controls based on the results, as well
     * as issue error messages.
     */
    private void validateInput() {
        boolean lChanged = isLocationChanged();
        boolean nsChanged = isNamespaceChanged();
        boolean pChanged = isPrefixChanged();
        if (!lChanged && !nsChanged && !pChanged) {
            setSaveEnabled(false);
            setResetEnabled(false);
        } else {
            setResetEnabled(true);
            String msg = null;
            if (mustNamespaceDiffer()) {
                if (lChanged && getEditedLocation() == null) {
                    msg = NbBundle.getMessage(ExternalReferenceCustomizer.class,
                            "LBL_ExternalReferenceCustomizer_InvalidLocation");
                }
                Map<String, String> prefixMap = getPrefixes(getModelComponent().getModel());
                String ep = getEditedPrefix();
                if (pChanged && (ep.length() == 0 || prefixMap.containsKey(ep))) {
                    msg = NbBundle.getMessage(ExternalReferenceCustomizer.class,
                            "LBL_ExternalReferenceCustomizer_InvalidPrefix");
                }
            }
            // Changing the location should allow the prefix to change.
            prefixTextField.setEditable(lChanged);
            if (msg != null) {
                showMessage(msg);
            }
            setSaveEnabled(msg == null);
        }
    }

    protected void showMessage(String msg) {
        if (msg == null) {
            messageLabel.setText(" ");
            messageLabel.setIcon(null);
        } else {
            messageLabel.setText(msg);
            messageLabel.setIcon(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/xml/xam/ui/resources/error.gif", false)); // NOI18N
        }
    }

    protected Node createRootNode() {
        Set/*<Project>*/ refProjects = null;
        if (catalogSupport.supportsCrossProject()) {
            refProjects = catalogSupport.getProjectReferences();
        }
        ExternalReferenceDecorator decorator = getNodeDecorator();
        Node[] rootNodes = new Node[1 + (refProjects == null ? 0 : refProjects.size())];
        Project prj = FileOwnerQuery.getOwner(sourceFO);
        LogicalViewProvider viewProvider = (LogicalViewProvider) prj.getLookup().
                lookup(LogicalViewProvider.class);
        rootNodes[0] = decorator.createExternalReferenceNode(
                viewProvider.createLogicalView());
        int rootIndex = 1;
        List<FileObject> projectRoots = new ArrayList<FileObject>();
        projectRoots.add(prj.getProjectDirectory());
        if (refProjects != null) {
            for (Object o : refProjects) {
                Project refPrj = (Project) o;
                viewProvider = (LogicalViewProvider) refPrj.getLookup().
                        lookup(LogicalViewProvider.class);
                rootNodes[rootIndex++] = decorator.createExternalReferenceNode(
                        viewProvider.createLogicalView());
                projectRoots.add(refPrj.getProjectDirectory());
            }
        }
        FileObject[] roots = projectRoots.toArray(
                new FileObject[projectRoots.size()]);
        Children fileChildren = new Children.Array();
        fileChildren.add(rootNodes);
        Node byFilesNode = new FolderNode(fileChildren);
        byFilesNode.setDisplayName(NbBundle.getMessage(
                ExternalReferenceCustomizer.class,
                "LBL_ExternalReferenceCustomizer_Category_By_File"));

        // Construct the By Namespace node.
        Children nsChildren = new NamespaceChildren(roots, decorator);
        Node byNSNode = new FolderNode(nsChildren);
        byNSNode.setDisplayName(NbBundle.getMessage(
                ExternalReferenceCustomizer.class,
                "LBL_ExternalReferenceCustomizer_Category_By_Namespace"));
// Hide the Retrieved node tree until we are sure the runtime can handle
// URLs with respect to the catalog.
//        Node retrievedNode;
//        CatalogWriteModel cwm = getCatalogWriteModel();
//        if (cwm != null) {
//            Children rChildren = new RetrievedFilesChildren(cwm , decorator);
//            retrievedNode = new ExternalReferenceNode(projectNode, rChildren, decorator);
//        } else {
//            retrievedNode = new ExternalReferenceNode(projectNode, Children.LEAF, decorator);
//        }
//        retrievedNode.setDisplayName(NbBundle.getMessage(
//                ExternalReferenceCustomizer.class,
//                "LBL_ExternalReferenceCustomizer_Category_By_Retrieved"));
        Children categories = new Children.Array();
//        categories.add(new Node[]{ byFilesNode, byNSNode, retrievedNode });
        categories.add(new Node[] { byFilesNode, byNSNode });
        return new AbstractNode(categories);
    }
    
//    private CatalogWriteModel getCatalogWriteModel() {
//        try {
//            FileObject myFobj = (FileObject) getModelComponent().getModel().
//                    getModelSource().getLookup().lookup(FileObject.class);
//            CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
//                    getCatalogWriteModelForProject(myFobj);
//            return cwm;
//        } catch (CatalogModelException cme) {
//        }
//        return null;
//    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
            // Reset everything to assume an invalid selection.
            showMessage(null);
            setSaveEnabled(false);
            String ns = null;
            referencedFO = null;
            Node[] nodes = (Node[]) event.getNewValue();
            // Validate the node selection.
            if (nodes != null && nodes.length > 0 &&
                    nodes[0] instanceof ExternalReferenceNode) {
                ExternalReferenceNode node = (ExternalReferenceNode) nodes[0];
                Model model = node.getModel();
                // Without a model, the selection is completely invalid.
                if (model != null) {
                    ns = getTargetNamespace(model);
                    if (model != getModelComponent().getModel()) {
                        referencedFO = (FileObject) model.getModelSource().
                                getLookup().lookup(FileObject.class);
                    }
                    // Ask decorator if selection is valid or not.
                    String msg = getNodeDecorator().validate(node);
                    if (msg != null) {
                        showMessage(msg);
                    } else {
                        // If node is okay, validate the rest of the input.
                        validateInput();
                    }
                }
            }
            namespaceTextField.setText(ns);
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    /**
     * This method is called from within the constructor to
     * initializeTypeView the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        locationLabel = new javax.swing.JLabel();
        locationPanel = new javax.swing.JPanel();
        namespaceLabel = new javax.swing.JLabel();
        namespaceTextField = new javax.swing.JTextField();
        prefixLabel = new javax.swing.JLabel();
        prefixTextField = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        locationLabel.setLabelFor(locationPanel);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("LBL_ExternalReferenceCustomizer_Location"));
        locationLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCustomizer_Location"));

        locationPanel.setLayout(new java.awt.BorderLayout());

        locationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("LBL_ExternalReferenceCustomizer_Namespace"));
        namespaceLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCustomizer_Namespace"));

        namespaceTextField.setEditable(false);

        prefixLabel.setLabelFor(prefixTextField);
        org.openide.awt.Mnemonics.setLocalizedText(prefixLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("LBL_ExternalReferenceCustomizer_Prefix"));
        prefixLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCustomizer_Prefix"));

        prefixTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCustomizer_Prefix"));

        messageLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(namespaceLabel)
                            .add(prefixLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(prefixTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .add(namespaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)))
                    .add(locationLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(locationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(namespaceLabel)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(prefixLabel)
                    .add(prefixTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel locationLabel;
    public javax.swing.JPanel locationPanel;
    public javax.swing.JLabel messageLabel;
    public javax.swing.JLabel namespaceLabel;
    public javax.swing.JTextField namespaceTextField;
    public javax.swing.JLabel prefixLabel;
    public javax.swing.JTextField prefixTextField;
    // End of variables declaration//GEN-END:variables
}
