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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.netbeans.modules.print.api.PrintManagerAccess;
import org.netbeans.modules.bpel.search.impl.util.Util;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
public final class View extends TopComponent {

  /**{@inheritDoc}*/
  public View() {
    setIcon(icon(Util.class, "find").getImage()); // NOI18N
    setLayout(new GridBagLayout());
    setFocusable(true);
  }

  void show(Tree tree) {
    myTree = tree;
    createPanel();
    open();
    requestActive();
  }

  private void createPanel() {
    removeAll();
    JScrollPane scrollPane = new JScrollPane(myTree);
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTH;

    // buttons
    add(createButtonPanel(), c);

    // tree
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    add(scrollPane, c);

    revalidate();
    repaint();
  }

  private JToolBar createButtonPanel() {
    JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
    toolBar.setFloatable(false);
    JButton button;

    // collapse/expand
    button = createButton(
      new ButtonAction(
        icon(Util.class, "expose"), // NOI18N
        i18n(View.class, "TLT_Expose")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.expose(myTree.getSelectedNode());
        }
      }
    );
    setSize(button);
    toolBar.add(button);

    // export
    button = createButton(
      new ButtonAction(
        icon(Util.class, "export"), // NOI18N
        i18n(View.class, "TLT_Export")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.export(myTree.getSelectedNode());
        }
      }
    );
    setSize(button);
    toolBar.add(button);

    // previous occurence
    button = createButton(
      new ButtonAction(
        icon(Util.class, "previous"), // NOI18N
        i18n(View.class, "TLT_Previous_Occurence")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.previousOccurence(myTree.getSelectedNode());
        }
      }
    );
    setSize(button);
    toolBar.add(button);

    // next occurence
    button = createButton(
      new ButtonAction(
        icon(Util.class, "next"), // NOI18N
        i18n(View.class, "TLT_Next_Occurence")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myTree.nextOccurence(myTree.getSelectedNode());
        }
      }
    );
    setSize(button);
    toolBar.add(button);

    // vlv: print
    button = createButton(PrintManagerAccess.getManager().getPreviewAction());
    setSize(button);
    toolBar.add(button);

    return toolBar;
  }

  private void setSize(JButton button) {
    button.setMaximumSize(IMAGE_BUTTON_SIZE);
    button.setMinimumSize(IMAGE_BUTTON_SIZE);
    button.setPreferredSize(IMAGE_BUTTON_SIZE);
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
    return i18n(View.class, "CTL_Search_Results_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  @Override
  public String getToolTipText()
  {
    return i18n(View.class, "CTL_Search_Results_Tooltip"); // NOI18N
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    myTree = null;
  }

  @Override
  protected String preferredID()
  {
    return NAME;
  }

  private Tree myTree;
  private static final Dimension IMAGE_BUTTON_SIZE = new Dimension(24, 24);
  public static final String NAME = "search"; // NOI18N
}
