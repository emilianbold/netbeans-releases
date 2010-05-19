/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.chooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.reference.ReferenceChild;
import org.netbeans.modules.xml.reference.ReferenceNode;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author anjeleevich
 */
public class OperationChooser {

    private ExplorerManager explorerManager;
    private Project project;
    private DialogDescriptor dialogDescriptor;
    private boolean obsolete = false;

    private PropertyChangeListener propertyChangeListener
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt
                    .getPropertyName()))
            {
                validate();
            }
        }
    };

    public OperationChooser(DataObject dataObject) {
        this(FileOwnerQuery.getOwner(dataObject.getPrimaryFile()));
    }

    public OperationChooser(Project project) {
        this.project = project;


        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(propertyChangeListener);

        if (project != null) {
            LogicalViewProvider logicalViewProvider = project.getLookup()
                    .lookup(LogicalViewProvider.class);
            Node logicalViewNode = logicalViewProvider.createLogicalView();
            ChooserNode chooserNode = new ChooserNode(logicalViewNode);
            explorerManager.setRootContext(chooserNode);
        }
    }

    private Node findOperationNode(Operation oldValue) {
        if (oldValue == null) {
            return null;
        }

        Node root = explorerManager.getRootContext();
        if (root == null) {
            return null;
        }

        return findOperationNode(root, oldValue);
    }

    private Node findOperationNode(Node node, Operation oldValue) {
        if (node == null) {
            return null;
        }

        if (node.getLookup().lookup(Operation.class) == oldValue) {
            return node;
        }

        Children children = node.getChildren();
        if (children == null || children == Children.LEAF) {
            return null;
        }

        Node[] nodes = children.getNodes(true);
        if (nodes == null) {
            return null;
        }

        for (Node child : nodes) {
            Node result = findOperationNode(child, oldValue);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public Operation choose() {
        return choose(null);
    }

    public Operation choose(Operation oldValue) {
        // TODO finish
        if (obsolete) {
            throw new IllegalStateException(
                    "Object is not reusable. Create new instance."); // NOI18N
        }
        obsolete = true;

        Node oldOperationNode = findOperationNode(oldValue);
        if (oldOperationNode != null) {
            try {
                explorerManager.setSelectedNodes(new Node[]{oldOperationNode});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        View view = new View();

        dialogDescriptor = new DialogDescriptor(view, NbBundle
                .getMessage(OperationChooser.class,
                "OperationChooser.Title")); // NOI18N

        Dialog dialog = DialogDisplayer.getDefault()
                .createDialog(dialogDescriptor);
        validate();
        
        dialog.setVisible(true);
        dialog.dispose();

        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return null;
        }

        return getSelectedOperation();
    }

    private void validate() {
        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(getSelectedOperation() != null);
        }
    }

    private Operation getSelectedOperation() {
        Node[] nodes = explorerManager.getSelectedNodes();
        Node node = (nodes != null && nodes.length == 1) ? nodes[0] : null;
        return (node == null) ? null : node.getLookup().lookup(Operation.class);
    }

    private class ChooserNode extends FilterNode {
        ChooserNode(Node node, org.openide.nodes.Children children) {
            super(node, children);
        }

        ChooserNode(Node node) {
            this(node, new ChooserChildren(node));
        }
    }

    private class ChooserChildren extends FilterNode.Children {
        public ChooserChildren(Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node[] createNodes(Node node) {
            if (node instanceof ReferenceNode) {
                return new Node[] { new ChooserNode(node) };
            }

            FileObject fileObject = null;
            if (node instanceof ReferenceChild) {
                fileObject = ((ReferenceChild) node).getFileObject();
            } else {
                Lookup lookup = node.getLookup();
                DataObject dataObject = lookup.lookup(DataObject.class);

                if (dataObject instanceof DataFolder) {
                    return new Node[] { new ChooserNode(node) };
                }

                if (dataObject != null) {
                    fileObject = dataObject.getPrimaryFile();
                }

                if (fileObject == null) {
                    fileObject = lookup.lookup(FileObject.class);
                }
            }

            if (fileObject == null || fileObject.isFolder()) {
                return new Node[0];
            }

            String ext = fileObject.getExt();
            if (!"wsdl".equalsIgnoreCase(ext)) { // NOI18N
                return new Node[0];
            }

            return new Node[] {
                new ChooserNode(node, new WSDLChildren(fileObject))
            };
        }
    }

    private class WSDLChildren extends Children.Keys<PortType> {
        private FileObject fileObject;

        public WSDLChildren(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        @Override
        protected void addNotify() {
            try {
                ModelSource modelSource = Utilities.createModelSource(fileObject,
                        true);
                WSDLModel model = (modelSource == null) ? null
                        : WSDLModelFactory.getDefault().getModel(modelSource);
                Definitions definitions = (model == null) ? null
                        : model.getDefinitions();
                Collection<PortType> portTypes = (definitions == null) ? null
                        : definitions.getPortTypes();
                
                if (portTypes != null) {
                    List<PortType> showablePortTypes
                            = new ArrayList<PortType>(portTypes.size());
                    for (PortType portType : portTypes) {
                        String portTypeName = portType.getName();
                        if (portTypeName != null
                                && portTypeName.trim().length() > 0)
                        {
                            showablePortTypes.add(portType);
                        }
                    }
                    setKeys(showablePortTypes);
                }
            } catch (CatalogModelException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO,
                        ex.getMessage(), ex);
            }
        }

        @Override
        protected Node[] createNodes(PortType portType) {
            return new Node[] { new PortTypeNode(portType) };
        }
    }

    private class PortTypeNode extends AbstractNode {
        public PortTypeNode(PortType portType) {
            super(new PortTypeChildren(portType), Lookups.singleton(portType));
            String name = portType.getName();
            setDisplayName(name);
            setIconBaseWithExtension(ICONS_FOLDER + "port_type.png"); // NOI18N
        }
    }

    private class PortTypeChildren extends Children.Keys<Operation> {
        private PortType portType;

        PortTypeChildren(PortType portType) {
            super(false);
            this.portType = portType;
        }

        @Override
        protected void addNotify() {
            Collection<Operation> operations = portType.getOperations();
            if (operations != null) {
                List<Operation> showableOperations = new ArrayList<Operation>(
                        operations.size());
                for (Operation operation : operations) {
                    String name = operation.getName();
                    if (name != null && name.trim().length() > 0) {
                        showableOperations.add(operation);
                    }
                }
                setKeys(showableOperations);
            }
        }

        @Override
        protected Node[] createNodes(Operation operation) {
            return new Node[] { new OperationNode(operation) };
        }
    }

    private class OperationNode extends AbstractNode {
        OperationNode(Operation operation) {
            super(Children.LEAF, Lookups.singleton(operation));
            setDisplayName(operation.getName());
            setIconBaseWithExtension(ICONS_FOLDER 
                    + "requestresponse_operation.png"); // NOI18N
        }
    }

    private class View extends JPanel implements ExplorerManager.Provider {
        public View() {
            BeanTreeView beanTreeView = new BeanTreeView();
            beanTreeView.setRootVisible(true);
            beanTreeView.setPopupAllowed(false);
            beanTreeView.setDefaultActionAllowed(false);
            beanTreeView.setSelectionMode(TreeSelectionModel
                    .SINGLE_TREE_SELECTION );

            setLayout(new BorderLayout());
            setBorder(VIEW_BORDER);
            add(beanTreeView, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return explorerManager;
        }
    }

    private static final Border VIEW_BORDER = new CompoundBorder(
            new EmptyBorder(4, 4, 0, 4), new EtchedBorder());

    private static final String ICONS_FOLDER
            = "org/netbeans/modules/worklist/editor/chooser/resources/"; // NOI18N
}
