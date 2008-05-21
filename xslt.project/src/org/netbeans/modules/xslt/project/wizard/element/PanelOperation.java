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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLModelVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLUtilities;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Vitaly Bychkov
 * @version 2007.08.31
 */
final class PanelOperation<T> extends Panel<T> {
    
  PanelOperation(
    Project project,
    Panel<T> parent,
    WSDLModel model,
    String fileName,
    boolean isReadOnly,
    boolean isInput)
  {
    super(project, parent);
    myModel = model;
    myFileName = fileName;
    myIsReadOnly = isReadOnly;
    myIsInput = isInput;
    myIsInputRequired = true;
    myIsOutputRequired = true;
  }

  @Override
  protected Object getResult()
  {
    return getOperation();
  }
  
  @Override
  protected String getError()
  {
    if (myFileName != null) {
      String fileName = myFile.getText().trim();
      if ( !PanelUtil.isValidFileName(fileName)) {
          return i18n("ERR_WrongFileName", fileName); // NOI18N
      }
      
      String name = addExtension(fileName);
      FileObject file = getFolder().getFileObject(name);
    }

    Operation operation = getOperation();

    if (operation == null) {
      return i18n("ERR_Operation_Is_Required"); // NOI18N
    }
    if (myIsInputRequired) {
      if ( !check(operation.getInput())) {
        return i18n("ERR_Operation_With_Input_Is_Required"); // NOI18N
      }
    }
    if (myIsOutputRequired) {
      if ( !check(operation.getOutput())) {
        return i18n("ERR_Operation_With_Output_Is_Required"); // NOI18N
      }
    }
    return null;
  }

  private boolean check(OperationParameter parameter) {
    return
      parameter != null &&
      parameter.getMessage() != null &&
      parameter.getMessage().get() != null;
  }

  @Override
  public void readSettings(Object object) {
    myWizardDescriptor = (WizardDescriptor) object;
  }

  @Override
  public void storeSettings(Object object) {
    WizardDescriptor descriptor = (WizardDescriptor) object;

    if (myFileName != null) {
      String file = addExtension(myFile.getText().trim());
      
      if (myIsInput) {
        descriptor.putProperty(INPUT_FILE, file);
      }
      else {
        descriptor.putProperty(OUTPUT_FILE, file);
      }
    }
    if (myIsInput) {
      descriptor.putProperty(INPUT_OPERATION, getOperation());
      descriptor.putProperty(INPUT_PORT_TYPE, getPortType());
    }
    else {
      descriptor.putProperty(OUTPUT_OPERATION, getOperation());
      descriptor.putProperty(OUTPUT_PORT_TYPE, getPortType());
    }
  }

  void setRequirement(boolean isInputRequired, boolean isOutputRequired) {
    myIsInputRequired = isInputRequired;
    myIsOutputRequired = isOutputRequired;
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    JButton button;
    JLabel label;

    // file
    createFilePanel(panel, c);

    // operation 
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n(myIsInput ? "LBL_Operation" : "LBL_Operation2")); // NOI18N
    a11y(label, "ACSN_LBL_Operation", "ACSD_LBL_Operation"); // NOI18N
    panel.add(label, c);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myOperation = new JComboBox();
    myOperation.setRenderer(new Renderer());
    myOperation.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          updateTypes();
        }
      }
    );
    label.setLabelFor(myOperation);
    panel.add(myOperation, c);

    // [type]
    if ( !myIsReadOnly) {
      c.weightx = 0.0;
      createTypePanel(panel, c);
    }

    // transform JBI
    if (myFileName != null) {
      c.gridy++;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.insets = new Insets(0, 0, 0, 0);
    }
    updatePortTypes(null);
    mainPanel.add(panel, cc);
  }

  private void createFilePanel(final JPanel panel, GridBagConstraints c) {
    JLabel label;

    // xsl file
    if (myFileName != null) {
      c.gridy++;

      GridBagConstraints c1 = new GridBagConstraints();
      c1.gridy = c.gridy;
      c1.anchor = GridBagConstraints.WEST;
      c1.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
      label = createLabel(i18n(myIsInput ? "LBL_XSL_File" : "LBL_XSL_File2")); // NOI18N
      a11y(label, "ACSN_LBL_XSL_File", "ACSD_LBL_XSL_File"); // NOI18N
      panel.add(label, c1);

      c1 = new GridBagConstraints();
      c1.gridy = c.gridy;
      c1.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
      c1.fill = GridBagConstraints.HORIZONTAL;
      c1.weightx = 1.0;
      myFile = new JTextField(myFileName);
      label.setLabelFor(myFile);
      panel.add(myFile, c1);
      
      myBrowseButton = createBrowseButton(myFile);
      c1 = new GridBagConstraints();
      c1.gridy = c.gridy;
      c1.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
      panel.add(myBrowseButton, c1);
    }                        
    // Partner/Role/Port
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n(myIsInput ? "LBL_Partner_Role_Port" : "LBL_Partner_Role_Port2")); // NOI18N
    a11y(label, "ACSN_LBL_Partner_Role_Port", "ACSD_LBL_Partner_Role_Port"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    myPortType = new JComboBox();
    myPortType.setRenderer(new Renderer());
    myPortType.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    label.setLabelFor(myPortType);
    panel.add(myPortType, c);
  }

  private void createTypePanel(JPanel panel, GridBagConstraints c) {
    JLabel label;

    // input type 
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n("LBL_Input_Type")); // NOI18N
    a11y(label, "ACSN_LBL_Input_Type", "ACSD_LBL_Input_Type"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    myInput = new JTextField();
    myInput.setEditable(false);
    label.setLabelFor(myInput);
    panel.add(myInput, c);

    // output type 
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.weighty = 1.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n("LBL_Output_Type")); // NOI18N
    a11y(label, "ACSN_LBL_Output_Type", "ACSD_LBL_Output_Type"); // NOI18N
    panel.add(label, c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myOutput = new JTextField();
    myOutput.setEditable(false);
    label.setLabelFor(myOutput);
    panel.add(myOutput, c);
  }

  private void updatePortTypes(PortType portType) {
    myPortType.removeAllItems();
    PortType [] ports = getPortTypes();

    for (PortType item : ports) {
      myPortType.addItem(item);
    }
    if (portType != null) {
      myPortType.setSelectedItem(portType);
    }
    update();
  }

  private PortType[] getPortTypes() {
    final List<PortType> list = new ArrayList<PortType>();

    WSDLUtilities.visitRecursively(myModel, new WSDLModelVisitor() {
      public void visit(WSDLModel model) {
        Definitions definitions = model.getDefinitions();
        Collection<PortType> portTypes = definitions.getPortTypes();
        if (portTypes != null) {
            list.addAll(portTypes);
        }
      }
    });

    return list.toArray(new PortType [list.size()]);
  }

//  private void processRole(
//    PartnerLinkType partnerLinkType,
//    Role role,
//    List<PartnerRolePort> list)
//  {
//    if (role == null) {
//      return;
//    }
//    NamedComponentReference<PortType> reference = role.getPortType();
//
//    if (reference == null) {
//      return;
//    }
//    PortType portType = reference.get();
//
//    if (portType != null) {
//      PartnerRolePort partnerRolePort = new PartnerRolePort(partnerLinkType, role, portType);
//
//      if ( !list.contains(partnerRolePort)) {
//        list.add(partnerRolePort);
//      }
//    }
//  }

  @Override
  protected void update()
  {
    myOperation.removeAllItems();
    Operation [] operations = getOperations(getPortType());

    for (Operation operation : operations) {
      myOperation.addItem(operation);
    }
    updateTypes();
  }

  private Operation [] getOperations(PortType portType) {
    List<Operation> list = new ArrayList<Operation>();

    if (portType != null) {
      Collection<Operation> operations =
        portType.getOperations();

      if (operations != null) {
          for (Operation operation : operations) {
            list.add(operation);
          }
      }
    }
    return list.toArray(new Operation [list.size()]);
  }

  @Override
  protected void setEnabled(boolean enabled)
  {
    if (myFileName != null) {
      myFile.setEnabled(enabled);
    }
  }

  private void updateTypes() {
    getParent().update();

    if (myIsReadOnly) {
      return;
    }
    Operation operation = getOperation();

    if (operation == null) {
      myInput.setText(EMPTY);
      myOutput.setText(EMPTY);
      return;
    }
    myInput.setText(getType(operation.getInput()));
    myOutput.setText(getType(operation.getOutput()));
  }

  private Operation getOperation() {
    return (Operation) myOperation.getSelectedItem();
  }

  private PortType getPortType() {
    return (PortType) myPortType.getSelectedItem();
  }

  private JTextField myFile;
  private JButton myBrowseButton;
  private JComboBox myPortType;
  private JComboBox myOperation;
  private JTextField myInput;
  private JTextField myOutput;
  private WSDLModel myModel;
  private String myFileName;
  private boolean myIsReadOnly;
  private boolean myIsInput;
  private boolean myIsInputRequired;
  private boolean myIsOutputRequired;
  private WizardDescriptor myWizardDescriptor;

  private static final String PART_IN_NAME = "PartIn"; // NOI18N
  private static final String PART_OUT_NAME = "PartOut"; // NOI18N
  private static final String MESSAGE_IN_NAME = "MessageIn"; // NOI18N
  private static final String MESSAGE_OUT_NAME = "MessageOut"; // NOI18N
}
