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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.search.impl.ui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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

import org.openide.windows.WindowManager;
import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchEvent;
import org.netbeans.modules.xml.search.spi.SearchListener;
import org.netbeans.modules.bpel.search.impl.util.Util;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
final class Tree extends JTree implements SearchListener {

  Tree() {
    super(new DefaultTreeModel(new DefaultMutableTreeNode()));
    myRoot = (DefaultMutableTreeNode) getModel().getRoot();

    // vlv: print
    String root = getModel().getRoot().toString();
    String name = i18n(Tree.class, "LBL_Tree_Name", root); // NOI18N
    putClientProperty(java.awt.print.Printable.class, name);
  }

  public void searchStarted(SearchEvent event) {
//out();
    myFoundCount = 0;
  }

  public void searchFound(SearchEvent event) {
    SearchElement element = event.getSearchElement();
//out("Found: " + element);
//out("       " + element.getName());
    addElement(myRoot, element, getElements(element));
    myFoundCount++;
  }

  public void searchFinished(SearchEvent event) {
    String text = event.getSearchOption().getText();
    String count = String.valueOf(myFoundCount);

    String title = i18n(
      Tree.class, "LBL_Found_Occurrences", text, "" + myFoundCount); // NOI18N

    myRoot.setUserObject(new SearchElement.Adapter(
      title, title, icon(Util.class, "find"), null)); // NOI18N

    createOccurences();

    updateRoot();
    View view = (View) WindowManager.getDefault().findTopComponent(View.NAME);
    view.show(this);
  }

  private void createOccurences() {
    myOccurences = new ArrayList<DefaultMutableTreeNode>();
    createOccurences(myRoot);
    myIndex = -1;
  }

  private void createOccurences(DefaultMutableTreeNode node) {
    Enumeration children = node.children();

    if (node.isLeaf()) {
      myOccurences.add(node);
    }
    while (children.hasMoreElements()) {
      createOccurences((DefaultMutableTreeNode) children.nextElement());
    }
  }

  private void updateRoot() {
    getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION);
    ToolTipManager.sharedInstance().registerComponent(this);
    setCellRenderer(new TreeRenderer());
    setShowsRootHandles(false);
    setRootVisible(true);
    setSelectionPath(new TreePath(myRoot.getPath()));
    expandPath(new TreePath(myRoot.getPath()));

    addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(TreeExpansionEvent event) {
      }
      public void treeWillCollapse(TreeExpansionEvent event) {
//out("Will collapse: " + getNode(event.getPath()));
        collapseChildren(getNode(event.getPath()));
      }
    });
    addTreeExpansionListener(new TreeExpansionListener() {
      public void treeExpanded(TreeExpansionEvent event) {
//out("Expanded: " + getNode(event.getPath()));
        expandChildren(getNode(event.getPath()));
        updateSize();
      }
      public void treeCollapsed(TreeExpansionEvent event) {
        updateSize();
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
          showPopupMenu(event, event.getX(), event.getY());
        }
      }
      public void mouseClicked(MouseEvent event) {
        // double click
        if (event.getClickCount() == 2) {
          DefaultMutableTreeNode node = getSelectedNode();

          if (node.isLeaf()) {
            select(node);
          }
        }
      }
    });
    addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent event) {
        handleEvent(event);
      }
    });
    updateSize();
  }

  private void handleEvent(KeyEvent event) {
    DefaultMutableTreeNode node = getSelectedNode();
    int code = event.getKeyCode();
    int modifiers = event.getModifiers();

    if (code == KeyEvent.VK_F10 && isShift(modifiers)) {
      showPopupMenu(event, 0, 0);
    }
    else {
      handleAction(code, modifiers, node);
    }
  }

  private void handleAction(int code, int modifiers, DefaultMutableTreeNode node) {
//    if (code == KeyEvent.VK_D && isAlt(modifiers)) {
//      select(node);
//    }
//    else if (code == KeyEvent.VK_O && isAlt(modifiers)) {
//      gotoSource(node);
//    }
//    else
    if (code == KeyEvent.VK_C && isCtrl(modifiers)) {
      copy(node);
    }
//    else if (code == KeyEvent.VK_H && isCtrl(modifiers)) {
//      export(node);
//    }
    else {
      handleResult(code, modifiers, node);
    }
  }

  private void handleResult(int code, int modifiers, DefaultMutableTreeNode node) {
    if (code == KeyEvent.VK_F12 && isShift(modifiers)) {
      previousOccurence(node);
    }
    else if (code == KeyEvent.VK_F12) {
      nextOccurence(node);
    }
//    else if (code == KeyEvent.VK_E && isCtrl(modifiers)) {
//      expose(node);
//    }
    else if (code == KeyEvent.VK_DELETE) {
      remove(node);
    }
  }

  private void showPopupMenu(ComponentEvent event, int x, int y) {
    final DefaultMutableTreeNode node = getSelectedNode();
    JPopupMenu popup = new JPopupMenu();
    JMenuItem item;

    createAction(popup, node);

    popup.addSeparator(); // -----------------------------------------------------

    // previous occurence
    item = createItem("LBL_Previous_Occurence"); // NOI18N
    item.setEnabled(true);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        previousOccurence(node);
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,KeyEvent.SHIFT_MASK));
    popup.add(item);

    // next occurence
    item = createItem("LBL_Next_Occurence"); // NOI18N
    item.setEnabled(true);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        nextOccurence(node);
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
    popup.add(item);

    popup.addSeparator(); // -----------------------------------------------------

    // collapse / expand
    item = createItem("LBL_Collapse_Expand"); // NOI18N
    item.setEnabled( !node.isLeaf());
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        expose(node);
      }
    });
//  item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
    popup.add(item);

    // remove
    item = createItem("LBL_Remove"); // NOI18N
    item.setEnabled( !node.isRoot());
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        remove(node);
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    popup.add(item);

    // show
    popup.show(event.getComponent(), x, y);
  }

  private void createAction(JPopupMenu popup, final DefaultMutableTreeNode node) {
    JMenuItem item;

    // select
    item = createItem("LBL_Select"); // NOI18N
    item.setEnabled( !node.isRoot());
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        select(node);
      }
    });
//  item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_MASK));
    popup.add(item);

    // go to source
    item = createItem("LBL_Go_to_Source"); // NOI18N
    item.setEnabled( !node.isRoot());
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        gotoSource(node);
      }
    });
//  item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_MASK));
    popup.add(item);

    // copy
    item = createItem("LBL_Copy"); // NOI18N
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        copy(node);
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    popup.add(item);

    // export
    item = createItem("LBL_Export"); // NOI18N
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        export(node);
      }
    });
//  item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
    popup.add(item);
  }

  private boolean isAlt(int modifiers) {
    return isModifier(modifiers, KeyEvent.ALT_MASK);
  }

  private boolean isShift(int modifiers) {
    return isModifier(modifiers, KeyEvent.SHIFT_MASK);
  }

  private boolean isCtrl(int modifiers) {
    return isModifier(modifiers, KeyEvent.CTRL_MASK);
  }

  private boolean isModifier(int modifiers, int mask) {
    return (modifiers & mask) != 0;
  }

  private void updateSize() {
    putClientProperty(Dimension.class, getMaximumSize());
  }

  private void gotoSource(DefaultMutableTreeNode node) {
    ((SearchElement) node.getUserObject()).gotoSource();
  }

  private void select(DefaultMutableTreeNode node) {
    ((SearchElement) node.getUserObject()).select();
  }

  public void previousOccurence(TreeNode node) {
    myIndex--;

    if (myIndex < 0) {
      myIndex = myOccurences.size() - 1;
    }
    selectOccurence();
  }

  public void nextOccurence(TreeNode node) {
    myIndex++;

    if (myIndex == myOccurences.size()) {
      myIndex = 0;
    }
    selectOccurence();
  }

  private void selectOccurence() {
    TreePath path = new TreePath(myOccurences.get(myIndex).getPath());
    setSelectionPath(path);
    scrollPathToVisible(path);
  }

  private void copy(TreeNode node) {
    StringBuffer buffer = new StringBuffer();

    copy(node, buffer, ""); // NOI18N
    buffer.append(LS);

    StringSelection selection = new StringSelection(buffer.toString());
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
      selection, selection);
  }

  private void copy(TreeNode node, StringBuffer buffer, String indent) {
    buffer.append(indent + node + LS);
    Enumeration children = node.children();

    while (children.hasMoreElements()) {
      TreeNode child = (TreeNode) children.nextElement();
      copy(child, buffer, indent + "    "); // NOI18N
    }
  }

  public void export(DefaultMutableTreeNode node) {
    List<List<String>> descriptions = new ArrayList<List<String>>();
    export(node, descriptions);
    descriptions.add (null);

    if (myExport == null) {
      myExport = new Export();
    }
    myExport.show(descriptions, myRoot.toString());
  }

  private void export(DefaultMutableTreeNode node, List<List<String>> descriptions) {
    if (node.isLeaf()) {
      List<String> description = new ArrayList<String>();
      description.add(getDescription(node));
      description.add(node.toString());
      descriptions.add(description);
    }
    Enumeration children = node.children();

    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
      export(child, descriptions);
    }
  }

  private String getDescription(DefaultMutableTreeNode node) {
    if (isRoot(node)) {
      return ""; // NOI18N
    }
    String description = getDescription((DefaultMutableTreeNode) node.getParent());

    if ( !node.isLeaf()) {
      if ( !isRoot((DefaultMutableTreeNode) node.getParent())) {
        description += LS;
      }
      description += node;
    }
    return description;
  }

  private boolean isRoot(DefaultMutableTreeNode node) {
    if (node == null) {
      return true;
    }
    if (node.getParent() == null) {
      return true;
    }
    if (node.getParent().getParent() == null) {
      return true;
    }
    return false;
  }

  public void expose(DefaultMutableTreeNode node) {
    if (node == null || node.isLeaf()) {
      return;
    }
    myIsReformAll = true;
    TreePath path = new TreePath(node.getPath());
    boolean isExpanded = isExpanded(path);

    // for root special check
    if (node.isRoot()) {
      Enumeration children = node.children();
      isExpanded = false;

      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) children.nextElement();
        isExpanded = isExpanded(new TreePath(child.getPath()));

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
        collapsePath(path);
      }
    }
    else {
      // expand
      expandPath(path);

      if (isExpanded(path)) {
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
        expandPath(path);

        if (isExpanded(path)) {
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
          expandPath(path);

          if (isExpanded(path)) {
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
        collapsePath(path);
      }
    }
  }

  private void remove(DefaultMutableTreeNode node) {
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

    if (parent == null) {
      return;
    }
    if (printConfirmation(i18n(Tree.class, "LBL_Are_You_Sure"))) { // NOI18N
      parent.remove(node);
      updateUI();
    }
  }

  private DefaultMutableTreeNode getNode(TreePath path) {
    return (DefaultMutableTreeNode) path.getLastPathComponent();
  }

  public DefaultMutableTreeNode getSelectedNode() {
    TreePath [] paths = getSelectionPaths();

    if (paths == null || paths.length == 0) {
      return myRoot;
    }
    TreePath path = paths [0];

    if (path == null) {
      return myRoot;
    }
    return getNode(path);
  }

  private JMenuItem createItem(String name) {
    return new JMenuItem(i18n(Tree.class, name));
  }

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

  // ----------------------------------------------------------------------
  private static final class TreeRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent (
      JTree tree, Object value, boolean select, boolean expanded,
      boolean leaf, int row, boolean focus)
    {
      super.getTreeCellRendererComponent(
        tree, value, select, expanded, leaf, row, focus);
      SearchElement element =
        (SearchElement) ((DefaultMutableTreeNode) value).getUserObject();
      setText(element.getName());
      setToolTipText(element.getToolTip());
      setIcon(element.getIcon());
      return this;
    }
  }

  private int myIndex;
  private Export myExport;
  private int myFoundCount;
  private boolean myIsReformAll;
  private DefaultMutableTreeNode myRoot;
  private List<DefaultMutableTreeNode> myOccurences;
}
