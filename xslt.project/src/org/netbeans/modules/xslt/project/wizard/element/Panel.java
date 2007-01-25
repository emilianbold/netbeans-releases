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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrType;

import static org.netbeans.modules.print.api.PrintUtil.*;
import static org.netbeans.modules.print.api.PrintUtil.Dialog.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
final class Panel implements WizardDescriptor.ValidatingPanel/*, ChangeListener todo r */{
    
  Panel(Project project) {
    myProject = project;
    myFolder = Util.getSrcFolder(project);
  }
  
  public JPanel getComponent() {
    if (myComponent == null) {
      myComponent = createPanel();
    }
    return myComponent;
  }
  
  /**
   * Is called when Next of Finish buttons are clicked and
   * allows deeper check to find out that panel is in valid
   * state and it is ok to leave it.
   *
   * @throws WizardValidationException when validation fails
   * @since 4.28
// todo r
   */
  public void validate() throws WizardValidationException {
//     String errorMessage = validateBindingName();
//
//     if (errorMessage != null) {
//        throw new WizardValidationException(component.getBindingNameComponent(), errorMessage, errorMessage);
//     }
//     errorMessage = validateOperations();
//     if (errorMessage != null) {
//         throw new WizardValidationException(component.getOperationsComponent(), errorMessage, errorMessage);
//     }
// todo r
    FileObject file = myFolder.getFileObject(myFile.getText());

    if (file != null) {
      String name = file.getNameExt();
      String error = "File " + name + " already exists."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_File_Exists", name); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    FileObject wsdl = getWSDLFile();

    if (wsdl == null) {
      String error = "Service is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Service_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    Operation operation = getOperation();

    if (operation == null) {
      String error = "Operation is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Operation_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    if (operation.getInput() == null) {
      String error = "Operation with input is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Operation_With_Input_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    if (operation.getInput().getMessage() == null) {
      String error = "Operation with input is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Operation_With_Input_Message_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    if (operation.getOutput() == null) {
      String error = "Operation with output is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Operation_With_Output_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
    if (operation.getOutput().getMessage() == null) {
      String error = "Operation with output is required."; // NOI18N
      String message = NbBundle.getMessage(Panel.class,
        "ERR_Operation_With_Output_Message_Is_Required"); // NOI18N
      throw new WizardValidationException(myComponent, error, message);
    }
  }

//todo r
  /** 
   * Test whether the panel is finished and it is safe to proceed to the next one.
   * If the panel is valid, the "Next" (or "Finish") button will be enabled.
   * <p><strong>Tip:</strong> if your panel is actually the component itself
   * (so {@link #getComponent} returns <code>this</code>), be sure to specifically
   * override this method, as the unrelated implementation in {@link java.awt.Component#isValid}
   * if not overridden could cause your wizard to behave erratically.
   * @return <code>true</code> if the user has entered satisfactory information
   * todo r
   */
  public boolean isValid() {
    return true;
//      // If it is always OK to press Next or Finish, then:
//      String errorMessage = getAnyErrors();
//      myWizard.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
//      return errorMessage == null;
//      // If it depends on some condition (form filled out...), then:
//      // return someCondition();
//      // and when this condition changes (last form field filled in...) then:
//      // fireChangeEvent();
//      // and uncomment the complicated stuff below.
  }

//  public void stateChanged(ChangeEvent event) {
//    isValid();
//  }
// todo r

//  private String getAnyErrors() {
//      String errorMessage = null;
////      errorMessage = validateOperations();
////      errorMessage = validateBindingName();
//      return errorMessage;
//  }
// todo r

  public HelpCtx getHelp() {
    return HelpCtx.DEFAULT_HELP;
  }

  public synchronized void addChangeListener(ChangeListener listener) {
//    listeners.add(listener);
// todo r
  }

  public synchronized void removeChangeListener(ChangeListener listener) {
//    listeners.remove(listener);
// todo r
  }

/*
  protected void fireChangeEvent() {
// todo r
      Iterator<ChangeListener> it;

      synchronized (listeners) {
          it = new HashSet<ChangeListener>(listeners).iterator();
      }
      ChangeEvent ev = new ChangeEvent(this);
      while (it.hasNext()) {
          it.next().stateChanged(ev);
      }
  }
*/   
  public void readSettings(Object settings) {
// todo r
//    myWizard = (WizardDescriptor) settings;
  // WizardDescriptor.getProperty & putProperty to store information entered
  }

// todo r
//  public void storeSettings(Object settings) {
//    if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
//      return;
//    }
//    if (isValid()) {
//        ;
//    }
//    myComponent.storeValues((WizardDescriptor) settings);
//  }

  public void storeSettings(Object settings) {
    WizardDescriptor descriptor = (WizardDescriptor) settings;
    descriptor.putProperty(FILE, myFile.getText());
//    descriptor.putProperty(WSDL, getWSDLFile());
    descriptor.putProperty(OPERATION, getOperation());
  }

  private JPanel createPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    createServicePanel(panel, c);
    createXmlPanel(panel, c);

    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(panel, c);
  
//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.yellow));
//  mainPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
    return mainPanel;
  }

  private void createServicePanel(JPanel panel, GridBagConstraints c) {
    JButton button;
    JLabel label;

    // file name
    c.gridy++;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(getClass(), "LBL_File"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myFile = new JTextField(getFileName());
    label.setLabelFor(myFile);
    panel.add(myFile, c);

    // service
    c.gridy++;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(getClass(), "LBL_Service"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myService = new JComboBox();
    updateWSDLFiles(null);
    myService.setRenderer(new Renderer());
    myService.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          updateOperations(null);
        }
      }
    );
    label.setLabelFor(myService);
    panel.add(myService, c);

    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    button = createButton(
      "TLT_Create_WSDL", // NOI18N
      getClass(),
      new AbstractAction(getMessage(getClass(), "LBL_Create_WSDL")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          createWSDL();
        }
      }
    );
    panel.add(button, c);

    // operation 
    c.gridy++;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(getClass(), "LBL_Operation"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myOperation = new JComboBox();
    updateOperations(null);
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

    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    button = createButton(
      "TLT_Create_Operation", // NOI18N
      getClass(),
      new AbstractAction(getMessage(getClass(), "LBL_Create_Operation")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          createOperation();
        }
      }
    );
    panel.add(button, c);
  }

  private void createOperation() {
    WSDLModel model = Util.getWSDLModel(getWSDLFile());

    if (model == null) {
      printError(getClass(), "ERR_Set_Service"); // NOI18N
    }
    else {
      new Operattio(model, myProject, this).show();
    }
  }

  private Operation [] getOperations(WSDLModel model) {
    List<Operation> operations = new ArrayList<Operation>();

    if (model == null) {
      return operations.toArray(new Operation[operations.size()]);
    }
    Collection<PortType> portTypes = model.getDefinitions().getPortTypes();

    for (PortType portType : portTypes) {
      Collection<Operation> ops = portType.getOperations();

      for (Operation operation : ops) {
        operations.add(operation);
      }
    }
      return operations.toArray(new Operation[operations.size()]);
  }

  void updateWSDLFiles(FileObject item) {
    myService.removeAllItems();
    FileObject [] files = Util.getWSDLFiles(myProject);

    for (FileObject file : files) {
      myService.addItem(file);
    }
    if (item != null) {
      myService.setSelectedItem(item);
    }
  }

  protected void updateOperations(Operation current) {
    myOperation.removeAllItems();
    Operation [] operations = getOperations(Util.getWSDLModel(getWSDLFile()));

    for (Operation operation : operations) {
      myOperation.addItem(operation);
    }
    if (current != null) {
      myOperation.setSelectedItem(current);
    }
  }

  private void createXmlPanel(JPanel panel, GridBagConstraints c) {
    JButton button;
    JLabel label;

    // input type 
    c.gridy++;
    c.weightx = 0.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(getClass(), "LBL_Input_Type"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myInput = new JTextField();
    myInput.setEnabled(false);
    label.setLabelFor(myInput);
    panel.add(myInput, c);

//todo r
//    c.weightx = 0.0;
//    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
//    button = createButton(
//      "TLT_Browse_Input_Type", // NOI18N
//      getClass(),
//      new AbstractAction(getMessage(getClass(), "LBL_Browse_Input_Type")) { // NOI18N
//        public void actionPerformed(ActionEvent event) {
////          browseType(true);
//        }
//      }
//    );
//    // todo a? setEnabled if wsdl is selected
////    panel.add(button, c);

    // output type 
    c.gridy++;
    c.weightx = 0.0;
    c.weighty = 1.0;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    label = createLabel(getClass(), "LBL_Output_Type"); // NOI18N
    panel.add(label, c);

    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myOutput = new JTextField();
    myOutput.setEnabled(false);
    label.setLabelFor(myOutput);
    panel.add(myOutput, c);

    updateTypes();

    //todo r
//    c.weightx = 0.0;
//    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
//    button = createButton(
//      "TLT_Browse_Output_Type", // NOI18N
//      getClass(),
//      new AbstractAction(getMessage(getClass(), "LBL_Browse_Output_Type")) { // NOI18N
//        public void actionPerformed(ActionEvent event) {
////          browseType(false);
//        }
//      }
//    );
////    panel.add(button, c);
  }

  private void updateTypes() {
    Operation operation = getOperation();

    if (operation == null) {
      myInput.setText(EMPTY);
      myOutput.setText(EMPTY);
      return;
    }
    myInput.setText(getType(operation.getInput()));
    myOutput.setText(getType(operation.getOutput()));
  }

  private String getType(OperationParameter parameter) {
    if (parameter == null) {
//out("1");
      return EMPTY;
    }
    NamedComponentReference<Message> reference = parameter.getMessage();

    if (reference == null) {
//out("2");
      return EMPTY;
    }
    Message message = reference.get();

    if (message == null) {
//out("3");
      return EMPTY;
    }
    Collection<Part> parts = message.getParts();

    if (parts == null) {
//out("4");
      return EMPTY;
    }
    java.util.Iterator<Part> iterator = parts.iterator();

    if ( !iterator.hasNext()) {
//out("5");
      return EMPTY;
    }
    Part part = iterator.next();
    NamedComponentReference<GlobalType> refType = part.getType();

    if (refType != null) {
      GlobalType type = refType.get();

      if (type != null) {
        return type.getName();
      }
    }
    NamedComponentReference<GlobalElement> refElement = part.getElement();

    if (refElement != null) {
      GlobalElement element = refElement.get();

      if (element != null) {
        return element.getName();
      }
    }
    return EMPTY;
  }

  private FileObject getWSDLFile() {
    return (FileObject) myService.getSelectedItem();
  }

  private Operation getOperation() {
    return (Operation) myOperation.getSelectedItem();
  }

  private String getFileName() {
    FileObject file = myFolder.getFileObject(NAME, EXT);
    int count = 1;

    while (file != null) {
      file = myFolder.getFileObject(NAME + (count++), EXT);
    }
    count--;

    if (count == 0) {
      return NAME + DOT + EXT;
    }
    return NAME + count + DOT + EXT;
  }

  private void createWSDL() {
    new WSDL(myFolder, this).show();
  }

  // -----------------------------------------------------
  private class Renderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(
      JList list, Object value, int index,
      boolean isSelected, boolean hasFocus)
   {
      super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);

      if (value instanceof FileObject) {
        setText(Util.calculateRelativeName((FileObject) value, myProject));
      }
      if (value instanceof Operation) {
        Operation operation = (Operation) value;
        PortType portType = (PortType) operation.getParent();
        setText(portType.getName() + "/" + operation.getName()); // NOI18N
      }
      return this;
    }
  }

  private JTextField myFile;
  private JComboBox myService;
  private JComboBox myOperation;
  private JTextField myInput;
  private JTextField myOutput;

  private Project myProject;
  private JPanel myComponent;
  private FileObject myFolder;

//  private WizardDescriptor myWizard;
//  private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
// todo r
// private PortType mPortType = null;
// private WSDLExtensibilityElements mElements;

  public static final String FILE = "file"; // NOI18N
//  public static final String WSDL = "wsdl"; // NOI18N
  public static final String OPERATION = "operation"; // NOI18N
//  public static final String INPUT = "input"; // NOI18N
//  public static final String OUTPUT = "output"; // NOI18N

  private static final String NAME = "xsl"; // NOI18N
  private static final String EXT = "xsl"; // NOI18N
  private static final String DOT = "."; // NOI18N

  private static final String EMPTY = ""; // NOI18N
}
