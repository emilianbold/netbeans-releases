/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.nodes;

import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;


/** Ancestor class for all nodes.<p>
 * Example:<p>
 * <pre>
 *  JTreeOperator tree = ExplorerOperator.invoke().repositoryTab().tree();
 *  Node n = new Node(tree, "src");
 *  System.out.println(n.getText());
 *  n.performAPIActionNoBlock("org.openide.actions.NewTemplateAction");
 * </pre> */
public class Node {
    
    static final String linkSuffix = Bundle.getString("org.openide.loaders.Bundle", "FMT_shadowName", new String[]{""});

    /** JTreeOperator of tree where node lives */    
    protected JTreeOperator treeOperator;
    /** TreePath of node */    
    protected TreePath treePath;
    
    /** creates new Node instance
     * @param treeOperator JTreeOperator of tree where node lives
     * @param treePath String tree path of node */    
    public Node(JTreeOperator treeOperator, String treePath) {
        this(treeOperator, treeOperator.findPath(treePath, "|"));
    }
    
    /** creates new Node instance
     * @param treeOperator JTreeOperator of tree where node lives
     * @param treePath String tree path of node     
     * @param indexes String list of indexes of nodes in each level */    
    public Node(JTreeOperator treeOperator, String treePath, String indexes) {
        this(treeOperator, treeOperator.findPath(treePath, indexes, "|"));
    }
    
    /** creates new Node instance
     * @param parent parent Node
     * @param treeSubPath String tree sub-path from parent */    
    public Node(Node parent, String treeSubPath) {
        this(parent.tree(), parent.findSubPath(treeSubPath, "|"));
    }
    
    /** creates new Node instance
     * @param parent parent Node
     * @param childIndex int index of child under parent node */    
     public Node(Node parent, int childIndex) {
        this(parent.tree(), parent.tree().getChildPath(parent.getTreePath(), childIndex));
    }
    
    /** creates new Node instance
     * @param treeOperator JTreeOperator of tree where node lives
     * @param path TreePath of node */    
    public Node(JTreeOperator treeOperator, TreePath path) {
        this.treeOperator=treeOperator;
        this.treePath=path;
    }
    
    /** getter for JTreeOperator of tree where node lives
     * @return JTreeOperator of tree where node lives */    
    public JTreeOperator tree() {
        return(treeOperator);
    }
    
    /** Getter for TreePath of node. If the node is recreated (node is removed and 
     * then physically new node with the same name is added), the old TreePath 
     * is no longer valid and this method tries to find a new valid TreePath.
     * @return TreePath of node */    
    public TreePath getTreePath() {
        if(!isPresent()) {
            treePath = tree().findPath(convertPath(treePath), "|");
        }
        return(treePath);
    }
    
    /** getter for node text
     * @return Streing node text */    
    public String getText() {
        return(getTreePath().getLastPathComponent().toString());
    }
    
    private static String convertPath(TreePath path) {
        if (path==null) return null;
        int pathCount=path.getPathCount();
        if (pathCount<2) return "";
        String result = path.getPathComponent(1).toString();
        for(int i = 2; i < pathCount; i++) {
            result += "|" + path.getPathComponent(i).toString();
        }
        return result;
    }        
    
    /** getter for node path
     * @return String node path */    
    public String getPath() {
        return convertPath(getTreePath());
    }
    
    /** getter for path of parent node
     * @return String path of parent node */    
    public String getParentPath() {
        return convertPath(getTreePath().getParentPath());
    }
    
    /** calls popup menu on node
     * @return JPopupMenuOperator */    
    public JPopupMenuOperator callPopup() {
        return new JPopupMenuOperator(treeOperator.callPopupOnPath(getTreePath()));
    }
    
    /** performs action on node through main menu
     * @param menuPath main menu path of action */    
    public void performMenuAction(String menuPath) {
        new Action(menuPath, null).performMenu(this);
    }
    
    /** performs action on node through popup menu
     * @param popupPath popup menu path of action */    
    public void performPopupAction(String popupPath) {
        new Action(null, popupPath).performPopup(this);
    }
    
    /** performs action on node through API menu
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public void performAPIAction(String systemActionClass) {
        new Action(null, null, systemActionClass).performAPI(this);
    }
    
    /** performs action on node through main menu
     * @param menuPath main menu path of action */    
    public void performMenuActionNoBlock(String menuPath) {
        new ActionNoBlock(menuPath, null).performMenu(this);
    }
    
    /** performs action on node through popup menu
     * @param popupPath popup menu path of action */    
    public void performPopupActionNoBlock(String popupPath) {
        new ActionNoBlock(null, popupPath).performPopup(this);
    }
    
    /** performs action on node through API menu
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public void performAPIActionNoBlock(String systemActionClass) {
        new ActionNoBlock(null, null, systemActionClass).performAPI(this);
    }
    
    /** selects node */    
    public void select() {
        tree().selectPath(getTreePath());
        // sleep to workaround IDE's behavior. IDE consider as double click
        // two single clicks on the same position with delay shorter than 300 ms.
        // See org.openide.awt.MouseUtils.isDoubleClick().
        try {
            Thread.sleep(300);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** adds node into set of selected nodes */    
    public void addSelectionPath() {
        tree().addSelectionPath(getTreePath());
    }
    
    /** tests if node is leaf
     * @return boolean true when node does not have children */    
    public boolean isLeaf() {
        return tree().getChildCount(getTreePath())<1;
    }
    
    /** returns list of names of children
     * @return String[] list of names of children */    
    public String[] getChildren() {
        Object o[]=tree().getChildren(getTreePath().getLastPathComponent());
        if (o==null) return new String[0];
        String s[]=new String[o.length];
        for (int i=0; i<o.length; i++)
            s[i]=o[i].toString();
        return s;
    }
    
    /** determines if current node is link
     * @return boolean true if node is link */    
    public boolean isLink() {
        return getText().endsWith(linkSuffix);
    }
    
    /** verifies if node is still present. It expands parent path of the node
     * during verification.
     * @return boolean true when node is still present */    
    public boolean isPresent() {
        tree().expandPath(treePath.getParentPath());
        return tree().getRowForPath(treePath)>=0;
    }
    
    /** verifies node's popup path for presence (without invocation)
     * @param popupPath String popup path */    
    public void verifyPopup(String popupPath) {
        verifyPopup(new String[]{popupPath});
    }
    
    /** verifies node's popup paths for presence (without invocation)
     * @param popupPaths String[] popup paths
     */    
    public void verifyPopup(String[] popupPaths) {
        //invocation of root popup
        final JPopupMenuOperator popup=callPopup();
        for (int i=0; i<popupPaths.length; i++) {
            try {
                popup.showMenuItem(popupPaths[i], "|");
            } catch (NullPointerException npe) {
                throw new JemmyException("Popup path ["+popupPaths[i]+"] not found.");
            }
        }
        //closing popup
        popup.waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                try {
                    popup.pushKey(KeyEvent.VK_ESCAPE);
                    return false;
                } catch (JemmyException e) {}
                return true;
            }
            public String getDescription() {
                return "Popup menu closer";
            }
        });
    }

    class StringArraySubPathChooser implements JTreeOperator.TreePathChooser {
	String[] arr;
	int[] indices;
	JTreeOperator.StringComparator comparator;
        TreePath parentPath;
        int parentPathCount;
	StringArraySubPathChooser(TreePath parentPath, String[] arr, int[] indices, JTreeOperator.StringComparator comparator) {
	    this.arr = arr;
	    this.comparator = comparator;
	    this.indices = indices;
            this.parentPath=parentPath;
            this.parentPathCount=parentPath.getPathCount();
        }
        /** implementation of JTreeOperator.TreePathChooser
         * @param path TreePath
         * @param indexInParent int
         * @return boolean */        
	public boolean checkPath(TreePath path, int indexInParent) {
	    return(path.getPathCount() == arr.length + parentPathCount &&
		   hasAsParent(path, indexInParent));
        }
        /** implementation of JTreeOperator.TreePathChooser
         * @param path TreePath
         * @param indexInParent int
         * @return boolean */        
	public boolean hasAsParent(TreePath path, int indexInParent) {
            if (path.getPathCount()<=parentPathCount)
                return path.isDescendant(parentPath);
            if(arr.length+parentPathCount < path.getPathCount()) {
                return(false);
            }
            if(indices.length >= path.getPathCount()-parentPathCount &&
               indices[path.getPathCount()-parentPathCount-1] != indexInParent) {
                return(false);
            }
	    Object[] comps = path.getPath();
	    for(int i = parentPathCount; i < comps.length; i++) {
		if(!comparator.equals(comps[i].toString(), arr[i-parentPathCount])) {
		    return(false);
		}
	    }
	    return(true);
        }
        
        /** implementation of JTreeOperator.TreePathChooser
         * @return String description */        
	public String getDescription() {
	    String desc = "";
            Object parr[]=parentPath.getPath();
	    for(int i = 0; i < parr.length; i++) {
		desc = desc + parr[i].toString() + ", ";
	    }
	    for(int i = 0; i < arr.length; i++) {
		desc = desc + arr[i] + ", ";
	    }
	    if(desc.length() > 0) {
		desc = desc.substring(0, desc.length() - 2);
	    }
	    return("[ " + desc + " ]");
	}
        
    }
    
    TreePath findSubPath(String subPath, String delimiter) {
        return findSubPath(subPath, "", delimiter);
    }
    
    TreePath findSubPath(String subPath, String indexes, String delimiter) {
        JTreeOperator o=tree();
        String indexStr[]=o.parseString(indexes, delimiter);
        int indexInt[]= new int[indexStr.length];
        for (int i=0; i<indexStr.length; i++)
            indexInt[i]=Integer.parseInt(indexStr[i]);
        return o.findPath(new Node.StringArraySubPathChooser(getTreePath(), o.parseString(subPath, delimiter), indexInt, o.getComparator()));
    }
    
    /** Expands current node to see children */    
    public void expand() {
        treeOperator.expandPath(getTreePath());
        waitExpanded();
    }
    
    /** Collapse current node to hide children */    
    public void collapse() {
        treeOperator.collapsePath(getTreePath());
        waitCollapsed();
    }
    
    /** Waits for node to be expanded */    
    public void waitExpanded() {
        treeOperator.waitExpanded(getTreePath());
    }
    
    /** Waits for node to be collapsed */    
    public void waitCollapsed() {
        treeOperator.waitCollapsed(getTreePath());
    }
    
    /** Informs if current node is expanded
     * @return boolean true when node is expanded
     */    
    public boolean isExpanded() {
        return treeOperator.isExpanded(getTreePath());
    }
    
    /** Informs if current node is collapsed
     * @return boolean true when node is collapsed
     */    
    public boolean isCollapsed() {
        return treeOperator.isCollapsed(getTreePath());
    }
         
/*    protected Action[] getActions() {
        return null;
    }
    
    public boolean hasAction(Class actionClass) {
        Action actions[] = getActions();
        for (int i=0; actions!=null && i<actions.length; i++)
            if (actionClass.equals(actions[i].getClass()))
                return true;
        return false;
    }*/
    
}
