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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.explorer.view.BeanTreeView;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Base class for external reference customizers.
 *
 * @author  Ajit Bhate
 * @author  Nathan Fiedler
 */
public abstract class ExternalReferenceCustomizer<T extends Component>
        extends AbstractComponentCustomizer<T>
        implements ExplorerManager.Provider, PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private transient DocumentListener prefixListener;
    /** If true, the prefix was generated and not edited by the user. */
    private transient boolean prefixGenerated;
    private transient String editedLocation;
    private BeanTreeView locationView;
    private ExplorerManager explorerManager;

    /**
     * Creates new form ExternalReferenceCustomizer
     *
     * @param  component  external reference to customize.
     */
    public ExternalReferenceCustomizer(T component, Model model) {
        super(component);
        initComponents();
        init(component, model);
        initializeLocationView();
        initializeUI();
    }

    public void applyChanges() throws IOException {
        if (mustNamespaceDiffer() && isPrefixChanged()) {
            prefixTextField.setEditable(false);
        }
    }

    public void reset() {
        // Rebuild the node tree and view to ensure we display the
        // latest available files in the project.
        initializeLocationView();
        // Reset the input fields.
        initializeUI();
        setSaveEnabled(false);
        setResetEnabled(false);
        showMessage(null);
    }

    /**
     * Retrieves the location value from the interface.
     *
     * @return  new location value.
     */
    protected String getEditedLocation() {
        return editedLocation;
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
     * Return the target namespace of the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  target namespace, or null if none.
     */
    protected abstract String getTargetNamespace(Model model);

    /**
     * Generate a unique prefix value (e.g. "ns1") for the component.
     *
     * @return  unique prefix value.
     */
    protected abstract String generatePrefix();

    /**
     * Return the model of the component being customized.
     *
     * @return  component model.
     */
    public Model getComponentModel() {
        return getModelComponent().getModel();
    }

    /**
     * Return the target namespace of the model that contains the
     * component being customized.
     *
     * @return  target namespace, or null if none.
     */
    public String getTargetNamespace() {
        return getTargetNamespace(getModelComponent().getModel());
    }

    /**
     * Return the existing external reference prefixes for the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  set of prefixes; empty if none.
     */
    protected abstract Map<String, String> getPrefixes(Model model);

    /**
     * Returns the NodeDecorator for this customizer, if any.
     *
     * @return  node decorator for files nodes, or null if none.
     */
    protected abstract ExternalReferenceDecorator getNodeDecorator();

    /**
     * Indicates if the namespace value must be different than that of
     * the model containing the component being customized. If false,
     * then the opposite must hold - the namespace must be the same.
     * The one exception is if the namespace is not defined at all.
     *
     * @return  true if namespace must differ, false if same.
     */
    public abstract boolean mustNamespaceDiffer();

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
        // subclasses may override this
    }

    /**
     * Load the component values into the interface widgets.
     */
    private void initializeUI() {
        editedLocation = getReferenceLocation();
        // TODO select in panel
        if (mustNamespaceDiffer()) {
            namespaceTextField.setText(getNamespace());
            prefixTextField.getDocument().removeDocumentListener(prefixListener);
            String prefix = getPrefix();
            if (prefix != null) {
                prefixTextField.setText(prefix);
                prefixTextField.setEditable(false);
                prefixGenerated = false;
            } else {
                prefix = generatePrefix();
                prefixGenerated = true;
                prefixTextField.setText(prefix);
                if (prefixListener == null) {
                    prefixListener = new DocumentListener() {
                        public void changedUpdate(DocumentEvent e) {
                        }
                        public void insertUpdate(DocumentEvent e) {
                            prefixGenerated = false;
                            determineValidity();
                        }
                        public void removeUpdate(DocumentEvent e) {
                            prefixGenerated = false;
                            determineValidity();
                        }
                    };
                }
                prefixTextField.getDocument().addDocumentListener(prefixListener);
            }
        } else {
            namespaceLabel.setVisible(false);
            namespaceTextField.setVisible(false);
            prefixLabel.setVisible(false);
            prefixTextField.setVisible(false);
        }
    }

    /**
     * Based on the current radio button status and node selections, decide
     * if we are in a valid state for accepting the user's input.
     */
    private void determineValidity() {
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
            showMessage(msg);
            setSaveEnabled(msg == null);
        }
    }

    /**
     * Display the given message, or reset the message label to blank.
     *
     * @param  msg  message to show, or null to hide messages.
     */
    private void showMessage(String msg) {
        if (msg == null) {
            messageLabel.setText(" ");
            messageLabel.setIcon(null);
        } else {
            messageLabel.setText(msg);
            // Image is in openide/dialogs module.
            messageLabel.setIcon(new ImageIcon(Utilities.loadImage(
                    "org/openide/resources/error.gif"))); // NOI18N
        }
    }

    /**
     * Construct the tree view and explorer manager for the location value.
     */
    private void initializeLocationView() {
        // View for selecting a external ref.
        locationView = new BeanTreeView();
        locationView.setPopupAllowed(false);
        locationView.setDefaultActionAllowed(false);
        locationView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        locationView.setRootVisible(false);
        locationView.getAccessibleContext().setAccessibleName(locationLabel.getToolTipText());
        locationView.getAccessibleContext().setAccessibleDescription(locationLabel.getToolTipText());
        locationPanel.add(locationView, BorderLayout.CENTER);
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(createRootNode());
        explorerManager.addPropertyChangeListener(this);
    }

    private Node createRootNode() {
        // Get the project source roots, if the file lives in a project.
        FileObject fo = (FileObject) getModelComponent().getModel().
                getModelSource().getLookup().lookup(FileObject.class);
        Project prj = FileOwnerQuery.getOwner(fo);
        FileObject[] roots = null;
        if (prj != null) {
            Sources srcs = ProjectUtils.getSources(prj);
            SourceGroup[] sgs = srcs.getSourceGroups(Sources.TYPE_GENERIC);
            if (sgs == null || sgs.length == 0) {
                roots = new FileObject[] {
                    prj.getProjectDirectory()
                };
            } else {
                roots = new FileObject[sgs.length];
                for (int ii = 0; ii < sgs.length; ii++) {
                    roots[ii] = sgs[ii].getRootFolder();
                }
            }
        } else {
            // The file may not be living in a project, in which case just
            // use the folder in which this file resides.
            roots = new FileObject[] {
                fo.getParent()
            };
        }

        // Construct the By File node for the source roots.
        ExternalReferenceDecorator decorator = getNodeDecorator();
        Node[] rootNodes = new Node[roots.length];
        for (int ii = 0; ii < roots.length; ii++) {
            try {
                Node node = DataObject.find(roots[ii]).getNodeDelegate();
                rootNodes[ii] = new ExternalReferenceDataNode(node, decorator);
            } catch (DataObjectNotFoundException donfe) {
                // ignore
            }
        }
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
            // Clear the message field first, then annotate later.
            showMessage(null);
            Node[] nodes = (Node[]) event.getNewValue();
            if (nodes != null && nodes.length >= 1) {
                Node node = nodes[0];
                String ns = null;
                String newLocation = null;
                if (node instanceof RetrievedFilesChildren.RetrievedFileNode) {
                    RetrievedFilesChildren.RetrievedFileNode rNode =
                            (RetrievedFilesChildren.RetrievedFileNode) node;
                    if (!rNode.isValid()) {
                        String msg = NbBundle.getMessage(ExternalReferenceCustomizer.class,
                                "LBL_ExternalReferenceCustomizer_InvalidCatalogEntry");
                        showMessage(msg);
                        return;
                    }
                    ns = rNode.getNamespace();
                    if (ns == null || mustNamespaceDiffer() !=
                            ns.equals(getTargetNamespace())) {
                        newLocation = rNode.getLocation();
                    }
                } else {
                    DataObject dobj = (DataObject) node.getLookup().
                            lookup(DataObject.class);
                    if (dobj != null && dobj.isValid()) {
                        FileObject fileObj = dobj.getPrimaryFile();
                        String sLocation = fileObj.getPath();
                        ModelCookie cookie = (ModelCookie) dobj.getCookie(
                                ModelCookie.class);
                        Model model;
                        try {
                            if (cookie != null && (model = cookie.getModel()) !=
                                    getModelComponent().getModel()) {
                                ns = getTargetNamespace(model);
                                if (ns == null || mustNamespaceDiffer() !=
                                        ns.equals(getTargetNamespace())) {
                                    newLocation = getRelativePath(sLocation);
                                }
                            }
                        } catch (IOException ioe) {
                        }
                    }
                }
                if (newLocation != null) {
                    try {
                        URI uri = new URI("file", newLocation, null);
                        uri = uri.normalize();
                        newLocation = uri.getRawSchemeSpecificPart();
                    } catch (URISyntaxException use) {
                        showMessage(use.toString());
                        // Push onward despite this exception.
                    }
                }
                editedLocation = newLocation;
                if (newLocation == null) {
                    ns = null;
                }
                namespaceTextField.setText(ns);
                determineValidity();
                if (isLocationChanged()) {
                    // Changing the location should allow the prefix to change.
                    prefixTextField.setEditable(true);
                }

                if (node instanceof ExternalReferenceNode) {
                    // Give decorator a chance to issue any warnings, errors.
                    ExternalReferenceNode ern = (ExternalReferenceNode) node;
                    ExternalReferenceDecorator decorator = getNodeDecorator();
                    String msg = decorator.annotate(ern);
                    if (msg != null) {
                        showMessage(msg);
                    }
                }
            }
        }
    }

    private String getRelativePath(final String sLocation) {
        FileObject myFileObj = (FileObject)getModelComponent().
                getModel().getModelSource().getLookup().lookup(
                FileObject.class);
        String myLocation = myFileObj.getPath();
        StringTokenizer st1 = new StringTokenizer(myLocation,"/");
        StringTokenizer st2 = new StringTokenizer(sLocation,"/");
        String relativeLoc = "";
        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            relativeLoc = st2.nextToken();
            if (!st1.nextToken().equals(relativeLoc)) {
                break;
            }
        }
        while (st1.hasMoreTokens()) {
            relativeLoc = "../".concat(relativeLoc);
            st1.nextToken();
        }
        while(st2.hasMoreTokens()) {
            relativeLoc = relativeLoc.concat("/");
            relativeLoc = relativeLoc.concat(st2.nextToken());
        }
        return relativeLoc;
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
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("LBL_ExternalReferenceCustomizer_Location"));
        locationLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("TIP_ExternalReferenceCustomizer_Location"));

        locationPanel.setLayout(new java.awt.BorderLayout());

        locationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("LBL_ExternalReferenceCustomizer_Namespace"));
        namespaceLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("TIP_ExternalReferenceCustomizer_Namespace"));

        namespaceTextField.setEditable(false);

        prefixLabel.setLabelFor(prefixTextField);
        org.openide.awt.Mnemonics.setLocalizedText(prefixLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("LBL_ExternalReferenceCustomizer_Prefix"));
        prefixLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("TIP_ExternalReferenceCustomizer_Prefix"));

        prefixTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Form").getString("TIP_ExternalReferenceCustomizer_Prefix"));

        messageLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
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
