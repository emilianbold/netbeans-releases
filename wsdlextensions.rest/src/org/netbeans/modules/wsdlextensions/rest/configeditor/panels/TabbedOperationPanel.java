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
package org.netbeans.modules.wsdlextensions.rest.configeditor.panels;

import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatablePropertiesHolder;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.rest.RESTConstants;
import org.netbeans.modules.wsdlextensions.rest.RESTMethod;
import org.netbeans.modules.wsdlextensions.rest.configeditor.RESTError;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatableProperties;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class TabbedOperationPanel extends javax.swing.JPanel 
        implements ChangeListener {

    private Map<RESTMethod, List<SelectableOperationPanel>> allOpPanels =
            new HashMap<RESTMethod, List<SelectableOperationPanel>>();
    private Map<RESTMethod, JPanel> method2OpPanelContainer;

    private String templateConst;
    private Project mProject;
    private WSDLModel mWSDLModel;

    /** Creates new form TabbedOperationPanel */
    public TabbedOperationPanel(String templateConst) {
        initComponents();
        this.templateConst = templateConst;

        method2OpPanelContainer = new HashMap<RESTMethod, JPanel>();
        method2OpPanelContainer.put(RESTMethod.GET, opPanelContainerTabGet);
        method2OpPanelContainer.put(RESTMethod.PUT, opPanelContainerTabPut);
        method2OpPanelContainer.put(RESTMethod.POST, opPanelContainerTabPost);
        method2OpPanelContainer.put(RESTMethod.DELETE, opPanelContainerTabDelete);
        method2OpPanelContainer.put(RESTMethod.HEAD, opPanelContainerTabHead);

        tabbedPane.addChangeListener(this);

        stateChanged(null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                validateMe(true, true);
            }
        });
    }

    protected List<SelectableOperationPanel> getSelectableOperationPanels(RESTMethod method) {
        return allOpPanels.get(method);
    }

    private SelectableOperationPanel getOperationPanelBeingEdited() {
        List<SelectableOperationPanel> opPanels =
                getSelectableOperationPanels(getSelectedRESTMethod());

        if (opPanels != null) {
            for (SelectableOperationPanel opPanel : opPanels) {
                if (opPanel.isSelected()) {
                    return opPanel;
                }
            }
        }

        return null;
    }

    public void setProject(Project mProject) {
        this.mProject = mProject;
    }

    public void setWSDLModel(WSDLModel wsdlModel) {
        this.mWSDLModel = wsdlModel;
    }

    private int getNumberOfOperationPanels() {
        List<SelectableOperationPanel> ret = new ArrayList<SelectableOperationPanel>();
        for (RESTMethod method : allOpPanels.keySet()) {
            List<SelectableOperationPanel> methodOpPanels = allOpPanels.get(method);
            if (methodOpPanels != null) {
                ret.addAll(methodOpPanels);
            }
        }
        return ret.size();
    }

    /**
     *
     * @param fireEvent             whether to fire property change event after validation
     * @param includingChildren     whether to validate individual child
     * @return
     */
    private RESTError validateMe(boolean fireEvent, boolean includingChildren) {
        RESTError error = new RESTError();

        if (getNumberOfOperationPanels() == 0) {
            error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
            error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                    "TabbedOperationPanel.OperationMissing")); // NOI18N
        } else {
            Set<String> allOpNames = new HashSet<String>();
            for (RESTMethod method : RESTMethod.values()) {
                List<SelectableOperationPanel> methodOpPanels = allOpPanels.get(method);
                if (methodOpPanels != null) {
                    for (SelectableOperationPanel opPanel : methodOpPanels) {
                        if (includingChildren) {
                            error = opPanel.validateMe(fireEvent);
                        }
                        if (error.isEmpty()) {
                            String opName = opPanel.getOperationName();
                            if (allOpNames.contains(opName)) {
                                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
                                error.setErrorMessage(NbBundle.getMessage(OperationPanel.class,
                                        "TabbedOperationPanel.OperationNameDuplicate", opName)); // NOI18N
                            } else {
                                allOpNames.add(opName);
                            }
                        }

                        if (!error.isEmpty()) {
                            break;
                        }
                    }

                    if (!error.isEmpty()) {
                        break;
                    }
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

    private void clearSelection() {
        RESTMethod method = getSelectedRESTMethod();
        List<SelectableOperationPanel> opPanels = getSelectableOperationPanels(method);
        if (opPanels != null) {
            for (SelectableOperationPanel opPanel : opPanels) {
                opPanel.setSelected(false);
            }
        }
    }

    private void addOperationPanel(boolean twoWay, RESTMethod method) {
        clearSelection();

        SelectableOperationPanel opPanel =
                new SelectableOperationPanel(templateConst, twoWay, descriptionPanel);
        opPanel.setSelected(true);
        opPanel.setProject(mProject);
        opPanel.setWSDLModel(mWSDLModel);

        List<SelectableOperationPanel> methodOpPanels = allOpPanels.get(method);
        if (methodOpPanels == null) {
            methodOpPanels = new ArrayList<SelectableOperationPanel>();
            allOpPanels.put(method, methodOpPanels);
        }
        methodOpPanels.add(opPanel);

        opPanel.addSelectionChangeListener(this);

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

        JPanel opPanelContainer = method2OpPanelContainer.get(method);
        opPanelContainer.add(opPanel);
        opPanelContainer.revalidate();

        validateMe(true, true);

        stateChanged(null);

        updateTabTitle(method);
    }

    private void removeOperationPanel(SelectableOperationPanel opPanel, RESTMethod method) {
        allOpPanels.get(method).remove(opPanel);

        JPanel opPanelContainer = method2OpPanelContainer.get(method);
        opPanelContainer.remove(opPanel);
        opPanelContainer.revalidate();

        validateMe(true, true);

        stateChanged(null);

        updateTabTitle(method);
    }

    private void updateTabTitle(RESTMethod method) {
        if (method == RESTMethod.GET) {
            updateTabTitle(0, method, "TabbedOperationPanel.tabGet.TabConstraints.tabTitle"); // NOI18N
        } else if (method == RESTMethod.PUT) {
            updateTabTitle(1, method, "TabbedOperationPanel.tabPut.TabConstraints.tabTitle"); // NOI18N
        } else if (method == RESTMethod.POST) {
            updateTabTitle(2, method, "TabbedOperationPanel.tabPost.TabConstraints.tabTitle"); // NOI18N
        } else if (method == RESTMethod.DELETE) {
            updateTabTitle(3, method, "TabbedOperationPanel.tabDelete.TabConstraints.tabTitle"); // NOI18N
        } else if (method == RESTMethod.HEAD) {
            updateTabTitle(4, method, "TabbedOperationPanel.tabHead.TabConstraints.tabTitle"); // NOI18N
        } else {
            assert false;
        }
    }

    public void stateChanged(ChangeEvent e) {
        List<SelectableOperationPanel> opPanels =
                getSelectableOperationPanels(getSelectedRESTMethod());

        boolean operationSelected = false;
        int selectionCount = 0;

        if (opPanels != null) {
            for (SelectableOperationPanel opPanel : opPanels) {
                if (opPanel.isSelected()) {
                    operationSelected = true;
                    selectionCount++;
                }
            }
        }

        btnRemoveOp.setEnabled(operationSelected);
        btnEditOp.setEnabled(selectionCount == 1);
    }

    private void updateTabTitle(int tabIndex, RESTMethod method, String titleBundleKey) {
        List<SelectableOperationPanel> opPanels = getSelectableOperationPanels(method);

        String tabTitle = NbBundle.getMessage(TabbedOperationPanel.class, titleBundleKey);

        if (opPanels != null && opPanels.size() > 0) {
            // HTML Tab title is broken in certain versions of JDK!
            // (See http://bugs.sun.com/view_bug.do?bug_id=6670274)
            //tabTitle = "<html><b>" + tabTitle + " (" + opPanels.size() + ")</b></html>";
            tabTitle = tabTitle + " (" + opPanels.size() + ")"; // NOI18N
        }
        tabbedPane.setTitleAt(tabIndex, tabTitle);
    }

    private RESTMethod getSelectedRESTMethod() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) {
            return RESTMethod.GET;
        } else if (index == 1) {
            return RESTMethod.PUT;
        } else if (index == 2) {
            return RESTMethod.POST;
        } else if (index == 3) {
            return RESTMethod.DELETE;
        } else if (index == 4) {
            return RESTMethod.HEAD;
        } else {
            assert false;
            return null;
        }
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
        jPanel6 = new javax.swing.JPanel();
        btnEditOp = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        tabGet = new javax.swing.JPanel();
        scrollPaneTabGet = new javax.swing.JScrollPane();
        scrollPaneViewTabGet = new javax.swing.JPanel();
        opPanelContainerTabGet = new javax.swing.JPanel();
        tabPut = new javax.swing.JPanel();
        scrollPaneTabPut = new javax.swing.JScrollPane();
        scrollPaneViewTabPut = new javax.swing.JPanel();
        opPanelContainerTabPut = new javax.swing.JPanel();
        tabPost = new javax.swing.JPanel();
        scrollPaneTabPost = new javax.swing.JScrollPane();
        scrollPaneViewTabPost = new javax.swing.JPanel();
        opPanelContainerTabPost = new javax.swing.JPanel();
        tabDelete = new javax.swing.JPanel();
        scrollPaneTabDelete = new javax.swing.JScrollPane();
        scrollPaneViewTabDelete = new javax.swing.JPanel();
        opPanelContainerTabDelete = new javax.swing.JPanel();
        tabHead = new javax.swing.JPanel();
        scrollPaneTabHead = new javax.swing.JScrollPane();
        scrollPaneViewTabHead = new javax.swing.JPanel();
        opPanelContainerTabHead = new javax.swing.JPanel();
        btnRemoveOp = new javax.swing.JButton();
        btnAddTwoWayOp = new javax.swing.JButton();
        descriptionPanel = new org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel();

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        btnEditOp.setMnemonic('E');
        btnEditOp.setText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnEditOp.text")); // NOI18N
        btnEditOp.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnEditOp.toolTipText")); // NOI18N
        btnEditOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditOpActionPerformed(evt);
            }
        });

        tabbedPane.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabbedPane.toolTipText")); // NOI18N

        tabGet.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabGet.toolTipText")); // NOI18N

        scrollPaneViewTabGet.setLayout(new java.awt.BorderLayout());

        opPanelContainerTabGet.setLayout(new javax.swing.BoxLayout(opPanelContainerTabGet, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneViewTabGet.add(opPanelContainerTabGet, java.awt.BorderLayout.NORTH);

        scrollPaneTabGet.setViewportView(scrollPaneViewTabGet);

        org.jdesktop.layout.GroupLayout tabGetLayout = new org.jdesktop.layout.GroupLayout(tabGet);
        tabGet.setLayout(tabGetLayout);
        tabGetLayout.setHorizontalGroup(
            tabGetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabGet, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        tabGetLayout.setVerticalGroup(
            tabGetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabGet, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabGet.TabConstraints.tabTitle"), null, tabGet, org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabGet.toolTipText")); // NOI18N

        tabPut.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabPut.toolTipText")); // NOI18N

        scrollPaneViewTabPut.setLayout(new java.awt.BorderLayout());

        opPanelContainerTabPut.setLayout(new javax.swing.BoxLayout(opPanelContainerTabPut, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneViewTabPut.add(opPanelContainerTabPut, java.awt.BorderLayout.NORTH);

        scrollPaneTabPut.setViewportView(scrollPaneViewTabPut);

        org.jdesktop.layout.GroupLayout tabPutLayout = new org.jdesktop.layout.GroupLayout(tabPut);
        tabPut.setLayout(tabPutLayout);
        tabPutLayout.setHorizontalGroup(
            tabPutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabPut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        tabPutLayout.setVerticalGroup(
            tabPutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabPut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabPut.TabConstraints.tabTitle"), null, tabPut, org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabPut.toolTipText")); // NOI18N

        tabPost.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabPost.toolTipText")); // NOI18N

        scrollPaneViewTabPost.setLayout(new java.awt.BorderLayout());

        opPanelContainerTabPost.setLayout(new javax.swing.BoxLayout(opPanelContainerTabPost, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneViewTabPost.add(opPanelContainerTabPost, java.awt.BorderLayout.NORTH);

        scrollPaneTabPost.setViewportView(scrollPaneViewTabPost);

        org.jdesktop.layout.GroupLayout tabPostLayout = new org.jdesktop.layout.GroupLayout(tabPost);
        tabPost.setLayout(tabPostLayout);
        tabPostLayout.setHorizontalGroup(
            tabPostLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabPost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        tabPostLayout.setVerticalGroup(
            tabPostLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabPost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabPost.TabConstraints.tabTitle"), tabPost); // NOI18N

        tabDelete.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabDelete.toolTipText")); // NOI18N

        scrollPaneViewTabDelete.setLayout(new java.awt.BorderLayout());

        opPanelContainerTabDelete.setLayout(new javax.swing.BoxLayout(opPanelContainerTabDelete, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneViewTabDelete.add(opPanelContainerTabDelete, java.awt.BorderLayout.NORTH);

        scrollPaneTabDelete.setViewportView(scrollPaneViewTabDelete);

        org.jdesktop.layout.GroupLayout tabDeleteLayout = new org.jdesktop.layout.GroupLayout(tabDelete);
        tabDelete.setLayout(tabDeleteLayout);
        tabDeleteLayout.setHorizontalGroup(
            tabDeleteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabDelete, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        tabDeleteLayout.setVerticalGroup(
            tabDeleteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabDelete, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabDelete.TabConstraints.tabTitle"), tabDelete); // NOI18N

        tabHead.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabHead.toolTipText")); // NOI18N

        scrollPaneViewTabHead.setLayout(new java.awt.BorderLayout());

        opPanelContainerTabHead.setLayout(new javax.swing.BoxLayout(opPanelContainerTabHead, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneViewTabHead.add(opPanelContainerTabHead, java.awt.BorderLayout.NORTH);

        scrollPaneTabHead.setViewportView(scrollPaneViewTabHead);

        org.jdesktop.layout.GroupLayout tabHeadLayout = new org.jdesktop.layout.GroupLayout(tabHead);
        tabHead.setLayout(tabHeadLayout);
        tabHeadLayout.setHorizontalGroup(
            tabHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabHead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        tabHeadLayout.setVerticalGroup(
            tabHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPaneTabHead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.tabHead.TabConstraints.tabTitle"), tabHead); // NOI18N

        btnRemoveOp.setMnemonic('R');
        btnRemoveOp.setText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnRemoveOp.text")); // NOI18N
        btnRemoveOp.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnRemoveOp.toolTipText")); // NOI18N
        btnRemoveOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveOpActionPerformed(evt);
            }
        });

        btnAddTwoWayOp.setMnemonic('A');
        btnAddTwoWayOp.setText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnAddTwoWayOp.text")); // NOI18N
        btnAddTwoWayOp.setToolTipText(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.btnAddTwoWayOp.toolTipText")); // NOI18N
        btnAddTwoWayOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTwoWayOpActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(tabbedPane)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                        .add(btnAddTwoWayOp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnRemoveOp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnEditOp)
                        .add(23, 23, 23))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnEditOp)
                    .add(btnRemoveOp)
                    .add(btnAddTwoWayOp))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel6);
        jSplitPane1.setRightComponent(descriptionPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 706, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TabbedOperationPanel.class, "TabbedOperationPanel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditOpActionPerformed
        int OPTION_OK = 0;
        int OPTION_CANCEL = OPTION_OK + 1;

        final String COMMAND_OK = "OK"; // NOI18N
        final String COMMAND_CANCEL = "CANCEL"; // NOI18N

        // Create options
        JButton[] options = new JButton[] {
                new JButton(
                    NbBundle.getMessage(TabbedOperationPanel.class, "LBL_Ok_Option") // NOI18N
                ), 
                new JButton(
                    NbBundle.getMessage(TabbedOperationPanel.class, "LBL_Cancel_Option" // NOI18N
                    )
                )
            };

        // Set commands
        options[OPTION_OK].setActionCommand(COMMAND_OK);
        options[OPTION_CANCEL].setActionCommand(COMMAND_CANCEL);

        //A11Y
        options[OPTION_OK].getAccessibleContext().setAccessibleDescription (
                NbBundle.getMessage(TabbedOperationPanel.class, "LBL_Ok_Option")); // NOI18N
        options[OPTION_CANCEL].getAccessibleContext().setAccessibleDescription (
                NbBundle.getMessage(TabbedOperationPanel.class, "LBL_Cancel_Option")); // NOI18N

        RESTMethod method = getSelectedRESTMethod();

        final ValidatablePropertiesHolder propertiesPanel =
            templateConst.equals(RESTConstants.TEMPLATE_IN) ?
                new InboundPropertiesPanel(method) :
                new OutboundPropertiesPanel(method);
        final SelectableOperationPanel opPanelBeingEdited = getOperationPanelBeingEdited();
        ValidatableProperties properties = opPanelBeingEdited.getValidatableProperties();
        propertiesPanel.setValidatableProperties(properties);

        // RegisterListener
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (COMMAND_OK.equals(e.getActionCommand())) {
                    ValidatableProperties properties = propertiesPanel.getValidatableProperties();
                    opPanelBeingEdited.setValidatableProperties(properties);
                }
            }
        };
        options[OPTION_OK].addActionListener(actionListener);
        options[OPTION_CANCEL].addActionListener(actionListener);

        String operationName = opPanelBeingEdited.getOperationName();
        if (operationName == null || operationName.trim().length() == 0) {
            operationName = NbBundle.getMessage(TabbedOperationPanel.class,
                "TabbedOperationPanel.propertyDialogTitle.UndefinedOperationName"); // NOI18N
        }
        String title = NbBundle.getMessage(TabbedOperationPanel.class,
                "TabbedOperationPanel.propertyDialogTitle.EditOperationProperties", // NOI18N
                operationName);

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                propertiesPanel,
                title,
                true,   // modal
                options, // options
                options[OPTION_OK], // initial value
                DialogDescriptor.BOTTOM_ALIGN, // options align
                null,    // helpCtx
                null     // listener
            ); 
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        dialog.toFront();

        validateMe(true, true);
}//GEN-LAST:event_btnEditOpActionPerformed

    private void btnAddTwoWayOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTwoWayOpActionPerformed
        addOperationPanel(true, getSelectedRESTMethod());
    }//GEN-LAST:event_btnAddTwoWayOpActionPerformed

    private void btnRemoveOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveOpActionPerformed
        RESTMethod method = getSelectedRESTMethod();
        List<SelectableOperationPanel> methodOpPanels = allOpPanels.get(method);
        for (int i = methodOpPanels.size() - 1; i >= 0; i--) {
            SelectableOperationPanel opPanel = methodOpPanels.get(i);
            if (opPanel.isSelected()) {
                removeOperationPanel(opPanel, method);
            }
        }
    }//GEN-LAST:event_btnRemoveOpActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTwoWayOp;
    private javax.swing.JButton btnEditOp;
    private javax.swing.JButton btnRemoveOp;
    private org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel descriptionPanel;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel opPanelContainerTabDelete;
    private javax.swing.JPanel opPanelContainerTabGet;
    private javax.swing.JPanel opPanelContainerTabHead;
    private javax.swing.JPanel opPanelContainerTabPost;
    private javax.swing.JPanel opPanelContainerTabPut;
    private javax.swing.JScrollPane scrollPaneTabDelete;
    private javax.swing.JScrollPane scrollPaneTabGet;
    private javax.swing.JScrollPane scrollPaneTabHead;
    private javax.swing.JScrollPane scrollPaneTabPost;
    private javax.swing.JScrollPane scrollPaneTabPut;
    private javax.swing.JPanel scrollPaneViewTabDelete;
    private javax.swing.JPanel scrollPaneViewTabGet;
    private javax.swing.JPanel scrollPaneViewTabHead;
    private javax.swing.JPanel scrollPaneViewTabPost;
    private javax.swing.JPanel scrollPaneViewTabPut;
    private javax.swing.JPanel tabDelete;
    private javax.swing.JPanel tabGet;
    private javax.swing.JPanel tabHead;
    private javax.swing.JPanel tabPost;
    private javax.swing.JPanel tabPut;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

}
