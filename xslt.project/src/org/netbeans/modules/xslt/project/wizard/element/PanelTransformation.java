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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.WizardDescriptor;

import org.netbeans.api.project.Project;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.dom.Utils;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Vitaly Bychkov
 * @version 2008.08.11
 */
final class PanelTransformation extends WizardSettingsPanel {
    private static final long serialVersionUID = 1L;
    
  PanelTransformation(
    Project project,
    Panel stepPanel,
    Operation wsdlOperation,
    boolean isReadOnly,
    boolean isInput)
  {
      this(project, stepPanel, 
              wsdlOperation != null ? wsdlOperation.getInput() : null, 
              wsdlOperation != null ? wsdlOperation.getOutput() : null, isReadOnly, isInput);
  }

  PanelTransformation(
    Project project,
    Panel stepPanel,
    OperationParameter inParam,
    OperationParameter outParam,
    boolean isReadOnly,
    boolean isInput)
  {
    super(project);
    myStepPanel = stepPanel;
    myInput = inParam;
    myOutput = outParam;
    myIsReadOnly = isReadOnly;
    myIsInput = isInput;
    myIsInputRequired = true;
    myIsOutputRequired = true;
  }

  protected void finishEditing() {
      if (myTransformationPanel != null) {
          myTransformationPanel.finishEditing();
      }
  }
  
  @Override
  protected Object getResult()
  {
    return myTransformationPanel.getDataModel();
  }
  
  @Override
  protected String getError()
  {
      List<TransformationItem> dataModel = myTransformationPanel.getDataModel();
      if (dataModel == null) {
          return i18n("ERR_EMPTY_TRANSFORMATIONS");
      }
      TransformationItem transformationItem = null;
      for (int i =0; i< dataModel.size(); i++) {
          transformationItem = dataModel.get(i);
          
          String tmpCheckResult = null;
          if (transformationItem == null) {
              return i18n("ERR_EMPTY_TRANSFORMATIONS");
          }
          tmpCheckResult = checkFileName(transformationItem.getXslFilePath());
          if (tmpCheckResult != null) {
              return tmpCheckResult;
          }
          
          tmpCheckResult = checkPartName(transformationItem.getInputPartName());
          if (tmpCheckResult != null) {
              return tmpCheckResult;
          }
          
          tmpCheckResult = checkPartName(transformationItem.getOutputPartName());
          if (tmpCheckResult != null) {
              return tmpCheckResult;
          }
          
          tmpCheckResult = checkTransformName(transformationItem.getName());
          if (tmpCheckResult != null) {
              return tmpCheckResult;
          }

          tmpCheckResult = isUniqueTransformName(dataModel, transformationItem.getName(), i);
          if (tmpCheckResult != null) {
              return tmpCheckResult;
          }
      }
      
//      if ( myIsInputRequired && !check(myInput)) {
//        return  
//            i18n( myIsInput 
//            ? "ERR_Operation_With_Input_Is_Required" // NOI18N
//            : "ERR_Operation_With_Output_Is_Required" ,getOperationName(myInput)); // NOI18N
//      }
//      if (myIsOutputRequired && !check(myOutput)) {
//        return  
//            i18n( myIsInput 
//            ? "ERR_Operation_With_Output_Is_Required" // NOI18N
//            : "ERR_Operation_With_Input_Is_Required" ,getOperationName(myOutput)); // NOI18N
//      }
    return null;
  }
  
  private String checkFileName(String fileName) {
    if (fileName != null) {
      if ( !PanelUtil.isValidFileName(fileName)) {
          return i18n("ERR_WrongFileName", fileName); // NOI18N
      } 
    }
    return null;
  }

  private String checkPartName(String name) {
    if (!Utils.isValidNCName(name)) {
      return i18n("ERR_WrongPartName", name); // NOI18N
    }
    return null;
  }

  private String checkTransformName(String name) {
    if (name == null || !name.matches("\\w+")) { // NOI18N
        return i18n("ERR_WrongTransformName", name); // NOI18N
    }
    return null;
  }

    @Override
    @SuppressWarnings("unchecked")
    public void readSettings(WizardDescriptor descriptor) {
        super.readSettings(descriptor);
        Object dataModel = null;
        if (myIsInput) {
            dataModel = descriptor.getProperty(INPUT_TRANSFORMATIONS);
        } else {
            dataModel = descriptor.getProperty(OUTPUT_TRANSFORMATIONS);
        }    
        if (dataModel instanceof List) {
            myTransformDataModel = (List<TransformationItem>) dataModel;
        }
        
    }
  
  @Override
  public void storeSettings(WizardDescriptor descriptor) {
    super.storeSettings(descriptor);
    List<TransformationItem> dataModel = myTransformationPanel.getDataModel();
    if (myIsInput) {
        descriptor.putProperty(INPUT_TRANSFORMATIONS, dataModel);
    } else {
        descriptor.putProperty(OUTPUT_TRANSFORMATIONS, dataModel);
    }    
    
////    List<TransformationItem> dataModel = myTransformationPanel.getDataModel();
////    if (myIsInput) {
////        descriptor.putProperty(INPUT_TRANSFORMATIONS, dataModel);
////    } else {
////        descriptor.putProperty(OUTPUT_TRANSFORMATIONS, dataModel);
////        if (myIsRequireTransfomBox.isSelected()) {
////          descriptor.putProperty(CHOICE, CHOICE_FILTER_REQUEST_REPLY);
////        }
////        else {
////          descriptor.putProperty(CHOICE, CHOICE_FILTER_ONE_WAY);
////        }
////    }
////
////    descriptor.putProperty(myIsInput ? IMPL_OPERATION : CALLED_OPERATION, getOperation(myInput));
    
//    
//    if (myIsInput) {
//      descriptor.putProperty(INPUT_OPERATION, getOperation(true));
//    }
//    else {
//      descriptor.putProperty(OUTPUT_OPERATION, getOperation(false));
//    } 
  }

  private Operation getOperation(OperationParameter opParam) {
      return opParam != null ? (Operation) opParam.getParent() : null;
  }
  
  
  private String getOperationName(OperationParameter opParam) {
    Operation op = getOperation(opParam) ;
    return op == null ? null : op.getName();
  }
  
  void setRequirement(boolean isInputRequired, boolean isOutputRequired) {
    myIsInputRequired = isInputRequired;
    myIsOutputRequired = isOutputRequired;
  }

  protected boolean isTransformEnabled() {
      return myIsRequireTransfomBox.isSelected();
  }
  
  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc) {
    JPanel panel = new JPanel(new GridBagLayout());
    
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    c.gridy++;
    c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
    c.weightx = 0.0;


    JLabel lblForTbl = new javax.swing.JLabel();
    lblForTbl.setText(org.openide.util.NbBundle.getMessage(PanelTransformation.class,
        myIsInput ? "LBL_For_Request_Table" : "LBL_For_Reply_Table")); // NOI18N
    lblForTbl.setDisplayedMnemonic(myIsInput ? 'U' : 'Y'); // NOI18N
    lblForTbl.getAccessibleContext().setAccessibleName(myIsInput ?
        "lblForTblRequest" : "lblForTblReply");
    lblForTbl.getAccessibleContext().setAccessibleDescription(
        org.openide.util.NbBundle.getMessage(PanelTransformation.class,
        myIsInput ? "LBL_For_Request_Table" : "LBL_For_Reply_Table")); // NOI18N
    panel.add(lblForTbl, c);
    c.gridy++;

    myIsRequireTransfomBox = createCheckBox(new ButtonAction(i18n(
        PanelTransformation.this.myIsInput ? "LBL_Transform_Request" : "LBL_Transform_Reply")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
            update();
            myTransformationPanel.setEnabled(myIsRequireTransfomBox.isSelected());
        }});
    
    if (myIsInput) {
        myIsRequireTransfomBox.setEnabled(false);
    }
    myIsRequireTransfomBox.setSelected(true);
    panel.add(myIsRequireTransfomBox, c);
    
    // text
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    myTransformViewText = new JTextField();
    if (myIsInput) {
        a11y(myTransformViewText, "ACSN_LBL_Transform_Request", "ACSD_LBL_Transform_Request"); // NOI18N
    } else {
        a11y(myTransformViewText, "ACSN_LBL_Transform_Reply", "ACSD_LBL_Transform_Reply"); // NOI18N
    }
    myTransformViewText.setEnabled(false);
    updateText(myTransformViewText, myInput, myOutput);
    panel.add(myTransformViewText, c);

    
    // operation 
    c.gridy++;
    c.gridwidth = 3;
    c.insets = new Insets(0, 0, 0, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    myTransformationPanel = new CommonTransformConfigurationPanel(myTransformDataModel,
            getProject(), myInput, myOutput, 
            myIsInput ? CommonTransformConfigurationPanel.DEFAULT_FILE_PREFIX:CommonTransformConfigurationPanel.DEFAULT_REPLY_FILE_PREFIX, 
            myIsInput ? CommonTransformConfigurationPanel.DEFAULT_TRANSFORM_NAME:CommonTransformConfigurationPanel.DEFAULT_REPLY_TRANSFORM_NAME);

    if (myIsInput) {
        myTransformationPanel.setA11y(i18n("ACSN_LBL_Request_TCPanel"), 
            i18n("ACSD_LBL_Request_TCPanel"), lblForTbl);
    } else {
        myTransformationPanel.setA11y(i18n("ACSN_LBL_Reply_TCPanel"), 
            i18n("ACSD_LBL_Reply_TCPanel"), lblForTbl);
    }
    panel.add(myTransformationPanel, c);

    mainPanel.add(panel, cc);
  }

  @Override
  protected void update()
  {
      if (myIsUpdating) {
          return;
      }
      myIsUpdating = true;
      
      if (myIsRequireTransfomBox != null) {
          myTransformationPanel.update(myInput, myOutput);
          myTransformationPanel.setEnabled(myIsRequireTransfomBox.isSelected());
          updateText(
            myTransformViewText,
            myInput, myOutput);
          updateTypes();
      }
      myIsUpdating = false;
  }

  private void updateText(JTextField text, OperationParameter input, OperationParameter output) {
      updateText(text, getType(input), getType(output));
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

  protected OperationParameter getInput() {
      return myInput;
  }
  
  protected OperationParameter getOutput() {
      return myOutput;
  }

  private void updateTypes() {
    getParent().update();

    if (myIsReadOnly) {
      return;
    }
  }

  private Panel getParent() {
      return myStepPanel;
  }

  private OperationParameter myInput;
  private OperationParameter myOutput;
  private boolean myIsUpdating = false;
  private boolean myIsReadOnly;
  private boolean myIsInput;
  private boolean myIsInputRequired;
  private boolean myIsOutputRequired;
  private CommonTransformConfigurationPanel myTransformationPanel;
  private JCheckBox myIsRequireTransfomBox;
  private JTextField myTransformViewText;
  private Panel myStepPanel;
  private List<TransformationItem> myTransformDataModel;
}
