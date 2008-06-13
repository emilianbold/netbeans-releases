/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.hqleditor.ui;

import java.awt.CardLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.hqleditor.HQLEditorController;
import org.netbeans.modules.hibernate.hqleditor.HQLResult;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;

/**
 * HQL editor top component.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public final class HQLEditorTopComponent extends TopComponent {

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/hibernate/hqleditor/ui/resources/queryEditor16X16.png"; //NOI18N
    private Logger logger = Logger.getLogger(HQLEditorTopComponent.class.getName());
    private HashMap<String, FileObject> hibernateConfigMap = new HashMap<String, FileObject>();
    private static List<Integer> windowCounts = new ArrayList<Integer>();
    private Integer thisWindowCount = new Integer(0);
    private HQLEditorController controller = null;
    private HibernateEnvironment env = null;

    private static int getNextWindowCount() {
        int count = 0;
        while (windowCounts.contains(count)) {
            count++;
        }
        windowCounts.add(count);
        return count;
    }

    public static HQLEditorTopComponent getInstance() {
        return new HQLEditorTopComponent(null);
    }

    public HQLEditorTopComponent(HQLEditorController controller) {
        this.controller = controller;
        initComponents();
        this.thisWindowCount = getNextWindowCount();
        setName(NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_HQLEditorTopComponent") + thisWindowCount);
        setToolTipText(NbBundle.getMessage(HQLEditorTopComponent.class, "HINT_HQLEditorTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));

        sqlToggleButton.setSelected(true);
    }

    public void fillHibernateConfigurations(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DataObject dO = node.getCookie(DataObject.class);
        if (dO instanceof HibernateCfgDataObject) {

            Project enclosingProject = FileOwnerQuery.getOwner(dO.getPrimaryFile());
            env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            if (env == null) {
                logger.warning("HiberEnv is not found in enclosing project.");
                return;
            }
            List<FileObject> configFileObjects = env.getAllHibernateConfigFileObjects();
            for (FileObject configFileObject : configFileObjects) {
                try {
                    HibernateCfgDataObject hibernateCfgDataObject = (HibernateCfgDataObject) DataObject.find(configFileObject);
                    String configName = hibernateCfgDataObject.getHibernateConfiguration().getSessionFactory().getAttributeValue("name");
                    if (configName == null || configName.equals("")) {
                        configName = configFileObject.getName();
                    }
                    hibernateConfigMap.put(configName, configFileObject);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            hibernateConfigurationComboBox.setModel(new DefaultComboBoxModel(hibernateConfigMap.keySet().toArray()));
            HibernateConfiguration config = ((HibernateCfgDataObject) dO).getHibernateConfiguration();
            String selectedConfigName = config.getSessionFactory().getAttributeValue("name");
            if (selectedConfigName == null || selectedConfigName.equals("")) {
                selectedConfigName = dO.getPrimaryFile().getName();
            }
            hibernateConfigurationComboBox.setSelectedItem(selectedConfigName);

        } else {
            //TODO Don't know whether this case will actually arise..
        }

    }

    public void setResult(HQLResult result) {
        if (result.getExceptions().size() == 0) {
            // logger.info(r.getQueryResults().toString());
            switchToResultView();
            StringBuilder strBuffer = new StringBuilder();
            String space = " ", separator = "; ";
            strBuffer.append(result.getUpdateOrDeleteResult());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(HQLEditorTopComponent.class, "queryUpdatedOrDeleted"));
            strBuffer.append(separator);

            strBuffer.append(space);
            strBuffer.append(result.getQueryResults().size());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(HQLEditorTopComponent.class, "rowsSelected"));

            setStatus(strBuffer.toString());

            Vector<String> tableHeaders = new Vector<String>();
            Vector<Vector> tableData = new Vector<Vector>();

            if (result.getQueryResults().size() != 0) {
                // Construct the table headers//

                Object firstObject = result.getQueryResults().get(0);
                for (java.lang.reflect.Method m : firstObject.getClass().getDeclaredMethods()) {
                    String methodName = m.getName();
                    if (methodName.startsWith("get")) {
                        if (!tableHeaders.contains(methodName)) {
                            tableHeaders.add(m.getName().substring(3));
                        }
                    }
                }
                for (Object o : result.getQueryResults()) {
                    try {
                        Vector<Object> oneRow = new Vector<Object>();
                        for (java.lang.reflect.Method m : o.getClass().getDeclaredMethods()) {
                            String methodName = m.getName();
                            if (methodName.startsWith("get")) {
                                oneRow.add(m.invoke(o, new Object[]{}));
                            }
                        }
                        tableData.add(oneRow);
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (SecurityException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }

            }
            resultsTable.setModel(new DefaultTableModel(tableData, tableHeaders));


        } else {
            logger.info("HQL query execution resulted in following " + result.getExceptions().size() + " errors.");

            switchToErrorView();
            setStatus(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionError"));
            errorTextArea.setText("");
            for (Throwable t : result.getExceptions()) {
                StringWriter sWriter = new StringWriter();
                PrintWriter pWriter = new PrintWriter(sWriter);
                t.printStackTrace(pWriter);
                errorTextArea.append(sWriter.toString());
            }
            logger.info(errorTextArea.getText());
        }
        runHQLButton.setEnabled(true);

    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        sessionLabel = new javax.swing.JLabel();
        hibernateConfigurationComboBox = new javax.swing.JComboBox();
        toolbarSeparator = new javax.swing.JToolBar.Separator();
        runHQLButton = new javax.swing.JButton();
        toolbarSeparator1 = new javax.swing.JToolBar.Separator();
        splitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        hqlEditor = new javax.swing.JEditorPane();
        containerPanel = new javax.swing.JPanel();
        toolBar2 = new javax.swing.JToolBar();
        resultToggleButton = new javax.swing.JToggleButton();
        sqlToggleButton = new javax.swing.JToggleButton();
        executionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sqlEditorPane = new javax.swing.JEditorPane();
        resultContainerPanel = new javax.swing.JPanel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        resultsOrErrorPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(sessionLabel, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.sessionLabel.text")); // NOI18N
        toolBar.add(sessionLabel);

        toolBar.add(hibernateConfigurationComboBox);
        toolBar.add(toolbarSeparator);

        runHQLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/hibernate/hqleditor/ui/resources/runsql16X16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runHQLButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.runHQLButton.text")); // NOI18N
        runHQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "runHQLQueryButtonToolTip")); // NOI18N
        runHQLButton.setFocusable(false);
        runHQLButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runHQLButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runHQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runHQLButtonActionPerformed(evt);
            }
        });
        toolBar.add(runHQLButton);

        toolbarSeparator1.setSeparatorSize(new java.awt.Dimension(300, 10));
        toolBar.add(toolbarSeparator1);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(7);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        hqlEditor.setContentType("text/x-hql");
        jScrollPane1.setViewportView(hqlEditor);

        splitPane.setTopComponent(jScrollPane1);

        toolBar2.setFloatable(false);
        toolBar2.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(resultToggleButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.resultToggleButton.text")); // NOI18N
        resultToggleButton.setFocusable(false);
        resultToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resultToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resultToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                resultToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(resultToggleButton);

        org.openide.awt.Mnemonics.setLocalizedText(sqlToggleButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.sqlToggleButton.text")); // NOI18N
        sqlToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sqlToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sqlToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sqlToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(sqlToggleButton);

        executionPanel.setLayout(new java.awt.CardLayout());

        sqlEditorPane.setEditable(false);
        jScrollPane2.setViewportView(sqlEditorPane);

        executionPanel.add(jScrollPane2, "card2");

        resultContainerPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.statusLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 607, Short.MAX_VALUE)
            .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelLayout.createSequentialGroup()
                    .add(0, 303, Short.MAX_VALUE)
                    .add(statusLabel)
                    .add(0, 304, Short.MAX_VALUE)))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
            .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelLayout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(statusLabel)
                    .add(0, 0, Short.MAX_VALUE)))
        );

        resultContainerPanel.add(statusPanel, java.awt.BorderLayout.NORTH);

        resultsOrErrorPanel.setLayout(new java.awt.CardLayout());

        errorTextArea.setColumns(20);
        errorTextArea.setForeground(new java.awt.Color(255, 102, 102));
        errorTextArea.setRows(5);
        jScrollPane4.setViewportView(errorTextArea);

        resultsOrErrorPanel.add(jScrollPane4, "card2");

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(resultsTable);

        resultsOrErrorPanel.add(jScrollPane3, "card3");

        resultContainerPanel.add(resultsOrErrorPanel, java.awt.BorderLayout.CENTER);

        executionPanel.add(resultContainerPanel, "card4");

        org.jdesktop.layout.GroupLayout containerPanelLayout = new org.jdesktop.layout.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
            .add(executionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containerPanelLayout.createSequentialGroup()
                .add(toolBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(executionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
        );

        splitPane.setRightComponent(containerPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void resultToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_resultToggleButtonItemStateChanged
    if (resultToggleButton.isSelected()) {
        ((CardLayout) (executionPanel.getLayout())).last(executionPanel);
        sqlToggleButton.setSelected(false);
    }
}//GEN-LAST:event_resultToggleButtonItemStateChanged

private void sqlToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sqlToggleButtonItemStateChanged
    if (sqlToggleButton.isSelected()) {
        ((CardLayout) (executionPanel.getLayout())).first(executionPanel);
        resultToggleButton.setSelected(false);
    }
}//GEN-LAST:event_sqlToggleButtonItemStateChanged

private void runHQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runHQLButtonActionPerformed
    runHQLButton.setEnabled(false);
    try {
        FileObject selectedConfigFile = (FileObject) hibernateConfigMap.get(hibernateConfigurationComboBox.getSelectedItem());

        controller.executeHQLQuery(hqlEditor.getText(), selectedConfigFile);
    } catch (Exception ex) {
        Exceptions.printStackTrace(ex);                                            
    }
}//GEN-LAST:event_runHQLButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JPanel executionPanel;
    private javax.swing.JComboBox hibernateConfigurationComboBox;
    private javax.swing.JEditorPane hqlEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel resultContainerPanel;
    private javax.swing.JToggleButton resultToggleButton;
    private javax.swing.JPanel resultsOrErrorPanel;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton runHQLButton;
    private javax.swing.JLabel sessionLabel;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JEditorPane sqlEditorPane;
    private javax.swing.JToggleButton sqlToggleButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar toolBar2;
    private javax.swing.JToolBar.Separator toolbarSeparator;
    private javax.swing.JToolBar.Separator toolbarSeparator1;
    // End of variables declaration//GEN-END:variables
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        windowCounts.remove(thisWindowCount);
    }

    private void switchToResultView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).last(resultsOrErrorPanel);
    }

    private void switchToErrorView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).first(resultsOrErrorPanel);
    }
}
