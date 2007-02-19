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
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.io.IOException;

import javax.swing.ButtonGroup;
//import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JTextField;

//import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;

//import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.30
 */
final class PanelWSDL extends Panel {
    
  PanelWSDL(Project project, Panel parent) {
    super(project, parent);
    myWebService = new PanelWebService(project, parent);
  }

  @Override
  protected String getComponentName()
  {
    return NAME_WSDL;
  }

  @Override
  protected Panel getNext()
  {
    return new PanelTransformation(getProject(), this, myModel);
  }

  @Override
  protected String getError()
  {
//    if (myExisting.isSelected()) {
      String error = myWebService.getError();

      if (error != null) {
        return error;
      }
      myModel = (WSDLModel) myWebService.getResult();
//    }
//    else {
//      String name = getName(myName.getText());
//
//      if (name == null) {
//        return i18n("ERR_File_Name_Must_Be_Specified"); // NOI18N
//      }
//      FileObject file = getFolder().getFileObject(name);
//
//      if (file != null) {
//        return i18n("ERR_File_Already_Exists", name); // NOI18N
//      }
//      try {
//        myModel = Util.getWSDLModel(createFile(name));
//        myWebService.update();
//      }
//      catch (IOException e) {
//        return i18n("ERR_Occurred", e.getMessage()); // NOI18N
//      }
//    }
    return null;
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    ButtonGroup group = new ButtonGroup();
    c.anchor = GridBagConstraints.WEST;
    c.weighty = 1.0;
/*
    // (o) Existing
    c.gridy++;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    myExisting = createRadioButton(i18n("LBL_Existing_WSDL")); // NOI18N
    myExisting.setSelected(true);
    myExisting.addItemListener(createItemListener(true));
    panel.add(myExisting, c);
    group.add(myExisting);
*/
    c.gridy++;
    c.weightx = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myWebService.createPanel(panel, c);
/*
    // (o) Create New
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    myCreate = createRadioButton(i18n("LBL_Create_New_WSDL")); // NOI18N
    myCreate.setSelected(false);
    myCreate.addItemListener(createItemListener(false));
    panel.add(myCreate, c);
    group.add(myCreate);

    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createNewPanel(), c);

    setEnabled(true);
//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue));
*/
    mainPanel.add(panel, cc);
  }
/*
  private JPanel createNewPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    // lanel
    c.gridy++;
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(
      TINY_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    myNameLabel = createLabel(i18n("LBL_Name")); // NOI18N
    panel.add(myNameLabel, c);

    // text field
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myName = new JTextField();
    myNameLabel.setLabelFor(myName);
    panel.add(myName, c);

    return panel;
  }

  private ItemListener createItemListener(final boolean existing) {
    return new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
        setEnabled(existing);
      }
    };
  }
*/
  @Override
  protected void setEnabled(boolean enabled)
  {
    myWebService.setEnabled(enabled);
//  myName.setEnabled( !enabled);
//  myNameLabel.setEnabled( !enabled);
  }
/*
  private String getName(String value) {
    String name = value.trim();

    if (name.equals(WSDL_EXT)) {
      return null;
    }
    if (name.length() == 0) {
      return null;
    }
    if (name.toLowerCase().endsWith(WSDL_EXT)) {
      return name;
    }
    return name + WSDL_EXT;
  }

  private FileObject createFile(String file) throws IOException {
    String text =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LS + LS + // NOI18N
      "<definitions" + LS + // NOI18N
      "    xmlns=\"http://schemas.xmlsoap.org/wsdl/\"" + LS + // NOI18N
      "    xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"" + LS + // NOI18N
      "    xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"" + LS + // NOI18N
      "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + LS + // NOI18N
      "</definitions>" + LS; // NOI18N

    FileObject wsdl = Util.createFile(getFolder(), file, text);
    WSDLModel model = Util.getWSDLModel(wsdl);
    String name = file;

    if (file.toLowerCase().endsWith(WSDL_EXT)) {
      name = file.substring(0, file.length() - WSDL_EXT.length());
    }
    Definitions definitions = model.getDefinitions();

    if (definitions == null) {
      return null;
    }
    try {
      model.startTransaction();
      definitions.setName(name);
      definitions.setTargetNamespace(HOST + name);
      Util.saveModel(model);
    } 
    finally {
      if (model.isIntransaction()) {
        model.endTransaction();
      }  
    }
    return wsdl;
  }
*/
//  private JTextField myName;
//  private JLabel myNameLabel;
//  private JRadioButton myExisting;
//  private JRadioButton myCreate;
  private WSDLModel myModel;
  private PanelWebService myWebService;

//  private static final String HOST =
//    "http://enterprise.netbeans.org/bpel/"; // NOI18N
//  private static final String WSDL_EXT = ".wsdl"; // NOI18N
}
