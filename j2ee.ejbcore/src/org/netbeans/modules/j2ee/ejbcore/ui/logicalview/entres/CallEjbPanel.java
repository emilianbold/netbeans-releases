/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.javacore.api.JavaModel;
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

    private Set refNameSet;
   
    private NodeDisplayPanel nodeDisplayPanel;
    private ServiceLocatorStrategyPanel slPanel;
    private NodeAcceptor nodeAcceptor;
    private Project project;
    private JavaClass beanClass;
    private EjbJar ejbJar;
    private FileObject srcFile;
    
    /** Creates new form CallEjbPanel */
    public CallEjbPanel(Node rootNode, String lastLocator, JavaClass beanClass) {
        initComponents();
        
        srcFile= JavaModel.getFileObject(beanClass.getResource());
        this.project = FileOwnerQuery.getOwner(srcFile);
        this.beanClass = beanClass;
        this.refNameSet = Collections.EMPTY_SET;
        this.nodeAcceptor = new NodeAcceptorImpl();

        // This is working only for EJB project. Will need some enhancement in EnterpriseReferenceContainer API?
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(srcFile);;
        if (ejbModule != null) {
            try {
                this.ejbJar = DDProvider.getDefault().getDDRoot(ejbModule.getDeploymentDescriptor());
                Ejb[] ejbs = ejbJar.getEnterpriseBeans().getEjbs();
                for (int i = 0; i < ejbs.length; i++) {
                    if (ejbs[i].getEjbClass().equals(beanClass.getName())) {
                        EjbRef[] ejbRefs = ejbs[i].getEjbRef();
                        EjbLocalRef[] ejbLocalRefs = ejbs[i].getEjbLocalRef();
                        this.refNameSet = new HashSet(ejbRefs.length + ejbLocalRefs.length);
                        for (int j = 0; j < ejbRefs.length; j++) {
                            this.refNameSet.add(ejbRefs[j].getEjbRefName());
                        }
                        for (int j = 0; j < ejbLocalRefs.length; j++) {
                            this.refNameSet.add(ejbLocalRefs[j].getEjbRefName());
                        }
                    }
                }
            } catch (IOException e) {
                this.refNameSet = Collections.EMPTY_SET;
            }
        }
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        errorField.setForeground(c == null ? new Color(89, 79, 191) : c);
        nodeDisplayPanel = new NodeDisplayPanel(rootNode);
        nodeDisplayPanel.setBorder(new EtchedBorder());
        displayPanel.add(nodeDisplayPanel);
        String errorMessage = " ";
        nodeDisplayPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                Node[] nodes = nodeDisplayPanel.getSelectedNodes();
                if (nodes.length == 0) {
                    return;
                }
                EjbReference ejbReference = (EjbReference) nodes[0].getCookie(EjbReference.class);
                if (ejbReference != null) {
                    generateName(ejbReference, remoteRadioButton.isSelected());
                }
                validateReferences();
            }
        });
        referenceNameTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
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
            public void insertUpdate(DocumentEvent e) {
                validateReferences();
            }
            public void removeUpdate(DocumentEvent e) {
                validateReferences();
            }
            public void changedUpdate(DocumentEvent e) {
                validateReferences();
            }
        });
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
        validateReferences();
    }

    private void setErrorMessage(String message) {
        if (message == null) {
            message = " ";
        }
        errorField.setText(message);
        errorField.setToolTipText(message);
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

        intefaceButtonGroup.add(localRadioButton);
        localRadioButton.setSelected(true);
        localRadioButton.setText(org.openide.util.NbBundle.getMessage(CallEjbPanel.class, "LBL_Local"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(localRadioButton, gridBagConstraints);

        intefaceButtonGroup.add(remoteRadioButton);
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

    }
    // </editor-fold>//GEN-END:initComponents

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
    
    private void generateName(EjbReference ejbReference, boolean remote) {
        if (ejbReference.getClientJarTarget() == null) {
            referenceNameTextField.setText("");
            return;
        }
        String name;
        if (remote) {
            name = ejbReference.createRef().getEjbRefName();
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
            Feature member = (Feature) nodes[0].getLookup().lookup(Feature.class);
            // non-EJB node is selected
            if (!(member instanceof JavaClass)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NodeIsNotEJB")); //NOI18N
                return false;
            }
            
            if (((JavaClass) member).equals(CallEjbPanel.this.beanClass)) {
                setErrorMessage(""); //NOI18N
                return false;
            }
            
            // builded archive with beans is not available
            if (!hasJarArtifact(member)) {
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
            Project nodeProject = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            if (!isRemoteInterfaceSelected() && 
                !nodeProject.equals(project) && 
                !Utils.areInSameJ2EEApp(project, nodeProject)) {
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_NotInSameEarOrProject")); //NOI18N
                return false;
            }
            
            return true;
        }

        private boolean acceptInterfaces(Node[] nodes) {
            EjbReference ejbReference = (EjbReference) nodes[0].getCookie(EjbReference.class);
            if (ejbReference == null) {
                return false;
            }

            String errorMessage = " "; //NOI18N
            boolean shouldEnableLocal = true;
            boolean shouldEnableRemote = true;
            if (!ejbReference.supportsLocalInvocation()) {
                shouldEnableLocal = false;
            }
            if (!ejbReference.supportsRemoteInvocation()) {
                shouldEnableRemote = false;
            }
            localRadioButton.setEnabled(shouldEnableLocal);
            remoteRadioButton.setEnabled(shouldEnableRemote);
            if (!shouldEnableLocal && !shouldEnableRemote) {
                if (!ejbReference.supportsLocalInvocation() && !ejbReference.supportsRemoteInvocation()) {
                    setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_ReferencesNotSupported")); //NOI18N
                }
                setErrorMessage(NbBundle.getMessage(CallEjbPanel.class, "LBL_LocalAndRemoteRefAlreadyExist")); //NOI18N
                return false;
            } else if (shouldEnableLocal && !shouldEnableRemote) {
                localRadioButton.setSelected(true);
            } else if (!shouldEnableLocal && shouldEnableRemote) {
                remoteRadioButton.setSelected(true);
            }
            setErrorMessage(errorMessage);
            return true;
        }
        
        private boolean hasJarArtifact(Feature feature) {
            if (feature != null) {
                JavaClass beanClass = JMIUtils.getDeclaringClass(feature);
                Project nodeProject = FileOwnerQuery.getOwner(srcFile);
                if (nodeProject.equals(project)) {
                    // we're in same project, no need for output jar
                    return true;
                }
                return AntArtifactQuery.findArtifactsByType(nodeProject, JavaProjectConstants.ARTIFACT_TYPE_JAR).length > 0;
            }
            return false;
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
