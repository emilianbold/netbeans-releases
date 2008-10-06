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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import static org.netbeans.modules.xml.ui.UI.*;

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
    if ( !PanelUtil.isValidFileName(name)) {
        return i18n("ERR_WrongFileName", name); // NOI18N
    }
    FileObject file = getFolder().getFileObject(name);

    name = addExtension(myRequestFile.getText().trim());
    if (myReplyBox.isSelected() && !PanelUtil.isValidFileName(name)) {
        return i18n("ERR_WrongFileName", name); // NOI18N
    }
    
    file = getFolder().getFileObject(name);

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
    mainPanel.getAccessibleContext().setAccessibleDescription(i18n("ACSD_LBL_NewBridgeService3"));   
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
    a11y(myRequestText, "ACSN_LBL_Transform_Request", "ACSD_LBL_Transform_Request"); // NOI18N
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
    a11y(myReplyBox, "ACSN_LBL_Transform_Reply", "ACSD_LBL_Transform_Reply"); // NOI18N
    myReplyBox.setEnabled(true);
    myReplyBox.setSelected(false);

    myReplyText = new JTextField();
    a11y(myReplyText, "ACSN_LBL_Transform_Reply", "ACSD_LBL_Transform_Reply"); // NOI18N
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
    c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
    panel.add(checkBox, c);

    // text
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(
      LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    text.setEnabled(false);
    panel.add(text, c);
    
    // label
    c.gridy++;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(
      LARGE_SIZE, HUGE_SIZE + LARGE_SIZE + TINY_SIZE + TINY_SIZE, TINY_SIZE, LARGE_SIZE);
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    JLabel label = createLabel(i18n(getNextXslFileLabel()));
    a11y(label, "ACSN_LBL_XSL_File", "ACSD_LBL_XSL_File"); // NOI18N
    label.setLabelFor(file);
    panel.add(label, c); // NOI18N

    // file
    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    panel.add(file, c);

    c.weightx = 0.0;
    c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    panel.add(browseButton, c);
    
    return panel;
  }

  private String getNextXslFileLabel() {
    myXslFileUsagesCounter++;
    return myXslFileUsagesCounter > 1 ? "LBL_XSL_File3" : "LBL_XSL_File" ; // NOI18N
  }
  
  private int myXslFileUsagesCounter = 0;
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
