/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.hl7.configeditor.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.HL7Error;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Vishnuvardhan P.R
 * @author  Jun Qian
 */
public class AbstractPanel extends JPanel {

    private List<SelectableOperationPanel> opPanels =
            new ArrayList<SelectableOperationPanel>();
    private String templateConst;
    private Project mProject;
    private WSDLModel mWSDLModel;

    /** Creates new form AbstractPanel */
    public AbstractPanel(String templateConst) {
        initComponents();
		//setting Accessibility
		setAccessibility();
        this.templateConst = templateConst;

        operationSelectionChanged();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                validateMe(true, true);
            }
        });
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(AbstractPanel.class,
                "AbstractPanel.StepLabel");
    }

    protected List<SelectableOperationPanel> getSelectableOperationPanels() {
        return opPanels;
    }

    public void setProject(Project mProject) {
        this.mProject = mProject;
    }

    public void setWSDLModel(WSDLModel wsdlModel) {
        this.mWSDLModel = wsdlModel;
    }

    /**
     *
     * @param fireEvent             whether to fire property change event after validation
     * @param includingChildren     whether to validate individual child
     * @return
     */
    private HL7Error validateMe(boolean fireEvent, boolean includingChildren) {
        HL7Error error = new HL7Error();

        if (opPanels.size() == 0) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "AbstractPanel.OperationMissing"));
        } else {
            Set<String> opNames = new HashSet<String>();
            Set<String> msgTypes = new HashSet<String>();
            for (SelectableOperationPanel opPanel : opPanels) {
                if (includingChildren) {
                    error = opPanel.validateMe(fireEvent);
                }
                if (error.isEmpty()) {
                    String opName = opPanel.getOperationName();
                    if (opNames.contains(opName)) {
                        error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                        error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                                "AbstractPanel.OperationNameDuplicate", opName));
                    } else {
                        opNames.add(opName);
                    }

                    String msgType = opPanel.getMessageType();
                    if (msgType != null) {
                        if (msgTypes.contains(msgType)) {
                            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                            error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                                    "AbstractPanel.MessageTypeDuplicate", msgType));
                        } else {
                            msgTypes.add(msgType);
                        }
                    }
                }

                if (!error.isEmpty()) {
                    break;
                }
            }
        }

        if (fireEvent && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, error.getErrorMessage());
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }

        return error;
    }

    private void addOperationPanel(boolean twoWay) {
        SelectableOperationPanel opPanel =
                new SelectableOperationPanel(templateConst, twoWay, descriptionPanel);
        opPanel.setProject(mProject);
        opPanel.setWSDLModel(mWSDLModel);
        opPanels.add(opPanel);

        opPanel.addSelectionChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                operationSelectionChanged();
            }
        });

        opPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(),
                        evt.getOldValue(), evt.getNewValue());

                if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(evt.getPropertyName()) &&
                        "".equals(evt.getNewValue())) {
                    boolean checkChildren = !StackTraceUtils.isCalledByMyself(); // to avoid infinite loop
                    validateMe(true, checkChildren);
                }
            }
        });

        opPanelContainer.add(opPanel);

//        JPanel emptyPanel = new JPanel();
//        emptyPanel.setSize(10, 10);
//        opPanelContainer.add(emptyPanel);

        opPanelContainer.revalidate();
        
        validateMe(true, true);

        operationSelectionChanged();
    }

    private void removeOperationPanel(SelectableOperationPanel opPanel) {
        opPanels.remove(opPanel);
        opPanel.setVisible(false);
        opPanelContainer.remove(opPanel);
        opPanelContainer.revalidate();

        validateMe(true, true);

        operationSelectionChanged();
    }

    private void operationSelectionChanged() {
        boolean operationSelected = false;
        for (SelectableOperationPanel opPanel : opPanels) {
            if (opPanel.isSelected()) {
                operationSelected = true;
                break;
            }
        }

        btnRemoveOp.setEnabled(operationSelected);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        scrollPaneView = new javax.swing.JPanel();
        opPanelContainer = new javax.swing.JPanel();
        btnAddTwoWayOp = new javax.swing.JButton();
        btnRemoveOp = new javax.swing.JButton();
        btnAddOneWayOp = new javax.swing.JButton();
        descriptionPanel = new org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.DescriptionPanel();

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);

        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane.setViewportBorder(javax.swing.BorderFactory.createEtchedBorder());

        scrollPaneView.setLayout(new java.awt.BorderLayout());

        opPanelContainer.setLayout(new javax.swing.BoxLayout(opPanelContainer, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneView.add(opPanelContainer, java.awt.BorderLayout.NORTH);

        scrollPane.setViewportView(scrollPaneView);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddTwoWayOp, org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddTwoWayOp.text")); // NOI18N
        btnAddTwoWayOp.setToolTipText(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddTwoWayOp.toolTipText")); // NOI18N
        btnAddTwoWayOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTwoWayOpActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveOp, org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnRemoveOp.text")); // NOI18N
        btnRemoveOp.setToolTipText(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnRemoveOp.toolTipText")); // NOI18N
        btnRemoveOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveOpActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnAddOneWayOp, org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddOneWayOp.text")); // NOI18N
        btnAddOneWayOp.setToolTipText(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddOneWayOp.toolTipText")); // NOI18N
        btnAddOneWayOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOneWayOpActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(btnAddOneWayOp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnAddTwoWayOp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemoveOp)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnRemoveOp)
                    .add(btnAddTwoWayOp)
                    .add(btnAddOneWayOp))
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel1);
        jSplitPane1.setRightComponent(descriptionPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 669, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddTwoWayOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTwoWayOpActionPerformed
        addOperationPanel(true);
}//GEN-LAST:event_btnAddTwoWayOpActionPerformed

    private void btnRemoveOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveOpActionPerformed
        for (int i = opPanels.size() - 1; i >= 0; i--) {
            SelectableOperationPanel opPanel = opPanels.get(i);
            if (opPanel.isSelected()) {
                removeOperationPanel(opPanel);
            }
        }
}//GEN-LAST:event_btnRemoveOpActionPerformed

    private void btnAddOneWayOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddOneWayOpActionPerformed
        addOperationPanel(false);
    }//GEN-LAST:event_btnAddOneWayOpActionPerformed

	private void setAccessibility() {
		btnAddOneWayOp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddOneWayOp.toolTipText")); // NOI18N
		btnAddOneWayOp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddOneWayOp.toolTipText")); // NOI18N
        btnAddTwoWayOp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddTwoWayOp.toolTipText")); // NOI18N
        btnAddTwoWayOp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnAddTwoWayOp.toolTipText")); // NOI18N
        btnRemoveOp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnRemoveOp.toolTipText")); // NOI18N
        btnRemoveOp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AbstractPanel.class, "AbstractPanel.btnRemoveOp.toolTipText")); // NOI18N
       	this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton btnAddOneWayOp;
    protected javax.swing.JButton btnAddTwoWayOp;
    protected javax.swing.JButton btnRemoveOp;
    protected org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.DescriptionPanel descriptionPanel;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JSplitPane jSplitPane1;
    protected javax.swing.JPanel opPanelContainer;
    protected javax.swing.JScrollPane scrollPane;
    protected javax.swing.JPanel scrollPaneView;
    // End of variables declaration//GEN-END:variables
}
