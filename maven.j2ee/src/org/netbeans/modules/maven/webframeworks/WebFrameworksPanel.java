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
import java.text.MessageFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
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
    private List newExtenders = new LinkedList();
    private List usedFrameworks = new LinkedList();
    private Map<WebFrameworkProvider, WebModuleExtender> extenders = new IdentityHashMap<WebFrameworkProvider, WebModuleExtender>();
    List<String> addedFrameworks = new LinkedList<String>();

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
        
        initFrameworksList();        
    }

    void applyChanges() {
            // extend project with selected frameworks
            if (newExtenders != null) {
                // #120108
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
                        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
                        for (int i = 0; i < newExtenders.size(); i++) {
                            ((WebModuleExtender) newExtenders.get(i)).extend(webModule);
                        }
                        
                        // ui logging of the added frameworks
                        if ((addedFrameworks != null) && (addedFrameworks.size() > 0)) {
                            LogRecord logRecord = new LogRecord(Level.INFO, "UI_WEB_PROJECT_FRAMEWORK_ADDED");  //NOI18N
                            logRecord.setLoggerName(UI_LOGGER_NAME); //NOI18N
                            logRecord.setResourceBundle(NbBundle.getBundle(WebFrameworksPanel.class));

                            logRecord.setParameters(addedFrameworks.toArray());
                            UI_LOGGER.log(logRecord);
                        }
//                    }
//                });
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
                ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());
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
        jSeparator1 = new javax.swing.JSeparator();
        jPanelConfig = new javax.swing.JPanel();
        jLabelConfig = new javax.swing.JLabel();

        jLabelFrameworks.setLabelFor(jListFrameworks);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFrameworks, org.openide.util.NbBundle.getMessage(WebFrameworksPanel.class, "LBL_UsedFrameworks")); // NOI18N

        jScrollPane1.setViewportView(jListFrameworks);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAdd, org.openide.util.NbBundle.getMessage(WebFrameworksPanel.class, "LBL_AddFramework")); // NOI18N
        jButtonAdd.setActionCommand("Add...");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        jLabelConfig.setLabelFor(jPanelConfig);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonAdd))
            .add(layout.createSequentialGroup()
                .add(jLabelFrameworks)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jLabelConfig)
                .addContainerGap(409, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelFrameworks)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonAdd)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(131, 131, 131)
                        .add(jLabelConfig))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanelConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))))
        );

        jButtonAdd.getAccessibleContext().setAccessibleDescription("null");
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
            List newFrameworks = panel.getSelectedFrameworks();
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            for(int i = 0; i < newFrameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) newFrameworks.get(i);
                if (!((DefaultListModel) jListFrameworks.getModel()).contains(framework.getName()))
                    ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());

                boolean added = false;
                if (usedFrameworks.size() == 0) {
                    usedFrameworks.add(framework);
                    added = true;
                }
                else
                    for (int j = 0; j < usedFrameworks.size(); j++)
                        if (!((WebFrameworkProvider) usedFrameworks.get(j)).getName().equals(framework.getName())) {
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
                        addedFrameworks.add(framework.getName());
                    }
                }

                jListFrameworks.setSelectedValue(framework.getName(), true);
            }
            
//            uiProperties.setNewExtenders(newExtenders);
//            uiProperties.setNewFrameworksNames(addedFrameworks);
        }
        
        if (WebFrameworks.getFrameworks().size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }//GEN-LAST:event_jButtonAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelFrameworks;
    private javax.swing.JList jListFrameworks;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        String frameworkName = (String) jListFrameworks.getSelectedValue();
	int selectedIndex = jListFrameworks.getSelectedIndex();
	if (selectedIndex != -1) {	
	    WebFrameworkProvider framework = (WebFrameworkProvider) usedFrameworks.get(selectedIndex);
	    if (framework.getName().equals(frameworkName)) {
                WebModuleExtender extender = extenders.get(framework);
		if (extender != null) {
		    String message = MessageFormat.format(NbBundle.getMessage(WebFrameworksPanel.class, "LBL_FrameworkConfiguration"), new Object[] {frameworkName}); //NOI18N
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
		} else {
		    hideConfigPanel();
		}
            }
	} else
	    hideConfigPanel();
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
