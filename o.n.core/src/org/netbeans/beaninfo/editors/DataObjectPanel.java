/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.explorer.view.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

/**
 * Component that displays an explorer that displays only certain
 * nodes. Similar to the node selector (retrieved from the TopManager)
 * but arranged a bit differently, plus allows the user to set the
 * currently selected node.
 * @author Joe Warzecha
 */
public class DataObjectPanel extends JPanel implements EnhancedCustomPropertyEditor {
    
    final static int DEFAULT_INSET = 10;
    
    private ExplorerPanel			expPanel;
    private TreeView                    	reposTree;
    
    private DataFilter				folderFilter;
    private DataFilter				dataFilter;
    private NodeAcceptor			nodeFilter;
    private Insets				insets;
    private String				subTitle;
    private DataObject				rootObject;
    
    private Node                                rootNode;
    private DataObject                          dObj;
    
    private DataObjectEditor                    myEditor;
    
    public DataObjectPanel(DataObjectEditor my) {
        myEditor = my;
        initComponent();
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
        reposTree.setPopupAllowed(false);
        reposTree.setDefaultActionAllowed(false);
        expPanel.add(reposTree, "Center");
    }
    
    /** Called from addNotify. */
    private void completeInitialization() {
        if (insets != null) {
            setBorder(new EmptyBorder(insets));
        }
        setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        int yval = 0;
        
        if (subTitle != null) {
            JLabel l = new JLabel(subTitle);
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = yval++;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(12, 12, 0, 12);
            add(l, gbc);
        }
        
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
                rootNode = TopManager.getDefault().getPlaces().
                nodes().repository(dFilter);
            } else {
                rootNode = TopManager.getDefault().getPlaces().
                nodes().repository(dataFilter);
            }
        } else {
            if (folderFilter != null) {
                rootNode = TopManager.getDefault().getPlaces().
                nodes().repository(folderFilter);
            } else {
                rootNode = TopManager.getDefault().getPlaces().
                nodes().repository();
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
                TopManager.getDefault().getErrorManager().notify(
                ErrorManager.INFORMATIONAL, pve);
            } catch (IllegalArgumentException iae) {
                TopManager.getDefault().getErrorManager().notify(
                ErrorManager.INFORMATIONAL, iae);
            }
        }
        
        expPanel.getExplorerManager().addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals
                (ExplorerManager.PROP_SELECTED_NODES)) {
                    Node [] nodes = (Node []) evt.getNewValue();
                    if ((nodes != null) && (nodes.length > 0) && 
                    (dataFilter != null) && (getDataObject() != null)) {
                        myEditor.setOkButtonEnabled(
                            dataFilter.acceptDataObject(getDataObject())); 
                    } else {
                        myEditor.setOkButtonEnabled(true);
                    }
                }
            }
        });
        expPanel.getExplorerManager().addVetoableChangeListener(
        new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (evt.getPropertyName().equals
                (ExplorerManager.PROP_SELECTED_NODES)) {
                    Node [] nodes = (Node []) evt.getNewValue();
                    if ((nodes != null) && (nodes.length > 1)) {
                        throw new PropertyVetoException("Only one node can be selected here", evt); // NOI18N
                    }
                }
            }
        });
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = yval;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;
        
        add(expPanel, gbc);
    }
    
    /**
     * Set the data filter used to filter the nodes displayed.
     * An example of a DataFilter is one that only returns 'true'
     * for JavaDataObjects so that only nodes representing those
     * objects are displayed in the Explorer.
     *
     * @param df The DataFilter used to filter nodes
     */
    public void setDataFilter(DataFilter df) {
        dataFilter = df;
    }
    
    /**
     * Set the Node filter used to filter nodes. For example,
     * if a user wants to display only JavaDataObjects, but not
     * any subnodes under those DataNodes, then set the node
     * filter to not accept any sub nodes.
     *
     * @param acceptor The NodeAcceptor used to filter subnodes.
     */
    public void setNodeFilter(NodeAcceptor acceptor) {
        nodeFilter = acceptor;
    }
    
    /**
     * Set the insets of the Explorer panel.
     *
     * @param insetVal The value used for all of the insets (top,
     *                 bottom, left, right).
     */
    public void setInsetValue(int insetVal) {
       insets = new Insets(insetVal, insetVal, insetVal, insetVal);
    }
    
    /**
     * Set explanation text displayed above the Explorer. If
     * not set, no text is displayed.
     *
     * @param text Text displayed on the GUI above the Explorer.
     */
    public void setText(String text) {
        subTitle = text;
    }
    
    /**
     * Sets the root object displayed on the Explorer. If
     * not set, then the normal 'FileSystems' node is displayed
     * as the Root.
     *
     * @param obj The DataObject used as the root node on the
     *            Explorer.
     */
    public void setRootObject(DataObject obj) {
        rootObject = obj;
    }
    
    /**
     * This filter can be used to filter folders.
     * It is applied before the data filter.
     */
    public void setFolderFilter(DataFilter f) {
        folderFilter = f;
    }
    
    /**
     * This filter can be used to filter folders.
     * It is applied before the data filter.
     */
    public DataFilter getFolderFilter() {
        return folderFilter;
    }
    
    /**
     * Sets the currently selected DataObject.
     *
     * @param d The DataObject to be selected in the Explorer.
     */
    public void setDataObject(DataObject d) {
        dObj = d;
    }
    
    private Node findNode(Node parent, DataObject val) {
        Children children = parent.getChildren();
        Node theNode = children.findChild(val.getName());
        if (theNode == null) {
            Node [] allNodes = children.getNodes();
            if ((allNodes != null) && (allNodes.length > 0)) {
                for (int i = 0;
                (i < allNodes.length) && (theNode == null); i++) {
                    DataObject dObj = (DataObject)
                    allNodes [i].getCookie(DataObject.class);
                    if ((dObj != null) && (dObj == val)) {
                        theNode = allNodes [i];
                    }
                }
            }
        }
        
        return theNode;
    }
    
    private Node findNodeForObj(Node rootNode, DataObject dObj) {
        Node node = null;
        DataFolder df = dObj.getFolder();
        Vector v = new Vector();
        while (df != null) {
            v.addElement(df);
            df = df.getFolder();
        }
        
        if (! v.isEmpty()) {
            Node parent = findParentNode(v, rootNode.getChildren());
            if (parent != null) {
                node = findNode(parent, dObj);
            } else {
                node = findNode(rootNode, dObj);
            }
        } else {
            node = findNode(rootNode, dObj);
        }
        
        return node;
    }
    
    private Node findParentNode(Vector v, Children children) {
        DataFolder df = (DataFolder) v.lastElement();
        
        //Node n = children.findChild (df.getPrimaryFile ().getName ());
        Node n = children.findChild(df.getNodeDelegate().getName());
        if (n == null) {
            Node [] nodes = children.getNodes();
            for (int i = 0; (i < nodes.length) && (n == null); i++) {
                DataFolder folder =
                (DataFolder) nodes [i].getCookie(DataFolder.class);
                if ((folder != null) && (folder == df)) {
                    n = nodes [i];
                }
            }
        }
        
        if (v.size() > 1) {
            v.removeElement(df);
            if (n != null) {
                return findParentNode(v, n.getChildren());
            } else {
                // Didn't find it, try next folder anyway
                return findParentNode(v, children);
            }
        }
        return n;
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
    
    static class FilteredChildren extends FilterNode.Children {
        private NodeAcceptor nodeAcceptor;
        private DataFilter dFilter;
        
        FilteredChildren(Node n, NodeAcceptor acceptor, DataFilter filter) {
            super (n);
            nodeAcceptor = acceptor;
            dFilter = filter;
        }
        
        private Node [] makeFilterNode(Node n) {
            FilteredChildren children =
            new FilteredChildren(n, nodeAcceptor, dFilter);
            return new Node [] { new FilterNode(n, children) };
        }
        
        protected Node [] createNodes(Object key) {
            if ((key != null) && (key instanceof Node)) {
                Node [] n = new Node [] {(Node) key};
                if (dFilter != null) {
                    DataObject dObj =
                    (DataObject) n [0].getCookie(DataObject.class);
                    if ((dObj != null) && (dFilter.acceptDataObject(dObj))) {
                        return makeFilterNode(n [0]);
                    }
                }
                
                if (nodeAcceptor.acceptNodes(n)) {
                    return makeFilterNode(n [0]);
                }
            }
            return new Node [0];
        }
    }
}
