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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;

import org.netbeans.modules.print.api.PrintUtil.Dialog;
import org.netbeans.modules.bpel.search.api.SearchOption;
import org.netbeans.modules.bpel.search.api.SearchMatch;
import org.netbeans.modules.bpel.search.spi.SearchEngine;
import org.netbeans.modules.bpel.search.spi.SearchListener;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.23
 */
public final class Search extends Dialog {

  /**{@inheritDoc}*/
  public Component getComponent(List<SearchEngine> engines, Object source) {
    mySource = source;
    myTree = new Tree();
    mySearchEngine = engines.get(0);
    mySearchEngine.addSearchListener(myTree);
    show();
    return getComponent();
  }

  private JPanel createMainPanel() {
//out("Create Main panel");
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 1;

    // text field
    myText = new JTextField("*"); // todo r
    // todo m
    int width = TEXT_WIDTH;
    int height = myText.getPreferredSize().height;
    myText.setPreferredSize(new Dimension(width, height));
    myText.setMinimumSize(new Dimension(width, height));
    panel.add(myText, c);

//    c.gridy++;

    return panel;
  }

  private JComponent createNavigatePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // first
    panel = new JPanel(new GridBagLayout());
    c.insets = new Insets(TINY_INSET, TINY_INSET, TINY_INSET, TINY_INSET);
/*
    myFirst = (JButton) createButton(new JButton(),
      "TLT_First", // NOI18N
      new AbstractAction(null, Util.getIcon("first")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          first();
        }
      }
    );
*/
    panel.add(myFirst, c);

    // previous
    panel.add(myPrevious, c);

//    int width = (int)Math.round(myPrevious.getPreferredSize().width/PREVIEW_FACTOR);
//    int height = myPrevious.getPreferredSize().height;
//    myGoto.setPreferredSize(new Dimension(width, height));
//    myGoto.setMinimumSize(new Dimension(width, height));

//    myGoto.setHorizontalAlignment(JTextField.CENTER);
//    myGoto.setToolTipText(getMessage("TLT_Goto")); // NOI18N
//    myGoto.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//        goTo();
//      }
//    });
//    panel.add(myGoto, c);
    
    // next
    panel.add(myNext, c);

    // last
    panel.add(myLast, c);

    return panel;
  }

  private JComponent createScalePanel() {
//out("Create scale panel");
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // fit to window
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, TINY_INSET, TINY_INSET);
    panel.add(myFit, c);

    // decrease
    panel.add(myDecrease, c);

    // increase
    panel.add(myIncrease, c);

    return panel;
  }

  private JComponent createScrollPanel() {
//out("Create scroll panel");
    GridBagConstraints c = new GridBagConstraints();

    // papers
//    myPaperPanel = new JPanel(new GridBagLayout());
//    myPaperPanel.setBackground(Color.lightGray);
    JPanel panel = new JPanel(new GridBagLayout());

    c.gridy = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    panel.setBackground(Color.lightGray);
//    panel.add(myPaperPanel, c);
    //panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.yellow));
    //optionPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));
    //myPaperPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));

    // scroll
    c.fill = GridBagConstraints.BOTH;

    return null;
  }

  private void search() {
//out("text: '" + myText.getText() + "'");
    SearchOption option = new SearchOption.Adapter(
      myText.getText(),
      mySource,
      SearchMatch.PATTERN_MATCH, // todo m
      false, // case sensitive
      false); // use selection

    mySearchEngine.search(option);
  }

  private void updateButtons() {
/*
    myGoto.setText (getPaper(myPaperNumber));
    myFirst.setEnabled (myPaperNumber > 1);
    myPrevious.setEnabled (myPaperNumber > 1);
    myGoto.setEnabled (enabled);
    myToggle.setEnabled (enabled);
    myFit.setEnabled (enabled);
    myIncrease.setEnabled (enabled);
    myDecrease.setEnabled (enabled);
    myPrnBtn.setEnabled (enabled);
    myOptBtn.setEnabled (enabled);
*/
// disable Search button if name is empty
  }

  @Override
  protected void closed()
  {
    mySearchEngine.removeSearchListener(myTree);
    mySearchEngine = null;
    mySource = null;
    myTree = null;
  }

  @Override
  protected void opened()
  {
//    myScrollPanel.requestFocus();
  }

  @Override
  protected DialogDescriptor getDescriptor()
  {
    Object[] buttons = getButtons();
    DialogDescriptor descriptor = new DialogDescriptor(
      createMainPanel(),
      getMessage("LBL_Advanced_Search"), // NOI18N
      true,
      buttons,
      mySearchButton,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      null
    );
//    descriptor.setClosingOptions(
//      new Object[] { myPrnBtn, DialogDescriptor.CANCEL_OPTION });

    return descriptor;
  }

  private Object[] getButtons() {
    mySearchButton = (JButton) createButton(new JButton(),
      "TLT_Search", // NOI18N
      new AbstractAction(getMessage("LBL_Search")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          search();
        }
      }
    );
    return new Object[] {
      mySearchButton,
      DialogDescriptor.CANCEL_OPTION,
    };
  }

  private JButton myFirst;
  private JButton myPrevious;
  private JButton myNext;
  private JButton myLast;

  private JButton myFit;
  private JButton myIncrease;
  private JButton myDecrease;

  
  private Object mySource;
  private JTextField myText;
  private SearchListener myTree;
  private JButton mySearchButton;
  private SearchEngine mySearchEngine;

  private static final int TEXT_WIDTH = 100;
}
