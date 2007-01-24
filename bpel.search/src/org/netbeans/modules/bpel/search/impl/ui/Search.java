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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.netbeans.modules.print.api.PrintUtil.Dialog;

import org.netbeans.modules.bpel.search.api.SearchException;
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
  public Component getUIComponent(List<SearchEngine> engines, Object source) {
    mySource = source;
    myTree = new Tree();
    mySearchEngine = engines.get(0);
    mySearchEngine.addSearchListener(myTree);
    // todo a? progress
    show();
    return getUIComponent();
  }

  @Override
  protected JPanel createMainPanel()
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    // text
    panel.add(createTextPanel(), c);

    // option
    panel.add(getSeparator("LBL_Options"), c); // NOI18N
    panel.add(createOptionPanel(), c);

    return panel;
  }

  private JComponent createTextPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    // text
    c.gridy++;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    JLabel label = createLabel("LBL_Name"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myName = new TextField(ASTERISK);
    myName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        updateButton();
      }
    });
    setWidth(myName.getUIComponent(), TEXT_WIDTH);
    label.setLabelFor(myName.getUIComponent());
    panel.add(myName.getUIComponent(), c);

    // type
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel("LBL_Type"); // NOI18N
    panel.add(label, c);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.weightx = 1.0;
    myTarget = new JComboBox(mySearchEngine.getTargets());
    label.setLabelFor(myTarget);
    panel.add(myTarget, c);

    return panel;
  }

  private JComponent createOptionPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(0, SMALL_INSET, 0, 0);
    c.weightx = 1.0;

    c.gridy++;
    myMatchCase = createCheckBox(
      "LBL_Match_Case", // NOI18N 
      new AbstractAction(getMessage("LBL_Match_Case")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myMatchCase, c);

    c.gridy++;
    myPatternMatch = createCheckBox(
      "LBL_Pattern_Match", // NOI18N 
      new AbstractAction(getMessage("LBL_Pattern_Match")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          exclusion(myPatternMatch, myRegularExpression);
        }
      }
    );
    myPatternMatch.setSelected(true);
    myPatternMatch.setEnabled(true);
    panel.add(myPatternMatch, c);

    c.gridy++;
    myRegularExpression = createCheckBox(
      "LBL_Regular_Expression", // NOI18N 
      new AbstractAction(getMessage("LBL_Regular_Expression")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          exclusion(myRegularExpression, myPatternMatch);
        }
      }
    );
    myRegularExpression.setSelected(false);
    myRegularExpression.setEnabled(false);
    panel.add(myRegularExpression, c);

    return panel;
  }

  private void exclusion(JCheckBox checkBox1, JCheckBox checkBox2) {
    checkBox1.setEnabled( !checkBox2.isSelected());

    if (checkBox2.isSelected()) {
      checkBox1.setSelected(false);
    }
    checkBox2.setEnabled( !checkBox1.isSelected());

    if (checkBox1.isSelected()) {
      checkBox2.setSelected(false);
    }
  }

  private SearchMatch getMatch() {
    if (myPatternMatch.isSelected()) {
      return SearchMatch.PATTERN_MATCH;
    }
    if (myRegularExpression.isSelected()) {
      return SearchMatch.REGULAR_EXPRESSION;
    }
    return null;
  }

  private void updateButton() {
//    String text = myName.getText();
//    boolean enabled = text != null && text.length() > 0;
//    mySearchButton.setEnabled(enabled);
// todo r
  }

  private void search() {
    myDescriptor.setClosingOptions(
      new Object[] { 
        mySearchButton,
        DialogDescriptor.CANCEL_OPTION
      }
    );
    SearchOption option = new SearchOption.Adapter(
      myName.getText(),
      mySource,
      myTarget.getSelectedItem(),
      getMatch(),
      myMatchCase.isSelected(),
      false); // use selection

    try {
      mySearchEngine.search(option);
    }
    catch (SearchException e) {
      myDescriptor.setClosingOptions(
        new Object[] { 
          DialogDescriptor.CANCEL_OPTION
        }
      );
      printError("ERR_Pattern_Error", e.getMessage()); // NOI18N
    }
  }

  @Override
  protected void closed()
  {
    myName.save();
    mySearchEngine.removeSearchListener(myTree);
    mySearchEngine = null;
    mySource = null;
    myTree = null;
  }

  @Override
  protected DialogDescriptor createDescriptor()
  {
    Object[] buttons = getButtons();
    myDescriptor = new DialogDescriptor(
      getPanel(),
      getMessage("LBL_Advanced_Search"), // NOI18N
      true, // modal
      buttons,
      mySearchButton,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (mySearchButton == event.getSource()) {
            search();
          }
        }
      }
    );
    updateButton();

    return myDescriptor;
  }

  private Object[] getButtons() {
    mySearchButton = createButton(
      "TLT_Search", // NOI18N
      new AbstractAction(getMessage("LBL_Search")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    return new Object[] {
      mySearchButton,
      DialogDescriptor.CANCEL_OPTION,
    };
  }

  private Object mySource;
  private TextField myName;
  private JComboBox myTarget;
  private SearchListener myTree;
  private JButton mySearchButton;
  private JCheckBox myMatchCase;
  private JCheckBox myPatternMatch;
  private JCheckBox myRegularExpression;
  private SearchEngine mySearchEngine;
  private DialogDescriptor myDescriptor;

  private static final int TEXT_WIDTH = 200;
  private static final String ASTERISK = "*"; // NOI18N
}
