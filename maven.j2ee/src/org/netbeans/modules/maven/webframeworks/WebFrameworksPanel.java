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

package org.netbeans.modules.maven.webframeworks;
import java.awt.Component;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 * 
 * @author mkleint
 */
public class WebFrameworksPanel extends javax.swing.JPanel implements ListSelectionListener {
    
    private final ProjectCustomizer.Category category;
    private Project project;
    private List<WebModuleExtender> newExtenders = new LinkedList<WebModuleExtender>();
    private List<WebFrameworkProvider> usedFrameworks = new LinkedList<WebFrameworkProvider>();
    private Map<WebFrameworkProvider, WebModuleExtender> extenders = new IdentityHashMap<WebFrameworkProvider, WebModuleExtender>();
    List<WebFrameworkProvider> addedFrameworks = new LinkedList<WebFrameworkProvider>();

    private ExtenderController controller = ExtenderController.create();
    private ModelHandle handle;
    // ui logging
    static final String UI_LOGGER_NAME = "org.netbeans.ui.web.project"; //NOI18N
    static final Logger UI_LOGGER = Logger.getLogger(UI_LOGGER_NAME);
    
    
    /** Creates new form WebFrameworksPanel */
    public WebFrameworksPanel(ProjectCustomizer.Category category, ModelHandle handle, Project prj) {
        this.category = category;
        project = prj;
        this.handle = handle;
        initComponents();
        btnRemoveAdded.setEnabled(false);
        
        initFrameworksList();

        jListFrameworks.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                WebFrameworkProvider prov = (WebFrameworkProvider)value;
                Component toRet = super.getListCellRendererComponent(list, prov.getName(), index, isSelected, cellHasFocus);
                if (toRet instanceof JLabel) {
                    JLabel lbl = (JLabel)toRet;
                    if (addedFrameworks.contains(prov)) {
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    } else {
                        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return toRet;
            }
        });
    }

    void applyChanges() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        for (int i = 0; i < newExtenders.size(); i++) {
            newExtenders.get(i).extend(webModule);
        }

        // ui logging of the added frameworks
        if ((addedFrameworks != null) && (addedFrameworks.size() > 0)) {
            LogRecord logRecord = new LogRecord(Level.INFO, "UI_WEB_PROJECT_FRAMEWORK_ADDED");  //NOI18N
            logRecord.setLoggerName(UI_LOGGER_NAME); //NOI18N
            logRecord.setResourceBundle(NbBundle.getBundle(WebFrameworksPanel.class));

            logRecord.setParameters(addedFrameworks.toArray());
            UI_LOGGER.log(logRecord);
        }
        
    }
    
    private void initFrameworksList() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());

        
        ExtenderController.Properties properties = controller.getProperties();
        String j2eeVersion = webModule.getJ2eePlatformVersion();
        properties.setProperty("j2eeLevel", j2eeVersion); // NOI18N
        
        jListFrameworks.setModel(new DefaultListModel());
        List frameworks = WebFrameworks.getFrameworks();
        for (int i = 0; i < frameworks.size(); i++) {
            WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
            if (framework.isInWebModule(webModule)) {
                usedFrameworks.add(framework);
                ((DefaultListModel) jListFrameworks.getModel()).addElement(framework);
                WebModuleExtender extender = framework.createWebModuleExtender(webModule, controller);
                extenders.put(framework, extender);
                extender.addChangeListener(new ExtenderListener(extender));
            }                
        }
        jListFrameworks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListFrameworks.addListSelectionListener(this);
        if (usedFrameworks.size() > 0)
            jListFrameworks.setSelectedIndex(0);
        
        if (frameworks.size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelFrameworks = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFrameworks = new javax.swing.JList();
        jButtonAdd = new javax.swing.JButton();
        btnRemoveAdded = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelConfig = new javax.swing.JPanel();
        jLabelConfig = new javax.swing.JLabel();

        jLabelFrameworks.setLabelFor(jListFrameworks);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFrameworks, org.openide.util.NbBundle.getMessage(WebFrameworksPanel.class, "LBL_UsedFrameworks")); // NOI18N

        jScrollPane1.setViewportView(jListFrameworks);
        jListFrameworks.getAccessibleContext().setAccessibleDescription("Used Frameworks");

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAdd, org.openide.util.NbBundle.getMessage(WebFrameworksPanel.class, "LBL_AddFramework")); // NOI18N
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveAdded, org.openide.util.NbBundle.getMessage(WebFrameworksPanel.class, "BTN_Remove")); // NOI18N
        btnRemoveAdded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAddedActionPerformed(evt);
            }
        });

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        jLabelConfig.setLabelFor(jPanelConfig);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelFrameworks)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jButtonAdd)
                    .add(btnRemoveAdded)))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(jLabelConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 368, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {btnRemoveAdded, jButtonAdd}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelFrameworks)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButtonAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemoveAdded))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
        );

        jButtonAdd.getAccessibleContext().setAccessibleDescription("Add Framework");
        btnRemoveAdded.getAccessibleContext().setAccessibleDescription("Remove framework");
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        AddFrameworkPanel panel = new AddFrameworkPanel(usedFrameworks);
        javax.swing.JPanel inner = new javax.swing.JPanel();
        inner.setLayout(new java.awt.GridBagLayout());
        inner.getAccessibleContext().setAccessibleDescription(panel.getAccessibleContext().getAccessibleDescription());
        inner.getAccessibleContext().setAccessibleName(panel.getAccessibleContext().getAccessibleName());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        inner.add(panel, gridBagConstraints);
 
        DialogDescriptor desc = new DialogDescriptor(inner, NbBundle.getMessage(WebFrameworksPanel.class, "LBL_SelectWebExtension_DialogTitle")); //NOI18N
        Object res = DialogDisplayer.getDefault().notify(desc);
        if (res.equals(NotifyDescriptor.YES_OPTION)) {
            List<WebFrameworkProvider> newFrameworks = panel.getSelectedFrameworks();
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            for (WebFrameworkProvider framework : newFrameworks) {
                if (!((DefaultListModel) jListFrameworks.getModel()).contains(framework))
                    ((DefaultListModel) jListFrameworks.getModel()).addElement(framework);

                boolean added = false;
                if (usedFrameworks.size() == 0) {
                    usedFrameworks.add(framework);
                    added = true;
                }
                else
                    for (int j = 0; j < usedFrameworks.size(); j++)
                        if (! usedFrameworks.get(j).getName().equals(framework.getName())) {
                            usedFrameworks.add(framework);
                            added = true;
                            break;
                        }
                
                if (added) {
                    WebModuleExtender extender = framework.createWebModuleExtender(wm, controller);
                    if (extender != null) {
                        extenders.put(framework, extender);
                        newExtenders.add(extender);
                        extender.addChangeListener(new ExtenderListener(extender));
                        addedFrameworks.add(framework);
                    }
                }
                jListFrameworks.setSelectedValue(framework, true);
            }
        }
        
        if (WebFrameworks.getFrameworks().size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void btnRemoveAddedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAddedActionPerformed
        WebFrameworkProvider framework = (WebFrameworkProvider) jListFrameworks.getSelectedValue();
        if (framework != null) {
            WebModuleExtender extender = extenders.get(framework);
            if (extender != null) {
                ((DefaultListModel)jListFrameworks.getModel()).removeElement(framework);
                addedFrameworks.remove(framework);
                newExtenders.remove(extender);
                extenders.remove(framework);
                usedFrameworks.remove(framework);
                boolean hasInvalid = false;
                for (WebModuleExtender ex : extenders.values()) {
                    if (!ex.isValid()) {
                        ex.update();
                        controller.setErrorMessage(null);
                        ex.isValid();
                        category.setValid(false);
                        category.setErrorMessage(controller.getErrorMessage());
                        hasInvalid = true;
                    }
                }
                if (!hasInvalid) {
                    if (!category.isValid()) {
                        category.setValid(true);
                        category.setErrorMessage(null);
                    }
                }
            }
        }
    }//GEN-LAST:event_btnRemoveAddedActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRemoveAdded;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelFrameworks;
    private javax.swing.JList jListFrameworks;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        btnRemoveAdded.setEnabled(false);
        WebFrameworkProvider framework = (WebFrameworkProvider) jListFrameworks.getSelectedValue();
        if (framework != null) {
            if (addedFrameworks.contains(framework)) {
                btnRemoveAdded.setEnabled(true);
            }
            WebModuleExtender extender = extenders.get(framework);
            if (extender != null) {
                String message = MessageFormat.format(NbBundle.getMessage(WebFrameworksPanel.class, "LBL_FrameworkConfiguration"), new Object[]{framework.getName()}); //NOI18N
                jLabelConfig.setText(message);
                jPanelConfig.removeAll();

                java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;

                jPanelConfig.add(extender.getComponent(), gridBagConstraints);
                jPanelConfig.repaint();
                jPanelConfig.revalidate();

                //always have the message according to the panel visible.
                extender.update();
                controller.setErrorMessage(null);
                extender.isValid();
                category.setErrorMessage(controller.getErrorMessage());
            } else {
                hideConfigPanel();
            }
        } else {
            hideConfigPanel();
        }
    }

    private final class ExtenderListener implements ChangeListener {
    
        private final WebModuleExtender extender;
        
        public ExtenderListener(WebModuleExtender extender) {
            this.extender = extender;
            extender.update();
            stateChanged(new ChangeEvent(this));
        }

        public void stateChanged(ChangeEvent e) {
            controller.setErrorMessage(null);
            if (extender.isValid()) {
                if (!category.isValid()) {
                    category.setValid(true);
                    category.setErrorMessage(null);
                }
            } else {
                category.setValid(false);
                category.setErrorMessage(controller.getErrorMessage());
            }
        }
    }
    
    private void hideConfigPanel() {
	jLabelConfig.setText(""); //NOI18N
	jPanelConfig.removeAll();
	jPanelConfig.repaint();
	jPanelConfig.revalidate();
    }
}
