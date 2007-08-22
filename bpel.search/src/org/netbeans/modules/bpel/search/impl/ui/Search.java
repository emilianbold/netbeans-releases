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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.netbeans.modules.print.ui.PrintUI;

import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.search.spi.SearchEngine;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.23
 */
public final class Search extends PrintUI {

  /**{@inheritDoc}*/
  public Component getUIComponent(
    List<SearchEngine> engines,
    Object source,
    SearchTarget [] targets)
  {
    mySource = source;
    myTargets = targets;
    mySearchEngine = engines.get(0);
    mySearchEngine.addSearchListener(new Tree());
    mySearchEngine.addSearchListener(new Progress());
    show();
    return getUIComponent();
  }

  @Override
  protected void updated()
  {
    setItems(myTarget, myTargets);
  }

  private JPanel createPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    // text
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    panel.add(createTextPanel(), c);

    // option
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createSeparator(i18n("LBL_Options")), c); // NOI18N
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
    JLabel label = createLabel(i18n("LBL_Name")); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myName = new TextField(ASTERISK);
    setWidth(myName.getUIComponent(), TEXT_WIDTH);
    label.setLabelFor(myName.getUIComponent());
    panel.add(myName.getUIComponent(), c);

    // type
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n("LBL_Type")); // NOI18N
    panel.add(label, c);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.weightx = 1.0;
    myTarget = new JComboBox(myTargets);
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
      new ButtonAction(i18n("LBL_Match_Case")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    panel.add(myMatchCase, c);

    c.gridy++;
    myPatternMatch = createCheckBox(
      new ButtonAction(i18n("LBL_Match_Pattern")) { // NOI18N
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
      new ButtonAction(i18n("LBL_Regular_Expression")) { // NOI18N
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
      return SearchMatch.PATTERN;
    }
    if (myRegularExpression.isSelected()) {
      return SearchMatch.REGULAR_EXPRESSION;
    }
    return null;
  }

  private void search() {
    myDescriptor.setClosingOptions(
      new Object [] { 
        mySearchButton,
        DialogDescriptor.CANCEL_OPTION
      }
    );
    SearchOption option = new SearchOption.Adapter(
      myName.getText().trim(),
      mySource,
      (SearchTarget) myTarget.getSelectedItem(),
      getMatch(),
      myMatchCase.isSelected(),
      false); // use selection

    try {
      mySearchEngine.search(option);
    }
    catch (SearchException e) {
      myDescriptor.setClosingOptions(
        new Object [] { 
          DialogDescriptor.CANCEL_OPTION
        }
      );
      printError(i18n("ERR_Pattern_Error", e.getMessage())); // NOI18N
    }
  }

  @Override
  protected void closed()
  {
    myName.save();
    mySearchEngine = null;
    mySource = null;
  }

  @Override
  protected DialogDescriptor createDescriptor()
  {
    Object [] buttons = getButtons();
    myDescriptor = new DialogDescriptor(
      getResizable(createPanel()),
      i18n("LBL_Advanced_Search"), // NOI18N
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
    return myDescriptor;
  }

  private Object [] getButtons() {
    mySearchButton = createButton(
      new ButtonAction(i18n("LBL_Search"), i18n("TLT_Search")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    return new Object [] {
      mySearchButton,
      DialogDescriptor.CANCEL_OPTION,
    };
  }

  private Object mySource;
  private TextField myName;
  private JButton mySearchButton;
  private JComboBox myTarget;
  private JCheckBox myMatchCase;
  private JCheckBox myPatternMatch;
  private JCheckBox myRegularExpression;
  private SearchTarget [] myTargets;
  private SearchEngine mySearchEngine;
  private DialogDescriptor myDescriptor;

  private static final int TEXT_WIDTH = 200;
  private static final String ASTERISK = "*"; // NOI18N
}
