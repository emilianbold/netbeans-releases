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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import org.netbeans.modules.bpel.search.impl.util.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
public final class View extends TopComponent implements FocusListener {

  /**{@inheritDoc}*/
  public View() {
    setIcon(Util.getIcon("find").getImage()); // NOI18N
    setLayout(new GridBagLayout());
  }

  void show(JTree tree) {
    myTree = tree;
    myModifiedDate = new Date(System.currentTimeMillis());
    myTree.addFocusListener(this);
    createPanel();
    open();
    requestActive();
    setActivatedNode();
  }

  /**{@inheritDoc}*/
  public void focusGained(FocusEvent event) {
    setActivatedNode();
  }

  /**{@inheritDoc}*/
  public void focusLost(FocusEvent event) {
    setActivatedNodes(null);
  }

  private void setActivatedNode() {
    String root = myTree.getModel().getRoot().toString();
    String name = NbBundle.getMessage(
      View.class, "CTL_Search_Results_Print", root); // NOI18N
    setActivatedNodes(new Node [] { new Node(myTree, name, myModifiedDate) });
  }

  private void createPanel() {
    removeAll();
    JScrollPane scrollPanel = new JScrollPane(myTree);
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    add(scrollPanel, c);

    revalidate();
    repaint();
  }
  
  /**{@inheritDoc}*/
  @Override
  public HelpCtx getHelpCtx()
  {
    return HelpCtx.DEFAULT_HELP;
  }

  /**{@inheritDoc}*/
  @Override
  public int getPersistenceType()
  {
    return PERSISTENCE_ALWAYS;
  }
      
  /**{@inheritDoc}*/
  @Override
  public String getName()
  {
    return NAME;
  }
  
  /**{@inheritDoc}*/
  @Override
  public String getDisplayName()
  {
    return NbBundle.getMessage(View.class, "CTL_Search_Results_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  @Override
  public String getToolTipText()
  {
    return NbBundle.getMessage(View.class, "CTL_Search_Results_Tooltip"); // NOI18N
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    myTree = null;
    myModifiedDate = null;
  }

  @Override
  protected String preferredID()
  {
    return NAME;
  }

  public static final String NAME = "search"; // NOI18N
  private JTree myTree;
  private Date myModifiedDate;
}
