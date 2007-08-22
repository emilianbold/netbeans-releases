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
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.UIManager;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.nodes.Children;
import org.openide.explorer.view.BeanTreeView;

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
    
    public MethodCheckedTreeBeanView() {
        super();
        // System.out.println("MethodCheckedTreeBeanView");
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
        //  System.out.println("after   rrrrrrr" + editor.getClass().getName());
        tree.setEditable(true);
        renderer.setContentStorage(this);
        editor.setContentStorage(this);
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
        if( fo.getValue( ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE ) != null &&
                    !((Boolean)fo.getValue( ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE )).booleanValue()) return null;
        
        String path ;
        
        if (fo.equals(root)){
            path = rootPath;
        } else{
            final String[] nodesPath = NodeOp.createPath(fo,root);
            path = manipulatePath(nodesPath);
        }
        // System.out.println("updateState of  " + fo.getDisplayName());
        // System.out.println("in update state path.length = "+ path.length);
        if (!acceptPath(path)) return null; // null means invalid
        //System.out.println("updateState of  " + fo.getDisplayName());
        //printPath(path);
        
        Object state = data.get(path);
        //  printState(state);
        final boolean forceState = state == SELECTED || state == UNSELECTED;
        // System.out.println("before getChildrenList");
        final List<Node> childrenList = getDescendants(fo,forceState);
        if (childrenList != null){
            //System.out.println("ChildrenList not null");
            for ( final Node chNode : childrenList ) {
                
                if (forceState) {
                    //System.out.println("in forceState");
                    final String[] cpNodes = NodeOp.createPath(chNode ,root);
                    //System.out.println("chngNode ="+ chNode.getDisplayName());
                    final String cp = manipulatePath(cpNodes);
                    if (acceptPath(cp)) data.put(cp, state);
                } else {
                    //System.out.println("NO forceState");
                    final Object childState = updateState(chNode);
                    if (childState != null) {
                        if (state == null) state = childState;
                        else if (state != childState) state = MIXED;
                    }
                }
            }
        }
        if (state == null) state = UNSELECTED; // if no valid children then SELECTED
        //String  finstate = state.equals(SELECTED)?"selected":"unselected";
        //System.out.println("finstate = "+ finstate + "   ");
        if (path.length() > 0) data.put(path, state);
        return state;
    }
    
    public Object getState(final Node fo) { // finds first SELECTED or UNSELECTED from root
        String path;
        //  System.out.println("in getState = " + fo.getDisplayName() + " root = "+ root.getDisplayName());
        if (fo.equals(root)){
            path = rootPath;
        } else{
            final String [] pathNodes = NodeOp.createPath(fo,root);
            path = manipulatePath(pathNodes);
        }
        // printPath(path);
        if (!acceptPath(path)) return null; //invalid
        //System.out.println("path != null = " + data.get(path).toString());
        final Object state = data.get(path);
        return state != null ? state : UNSELECTED;
    }
    
    public synchronized void setState(Node fo, final boolean selected) {
        String  path;
        //System.out.println("in setState" + fo.getDisplayName() + printSelected(selected));
        if (fo.equals(root)){
            path = rootPath;
        } else{
            final String [] pathNodes = NodeOp.createPath(fo,root);
            path = manipulatePath(pathNodes);
            
        }
        printMap(data);
        if (path == null || path.length() == 0) return; // invalid file object
        // System.out.println("retutrn invalid for "+ fo.getDisplayName());
        data.put(path, selected ? SELECTED : UNSELECTED); // set the one
        
        if (!fo.equals(root)){
            data.remove(rootPath);
            
            fo = fo.getParentNode();
            if (fo != null)
                //System.out.println("parent node =  "+ fo.getDisplayName());
                while (fo != null && !root.equals(fo)) { // clean the path to parent
                //System.out.println("in loop : " + fo.getDisplayName());
                final String [] pathNodes = NodeOp.createPath(fo,root);
                final String removePath = manipulatePath(pathNodes);
                
                data.remove(removePath);
                fo = fo.getParentNode();
                }
        }
        //System.out.println("Before updatestate(root)");
        printMap(data);
        updateState(root); // renew the path from parent
        fireChange();
        //System.out.println("After updatestate(root)");
        printMap(data);
        //if (properties != null || propertyName != null) properties.put(propertyName, getExcludesRegex());
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
        //System.out.println("initTreeWithAllUnselected root = " + root.getDisplayName());
        String pathNodes;
        final Node[] nodes = findAllNodes();
        for (int i=0;i<nodes.length;i++){
            if (nodes[i].equals(root)){
                pathNodes = rootPath;
                //  System.out.println("in node[i].equals rootttttt");
            } else{
                //System.out.println("node = " + nodes[i].getDisplayName());
                pathNodes = manipulatePath(NodeOp.createPath( nodes[i], root));
                
            }
            data.put(pathNodes,UNSELECTED);
            
        }
        
    }
    
    private Node[] findAllNodes(){
        final List<Node> nodesList = new ArrayList<Node>();
        
        // System.out.println("in findAllNodes root = " + root.getDisplayName());
        
        findAllNodes(root,nodesList);
        if (nodesList == null)
            return null;
        final Node nodes[] = new Node[ nodesList.size() ];
        nodesList.toArray( nodes );
        return nodes;
    }
    
    
    
    private void  findAllNodes(final Node currentNode,final List<Node> nodesList){
        if (currentNode == null)
            return;
        if (currentNode.isLeaf()) {
            nodesList.add(currentNode);
            
        } else{
            nodesList.add(currentNode);
            final Children children = currentNode.getChildren();
            if (children != null){
                final Node[] subNodes = children.getNodes();
                for (int k = 0;k < subNodes.length; k++) {
                    findAllNodes(subNodes[k],nodesList);
                }
            }
        }
    }
    
    
    public static  void printPath(final String path){
        //System.out.print("path = ");
        if (path == null) {
            ///
            //System.out.println("null");
            return;
        }
        
        //System.out.println(path);
    }
    
    public static  void printPath(final String[] path){
//        System.out.print("path = ");
        if (path == null) {
//            System.out.println("null");
            return;
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
        
    private void printMap(final Map<String,Object> data){
        //System.out.println(" **** MAP *****" );
        final Set<String> keys = data.keySet();
        for ( final String key : keys ) {
            printPath(key);
            final Object elem = data.get(key);
            if (elem !=null)             {
                printPath(key);
            }
        }
    }
    
    
    private String manipulatePath(final String[] nodesPath){
        StringBuffer returnString = new StringBuffer();
        for (int i=0;i<nodesPath.length - 1 ;i++){
            returnString = returnString.append(nodesPath[i]);
            returnString = returnString.append("."); // NOI18N
            
        }
        returnString = returnString.append(nodesPath[nodesPath.length - 1]);
        return returnString.toString();
    }
    
    private List<Node> getDescendants(final Node n,final boolean recursive){
        final List<Node> result = new ArrayList<Node>();
        if (n == null  || n.isLeaf())
            return null;
        
        for (final Enumeration e = n.getChildren().nodes(); e.hasMoreElements() ;) {
            final Node subNode = (Node)e.nextElement();
            result.add(subNode);
            if (recursive){
                final List<Node> subNodesList = getDescendants(subNode,true);
                if (subNodesList != null){
                    result.addAll(subNodesList);
                }
            }
        }
        
        return result;
        
        
    }
    
    private void printTree(final Node root){
        if( root == null ) {
            //System.out.println("root = null");
            
            return;
        }
        if( root.isLeaf()){
            //System.out.println(root.getName() + " = " + root.getClass().getName());
            return;
        }
        
        for (final Enumeration e = root.getChildren().nodes(); e.hasMoreElements() ;) {
            final Node n = (Node)e.nextElement();
            //System.out.println( n.getName() + " = " + n.getClass().getName());
            printTree(n);
            
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
    
    public Node findNode( final String packageAndName ){
        //System.out.println("in findNode for " + packageAndName +"  tree ====    ");
        printTree(root);
        
        if (packageAndName == null)
            return null;
        if (packageAndName.equals(rootPath))
            return root;
        try{
            final String[] sections = seperatePath(packageAndName);           
            return NodeOp.findPath(root,sections);
        }catch (org.openide.nodes.NodeNotFoundException ex){
            //System.out.println("NodeNotFoundException" );
            return null;
        }
    }
    
    private String[] seperatePath(final String path){
        //FIXME
        //System.out.println("in seperatePath " + path);
        final StringTokenizer tk = new StringTokenizer(path,"."); //NOI18N
        final int numTokens = tk.countTokens();
        String[] segments = new String[numTokens];
        int i=0;
        //just for presentation
        // should be the project/src direcory
        // segments[i++] = root.getName();
        //  System.out.println("root name = "+ root.getName());
        while (tk.hasMoreTokens()) {
            final String segment = tk.nextToken();
            //System.out.println("segment =" + segment);
            segments[i++] = segment;
        }
        
        return segments;
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e){}
}
