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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.NbBundle;


/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class CallEjbPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "CallEjbPanel_isValid"; //NOI18N

    private final WorkingCopy workingCopy;
    private final Set<String> refNameSet;
    private final NodeDisplayPanel nodeDisplayPanel;
    private final ServiceLocatorStrategyPanel slPanel;
    private final NodeAcceptor nodeAcceptor;
    private final Project project;
    private final TypeElement beanClass;
    private final FileObject srcFile;
    
    /** Creates new form CallEjbPanel */
    public CallEjbPanel(WorkingCopy workingCopy, Node rootNode, String lastLocator, TypeElement beanClass) {
        initComponents();
        this.workingCopy = workingCopy;
        this.srcFile= workingCopy.getFileObject();
        this.project = FileOwnerQuery.getOwner(srcFile);
        this.beanClass = beanClass;
        this.nodeAcceptor = new NodeAcceptorImpl();
        
        Set<String> refNames = null;
        // This is working only for EJB project. Will need some enhancement in EnterpriseReferenceContainer API?
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(srcFile);
        if (ejbModule != null) {
            EjbJar ejbJar = null;
            try {
                ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            if (ejbJar != null) {
                EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                if (enterpriseBeans != null) {
                    Ejb[] ejbs = enterpriseBeans.getEjbs();
                    for (int i = 0; i < ejbs.length; i++) {
                        if (beanClass.getQualifiedName().contentEquals(ejbs[i].getEjbClass())) {
                            EjbRef[] ejbRefs = ejbs[i].getEjbRef();
                            EjbLocalRef[] ejbLocalRefs = ejbs[i].getEjbLocalRef();
                            refNames = new HashSet<String>(ejbRefs.length + ejbLocalRefs.length);
                            for (int j = 0; j < ejbRefs.length; j++) {
                                this.refNameSet.add(ejbRefs[j].getEjbRefName());
                            }
                            for (int j = 0; j < ejbLocalRefs.length; j++) {
                                this.refNameSet.add(ejbLocalRefs[j].getEjbRefName());
                            }
                        }
                    }
                }
            }
        }
        this.refNameSet = (refNames == null ? Collections.<String>emptySet() : refNames);
        setErrorFieldColor(true);
        nodeDisplayPanel = new NodeDisplayPanel(rootNode);
        nodeDisplayPanel.setBorder(new EtchedBorder());
        displayPanel.add(nodeDisplayPanel);
        nodeDisplayPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                Node[] nodes = nodeDisplayPanel.getSelectedNodes();
                if (nodes.length == 0) {
                    return;
                }
                EjbReference ejbReference = (EjbReference) nodes[0].getCookie(EjbReference.class);
                if (ejbReference != null) {
                    generateName(ejbReference, remoteRadioButton.isSelected(), nodes[0]);
                }
                validateReferences();
            }
        });
        referenceNameTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent) {
                validateReferences();
            }
        });
        
        slPanel = new ServiceLocatorStrategyPanel(lastLocator);
        slPanel.getUnreferencedServiceLocator().addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                validateReferences();
            }
        });
        slPanel.getClassName().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
            public void changedUpdate(DocumentEvent documentEvent) {
                validateReferences();
            }
        });
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
        validateReferences();
    }
    
    public void disableServiceLocator() {
        serviceLocatorPanel.setVisible(false);
    }
    
    private void setErrorFieldColor(boolean error){
        if (error){
            Color color = UIManager.getColor("nb.errorForeground"); //NOI18N
            errorField.setForeground(color == null ? new Color(89, 79, 191) : color);
        } else {
            Color color = UIManager.getColor("nb.warningForeground"); //NOI18N
            errorField.setForeground(color == null ? Color.DARK_GRAY : color);
        }
    }
    
    private void setErrorMessage(String message) {
        setErrorFieldColor(true);
        setMessage(message);
    }
    
    private void setWarningMessage(String message){
        setErrorFieldColor(false);
        setMessage(message);
    }
    
    private void setMessage(String message){
        String messageCopy = (message == null ? " " : message);
        errorField.setText(messageCopy);
        errorField.setToolTipText(messageCopy);
        errorField.setCaretPosition(0);
    }
    
    public void validateReferences() {
        boolean nodeAccepted = nodeAcceptor.acceptNodes(nodeDisplayPanel.getSelectedNodes());
        if ((slPanel.getUnreferencedServiceLocator().isSelected() &&
                slPanel.getClassName().getText().trim().equals("")) ||
                !nodeAccepted) {
            firePropertyChange(IS_VALID, true, false);
        } else {
            firePropertyChange(IS_VALID, false, true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;

        intefaceButtonGroup = new javax.swing.ButtonGroup();
        serviceLocatorPanel = new javax.swing.JPanel();
        convertToRuntime = new javax.swing.JCheckBox();
        displayPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        errorField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        referenceNameTextField = new javax.swing.JTextField();
        localRadioButton = new javax.swing.JRadioButton();
        remoteRadioButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("ACS_CallEJBPanel"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("ACS_CallEJBPanel"));
        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        convertToRuntime.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ConvertToRuntimeMneumonic").charAt(0));
        convertToRuntime.setSelected(true);
        convertToRuntime.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ConvertToRuntime"));
        serviceLocatorPanel.add(convertToRuntime, java.awt.BorderLayout.SOUTH);
        convertToRuntime.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "ACSD_ConvertToRuntime"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(serviceLocatorPanel, gridBagConstraints);

        displayPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(displayPanel, gridBagConstraints);
        displayPanel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_DisplayPanel"));
        displayPanel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("ACSD_DisplayPanel"));

        jLabel1.setLabelFor(displayPanel);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ModuleMustBeInSameApplication"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(jLabel1, gridBagConstraints);

        errorField.setBackground(getBackground());
        errorField.setEditable(false);
        errorField.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(errorField, gridBagConstraints);
        errorField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("ACS_ErrorField"));
        errorField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("ACS_ErrorField"));

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_ReferenceName").charAt(0));
        jLabel2.setLabelFor(referenceNameTextField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferenceName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jLabel2, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencedInterface"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(referenceNameTextField, gridBagConstraints);
        referenceNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ReferenceName"));
        referenceNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_ReferenceName"));

        intefaceButtonGroup.add(localRadioButton);
        localRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_Local").charAt(0));
        localRadioButton.setSelected(true);
        localRadioButton.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_Local"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(localRadioButton, gridBagConstraints);
        localRadioButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_Local"));
        localRadioButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_Local"));

        intefaceButtonGroup.add(remoteRadioButton);
        remoteRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("MN_Remote").charAt(0));
        remoteRadioButton.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_Remote"));
        remoteRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                remoteRadioButtonItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(remoteRadioButton, gridBagConstraints);
        remoteRadioButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_Remote"));
        remoteRadioButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_Remote"));

    }// </editor-fold>//GEN-END:initComponents
    
    private void remoteRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_remoteRadioButtonItemStateChanged
        validateReferences();
    }//GEN-LAST:event_remoteRadioButtonItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox convertToRuntime;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JTextField errorField;
    private javax.swing.ButtonGroup intefaceButtonGroup;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JRadioButton localRadioButton;
    private javax.swing.JTextField referenceNameTextField;
    private javax.swing.JRadioButton remoteRadioButton;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables
    
    public boolean convertToRuntime() {
        return convertToRuntime.isSelected();
    }
    
    public Node getEjb() {
        Node[] selectedNodes = nodeDisplayPanel.getSelectedNodes();
        return selectedNodes.length > 0 ? selectedNodes[0] : null;
    }
    
    public String getServiceLocator() {
        return slPanel.classSelected();
    }
    
    public String getReferenceName() {
        return referenceNameTextField.getText();
    }
    
    public boolean isRemoteInterfaceSelected() {
        return remoteRadioButton.isSelected();
    }
    
    private void generateName(EjbReference ejbReference, boolean remote, Node selectedNode) {
        if (ejbReference.getClientJarTarget() == null) {
            referenceNameTextField.setText("");
            return;
        }
        String name = "";
        if (remote) {
            boolean targetIsJavaSE = Utils.isTargetJavaSE(workingCopy, beanClass);
            if (targetIsJavaSE && Utils.isJavaEE5orHigher(project)){
                //TODO: RETOUCHE elements in nodes
//                Feature ejbRefClass = (Feature) selectedNode.getLookup().lookup(Feature.class);
//                name = ejbRefClass.getName();
            } else if (targetIsJavaSE){
                name = ejbReference.createRef().getHome();
            } else {
                name = ejbReference.createRef().getEjbRefName();
            }
        } else {
            name = ejbReference.createLocalRef().getEjbRefName();
        }
        int uniquifier = 1;
        String newName = name;
        while (refNameSet.contains(newName)) {
            newName = name + String.valueOf(uniquifier++);
        }
        referenceNameTextField.setText(name);
    }
    
    private class NodeAcceptorImpl implements  NodeAcceptor {
        
        public NodeAcceptorImpl() {}
        
        public boolean acceptNodes(Node[] nodes) {
            setErrorMessage(" "); //NOI18N
            
            // no node selected
            if (nodes.length == 0) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_SelectOneEJB")); //NOI18N
                return false;
            }
            // more than one node selected
            if (nodes.length > 1) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_MoreEJBsSelected")); //NOI18N
                return false;
            }
            //TODO: RETOUCHE elements in nodes
//            Feature member = (Feature) nodes[0].getLookup().lookup(Feature.class);
//            // non-EJB node is selected
//            if (!(member instanceof JavaClass)) {
//                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NodeIsNotEJB")); //NOI18N
//                return false;
//            }
//            
//            if (((JavaClass) member).equals(CallEjbPanel.this.beanClass)) {
//                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallItself", ejbName)); //NOI18N
//                return false;
//            }
            
            // builded archive with beans is not available
            if (!hasJarArtifact()) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_EJBNotInDistributionArchive")); //NOI18N
                return false;
            }
            // node cannot act as EJB reference
            if (nodes[0].getCookie(EjbReference.class) == null) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencesNotSupported")); //NOI18N
                return false;
            }
            // check interfaces radiobuttons in context of selected node
            if (!acceptInterfaces(nodes)) {
                return false;
            }
            // validate reference name
            if (!validateRefName()) {
                return false;
            }
            
            // if local ref is used, modules must be in same module or J2EE application
            DataObject dataObject = (DataObject) nodes[0].getCookie(DataObject.class);
            if (dataObject == null) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NoSourcesForBean")); //NOI18N
                return false;
            }
            Project nodeProject = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            if (!isRemoteInterfaceSelected() &&
                    !nodeProject.equals(project) &&
                    !Utils.areInSameJ2EEApp(project, nodeProject)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NotInSameEarOrProject")); //NOI18N
                return false;
            }
            
            //AC cannot contain references to local beans
            if (!isRemoteInterfaceSelected() &&
                    Utils.isAppClient(project)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallLocalInAC")); //NOI18N
                return false;
            }

            //Unit tests or classes in a JSE project cannot contain references to local beans
            if (!isRemoteInterfaceSelected() &&
                    Utils.isTargetJavaSE(workingCopy, beanClass)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_CannotCallLocalInJSE")); //NOI18N
                return false;
            }
            
            // see #75876
            if (!Utils.isJavaEE5orHigher(project) && Utils.isJavaEE5orHigher(nodeProject)){
                setWarningMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_JEESpecificationLevelsDiffer")); //NOI18N
            }
            
            return true;
        }
        
        private boolean acceptInterfaces(Node[] nodes) {
            EjbReference ejbReference = (EjbReference) nodes[0].getCookie(EjbReference.class);
            if (ejbReference == null) {
                return false;
            }
            
            boolean shouldEnableLocal = ejbReference.supportsLocalInvocation();
            boolean shouldEnableRemote = ejbReference.supportsRemoteInvocation();
            localRadioButton.setEnabled(shouldEnableLocal);
            remoteRadioButton.setEnabled(shouldEnableRemote);
            if (!shouldEnableLocal && !shouldEnableRemote) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencesNotSupported")); //NOI18N
                return false;
            } else if (shouldEnableLocal && !shouldEnableRemote) {
                localRadioButton.setSelected(true);
            } else if (!shouldEnableLocal && shouldEnableRemote) {
                remoteRadioButton.setSelected(true);
            }
            setErrorMessage(" "); //NOI18N
            return true;
        }
        
        private boolean hasJarArtifact() {
            Project nodeProject = FileOwnerQuery.getOwner(srcFile);
            if (nodeProject.equals(project)) {
                // we're in same project, no need for output jar
                return true;
            }
            return AntArtifactQuery.findArtifactsByType(nodeProject, JavaProjectConstants.ARTIFACT_TYPE_JAR).length > 0;
        }
        
        private boolean validateRefName() {
            String refName = referenceNameTextField.getText();
            if (refNameSet.contains(refName)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "ERR_ReferenceNameExists", refName)); //NOI18N
                return false;
            }
            return true;
        }
    }
    
}
