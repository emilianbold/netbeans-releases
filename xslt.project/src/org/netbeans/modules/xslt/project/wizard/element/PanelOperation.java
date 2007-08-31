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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrType;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Vitaly Bychkov
 * @version 2007.08.31
 */
final class PanelOperation<T> extends Panel<T> {
    
    WizardDescriptor myWizardDescriptor;
    
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
      if (!Util.isValidFileName(fileName)) {
          return i18n("ERR_WrongFileName", fileName); // NOI18N
      }
      
      String name = addExtension(fileName);
      FileObject file = getFolder().getFileObject(name);

      if (file != null) {
        return i18n("ERR_File_Already_Exists", name); // NOI18N
      }
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
      descriptor.putProperty(INPUT_PARTNER_ROLE_PORT, getPartnerRolePort());
    }
    else {
      descriptor.putProperty(OUTPUT_OPERATION, getOperation());
      descriptor.putProperty(OUTPUT_PARTNER_ROLE_PORT, getPartnerRolePort());
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
    label = createLabel(i18n("LBL_Operation")); // NOI18N
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

    // [create]
    if ( !myIsReadOnly) {
      c.weightx = 0.0;
      button = createButton(
        new ButtonAction(
          i18n("LBL_Create_Operation"), // NOI18N
          i18n("TLT_Create_Operation")) { // NOI18N
          public void actionPerformed(ActionEvent event) {
            new DialogOperation<T>(
              myModel, PanelOperation.this, getPartnerRolePort()).show();
          }
        }
      );
//    panel.add(button, c);

      // type
      createTypePanel(panel, c);
    }

    // transform JBI
    if (myFileName != null) {
      c.gridy++;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.insets = new Insets(0, 0, 0, 0);
    }
    updatePartnerRolePorts(null);
    mainPanel.add(panel, cc);
  }

  private void createFilePanel(final JPanel panel, GridBagConstraints c) {
    JLabel label;

    // xsl file
    if (myFileName != null) {
      c.gridy++;

      GridBagConstraints c1 = new GridBagConstraints();
      c1.gridy = c.gridy;
//      c1.gridx = 0;
      c1.anchor = GridBagConstraints.WEST;
      c1.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
      label = createLabel(i18n("LBL_XSL_File")); // NOI18N
      panel.add(label, c1);

      c1 = new GridBagConstraints();
      c1.gridy = c.gridy;
      c1.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
      c1.fill = GridBagConstraints.HORIZONTAL;
//      c1.gridx = 1;
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
    label = createLabel(i18n("LBL_Partner_Role_Port")); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    myPartnerRolePort = new JComboBox();
    myPartnerRolePort.setRenderer(new Renderer());
    myPartnerRolePort.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          update();
        }
      }
    );
    label.setLabelFor(myPartnerRolePort);
    panel.add(myPartnerRolePort, c);
  }

  private void createTypePanel(JPanel panel, GridBagConstraints c) {
    JLabel label;

    // input type 
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n("LBL_Input_Type")); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    myInput = new JTextField();
    myInput.setEditable(false);
    label.setLabelFor(myInput);
    panel.add(myInput, c);

    // [choose]
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myInputChoose = createButton(
      new ButtonAction(
        i18n("LBL_Choose_Input_Type"), // NOI18N
        i18n("TLT_Choose_Input_Type")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          new DialogType<T>(getProject(), myModel, PanelOperation.this, true).show();
        }
      }
    );
//  panel.add(myInputChoose, c);

    // output type 
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0.0;
    c.weighty = 1.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(i18n("LBL_Output_Type")); // NOI18N
    panel.add(label, c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myOutput = new JTextField();
    myOutput.setEditable(false);
    label.setLabelFor(myOutput);
    panel.add(myOutput, c);

    // [choose]
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myOutputChoose = createButton(
      new ButtonAction(
        i18n("LBL_Choose_Output_Type"), // NOI18N
        i18n("TLT_Choose_Output_Type")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          new DialogType<T>(
            getProject(), myModel, PanelOperation.this, false).show();
        }
      }
    );
//  panel.add(myOutputChoose, c);
  }

  private void updatePartnerRolePorts(PartnerRolePort partnerRolePort) {
    myPartnerRolePort.removeAllItems();
    PartnerRolePort [] partnerRolePorts = getPartnerRolePorts();

    for (PartnerRolePort item : partnerRolePorts) {
      myPartnerRolePort.addItem(item);
    }
    if (partnerRolePort != null) {
      myPartnerRolePort.setSelectedItem(partnerRolePort);
    }
    update();
  }

  private PartnerRolePort [] getPartnerRolePorts() {
    List<PartnerRolePort> list = new ArrayList<PartnerRolePort>();
    Definitions definitions = myModel.getDefinitions();
    List<ExtensibilityElement> elements = definitions.getExtensibilityElements();

    for (ExtensibilityElement element : elements) {
      if (element instanceof PartnerLinkType) {
        PartnerLinkType partnerLinkType = (PartnerLinkType) element;
        processRole(partnerLinkType, partnerLinkType.getRole1(), list);
        processRole(partnerLinkType, partnerLinkType.getRole2(), list);
      }
    }
    return list.toArray(new PartnerRolePort [list.size()]);
  }

  private void processRole(
    PartnerLinkType partnerLinkType,
    Role role,
    List<PartnerRolePort> list)
  {
    if (role == null) {
      return;
    }
    NamedComponentReference<PortType> reference = role.getPortType();

    if (reference == null) {
      return;
    }
    PortType portType = reference.get();

    if (portType != null) {
      list.add(new PartnerRolePort(partnerLinkType, role, portType));
    }
  }

  @Override
  protected void update()
  {
    myOperation.removeAllItems();
    Operation [] operations = getOperations(getPartnerRolePort());

    for (Operation operation : operations) {
      myOperation.addItem(operation);
    }
    updateTypes();
  }

  void setOperation(Operation operation, PartnerRolePort partnerRolePort) {
    myOperation.removeAllItems();
    Operation [] operations = getOperations(getPartnerRolePort());

    for (Operation item : operations) {
      myOperation.addItem(item);
    }
    updatePartnerRolePorts(partnerRolePort);
    myOperation.setSelectedItem(operation);
    updateTypes();
  }
  
  private Operation [] getOperations(PartnerRolePort partnerRolePort) {
    List<Operation> list = new ArrayList<Operation>();

    if (partnerRolePort != null) {
      Collection<Operation> operations =
        partnerRolePort.getPortType().getOperations();

      for (Operation operation : operations) {
        list.add(operation);
      }
    }
    return list.toArray(new Operation [list.size()]);
  }

  void setElementOrType(ElementOrType elementOrType, boolean isInput) {
    try {
      myModel.startTransaction();
      updateOperation(getOperation(), elementOrType, isInput);
    } 
    finally {
      if (myModel.isIntransaction()) {
        myModel.endTransaction();
      }  
    }
    Util.saveModel(myModel);
    updateTypes();
  }

  private void updateOperation(
    Operation operation,
    ElementOrType elementOrType,
    boolean isInput)
  {
    Definitions definitions = myModel.getDefinitions();
    WSDLComponentFactory factory = myModel.getFactory();

    if (isInput) {
      updateInput(
        operation,
        definitions,
        factory,
        elementOrType
      );
    }
    else {
      updateOutput(
        operation,
        definitions,
        factory,
        elementOrType
      );
    }
  }

  private void updateInput(
    Operation operation,
    Definitions definitions,
    WSDLComponentFactory factory,
    ElementOrType elementOrType)
  {
    Part part = factory.createPart();
    part.setName(PART_IN_NAME + operation.getName());
    setElementOrType(part, elementOrType);
    
    String messageName = MESSAGE_IN_NAME + operation.getName();
    Message message = getMessage(definitions, messageName);

    if (message == null) {
      message = factory.createMessage();
      message.setName(messageName);
      message.addPart(part);
      definitions.addMessage(message);
    }
    Input input = factory.createInput();
    input.setMessage(input.createReferenceTo(message, Message.class));
    operation.setInput(input);
  }

  private void updateOutput(
    Operation operation,
    Definitions definitions,
    WSDLComponentFactory factory,
    ElementOrType elementOrType)
  {
    Part part = factory.createPart();
    part.setName(PART_OUT_NAME + operation.getName());
    setElementOrType(part, elementOrType);

    String messageName = MESSAGE_OUT_NAME + operation.getName();
    Message message = getMessage(definitions, messageName);

    if (message == null) {
      message = factory.createMessage();
      message.setName(MESSAGE_OUT_NAME + operation.getName());
      message.addPart(part);
      definitions.addMessage(message);
    }
    Output output = factory.createOutput();
    output.setMessage(output.createReferenceTo(message, Message.class));
    operation.setOutput(output);
  }

  private void setElementOrType(Part part, ElementOrType elementOrType) {
    GlobalElement element = elementOrType.getElement();

    if (element != null) {
      updateImports(element);
      part.setElement(part.createSchemaReference(element, GlobalElement.class));
    }
    GlobalType type = elementOrType.getType();

    if (type != null) {
      updateImports(type);
      part.setType(part.createSchemaReference(type, GlobalType.class));
    }
  }

  private void updateImports(SchemaComponent component) {
    Schema oSchema = component.getModel().getSchema();
    String location = getLocation(oSchema);

    if (location == null) { // built-in type
      return;
    }
    Definitions definitions = myModel.getDefinitions();
    Types types = getTypes(definitions);
    Schema wSchema = null;
    String tns = definitions.getTargetNamespace();

    if (tns != null) {
      Collection<Schema> schemas = types.getSchemas();

      if (schemas != null) {
        for (Schema s : schemas) {
          if (s.getTargetNamespace() != null && s.getTargetNamespace().equals(tns)) {
            wSchema = s;
            break;
          }
        }
      }
    }
    WSDLSchema wsdlSchema = null;

    if (wSchema == null) {
      wsdlSchema = myModel.getFactory().createWSDLSchema();
      SchemaModel schemaModel = wsdlSchema.getSchemaModel();
      wSchema = schemaModel.getSchema();
      wSchema.setTargetNamespace(myModel.getDefinitions().getTargetNamespace());
    }
    Import schemaImport = oSchema.getModel().getFactory().createImport();
    schemaImport.setSchemaLocation(location);

    String namespace = oSchema.getTargetNamespace();
    schemaImport.setNamespace(namespace);

    setPrefix(namespace);
    wSchema.addExternalReference(schemaImport);
    
    if (definitions.getTypes() == null) {
      definitions.setTypes(types);
    }
    if (wsdlSchema != null) {
      types.addExtensibilityElement(wsdlSchema);
    }
  }

  private Types getTypes(Definitions definitions) {
    Types types = definitions.getTypes();

    if (types == null) {
      types = myModel.getFactory().createTypes();
    }
    return types;
  }

  private void setPrefix(String namespace) {
    String prefix = Util.generatePrefix(myModel);

    if (prefix.length() > 0) {
      AbstractDocumentComponent def =
        (AbstractDocumentComponent) myModel.getDefinitions();

      Map prefixes = def.getPrefixes();

      if ( !prefixes.containsKey(prefix)) {
        def.addPrefix(prefix, namespace);
      }
    }
  }

  private String getLocation(Schema schema) {
    SchemaModel model = schema.getModel();
    return Util.calculateRelativeName(Util.getFileObject(model), getProject());
  }

  private Message getMessage(Definitions definitions, String name) {
    Collection<Message> messages = definitions.getMessages();

    for (Message message : messages) {
      if (message.getName().equals(name)) {
        return message;
      }
    }
    return null;
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
    myInputChoose.setEnabled(operation != null);
    myOutputChoose.setEnabled(operation != null);

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

  private PartnerRolePort getPartnerRolePort() {
    return (PartnerRolePort) myPartnerRolePort.getSelectedItem();
  }

  private JTextField myFile;
  private JButton myBrowseButton;
  private JComboBox myPartnerRolePort;
  private JComboBox myOperation;
  private JTextField myInput;
  private JTextField myOutput;
  private JButton myInputChoose;
  private JButton myOutputChoose;
  private WSDLModel myModel;
  private String myFileName;
  private boolean myIsReadOnly;
  private boolean myIsInput;
  private boolean myIsInputRequired;
  private boolean myIsOutputRequired;

  private static final String PART_IN_NAME = "PartIn"; // NOI18N
  private static final String PART_OUT_NAME = "PartOut"; // NOI18N
  private static final String MESSAGE_IN_NAME = "MessageIn"; // NOI18N
  private static final String MESSAGE_OUT_NAME = "MessageOut"; // NOI18N
}
