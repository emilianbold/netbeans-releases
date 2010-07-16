/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CommonTransformConfigurationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public CommonTransformConfigurationPanel(List<TransformationItem> dataModel, 
            Project project, OperationParameter input, 
            OperationParameter output, String fileNamePrefix, 
            String transformNamePrefix) 
    {
//        myDataModel = dataModel;
        myProject = project;
        myInput = input;
        myOutput = output;
        myFileNamePrefix = fileNamePrefix == null ? DEFAULT_FILE_PREFIX : fileNamePrefix;
        myTransformNamePrefix = transformNamePrefix == null ? DEFAULT_TRANSFORM_NAME : transformNamePrefix;
        
        myDataModel = new ArrayList<TransformationItem>(); 
        initDataModel();
        update(dataModel);
        
        initComponents();
    }

    public List<TransformationItem> getDataModel() {
        return myDataModel;
    }
  
    public void update(List<TransformationItem> oldDataModel) {
        if (oldDataModel == null) {
            return;
        }
        assert myDataModel != null;

        int oldDataLength = oldDataModel.size();
        int newDataLength = myDataModel.size();
        if (oldDataLength != newDataLength) {
            return;
        }
        
        for (int i = 0; i < myDataModel.size(); i++) {
            TransformationItem curItem = myDataModel.get(i);
            TransformationItem oldItem = oldDataModel.get(i);
            String curInput = curItem.getInputPartName();
            String curOutput =  curItem.getOutputPartName();
            String oldInput = oldItem.getInputPartName();
            String oldOutput =  oldItem.getOutputPartName();
            if ((curInput != null && !curInput.equals(oldInput)) 
                    || (curInput == null && oldInput != null) 
                    || (curOutput != null && !curOutput.equals(oldOutput)) 
                    || (curOutput == null && oldOutput != null) ) 
            {
                return;
            }
        }
        myDataModel = oldDataModel;
    }
    
    // todo m
    public void update(OperationParameter input, OperationParameter output) {
        if ((myInput != null && myInput.equals(input) || myInput == null && input == null)
                && (myOutput != null && myOutput.equals(output) || myOutput == null && output == null)) 
        {
            // input and output parameters are the same - doesn't required update data model;
            return;
        }
        myInput = input;
        myOutput = output;
        myDataModel = new ArrayList<TransformationItem>(); 
        reinitDataModel();
//        reinitDataModel();
    }
    void setA11y(String acsn, String acsd, JLabel lblForTbl) {
        setA11y(acsn, acsd);
        if (lblForTbl != null) {
            lblForTbl.setLabelFor(myTransformsTbl);
            lblForTbl.getAccessibleContext().setAccessibleParent(myTransformsTbl);
        }
    }
    
    void setA11y(String acsn, String acsd) {
        if (myTransformsTbl == null) {
            throw new IllegalStateException(NbBundle.getMessage(CommonTransformConfigurationPanel.class, "MSG_TCPanelIsNotFullyInit")); // NOI18N
        }
        if (acsn != null) {
            myTransformsTbl.getAccessibleContext().setAccessibleName(acsn);
        }
        if (acsd != null) {
            myTransformsTbl.getAccessibleContext().setAccessibleDescription(acsd);
        }
    }
    
    protected void finishEditing() {
        assert SwingUtilities.isEventDispatchThread();
        if (myTransformsTbl != null) {
            if (myTransformsTbl.isEditing()) {
                int r =myTransformsTbl.getEditingRow();
                int c =myTransformsTbl.getEditingColumn();
                TableCellEditor cellEditor = myTransformsTbl.getCellEditor(r, c);
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }
        }
    }
    
    // todo m
    private  void reinitDataModel() {
        initDataModel();
        myTransformsTbl.setModel(new TransformConfigurationTableModel(myDataModel));
    }
    
    protected void setDataModel(List<TransformationItem> extDataModel) {
        myDataModel = extDataModel;
        myTransformsTbl.setModel(new TransformConfigurationTableModel(myDataModel));
    }
    
    private void initDataModel() {
        if (myInput == null || myOutput == null) {
            return;
        }
        
        Collection<Part> outputParts = getParts(myOutput);
        if (outputParts == null) {
            return;
        }
        Part[] outputPartsArr = outputParts.toArray(new Part[outputParts.size()]);
        
        Collection<Part> inputParts = getParts(myInput);
        Part[] inputPartsArr = inputParts != null? inputParts.toArray(new Part[inputParts.size()]) : new Part[0];
        
        String tmpOutPartName = null;
        String tmpInPartName = null;
        String tmpFileName = null;
        String tmpTransformName = null;
        int xslFileNumber = 1;
        int tmpInputIndex = 0;
        for (int i = 0; i < outputPartsArr.length; i++) {
            Part outPart = outputPartsArr[i];
            tmpOutPartName = outPart == null ? null : outPart.getName();
            //
            if (outPart == null) {
                continue;
            }
            //
            Part inPart = tmpInputIndex< inputPartsArr.length ? inputPartsArr[tmpInputIndex] : null;
            tmpInPartName = inPart !=null ? inPart.getName() : tmpInPartName;
            
            xslFileNumber = Panel.getXslFileNumber(ReferenceUtil.getSrcFolder(myProject), xslFileNumber);
            tmpFileName = myFileNamePrefix+xslFileNumber;
            xslFileNumber++;
            
            tmpTransformName = myTransformNamePrefix+(tmpInputIndex+1);
            
            TransformationItem tmpItem = new TransformationItem(tmpTransformName,
                    tmpInPartName, tmpOutPartName, tmpFileName);
            myDataModel.add(tmpItem);
            tmpInputIndex++;
        }

    }
    
    private int getNumParts(OperationParameter opParam) {
        Collection<Part> parts = getParts(opParam);
        return parts == null ? 0 : parts.size();
    }
    
    private Collection<Part> getParts(OperationParameter opParam) {
        Reference<Message> messRef = opParam == null ? null : opParam.getMessage();
        Message mess = messRef == null ? null : messRef.get();
        return mess == null ? null : mess.getParts();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        myTransformsScrlPane.setEnabled(enabled);
        myTransformsTbl.setEnabled(enabled);
    }
    
    private void initComponents() {
        myTransformsScrlPane = new javax.swing.JScrollPane();
        myTransformsTbl = new javax.swing.JTable();
        myTransformsScrlPane.setAutoscrolls(true);
        myTransformsTbl.setModel(new TransformConfigurationTableModel(myDataModel));
        setupCellEditors(myTransformsTbl.getColumnModel());
        setupCellRenderers(myTransformsTbl.getColumnModel());
        
        myTransformsTbl.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        myTransformsTbl.setSurrendersFocusOnKeystroke(true);
        myTransformsTbl.setPreferredScrollableViewportSize(new Dimension(464, 100));
        
        
        myTransformsScrlPane.setViewportView(myTransformsTbl);

        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        this.add(myTransformsScrlPane);
    }
    
    private void setupCellEditors(TableColumnModel columnModel) {
        setupPartsCellEditor(columnModel.getColumn(0), true);
//        setupPartsCellEditor(columnModel.getColumn(2), false);
        
        setupFilePathCellEditor(columnModel.getColumn(1));
    }
    
    private void setupFilePathCellEditor(TableColumn filePathColumn) {
        filePathColumn.setCellEditor(new TransformCellEditor(ReferenceUtil.getSrcFolder(myProject), myProject));
    }
    
    private void setupPartsCellEditor(TableColumn partColumn, boolean isInputParts) {
        JComboBox partsComboBox = new JComboBox();
        Collection<Part> parts = isInputParts ? getParts(myInput) : getParts(myOutput);
        if (parts != null) {
            for (Part part : parts) {
                if (part != null) {
                    partsComboBox.addItem(part.getName());
                }
            }
        }
        partColumn.setCellEditor(new DefaultCellEditor(partsComboBox));
    }
    
    private void setupCellRenderers(TableColumnModel columnModel) {
        myTransformsTbl.getColumnModel().getColumn(1).setCellRenderer(new TransformCellRenderer());
    }
    
    private String myFileNamePrefix;
    private String myTransformNamePrefix;
    private Project myProject;
    private OperationParameter myInput;
    private OperationParameter myOutput;
    
    private JTable myTransformsTbl;
    private JScrollPane myTransformsScrlPane;
    private List<TransformationItem> myDataModel = new ArrayList<TransformationItem>();
    public static final String DEFAULT_FILE_PREFIX = "InXslFile"; // NOI18N
    public static final String DEFAULT_REPLY_FILE_PREFIX = "OutXslFile"; // NOI18N
    public static final String DEFAULT_TRANSFORM_NAME = "InTransform"; // NOI18N
    public static final String DEFAULT_REPLY_TRANSFORM_NAME = "OutTransform"; // NOI18N
}
