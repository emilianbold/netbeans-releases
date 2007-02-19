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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.02.01
 */
final class PanelProxy extends Panel {
    
  PanelProxy(
    Project project,
    Panel parent,
    WSDLModel modelImplement,
    WSDLModel modelCall)
  {
    super(project, parent);
    int numberImplement = getXslFileNumber(1);
    int numberCall = getXslFileNumber(numberImplement + 1);

    myOperationImplement = new PanelOperation(
      project, this, modelImplement, getXslFileName(numberImplement), true, true);

    myOperationCall = new PanelOperation(
      project, this, modelCall, getXslFileName(numberCall), true, false);
 }

  @Override
  protected String getComponentName()
  {
    return NAME_XSLT;
  }

  @Override
  protected String getError()
  {
    return getError(myOperationImplement.getError(), myOperationCall.getError());
  }

  public void storeSettings(Object object) {
    WizardDescriptor descriptor = (WizardDescriptor) object;
    myOperationImplement.storeSettings(object);
    myOperationCall.storeSettings(object);

    if (myReplyBox.isSelected()) {
      descriptor.putProperty(CHOICE, CHOICE_FILTER_REQUEST_REPLY);
    }
    else {
      descriptor.putProperty(CHOICE, CHOICE_FILTER_ONE_WAY);
    }
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    // we implement
    panel.add(createSeparator(i18n("LBL_We_Implement")), c); // NOI18N
    myOperationImplement.createPanel(panel, c);

    // we call
    panel.add(createSeparator(i18n("LBL_We_Call")), c); // NOI18N
    myOperationCall.createPanel(panel, c);

    // transform request
    panel.add(createTransformRequestPanel(), c);

    // transform reply
    panel.add(createTransformReplyPanel(), c);

    update();
    mainPanel.add(panel, cc);
  }

  @Override
  protected void update()
  {
    if (myRequestBox == null || myReplyBox == null) {
      return;
    }
    myOperationImplement.setEnabled(myRequestBox.isSelected());
    myOperationCall.setEnabled(myReplyBox.isSelected());

    myOperationImplement.setRequirement(
      myRequestBox.isSelected(), myReplyBox.isSelected());

    myOperationCall.setRequirement(
      myRequestBox.isSelected(), myReplyBox.isSelected());

    updateText(
      myRequestText,
      true,
      (Operation) myOperationImplement.getResult(),
      (Operation) myOperationCall.getResult());

    updateText(
      myReplyText,
      false,
      (Operation) myOperationCall.getResult(),
      (Operation) myOperationImplement.getResult());
  }

  private void updateText(
    JTextField text, 
    boolean isInput,
    Operation operation1,
    Operation operation2)
  {
    String type1 = getType(operation1, isInput);
    String type2 = getType(operation2, isInput);
    text.setText(i18n("LBL_From_To", type1, type2)); // NOI18N
  }

  private String getType(Operation operation, boolean isInput) {
    if (operation == null) {
      return EMPTY;
    }
    if (isInput) {
      return getType(operation.getInput());
    }
    else {
      return getType(operation.getOutput());
    }
  }

  private JPanel createTransformRequestPanel() {
    myRequestBox = createCheckBox(
      i18n("LBL_Transform_Request"), // NOI18N
      new AbstractAction(i18n("LBL_Transform_Request")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    myRequestBox.setEnabled(false);
    myRequestBox.setSelected(true);
    myRequestText = new JTextField();

    return createTransformPanel(myRequestBox, myRequestText);
  }

  private JPanel createTransformReplyPanel() {
    myReplyBox = createCheckBox(
      i18n("LBL_Transform_Reply"), // NOI18N
      new AbstractAction(i18n("LBL_Transform_Reply")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    myReplyBox.setEnabled(true);
    myReplyBox.setSelected(false);
    myReplyText = new JTextField();

    return createTransformPanel(myReplyBox, myReplyText);
  }

  private JPanel createTransformPanel(JCheckBox checkBox, JTextField text)  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    JButton button;
    JLabel label;

    // check box
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    panel.add(checkBox, c);

    // text
    c.gridy++;
    c.insets = new Insets(
      SMALL_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    text.setEnabled(false);
    panel.add(text, c);
    
    return panel;
  }

  private PanelOperation myOperationImplement;
  private PanelOperation myOperationCall;
  private JCheckBox myRequestBox; 
  private JCheckBox myReplyBox; 
  private JTextField myRequestText; 
  private JTextField myReplyText; 
}
