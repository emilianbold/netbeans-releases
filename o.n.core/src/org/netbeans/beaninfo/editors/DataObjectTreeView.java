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

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.view.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.netbeans.beaninfo.ExplorerPanel;
import org.netbeans.beaninfo.editors.DataObjectPanel.FilteredChildren;

/**
 * Component that displays an explorer that displays only certain
 * nodes. Similar to the node selector (retrieved from the TopManager)
 * but arranged a bit differently, plus allows the user to set the
 * currently selected node.
 * @author Joe Warzecha
 */
public class DataObjectTreeView extends DataObjectPanel {
    
    final static int DEFAULT_INSET = 10;
    
    private ExplorerPanel			expPanel;
    private TreeView                    	reposTree;
    
    public DataObjectTreeView (PropertyEditorSupport my, PropertyEnv env) {
        super(my, env);
        initComponent();
        
        reposTree.getAccessibleContext().setAccessibleName( NbBundle.getMessage(DataObjectTreeView.class, "ACSN_DataObjectPanel"));
        setDescription( NbBundle.getMessage(DataObjectTreeView.class, "ACSD_DataObjectPanel"));
    }
    
    public void addNotify() {
        completeInitialization();
        super.addNotify();
    }
    
    /** Called from the constructor. */
    private void initComponent() {
        expPanel = new ExplorerPanel();
        expPanel.setLayout(new BorderLayout());
        reposTree = new BeanTreeView();
        reposTree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        reposTree.setPopupAllowed(false);
        reposTree.setDefaultActionAllowed(false);
        expPanel.add(reposTree, "Center"); // NOI18N
    }

    private boolean initialized=false;
    /** Called from addNotify. */
    private void completeInitialization() {
        if (initialized) {
            //Do not re-initialize if the dialog has already been used,
            //otherwise we will end up listening to the wrong thing and
            //the OK button will never be enabled
            return;
        }
        if (insets != null) {
            setBorder(new EmptyBorder(insets));
        } else {
            setBorder(new EmptyBorder(12, 12, 0, 11));
        }
        setLayout(new BorderLayout(0, 2));
        
        if (subTitle != null) {
            JLabel l = new JLabel(subTitle);
            l.setLabelFor(reposTree);
            add(l, BorderLayout.NORTH);
        }
        
        if (rootNode == null) {
            if (dataFilter != null) {
                if (folderFilter != null) {
                    DataFilter dFilter = new DataFilter() {
                        public boolean acceptDataObject(DataObject obj) {
                            if (folderFilter.acceptDataObject(obj)) {
                                return true;
                            }
                            return dataFilter.acceptDataObject(obj);
                        }
                    };
                    rootNode = RepositoryNodeFactory.getDefault().repository(dFilter);
                } else {
                    rootNode = RepositoryNodeFactory.getDefault().repository(dataFilter);
                }
            } else {
                if (folderFilter != null) {
                    rootNode = RepositoryNodeFactory.getDefault().repository(folderFilter);
                } else {
                    rootNode = RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);
                }
            }
        }

        if (nodeFilter != null) {
            FilteredChildren children = 
                new FilteredChildren(rootNode, nodeFilter, dataFilter);
            FilterNode n = new FilterNode(rootNode, children);
            rootNode = n;
        }
        
        Node rNode = rootNode;
        if (rootObject != null) {
            Node n = findNodeForObj(rootNode, rootObject);
            if (n != null) {
                NodeAcceptor naccep = nodeFilter;
                if (naccep == null) {
                    naccep = new NodeAcceptor() {
                        public boolean acceptNodes(Node [] nodes) {
                            return false;
                        }
                    };
                }
                FilteredChildren children =
                    new FilteredChildren(n, naccep, dataFilter);
                FilterNode filtNode = new FilterNode(n, children);
                rNode = filtNode;
            }
        }
        
        expPanel.getExplorerManager().setRootContext(rNode);
        
        Node theNode = null;
        if (dObj != null) {
            theNode = findNodeForObj(rNode, dObj);
        }
        if (theNode != null) {
            try {
                expPanel.getExplorerManager().setSelectedNodes
                (new Node [] { theNode });
            } catch (PropertyVetoException pve) {
                Logger.global.log(Level.WARNING, null, pve);
            } catch (IllegalArgumentException iae) {
                Logger.global.log(Level.WARNING, null, iae);
            }
        }
        
        expPanel.getExplorerManager().addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals
                (evt.getPropertyName())) {
                    Node [] nodes = (Node []) evt.getNewValue();
                    DataObject d = getDataObject();
                    boolean enableOK = false;
                    if ((nodes != null) && (nodes.length > 0) && 
                    (dataFilter != null) && (d != null)) {
                        enableOK = dataFilter.acceptDataObject( d );
                    } else {
                        enableOK = ( d != null );
                    }
                    if ( enableOK )
                        myEditor.setValue( d );
                    setOkButtonEnabled( enableOK );
                }
            }
        });
        
        add(expPanel, BorderLayout.CENTER);
        
        if ((dataFilter != null) && (getDataObject() != null)) {
            setOkButtonEnabled(
                dataFilter.acceptDataObject(getDataObject())); 
        } else {
            setOkButtonEnabled(getDataObject() != null);
        }
        initialized=true;
    }
    
    /**
     * Sets description of the panel.
     *
     * @param desc Desciption of the panel.
     */
    public void setDescription(String desc) {
        getAccessibleContext().setAccessibleDescription(desc);
        reposTree.getAccessibleContext().setAccessibleDescription(desc);
    }
    
    /**
     * Return the currently selected DataObject. 
     * @return The currently selected DataObject or null if there is no node seleted
     */
    public DataObject getDataObject() {
        DataObject retValue = null;
        Node[] na = expPanel.getExplorerManager().getSelectedNodes();
        if ((na != null) && (na.length>0)) {
            retValue = (DataObject)na[0].getCookie(DataObject.class);
        }
        return retValue;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node getNode() {
        Node retValue = null;
        Node[] na = expPanel.getExplorerManager().getSelectedNodes();
        if ((na != null) && (na.length>0)) {
            retValue = na[0];
        }
        return retValue;
    }
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        return getDataObject();
    }
}
