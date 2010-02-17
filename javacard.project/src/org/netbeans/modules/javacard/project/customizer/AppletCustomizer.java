/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import com.sun.javacard.AID;
import com.sun.javacard.filemodels.*;
import java.awt.Component;
import java.awt.EventQueue;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.validation.api.Problems;
import org.openide.DialogDescriptor;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.project.ui.CheckboxListView;
import org.netbeans.modules.javacard.project.ui.FileModelFactory;
import org.netbeans.modules.javacard.project.ui.NodeCheckObserver;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public class AppletCustomizer extends AllClassesOfTypeExplorerPanel implements NodeCheckObserver, PropertyChangeListener, ActionListener, ChangeListener, DocumentListener {

    private AppletProjectProperties props;
    private Category category;

    public AppletCustomizer(AppletProjectProperties props, Category category) {
        super("javacard.framework.Applet", NbBundle.getMessage(AppletCustomizer.class,
                "APPLETS_WAIT_MSG")); //NOI18N
        this.category = category;
        initComponents();
        instancesList.setCellRenderer(HtmlRenderer.createRenderer());
        countLabel.setText("  ");
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                Mnemonics.setLocalizedText((AbstractButton) c, ((AbstractButton) c).getText());
            } else if (c instanceof JLabel) {
                Mnemonics.setLocalizedText((JLabel) c, ((JLabel) c).getText());
            }
        }
        ((CheckboxListView) appletsList).setCheckboxesVisible(false);
        ((CheckboxListView) appletsList).setNodeCheckObserver(this);
        if (props != null) {
            setProperties(props);
        }
        mgr.addPropertyChangeListener(this);
        unlockPanel.addActionListener(l);
        unlockPanel.setVisible(false);
        aIDPanel1.addChangeListener(this);
        displayNameField.getDocument().addDocumentListener(this);
        instancesList.setSelectionModel(new NullSelectionModel());
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.SettingApplets"); //NOI18N
    }
    AppletXmlModel fromFile;
    boolean locked;

    @Override
    protected void onSearchBegun() {
        //Build model from project properties information
        JCProject p = props.getProject();
        FileObject appletXml = p.getProjectDirectory().getFileObject(JCConstants.APPLET_DESCRIPTOR_PATH); //NOI18N
        AppletXmlParseErrorHandler aHandler = new AppletXmlParseErrorHandler();
        if (appletXml != null) {
            try {
                AppletXmlModel mdl;
                mdl = FileModelFactory.appletXmlModel(appletXml, aHandler);
                if (mdl == null) {
                    mdl = new AppletXmlModel();
                    mdl.close();
                }
                synchronized (this) {
                    this.fromFile = mdl;
                }
                if (mdl.hasUnknownTags()) {
                    //Need to disable component on the event queue
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            locked = true;
                            ((CheckboxListView) appletsList).setCheckboxesEnabled(false);
                            unlockPanel.setVisible(true);
                            repaint();
                        }
                    });
                }
                if (mdl.hasUnknownTags()) {
                    unlockPanel.setVisible(true);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            fromFile = new AppletXmlModel();
            fromFile.close();
            category.setErrorMessage(NbBundle.getMessage(AppletCustomizer.class,
                    "WARN_APPLET_XML_NOT_FOUND")); //NOI18N
        }
        FileObject deploymentXml = p.getProjectDirectory().getFileObject(JCConstants.DEPLOYMENT_XML_PATH);
        DeploymentXmlModel dmdl;
        if (deploymentXml != null) {
            DeploymentParseErrorHandler handler = new DeploymentParseErrorHandler();
            try {
                dmdl = FileModelFactory.deploymentXmlModel(deploymentXml, handler);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                dmdl = new DeploymentXmlModel();
                dmdl.close();
            }
            String badAids = handler.getBadAidsString();
            if (badAids != null) {
                category.setErrorMessage(NbBundle.getMessage(AppletCustomizer.class,
                        "BAD_AIDS_FOUND_IN_DEPLOYMENT", badAids));
            }
        } else {
            dmdl = new DeploymentXmlModel();
            dmdl.close();
            category.setErrorMessage(NbBundle.getMessage(AppletCustomizer.class,
                    "WARN_DEPLOYMENT_XML_NOT_FOUND")); //NOI18N
        }
        synchronized (this) {
            deployModelFromFile = dmdl;
        }
    }
    private DeploymentXmlModel deployModelFromFile;

    @Override
    protected void onSearchCompleted() {
        ((CheckboxListView) appletsList).setCheckboxesVisible(true);
        appletsLabel.setEnabled(true);
        Node[] n = mgr.getRootContext().getChildren().getNodes(true);
        if (n.length == 0) {
            category.setErrorMessage(NbBundle.getMessage(AppletCustomizer.class,
                    "MSG_NO_APPLETS")); //NOI18N
        }
        AppletXmlModel m;
        synchronized (this) {
            m = fromFile;
        }
        props.setAppletXmlFromFile(m == null ? new AppletXmlModel() : m);
        if (!m.isError()) {
            FileModelFactory.writeTo(m, n);
            FileModelFactory.writeTo(deployModelFromFile, n);
        }
        AID aid = props.getDefaultApplet();
        for (Node nn : n) {
            if (aid != null) {
                AID nodesAID = (AID) nn.getValue(FileModelFactory.APPLET_AID);
                if (aid.equals(nodesAID)) {
                    nn.setValue(FileModelFactory.DEFAULT, Boolean.TRUE);
                }
            }
            if (m.containsClass(nn.getLookup().lookup(String.class))) {
                nn.setValue(CheckboxListView.SELECTED, Boolean.TRUE);
                checkedNodes.add(nn);
            }
        }

        if (isShowing()) {
            appletsList.requestFocus();
        }
        propertyChange(null);
    }

    private void updatePropsUiModel() {
        if (locked) {
            return;
        }
        AppletXmlModel m = getModelFromUI();
        String problem = m.getProblem();
        setProblem(problem);
        if (problem == null) {
            props.setAppletXmlFromUI(m);
        }
    }

    synchronized AppletXmlModel getModelFromDisk() {
        return fromFile;
    }

    synchronized DeploymentXmlModel getDeployModelFromDisk() {
        return deployModelFromFile;
    }

    DeploymentXmlModel getDeployModelFromUI() {
        DeploymentXmlModel result = FileModelFactory.deploymentXmlModel(
                mgr.getRootContext().getChildren().getNodes(true));
        return result;
    }

    AppletXmlModel getModelFromUI() {
        Node[] n = mgr.getRootContext().getChildren().getNodes(true);
        return FileModelFactory.appletXmlModel(n);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        aIDPanel1 = new org.netbeans.modules.javacard.common.AIDPanel();
        unlockPanel = new org.netbeans.modules.javacard.project.customizer.AppletEditorUnlockPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        countLabel = new javax.swing.JLabel();
        appletsList = new CheckboxListView();
        appletsLabel = new javax.swing.JLabel();
        instancesLabel = new javax.swing.JLabel();
        instancesPane = new javax.swing.JScrollPane();
        instancesList = new javax.swing.JList();
        customizeButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(360, 400));
        setPreferredSize(new java.awt.Dimension(360, 400));
        setLayout(new java.awt.GridBagLayout());

        aIDPanel1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(aIDPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(unlockPanel, gridBagConstraints);

        displayNameLabel.setLabelFor(displayNameField);
        displayNameLabel.setText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.displayNameLabel.text")); // NOI18N
        displayNameLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(displayNameLabel, gridBagConstraints);

        displayNameField.setText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.displayNameField.text")); // NOI18N
        displayNameField.setToolTipText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.displayNameField.toolTipText", new Object[] {})); // NOI18N
        displayNameField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(displayNameField, gridBagConstraints);

        countLabel.setText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.countLabel.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(countLabel, gridBagConstraints);

        appletsList.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        appletsList.setToolTipText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.appletsList.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.87;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(appletsList, gridBagConstraints);

        appletsLabel.setLabelFor(appletsList);
        appletsLabel.setText(NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.appletsLabel.text")); // NOI18N
        appletsLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(appletsLabel, gridBagConstraints);

        instancesLabel.setLabelFor(instancesList);
        instancesLabel.setText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.instancesLabel.text", new Object[] {})); // NOI18N
        instancesLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(instancesLabel, gridBagConstraints);

        instancesList.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        instancesList.setToolTipText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.instancesList.toolTipText", new Object[] {})); // NOI18N
        instancesList.setEnabled(false);
        instancesPane.setViewportView(instancesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.13;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(instancesPane, gridBagConstraints);

        customizeButton.setText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.customizeButton.text", new Object[] {})); // NOI18N
        customizeButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppletCustomizer.class, "AppletCustomizer.customizeButton.toolTipText", new Object[] {})); // NOI18N
        customizeButton.setEnabled(false);
        customizeButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(customizeButton, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == customizeButton) {
            AppletCustomizer.this.onCustomizeDeployment(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void onCustomizeDeployment(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onCustomizeDeployment
        Node n = mgr.getSelectedNodes()[0];
        final CustomizeDeploymentPanel panel = new CustomizeDeploymentPanel(n);

        String name = n.getDisplayName();

        class L extends ValidationListener implements ChangeListener {

            @Override
            protected boolean validate(Problems prblms) {
                String problem = panel.getProblem();
                boolean result = problem == null;
                if (problem != null) {
                    prblms.add(problem);
                }
                return result;
            }

            public void stateChanged(ChangeEvent e) {
                validate();
            }
        }
        L validator = new L();

        ChangeListener cl = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (panel.getProblem() == null) {
                    props.setDeploymentXmlUIModel(getDeployModelFromUI());
                }
            }
        };
        panel.addChangeListener(cl);
        panel.addChangeListener(validator);
        ValidationGroup gp = ValidationGroup.create();
        DialogBuilder db = new DialogBuilder(AppletCustomizer.class).setModal(true).setTitle(
                NbBundle.getMessage(CustomizeDeploymentPanel.class,
                "TITLE_CUSTOMIZE_DEPLOYMENT", name)).
                setContent(panel).setValidationGroup(gp);
        gp.add(validator);

        if (db.showDialog(DialogDescriptor.OK_OPTION)) {
            //XXX probably same exception w/ using ValidationPanel
            panel.saveChanges();
            propertyChange(null);

        }
    }//GEN-LAST:event_onCustomizeDeployment
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.javacard.common.AIDPanel aIDPanel1;
    private javax.swing.JLabel appletsLabel;
    private javax.swing.JScrollPane appletsList;
    private javax.swing.JLabel countLabel;
    private javax.swing.JButton customizeButton;
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JLabel instancesLabel;
    private javax.swing.JList instancesList;
    private javax.swing.JScrollPane instancesPane;
    private org.netbeans.modules.javacard.project.customizer.AppletEditorUnlockPanel unlockPanel;
    // End of variables declaration//GEN-END:variables

    private void setProperties(AppletProjectProperties props) {
        this.props = props;
        setClassPath(props.getProject().getSourceClassPath());
    }
    private final Set<Node> checkedNodes = new HashSet<Node>();

    public void onNodeChecked(Node node) {
        checkedNodes.add(node);
        DeploymentXmlAppletEntry entry = (DeploymentXmlAppletEntry) node.getValue(FileModelFactory.DEPLOYMENT_ENTRY);
        if (entry == null) {
            entry = new DeploymentXmlAppletEntry();
        }
        DeploymentXmlInstanceEntry instance = new DeploymentXmlInstanceEntry();
        AID aid = (AID) node.getValue(FileModelFactory.APPLET_AID);
        if (aid == null) {
            aIDPanel1.setClassFqn(node.getLookup().lookup(String.class));
            aIDPanel1.generateAid();
            aid = aIDPanel1.getAID();
            node.setValue(FileModelFactory.APPLET_AID, aid);
        }
        instance.setInstanceAID(aid.increment());
        if (entry.isEmpty()) {
            entry.add(instance);
        }
        propertyChange(null);
    }

    public void onNodeUnchecked(Node node) {
        checkedNodes.remove(node);
        propertyChange(null);
    }
    boolean inUpdateFields;

    public void propertyChange(PropertyChangeEvent evt) {
        inUpdateFields = true;
        try {
            Node[] n = mgr.getSelectedNodes();
            boolean enable = n != null &&
                    n.length == 1 &&
                    checkedNodes.contains(n[0]) &&
                    n[0].getLookup().lookup(String.class) != null && !locked;
            aIDPanel1.setEnabled(enable);
            displayNameLabel.setEnabled(enable);
            displayNameField.setEnabled(enable);
            if (!enable) {
                countLabel.setText(" "); //NOI18N
            }
            instancesList.setEnabled(true);
            instancesLabel.setEnabled(true);
            if (n != null && n.length == 1) {
                customizeButton.setEnabled(enable);
                String displayName = (String) n[0].getValue(FileModelFactory.DISPLAY_NAME);
                displayNameField.setText(displayName);
                DeploymentXmlAppletEntry instances = (DeploymentXmlAppletEntry) n[0].getValue(FileModelFactory.DEPLOYMENT_ENTRY);
                DefaultListModel instanceListModel = new DefaultListModel();
                if (instances != null) {
                    for (DeploymentXmlInstanceEntry e : instances.getData()) {
                        AID aid = e.getInstanceAID();
                        String params = e.getDeploymentParams();
                        StringBuilder sb = new StringBuilder("<html>"); //NOI18N
                        if (aid == null) {
                            sb.append("<s><font color='!nb.errorForeground'>"); //NOI18N
                            sb.append(NbBundle.getMessage(AppletCustomizer.class,
                                    "LBL_INVALID_AID")); //NOI18N
                            sb.append("</s></font>"); //NOI18N
                        } else {
                            sb.append(aid.toString());
                        }
                        if (params != null && params.length() > 0) {
                            sb.append("<font color='!controlShadow'>"); //NOI18N
                            sb.append(" ("); //NOI18N
                            sb.append(params);
                            sb.append(')'); //NOI18N
                        }
                        instanceListModel.addElement(sb.toString());
                    }
                }
                instancesList.setModel(instanceListModel);

                String classname = n[0].getLookup().lookup(String.class);
                if (instances == null) {
                    countLabel.setText(NbBundle.getMessage(AppletCustomizer.class,
                            "NO_INSTANCES_CONFIGURED", classname)); //NOI18N
                } else {
                    int ct = instances.getData().size();
                    countLabel.setText(NbBundle.getMessage(AppletCustomizer.class,
                            "NUMBER_OF_INSTANCES", ct)); //NOI18N
                }

                if (n[0].getValue(FileModelFactory.APPLET_AID) instanceof AID) {
                    AID aid = (AID) n[0].getValue(FileModelFactory.APPLET_AID);
                    aIDPanel1.setAID(aid);
                } else if (n[0].getLookup().lookup(String.class) != null) {
                    aIDPanel1.setClassFqn(n[0].getLookup().lookup(String.class));
                    aIDPanel1.generateAid();
                    AID aid = aIDPanel1.getAID();
                    if (aid != null) {
                        n[0].setValue(FileModelFactory.APPLET_AID, aid);
                    }
                }
            } else {
                customizeButton.setEnabled(false);
            }
        } finally {
            props.setDeploymentXmlUIModel(getDeployModelFromUI());
            inUpdateFields =
                    false;
            String problem = getModelFromUI().getProblem();
            if (problem == null) {
                problem = getDeployModelFromUI().getProblem();
            }

            setProblem(problem);
        }

    }

    L l = new L();
    private class L implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            locked = false;
            ((CheckboxListView) appletsList).setCheckboxesEnabled(true);
            unlockPanel.setVisible(false);
            appletsList.requestFocus();
            propertyChange(null);
            invalidate();
            revalidate();
            repaint();
        }
    }

    public void stateChanged(ChangeEvent e) {
        if (inUpdateFields) {
            return;
        }

        AID aid = aIDPanel1.getAID();
        String problem = null;
        if (aid == null) {
            problem = aIDPanel1.getProblem();
        } else {
            Node[] n = mgr.getSelectedNodes();
            if (n != null && n.length == 1) {
                n[0].setValue(FileModelFactory.APPLET_AID, aid);
                DeploymentXmlAppletEntry entry = (DeploymentXmlAppletEntry) n[0].getValue(FileModelFactory.DEPLOYMENT_ENTRY);
                entry.setAppletAid(aid);
            }

        }
        if (problem != null) {
            category.setValid(false);
            category.setErrorMessage(problem);
        } else {
            category.setValid(true);
            category.setErrorMessage(null);
            updatePropsUiModel();

        }


    }

    public boolean isProblem() {
        return category.isValid();
    }

    public void setProblem(String problem) {
        boolean bad = problem == null ? false : problem.trim().length() == 0 ? false : true;
        category.setValid(!bad);
        if (bad) {
            category.setErrorMessage(problem);
        } else {
            category.setErrorMessage("");
        }

    }

    public void insertUpdate(DocumentEvent e) {
        displayNameChange();
    }

    public void removeUpdate(DocumentEvent e) {
        displayNameChange();
    }

    public void changedUpdate(DocumentEvent e) {
        displayNameChange();
    }

    private void displayNameChange() {
        if (inUpdateFields) {
            return;
        }

        Node[] n = mgr.getSelectedNodes();
        if (n != null && n.length == 1) {
            String text = displayNameField.getText().trim();
            DeploymentXmlAppletEntry entry = (DeploymentXmlAppletEntry) n[0].getValue(FileModelFactory.DEPLOYMENT_ENTRY);
            if (entry != null && text.length() > 0) {
                entry.setDisplayNameHint(text);
            }
            n[0].setValue(FileModelFactory.DISPLAY_NAME, text);
            AppletXmlModel uiModel = getModelFromUI();
            String problem = uiModel.getProblem();
            setProblem(problem);
            if (problem == null || problem.trim().length() == 0) {
                updatePropsUiModel();
            }
        }
    }

    private class DeploymentParseErrorHandler implements ParseErrorHandler {

        Set<String> badAids = new HashSet<String>();

        public void handleError(IOException e) throws IOException {
            if (e.getCause() instanceof SAXException) {
                category.setErrorMessage(NbBundle.getMessage(DeploymentParseErrorHandler.class,
                        "MSG_INVALID_DEPLOYMENT_XML")); //NOI18N
            }
            Logger.getLogger(AppletXmlParseErrorHandler.class.getName()).log(
                    Level.INFO, "Error parsing applet.xml", e);
        }

        public void handleBadAIDError(IllegalArgumentException exc, String aidString) {
            badAids.add(aidString);
        }

        public void unrecognizedElementEncountered(String arg0) throws IOException {
            //do nothing
        }

        public String getBadAidsString() {
            if (badAids.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> it = badAids.iterator(); it.hasNext();) {
                String string = it.next();
                sb.append(string);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            return sb.toString();
        }
    }

    private class AppletXmlParseErrorHandler implements ParseErrorHandler {

        Set<String> badAids = new HashSet<String>();

        public void handleError(IOException e) throws IOException {
            //do nothing
            Logger.getLogger(AppletXmlParseErrorHandler.class.getName()).log(
                    Level.INFO, "Error parsing applet.xml", e);
        }

        public void handleBadAIDError(IllegalArgumentException arg0, String arg1) {
            //do nothing
        }

        public void unrecognizedElementEncountered(String arg0) throws IOException {
            //do nothing, already handled by model
        }

        public String getBadAidsString() {
            if (badAids.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> it = badAids.iterator(); it.hasNext();) {
                String string = it.next();
                sb.append(string);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            return sb.toString();
        }
    }
}
