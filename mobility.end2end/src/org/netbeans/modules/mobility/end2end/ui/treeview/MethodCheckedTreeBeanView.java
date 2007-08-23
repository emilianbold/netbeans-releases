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

package org.netbeans.modules.mobility.end2end.ui.treeview;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;

/**
 * User: suchys
 * Date: Dec 12, 2003
 * Time: 3:57:41 PM
 */
public class MethodCheckedTreeBeanView extends BeanTreeView implements ChangeListener {
    
    private Node root;
    private Map<String,Object> data;
    final private MethodCheckedNodeRenderer renderer;
    final private MethodCheckedNodeEditor editor;
    
    public static final Object SELECTED = new Object();
    static final Object UNSELECTED = new Object();
    static final Object MIXED = new Object();
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static final String rootPath = "#root"; // NOI18N
    
    private final Node waitNode = new AbstractNode(Children.LEAF);
    
    public MethodCheckedTreeBeanView() {
        super();
        FocusListener[] fl = tree.getFocusListeners();
        for (int i = 0; i < fl.length; i++) {
            if (fl[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeFocusListener(fl[i]);
            }
        }
        MouseListener[] ml = tree.getMouseListeners();
        for (int i = 0; i < ml.length; i++) {
            if (ml[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeMouseListener(ml[i]);
            }
        }
        tree.setCellRenderer(renderer = new MethodCheckedNodeRenderer(tree.getCellRenderer()));
        tree.setCellEditor(editor = new MethodCheckedNodeEditor(tree));
        data = new HashMap<String,Object>();
        tree.setEditable(true);
        renderer.setContentStorage(this);
        editor.setContentStorage(this);
    }
    
    public Node getWaitNode() {
        return waitNode;
    }
    
    public void setEditable(final boolean editable) {
        tree.setEditable(editable);
        tree.setBackground(UIManager.getDefaults().getColor(editable ?  "Tree.background" : "TextField.inactiveBackground")); //NOI18N
    }
    
    public void setRoot(final Node root) {
        this.root = root;
    }
    
    private boolean acceptPath(final String path) {
        return (path != null && path.length()!=0);
    }
    
    private synchronized Object updateState(final Node fo) {
        Boolean valid = (Boolean)fo.getValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE);
        if( valid != null && !valid.booleanValue()) return null;
        final String path = fo.equals(root) ? rootPath : manipulatePath(NodeOp.createPath(fo,root));
        if (!acceptPath(path)) return null; // null means invalid
        Object state = data.get(path);
        final boolean forceState = state == SELECTED || state == UNSELECTED;
        final List<Node> childrenList = getDescendants(fo,forceState);
        if (forceState) {
            for ( final Node chNode : childrenList ) {
                final String cp = manipulatePath(NodeOp.createPath(chNode, root));
                if (acceptPath(cp)) data.put(cp, state);
            }
        } else {
            for ( final Node chNode : childrenList ) {
                final Object childState = updateState(chNode);
                if (childState != null) {
                    if (state == null) state = childState;
                    else if (state != childState) state = MIXED;
                }
            }
        }
        if (state == null) state = UNSELECTED; // if no valid children then SELECTED
        if (acceptPath(path)) data.put(path, state);
        return state;
    }
    
    public Object getState(final Node fo) { // finds first SELECTED or UNSELECTED from root
        String path = fo.equals(root) ? rootPath : manipulatePath(NodeOp.createPath(fo,root));
        if (!acceptPath(path)) return null; //invalid
        final Object state = data.get(path);
        return state != null ? state : UNSELECTED;
    }
    
    public synchronized void setState(Node fo, final boolean selected) {
        String path = fo.equals(root) ? rootPath : manipulatePath(NodeOp.createPath(fo,root));
        if (path == null || path.length() == 0) return; // invalid file object
        data.put(path, selected ? SELECTED : UNSELECTED); // set the one
        if (!fo.equals(root)){
            data.remove(rootPath);
            fo = fo.getParentNode();
            while (fo != null && !root.equals(fo)) { // clean the path to parent
                data.remove(manipulatePath(NodeOp.createPath(fo,root)));
                fo = fo.getParentNode();
            }
        }
        updateState(root); // renew the path from parent
        fireChange();
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    public void initTreeWithAllUnselected(){
        data.put(rootPath, UNSELECTED);
        for (Node node : getDescendants(root, true)) {
            data.put(manipulatePath(NodeOp.createPath( node, root)),UNSELECTED);
        }
    }
    
    public Node[] getSelectedMethods(){
        final List<Node> selectedMethodsList = getSelectedMethods(root);
        if (selectedMethodsList == null) return null;
        return selectedMethodsList.toArray(new Node[selectedMethodsList.size()]);
        
    }
    
    public synchronized void registerData(final Map<String,Object> data) {
        this.data = data;
        updateState(root);
        renderer.setContentStorage(this);
        editor.setContentStorage(this);
    }
    
    private String manipulatePath(final String[] nodesPath){
        StringBuffer returnString = new StringBuffer();
        for (int i=0;i<nodesPath.length - 1 ;i++) returnString.append(nodesPath[i]).append('.');
        return returnString.append(nodesPath[nodesPath.length - 1]).toString();
    }
    
    private List<Node> getDescendants(final Node n,final boolean recursive){
        final List<Node> result = new ArrayList<Node>();
        getDescendants(result, n, recursive);
        return result;
    }
    
    private void getDescendants(List<Node> list, final Node n,final boolean recursive){
        if (n != null) for (final Enumeration e = n.getChildren().nodes(); e.hasMoreElements() ;) {
            final Node subNode = (Node)e.nextElement();
            list.add(subNode);
            if (recursive) getDescendants(list, subNode,true);
        }
    }
    
    private List<Node> getSelectedMethods(final Node root){
        final List<Node> nodes = new ArrayList<Node>();
        if (root == null)
            return null;
        if (root.isLeaf() && getState(root).equals(SELECTED)){
            nodes.add(root);
        }
        for (final Enumeration e = root.getChildren().nodes(); e.hasMoreElements() ;) {
            final Node n = (Node)e.nextElement();
            nodes.addAll(getSelectedMethods(n));
            
        }
        return nodes;
    }
    
    public Node findNode(final String packageAndName){
        if (packageAndName == null) return null;
        if (packageAndName.equals(rootPath)) return root;
        try{
            return NodeOp.findPath(root ,packageAndName.split("\\.")); //NOI18N
        }catch (org.openide.nodes.NodeNotFoundException ex){
            return null;
        }
    }
    
    public void stateChanged(final ChangeEvent e) {
    }
}
