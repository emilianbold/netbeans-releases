/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.visualweb.gravy.nodes;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.visualweb.gravy.actions.Action;
import org.netbeans.modules.visualweb.gravy.actions.ActionNoBlock;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.openide.explorer.view.Visualizer;

/** Ancestor class for all nodes.<p>
 * Nodes should help to easier testing of JTree's.
 * The most frequent usage in IDE is in the Explorer Window but nodes can be 
 * used in any component which includes a JTree instance.
 * Nodes are also used as parameters for action's performing.
 * <p>
 * Example:<p>
 * <pre>
 *  Node node = new Node(RepositoryTabOperator.invoke().getRootNode(), "jellytools/src|org|netbeans|jellytools");
 *  System.out.println(node.getText());
 *  new NewTemplateAction().performAPI(node);
 * </pre> 
 */
public class Node {
    
    static final String linkSuffix = Bundle.getString("org.openide.loaders.Bundle", "FMT_shadowName", new String[]{""});
    
    /** JTreeOperator of tree where node lives */
    protected JTreeOperator treeOperator;
    /** TreePath of node */
    protected TreePath treePath;
    /** Comparator used for this node instance. */
    private Operator.StringComparator comparator;
    
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
    
    /** Sets comparator fot this node. Comparator is used for all methods
     * after this method is called.
     * @param comparator new comparator to be set (e.g.
     *                   new Operator.DefaultStringComparator(true, true);
     *                   to search string item exactly and case sensitive)
     */
    public void setComparator(Operator.StringComparator comparator) {
        this.comparator = comparator;
        tree().setComparator(comparator);
    }
    
    /** Gets comparator for this node instance.
     * @return comparator for this node instance.
     */
    public Operator.StringComparator getComparator() {
        if(comparator == null) {
            comparator = tree().getComparator();
        }
        return comparator;
    }
    
    /** Getter for JTreeOperator of tree where node lives
     * @return JTreeOperator of tree where node lives */
    public JTreeOperator tree() {
        return(treeOperator);
    }
    
    /** Getter for TreePath of node.
     * @return TreePath of node */
    public TreePath getTreePath() {
        return(treePath);
    }
    
    /** Getter for node text
     * @return String node text */
    public String getText() {
        return(treePath.getLastPathComponent().toString());
    }
    
    /** Convert TreePath to string
     * @return String TreePath converted to string */
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
    
    /** Getter for node path
     * @return String node path */
    public String getPath() {
        return convertPath(treePath);
    }
    
    /** Getter for path of parent node
     * @return String path of parent node */
    public String getParentPath() {
        return convertPath(treePath.getParentPath());
    }
    
    /** Returns Object instance which represents org.openide.nodes.Node
     * for this jellytools node.
     * @return Object instance which represents org.openide.nodes.Node
     */
    public Object getOpenideNode() {
        return Visualizer.findNode(this.getTreePath().getLastPathComponent());
    }
    
    /** calls popup menu on node
     * @return JPopupMenuOperator */
    public JPopupMenuOperator callPopup() {
        return new JPopupMenuOperator(treeOperator.callPopupOnPath(treePath));
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
    
    /** 
     * Selects node.
     */
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
        return tree().getChildCount(treePath)<1;
    }
    
    /** returns list of names of children
     * @return String[] list of names of children */
    public String[] getChildren() {
        tree().expandPath(treePath);
        Object o[]=tree().getChildren(treePath.getLastPathComponent());
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
        return o.findPath(new Node.StringArraySubPathChooser(treePath, o.parseString(subPath, delimiter), indexInt, getComparator()));
    }
    
    /** Expands current node to see children */
    public void expand() {
        treeOperator.expandPath(treePath);
        waitExpanded();
    }
    
    /** Collapse current node to hide children */
    public void collapse() {
        treeOperator.collapsePath(treePath);
        waitCollapsed();
    }
    
    /** Waits for node to be expanded */
    public void waitExpanded() {
        treeOperator.waitExpanded(treePath);
    }
    
    /** Waits for node to be collapsed */
    public void waitCollapsed() {
        treeOperator.waitCollapsed(treePath);
    }
    
    /** Informs if current node is expanded
     * @return boolean true when node is expanded
     */
    public boolean isExpanded() {
        return treeOperator.isExpanded(treePath);
    }
    
    /** Informs if current node is collapsed
     * @return boolean true when node is collapsed
     */
    public boolean isCollapsed() {
        return treeOperator.isCollapsed(treePath);
    }
    
    /*protected Action[] getActions() {
        return null;
    }
 
    public boolean hasAction(Class actionClass) {
        Action actions[] = getActions();
        for (int i=0; actions!=null && i<actions.length; i++)
            if (actionClass.equals(actions[i].getClass()))
                return true;
        return false;
    }*/
    
    /** verifies node's popup paths (of all actions) for presence (without invocation)
     * @param actions array of actions to be verified
     */
    public void verifyPopup(Action actions[]) {
        ArrayList popupPaths=new ArrayList();
        String path;
        for (int i=0; i<actions.length; i++) {
            path=actions[i].getPopupPath();
            if (path!=null) {
                popupPaths.add(path);
            }
        }
        verifyPopup((String[])popupPaths.toArray(new String[0]));
    }
    
    
    /** Checks whether child with specified name is present under this node.
     * @param childName name of child node
     * @return true if child is present; false otherwise
     */
    public boolean isChildPresent(String childName) {
        String[] children = this.getChildren();
        for(int i=0;i<children.length;i++) {
            if(getComparator().equals(children[i], childName)) {
                return true;
            }
        }
        return false;
    }
    
    /** Waits until child with specified name is not present under this node.
     * It can throw TimeoutExpiredException, if child is still present.
     * @param childName name of child node
     */
    public void waitChildNotPresent(final String childName) {
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    return isChildPresent(childName) ? null : Boolean.TRUE;
                }
                public String getDescription() {
                    return("Child \""+childName+"\" not present under parent \""+getPath()+"\"");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
    /** Waits until this node is no longer present.
     * It can throw TimeoutExpiredException, if the node is still present.
     */
    public void waitNotPresent() {
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    return isPresent() ? null : Boolean.TRUE;
                }
                public String getDescription() {
                    return("Wait node "+getPath()+" not present.");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
}
