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
package org.netbeans.modules.bpel.search.impl.ui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import org.netbeans.modules.print.api.PrintUtil;
import org.netbeans.modules.bpel.search.api.SearchEvent;
import org.netbeans.modules.bpel.search.api.SearchElement;
import org.netbeans.modules.bpel.search.spi.SearchListener;
import org.netbeans.modules.bpel.search.impl.util.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
final class Tree implements SearchListener {
/*
  Tree() {
    myConfig = new Config (key);
    mySeparator = I18n.get (myConfig.get ("separator", "$separator." + myKey));
  }
// todo r
*/
  public void searchStarted(SearchEvent event) {
//out();
    myRoot = new DefaultMutableTreeNode();
    myFoundCount = 0;
  }

  public void searchFound(SearchEvent event) {
    SearchElement element = event.getSearchElement();
//out("Found: " + element);
//out("       " + element.getText());
    addElement(myRoot, element, getElements(element));
    myFoundCount++;
  }

  public void searchFinished(SearchEvent event) {
    String text = event.getSearchOption().getText();
    String count = String.valueOf(myFoundCount);
    String title = NbBundle.getMessage(
      Tree.class, "LBL_Found_Occurrences", text, myFoundCount); // NOI18N
    myRoot.setUserObject(
      new SearchElement.Adapter(title, title, Util.getIcon("find"), null)); // NOI18N
// todo r
//  factorizeRoot(myRoot);
    View view = (View) WindowManager.getDefault().findTopComponent(View.NAME);
    view.show(createTree());
  }

  private JTree createTree() {
    myTree = new JTree(new DefaultTreeModel(myRoot));
    myTree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    ToolTipManager.sharedInstance().registerComponent(myTree);
    myTree.setCellRenderer(new TreeRenderer());
    myTree.setShowsRootHandles(false);
    myTree.setRootVisible(true);

    myTree.addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(TreeExpansionEvent event) {
      }
      public void treeWillCollapse(TreeExpansionEvent event) {
//out("Will collapse: " + getNode(event.getPath()));
        collapseChildren(getNode(event.getPath()));
      }
    });
    myTree.addTreeExpansionListener(new TreeExpansionListener() {
      public void treeExpanded(TreeExpansionEvent event) {
//out("Expanded: " + getNode(event.getPath()));
        expandChildren(getNode(event.getPath()));
        updateSize();
      }
      public void treeCollapsed(TreeExpansionEvent event) {
        updateSize();
      }
    });
    myTree.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
          showPopupMenu(event);
        }
      }
      public void mouseClicked(MouseEvent event) {
        // simple click
        if (event.getClickCount() == 1) {
          select(getNode(event));
        }
      }
    });
    myTree.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == event.VK_ENTER) {
          reform(getSelectedNodes());
//todo a  goto(getSelectedNodes());
        }
      }
    });
    updateSize();

    return myTree;
  }

  private void updateSize() {
    myTree.putClientProperty(Dimension.class.getName(), myTree.getMaximumSize());
  }

  private void showPopupMenu(MouseEvent event) {
    DefaultMutableTreeNode node = getNode(event);
    JPopupMenu popup = new JPopupMenu();
    JMenuItem item;

    // select
    item = createItem("LBL_Select_Action"); // NOI18N
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        select(getSelectedNodes());
      }
    });
    if ( !node.isRoot()) {
      popup.add(item);
    }
    popup.addSeparator();
    // todo start here

    if (popup.getSubElements().length != 0) {
      popup.show(event.getComponent(), event.getX(), event.getY());
    }
  }

  private JMenuItem createItem(String name) {
    return new JMenuItem(NbBundle.getMessage(Tree.class, name));
  }

  private void select(DefaultMutableTreeNode [] nodes) {
    if (nodes == null) {
      return;
    }
    for (DefaultMutableTreeNode node : nodes) {
      select(node);
    }
  }

  private void select(DefaultMutableTreeNode node) {
    if (node == null || node.isRoot()) {
      return;
    }
    ((SearchElement) node.getUserObject()).select();
  }

  private void reform(DefaultMutableTreeNode [] nodes) {
    if (nodes == null) {
      return;
    }
    for (DefaultMutableTreeNode node : nodes) {
      reform(node);
    }
  }

  private void reform(DefaultMutableTreeNode node) {
    if (node == null || node.isLeaf()) {
      return;
    }
    myIsReformAll = true;
    TreePath path = new TreePath(node.getPath());
    boolean isExpanded = myTree.isExpanded(path);

    // for root special check
    if (node.isRoot()) {
      Enumeration children = node.children();
      isExpanded = false;

      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) children.nextElement();
        isExpanded = myTree.isExpanded(new TreePath(child.getPath()));

        if (isExpanded) {
          break;
        }
      }
    }
    if (isExpanded) {
      // collapse
      if (node.isRoot()) {
        collapseChildren(node);
      }
      else {
        myTree.collapsePath(path);
      }
    }
    else {
      // expand
      myTree.expandPath(path);

      if (myTree.isExpanded(path)) {
        expandChildren(node);
      }
    }
    myIsReformAll = false;
  }

  private void expandChildren(TreeNode node) {
    Enumeration children = node.children();
    
    if (myIsReformAll) {
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) children.nextElement();
        TreePath path = new TreePath(child.getPath());
        myTree.expandPath(path);

        if (myTree.isExpanded(path)) {
          expandChildren(child);
        }
      }
    }
    else {
      // process node which has only one child
      if (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) children.nextElement();

        if ( !children.hasMoreElements()) {
          TreePath path = new TreePath(child.getPath());
          myTree.expandPath(path);

          if (myTree.isExpanded(path)) {
            expandChildren(child);
          }
        }
      }
    }
  }

  private void collapseChildren(TreeNode node) {
    if (myIsReformAll) {
      Enumeration children = node.children();
    
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) children.nextElement();
        TreePath path = new TreePath(child.getPath());
        myTree.collapsePath(path);
      }
    }
  }

  private DefaultMutableTreeNode getNode(MouseEvent event) {
    return getNode(myTree.getPathForLocation(event.getX(), event.getY()));
  }

  private DefaultMutableTreeNode getNode(TreePath path) {
    if (path == null) {
      return null;
    }
    return (DefaultMutableTreeNode) path.getLastPathComponent();
  }

  private DefaultMutableTreeNode [] getSelectedNodes() {
    TreePath [] treePaths = myTree.getSelectionPaths();

    if (treePaths == null) {
      return null;
    }
    DefaultMutableTreeNode [] nodes = new DefaultMutableTreeNode[treePaths.length];

    for (int i=0; i < treePaths.length; i++) {
      nodes [i] = getNode(treePaths[i]);
    }
    return nodes;
  }

/*todo r
  private void factorizeRoot(DefaultMutableTreeNode root) {
    int count = root.getChildCount ();

    for (int i=0; i < count; i++) {
      factorize ((DefaultMutableTreeNode) root.getChildAt (i));
    }
  }

  private void factorize (DefaultMutableTreeNode node) {
//System.out.println ();
//System.out.println ("SEE: " + node);
    if (node.getChildCount () != 1) {
//System.out.println ("    return: has more children: " + node.getChildCount ());
      return;
    }
    DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt (0);

    if (child.isLeaf ()) {
//System.out.println ("    return: child is leaf: " + child);
      return;
    }
    DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) child.getChildAt (0);

    if (grandChild.isLeaf ()) {
//System.out.println ("    return: grandchild is leaf: " + grandchild);
      return;
    }
    // union node with child
//System.out.println ("==== see  node: " + node  + " " + ((SearchElement) node.getUserObject ()));
//System.out.println ("==== see child: " + child + " " + ((SearchElement) child.getUserObject ()));
    SearchElement searchElement = (SearchElement) child.getUserObject ();
    String text = "" + node + mySeparator + child;
    node.setUserObject (searchElement.clone (text));
//System.out.println ("==== set text: " + text);

    node.remove (child);
//System.out.println ("      remove child: " + child);

    Vector grandchildren = new Vector ();
    Enumeration children = child.children ();

    while (children.hasMoreElements ()) {
      DefaultMutableTreeNode next = (DefaultMutableTreeNode) children.nextElement ();
      grandchildren.add (next);
//System.out.println ("        grandchild: " + next);
    }
//System.out.println ("        --------------------");

    for (int i=0; i < grandchildren.size (); i++) {
      DefaultMutableTreeNode next = (DefaultMutableTreeNode) grandchildren.get (i);
      node.add (next); 
//System.out.println ("    add grandchild: " + next);
    }
//System.out.println ("           updated: " + node);
    factorize (node);
  }

  public void copy () {
    DefaultMutableTreeNode [] nodes = getSelectedNodes();

    if (nodes == null) {
      return;
    }
    StringBuffer buffer = new StringBuffer ();

    for (int i=0; i < nodes.length; i++) {
      copy (nodes [i], buffer, "");
      buffer.append ("\n");
    }
    StringSelection stringSelection = new StringSelection (buffer.toString ());
    Toolkit.getDefaultToolkit ().getSystemClipboard ().setContents (stringSelection, stringSelection);
  }

  private void copy (DefaultMutableTreeNode node, StringBuffer buffer, String indent) {
    buffer.append (indent + node + "\n");
    Enumeration children = node.children ();

    while (children.hasMoreElements ()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement ();
      copy (child, buffer, indent + "   ");
    }
  }

  public void remove () {
    DefaultMutableTreeNode [] nodes = getSelectedNodes();

    if (nodes == null) {
      return;
    }
    for (int i=0; i < nodes.length; i++) {
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodes [i].getParent ();

      if (parent == null) {
        continue;
      }
      parent.remove (nodes [i]);
    }
//todo r    myTree.updateUI ();
myTree.revalidate();
myTree.repaint();
  }

  public void removeAll () {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) myTree.getModel ().getRoot ();
    root.removeAllChildren ();
//todo r    myTree.updateUI ();
myTree.revalidate();
myTree.repaint();
  }

  public void export () { // todo rename save
    DefaultMutableTreeNode [] nodes = getSelectedNodes();

    if (nodes == null) {
      return;
    }
    Vector descriptions = new Vector ();

    for (int i=0; i < nodes.length; i++) {
      export (nodes [i], descriptions);
      descriptions.add (null);
    }
    if (myExport == null) {
      myExport = new Export ();
    }
    myExport.show (descriptions, myTitle);
  }

  private void export (DefaultMutableTreeNode node, Vector descriptions) {
    if (node.isLeaf ()) {
      Vector description = new Vector ();
      description.add (getDescription (node));
      description.add ("" + node);
      descriptions.add (description);
    }
    Enumeration children = node.children ();

    while (children.hasMoreElements ()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement ();
      export (child, descriptions);
    }
  }

  private String getDescription (DefaultMutableTreeNode node) {
    if (isRoot (node)) {
      return "";
    }
    String description = getDescription ((DefaultMutableTreeNode) node.getParent ());

    if ( !node.isLeaf () ) {
      if ( !isRoot ((DefaultMutableTreeNode) node.getParent ())) {
        description += mySeparator;
      }
      description += node;
    }
    return description;
  }

  private boolean isRoot (DefaultMutableTreeNode node) {
    if (node == null) {
      return true;
    }
    if (node.getParent () == null) {
      return true;
    }
    if (node.getParent ().getParent () == null) {
      return true;
    }
    return false;
  }

  private String myId;
  private String myTitle;
  private String mySeparator;
  private Export myExport = null;
//todo r  private JPanel myPanel = new JPanel (new GridBagLayout ());
*/
  private void addElement(
    MutableTreeNode root,
    SearchElement element,
    Iterator<SearchElement> elements)
  {
    if (root == null) {
      return;
    }
    SearchElement next = null;

    if (elements.hasNext()) {
      next = elements.next();
    }
    if (next == null) { // it is leaf
//out("add leaf: " + element);
      root.insert(new DefaultMutableTreeNode(element), 0);
      return;
    }
    // try to find node among children
    Enumeration children = root.children();

//out("try to add child: " + next);
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

      if (child.isLeaf()) {
//out("skip leaf: " + child);
        continue;
      }
//todo r      String text = ((SearchElement) child.getUserObject()).getText();
//out("see child: " + text);
//out("see next: " + next.toString());

//todo r?      if (next.getText().equals(text)) {
//      if (next.toString().equals(child.toString())) {
      if (child.getUserObject().equals(next)) {
        // go to the next level
//out("next level");
        addElement(child, element, elements);
        return;
      }
    }
    // cannot find the same node, add new node
    MutableTreeNode node = new DefaultMutableTreeNode(next);
//out("add internal: " + next);
    root.insert(node, 0);
    addElement(node, element, elements);
  }

  private Iterator<SearchElement> getElements(SearchElement element) {
    List<SearchElement> elements = new ArrayList<SearchElement>();
    SearchElement parent = element.getParent();

    while (parent != null) {
      elements.add(0, parent);
      parent = parent.getParent();
    }
    return elements.iterator();
  }

  private void out() {
    PrintUtil.out();
  }

  private void out(Object object) {
    PrintUtil.out(object);
  }

  // ----------------------------------------------------------------
  private static class TreeRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent (
      JTree tree, Object value, boolean select, boolean expanded,
      boolean leaf, int row, boolean focus)
    {
      super.getTreeCellRendererComponent(
        tree, value, select, expanded, leaf, row, focus);
      SearchElement element =
        (SearchElement) ((DefaultMutableTreeNode) value).getUserObject();
      setText(element.getText());
      setToolTipText(element.getToolTip());
      setIcon(element.getIcon());
      return this;
    }
  }

// todo r
//  private String myKey;
//  private Config myConfig;
//  private String mySeparator;
  private JTree myTree;
  private int myFoundCount;
  private boolean myIsReformAll;
  private MutableTreeNode myRoot;
}
