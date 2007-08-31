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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileUtil;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.02.01
 */
final class PanelProxy<T> extends Panel<T> {
    
  PanelProxy(
    Project project,
    Panel<T> parent,
    WSDLModel modelImplement,
    WSDLModel modelCall)
  {
    super(project, parent);
    myOperationImplement = new PanelOperation<T>(
      project, this, modelImplement, null, true, true);

    myOperationCall = new PanelOperation<T>(
      project, this, modelCall, null, true, false);
 }

  @Override
  protected String getComponentName()
  {
    return NAME_XSLT;
  }

  @Override
  protected String getError()
  {
    String name = addExtension(myReplyFile.getText().trim());
    if (!Util.isValidFileName(name)) {
        return i18n("ERR_WrongFileName", name); // NOI18N
    }
    FileObject file = getFolder().getFileObject(name);

    if (file != null) {
      return i18n("ERR_File_Already_Exists", name); // NOI18N
    }

    name = addExtension(myRequestFile.getText().trim());
    if (myReplyBox.isSelected() && !Util.isValidFileName(name)) {
        return i18n("ERR_WrongFileName", name); // NOI18N
    }
    
    file = getFolder().getFileObject(name);

    if (file != null) {
      return i18n("ERR_File_Already_Exists", name); // NOI18N
    }
    return getError(myOperationImplement.getError(), myOperationCall.getError());
  }

  public void storeSettings(Object object) {
    WizardDescriptor descriptor = (WizardDescriptor) object;
    myOperationImplement.storeSettings(object);
    myOperationCall.storeSettings(object);

    descriptor.putProperty(INPUT_FILE,
      addExtension(myRequestFile.getText().trim()));

    descriptor.putProperty(OUTPUT_FILE,
      addExtension(myReplyFile.getText().trim()));
    
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

    int number1 = getXslFileNumber(1);
    int number2 = getXslFileNumber(number1 + 1);

    // transform request
    panel.add(createTransformRequestPanel(getXslFileName(number1)), c);

    // transform reply
    panel.add(createTransformReplyPanel(getXslFileName(number2)), c);

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

    myRequestFile.setEnabled(myRequestBox.isSelected());
    myRequestFileBrowseButton.setEnabled(myRequestBox.isSelected());

    myReplyFile.setEnabled(myReplyBox.isSelected());
    myReplyFileBrowseButton.setEnabled(myReplyBox.isSelected());

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
    updateText(text, getType(operation1, isInput), getType(operation2, isInput));
  }

  private void updateText(
    JTextField text, 
    String text1,
    String text2)
  {
    text.setText(i18n("LBL_From_To", text1, text2)); // NOI18N
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

  private JPanel createTransformRequestPanel(String fileName) {
    myRequestBox = createCheckBox(
      new ButtonAction(i18n("LBL_Transform_Request")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    myRequestBox.setEnabled(false);
    myRequestBox.setSelected(true);

    myRequestText = new JTextField();
    myRequestFile = new JTextField(fileName);
    myRequestFileBrowseButton = createBrowseButton(myRequestFile);

    return createTransformPanel(
      myRequestBox, myRequestText, myRequestFile, myRequestFileBrowseButton);
  }

  private JPanel createTransformReplyPanel(String fileName) {
    myReplyBox = createCheckBox(
      new ButtonAction(i18n("LBL_Transform_Reply")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    myReplyBox.setEnabled(true);
    myReplyBox.setSelected(false);

    myReplyText = new JTextField();
    myReplyFile = new JTextField(fileName);
    myReplyFileBrowseButton = createBrowseButton(myReplyFile, "LBL_Browse2");

    return createTransformPanel(
      myReplyBox, myReplyText, myReplyFile, myReplyFileBrowseButton);
  }

  private JPanel createTransformPanel(
    JCheckBox checkBox,
    JTextField text,
    JTextField file,
    JButton browseButton)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    
    // check
    c.gridy++;
    c.gridwidth = 2;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    panel.add(checkBox, c);

    // text
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(
      SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    text.setEnabled(false);
    panel.add(text, c);
    
    // label
    c.gridy++;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(
      SMALL_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET + TINY_INSET, TINY_INSET, SMALL_INSET);
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    panel.add(createLabel(i18n("LBL_XSL_File")), c); // NOI18N

    // file
    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(
      SMALL_INSET, SMALL_INSET, TINY_INSET, 0);
    panel.add(file, c);

      c.weightx = 0.0;
      c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
      panel.add(browseButton, c);
    
    
    return panel;
  }

  private PanelOperation<T> myOperationImplement;
  private PanelOperation<T> myOperationCall;
  private JCheckBox myRequestBox; 
  private JCheckBox myReplyBox; 
  private JTextField myRequestText; 
  private JTextField myReplyText;
  private JButton myReplyFileBrowseButton;
  private JTextField myRequestFile;
  private JButton myRequestFileBrowseButton;
  private JTextField myReplyFile;
}
