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
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.PropertyEnv;
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
public class DataObjectPanel extends JPanel {
    
    final static int DEFAULT_INSET = 10;
    
    protected DataFilter       folderFilter;
    protected DataFilter       dataFilter;
    protected NodeAcceptor     nodeFilter;
    protected Insets           insets;
    protected String           subTitle;
    protected String           description;
    protected DataObject       rootObject;
    
    protected Node             rootNode;
    protected DataObject       dObj;
    /** Set to true when panel is used by DataObjectArrayEditor. Relevant only
     * for list view. Tree view allows only single selection. */
    protected boolean          multiSelection;
    protected int              selectionMode = JFileChooser.FILES_ONLY;
    
    protected PropertyEditorSupport myEditor;
    
    private PropertyEnv env;
    
    public DataObjectPanel(PropertyEditorSupport my, PropertyEnv env) {
        this.env = env;
        myEditor = my;
    }

    /** Allows the panel to be redisplayed in a new dialog controlled
     * by a new PropertyEnv. */
    public void setEnv(PropertyEnv env) {
        this.env = env;
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

    public void setRootNode(Node n) {
        rootNode = n;
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
    
    /**
     * Sets selection mode for dialog. It is valid only for list view GUI (JFileChooser).
     * It is set to false when used by DataObjectEditor and to true when used by
     * DataObjectArrayEditor.
     *
     * @param multiSelection True if multiple object selection is enabled.
     */
    public void setMultiSelection (boolean multiSelection) {
        this.multiSelection = multiSelection;
    }
    
    /**
     * Sets selection mode for JFileChooser. It is valid only for list view GUI (JFileChooser).
     * Valid values are:
     * JFileChooser.FILES_ONLY
     * JFileChooser.DIRECTORIES_ONLY
     * JFileChooser.FILES_AND_DIRECTORIES
     *
     * @param selectionMode integer value controling if files, directories or both can be
     * selected in dialog
     */
    public void setSelectionMode (int selectionMode) {
        this.selectionMode = selectionMode;
    }
    
    /**
     * Sets description of the panel.
     *
     * @param desc Desciption of the panel.
     */
    public void setDescription(String desc) {
    }
    
    protected Node findNode(Node parent, DataObject val) {
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
    
    protected Node findNodeForObj(Node rootNode, DataObject dObj) {
        Node node = null;
        DataFolder df = dObj.getFolder();
        Vector<DataFolder> v = new Vector<DataFolder>();
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
    
    protected Node findParentNode(Vector<DataFolder> v, Children children) {
        DataFolder df = v.lastElement();
        
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
        return null;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node getNode() {
        return null;
    }
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        return getDataObject();
    }
    
    protected void setOkButtonEnabled (boolean b) {
        if (env != null) {
            env.setState(b ? env.STATE_VALID : env.STATE_INVALID);
        }
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
        
        @Override
        protected Node[] createNodes(Node key) {
            if (key != null) {
                Node[] n = new Node[] {key};
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
