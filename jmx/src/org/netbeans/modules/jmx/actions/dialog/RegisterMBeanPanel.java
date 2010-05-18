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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.ClassButton;
import org.netbeans.modules.jmx.MBeanClassButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jmx.JavaModelHelper;

/**
 * Panel which is used to ask which MBean to instantiate and register.
 * @author  tl156378
 */
public class RegisterMBeanPanel extends javax.swing.JPanel
        implements ItemListener, DocumentListener {
    
    /** class to add registration of MBean */
    private JavaSource agentClass;
    private JavaSource mbeanClass = null;
    private Project project = null;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    private boolean isMBean = false;
    private boolean isExistingClass = false;
    private boolean isValid = false;
    private DataObject dob;
    private Map<String, ExecutableElement> constructorsMap;
    /**
     * Returns the current Java class.
     * @return <CODE>JavaClass</CODE> current specified Java class
     */
    public JavaSource getAgentJavaSource() {
        return agentClass;
    }
    
    /**
     * Returns the current user defined MBean class.
     * @return <CODE>JavaClass</CODE> specified MBean class
     */
    public JavaSource getMBeanJavaSource() {
        return mbeanClass;
    }
    
    /**
     * Returns if the current user defined MBean class is StandardMBean class.
     * @return <CODE>boolean</CODE> true only if StandardMBean class is selected
     */
    public boolean standardMBeanSelected() {
        return standardMBeanRadioButton.isSelected();
    }
    
    /**
     * Returns the current user defined MBean objectName.
     * @return <CODE>String</CODE> specified ObjectName
     */
    public String getMBeanObjectName() {
        return objectNameTextField.getText();
    }
    
    /**
     * Returns the current user defined class name.
     * @return <CODE>String</CODE> specified class name
     */
    public String getClassName() {
        if (standardMBeanRadioButton.isSelected())
            return classNameTextField.getText();
        else
            return mbeanClassTextField.getText();
    }
    
    /**
     * Returns the current user defined class name.
     * @return <CODE>String</CODE> specified interface name
     */
    public String getInterfaceName() {
        if (standardMBeanRadioButton.isSelected()) {
            String interfaceName = (String) interfaceComboBox.getSelectedItem();
            if (bundle.getString("LBL_GeneratedInterface").equals(interfaceName)) // NOI18N
                return null;
            else
                return interfaceName;
        } else
            return null;
    }
    
    /**
     * Returns the current user defined constructor signature.
     * @return <CODE>String</CODE> signature of choosed constructor
     */
    public String getConstructor() {
        String construct = (String) constructorComboBox.getSelectedItem();
        if (bundle.getString("LBL_StandardMBeanDefaultConstructor").equals(construct) ||
            bundle.getString("LBL_ConstructorNotGenerated").equals(construct)) // NOI18N
            return null;
        else
            return construct;
    }
    /**
     * Returns the current user defined constructor signature.
     * @return <CODE>String</CODE> signature of choosed constructor
     */
    public ExecutableElement getExecutableConstructor() {
        String construct = (String) constructorComboBox.getSelectedItem();
        if (bundle.getString("LBL_StandardMBeanDefaultConstructor").equals(construct)||
            bundle.getString("LBL_ConstructorNotGenerated").equals(construct)) // NOI18N
            return null;
        else
            return constructorsMap.get(construct);
    }
    
    /**
     * Creates new form RegisterMBeanPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public RegisterMBeanPanel(Node node) {
        bundle = NbBundle.getBundle(RegisterMBeanPanel.class);
        
        initComponents();
        
        dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        project = WizardHelpers.getProject(fo);
        
        agentClass = JavaModelHelper.getSource(fo);
        
        // init tags
        userMBeanRadioButton.setSelected(true);
        
        // init labels
        chooseLabel.setText(bundle.getString("LBL_RegistMbean_ChooseOptions")); // NOI18N
        Mnemonics.setLocalizedText(userMBeanRadioButton,
                bundle.getString("LBL_RegisterUserMBean")); // NOI18N
        Mnemonics.setLocalizedText(standardMBeanRadioButton,
                bundle.getString("LBL_RegisterStandardMBean")); // NOI18N
        Mnemonics.setLocalizedText(mbeanClassLabel,
                bundle.getString("LBL_MBean_Class")); // NOI18N
        Mnemonics.setLocalizedText(classNameLabel,
                bundle.getString("LBL_Class")); // NOI18N
        Mnemonics.setLocalizedText(isMXBean,
                bundle.getString("LBL_IsMXBean")); // NOI18N
        Mnemonics.setLocalizedText(objectNameLabel,
                bundle.getString("LBL_ObjectName")); // NOI18N
        addedInfosLabel.setText(bundle.getString("LBL_RegistMbean_AddedInfos")); // NOI18N
        Mnemonics.setLocalizedText(constructorLabel,
                bundle.getString("LBL_Constructor")); // NOI18N
        Mnemonics.setLocalizedText(interfaceLabel,
                bundle.getString("LBL_Interface")); // NOI18N
        
        //for accesibility
        userMBeanRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_USER_MBEAN")); // NOI18N
        userMBeanRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_USER_MBEAN_DESCRIPTION")); // NOI18N
        mbeanClassTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_MBEAN_CLASS")); // NOI18N
        mbeanClassTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_MBEAN_CLASS_DESCRIPTION")); // NOI18N
        mbeanBrowseButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_MBEAN_BROWSE_MBEANCLASS")); // NOI18N
        mbeanBrowseButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_MBEAN_BROWSE_MBEANCLASS_DESCRIPTION")); // NOI18N
        objectNameTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_OBJECTNAME")); // NOI18N
        objectNameTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_OBJECTNAME_DESCRIPTION")); // NOI18N
        constructorComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_CONSTRUCTOR")); // NOI18N
        constructorComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_CONSTRUCTOR_DESCRIPTION")); // NOI18N
        standardMBeanRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STANDARD_MBEAN")); // NOI18N
        standardMBeanRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STANDARD_MBEAN_DESCRIPTION")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CLASSNAME")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_CLASSNAME_DESCRIPTION")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_BROWSE_CLASSNAME")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_BROWSE_CLASSNAME_DESCRIPTION")); // NOI18N
        interfaceComboBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_INTERFACE")); // NOI18N
        interfaceComboBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTER_STDMBEAN_INTERFACE_DESCRIPTION")); // NOI18N
        isMXBean.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_IS_MXBEAN")); // NOI18N
        isMXBean.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_IS_MXBEAN_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    private boolean isAcceptable() {
        return ((standardMBeanSelected() && isExistingClass && isValid) ||
                (!standardMBeanSelected() && isMBean && isValid));
    }
    
    public boolean isMXBean() {
        return isMXBean.isSelected();
    }
    
    public DataObject getDataObject() {
        return dob;
    }
    /**
     * Displays a configuration dialog and updates Register MBean options
     * according to the user's settings.
     */
    public boolean configure() throws IOException {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_RegisterMBeanAction.Title"); // NOI18N
        btnOK = new JButton(bundle.getString("LBL_OK")); //NOI18N
        btnOK.setEnabled(isAcceptable());
        btnOK.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        //set listeners
        mbeanClassTextField.getDocument().addDocumentListener(this);
        classNameTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
            
            public void removeUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
            
            public void changedUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateState(null);
                } catch (BadLocationException excep) {}
            }
        });
        
        //init state
        updateComponentsState();
        updateState((String) mbeanClassTextField.getText());
        SourceGroup[] srcGroups = WizardHelpers.getSourceGroups(project);
        ClassButton classBut = new ClassButton(browseButton,classNameTextField,srcGroups);
        MBeanClassButton mbeanClassBut =
                new MBeanClassButton(mbeanBrowseButton,mbeanClassTextField,srcGroups);
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx("jmx_agent_instantiate"), // NOI18N
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    private void updateState(String currentMBeanClass) {
        try {
        updateComponentsState();
        if (standardMBeanSelected()) {
            String className = classNameTextField.getText();
            updateIntfAndConst(className);
        } else
            updateConstructors(currentMBeanClass);
        }catch(IOException ex) {
            //ex.printStackTrace();
            //System.out.println(ex);
        }
    }
    
    private void updateComponentsState() {
        boolean standardMBean = standardMBeanSelected();
        // update state of user MBean use case
        mbeanClassLabel.setEnabled(!standardMBean);
        mbeanClassTextField.setEnabled(!standardMBean);
        mbeanBrowseButton.setEnabled(!standardMBean);
        // update state of StandardMBean use case
        classNameLabel.setEnabled(standardMBean);
        classNameTextField.setEnabled(standardMBean);
        isMXBean.setEnabled(standardMBean);
        browseButton.setEnabled(standardMBean);
        interfaceLabel.setEnabled(standardMBean);
        interfaceComboBox.setEnabled(standardMBean);
    }
    
    private void updateIntfAndConst(String className) throws IOException {
        isValid = true;
        stateLabel.setText(""); // NOI18N
        
        
        JavaSource clazz = JavaModelHelper.findClassInProject(project, className);
        //clear combobox list of interfaces and constructors
        interfaceComboBox.removeAllItems();
        constructorComboBox.removeAllItems();
        isExistingClass = (clazz != null);
        objectNameLabel.setEnabled(isExistingClass);
        objectNameTextField.setEnabled(isExistingClass);
        interfaceLabel.setEnabled(isExistingClass);
        interfaceComboBox.setEnabled(isExistingClass);
        constructorLabel.setEnabled(isExistingClass);
        constructorComboBox.setEnabled(isExistingClass);
        if (isExistingClass) {
            objectNameTextField.setText(WizardHelpers.reversePackageName(
                    WizardHelpers.getPackageName(className)) +
                    ":type=" + WizardHelpers.getClassName(className)); // NOI18N
            
            boolean isMBean = false;
            
            isMBean = JavaModelHelper.testMBeanCompliance(clazz);
            
            if (isMBean)
                interfaceComboBox.addItem(bundle.getString("LBL_GeneratedInterface")); // NOI18N
            String[] interfaces = JavaModelHelper.getInterfaceNames(clazz);
            boolean hasIntf = (interfaces.length > 0);
            if (hasIntf) {
                for (int i = 0; i < interfaces.length ; i++) {
                    String intf = interfaces[i];
                    int indexOfType = intf.lastIndexOf('<');
                    if (indexOfType != -1)
                        intf = intf.substring(0,indexOfType);
                    interfaceComboBox.addItem(intf);
                }
            }
            
            //select first item
            if (isMBean)
                interfaceComboBox.setSelectedItem(bundle.getString("LBL_GeneratedInterface")); // NOI18N
            else if (hasIntf)
                interfaceComboBox.setSelectedIndex(0);
            else {
                isValid = false;
                interfaceComboBox.setEnabled(false);
                constructorComboBox.setEnabled(false);
                stateLabel.setText(bundle.getString("LBL_ClassWithNoInterface")); // NOI18N
            }
            
            //discovery of class constructors
            // WARNING THE Constructor is the JMI one, not from Reflect
            constructorsMap = JavaModelHelper.getConstructors(clazz);
            
            Object[] constructors = getConstructors(constructorsMap);
                    
            if (constructors.length > 0) {
                constructorComboBox.addItem(
                        bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
                for (int i = 0; i < constructors.length; i++) {
                    constructorComboBox.addItem(constructors[i]);
                }
                //select first row
                constructorComboBox.setSelectedItem(
                        bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
            } else {
                constructorComboBox.addItem(
                        bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
                if (JavaModelHelper.hasOnlyDefaultConstruct(clazz))
                    constructorComboBox.addItem(JavaModelHelper.getSimpleName(clazz) + "()"); // NOI18N
                //select first row
                constructorComboBox.setSelectedItem(
                        bundle.getString("LBL_StandardMBeanDefaultConstructor")); // NOI18N
            }
        } else {
            if (className.equals("")) // NOI18N
                stateLabel.setText(""); // NOI18N
            else
                stateLabel.setText(bundle.getString("LBL_ClassNotExist")); // NOI18N
        }
        btnOK.setEnabled(isAcceptable());
    }
    
    private Object[] getConstructors(Map<String, ExecutableElement> m) {
        return m.keySet().toArray();
    }
    private void updateConstructors(String currentMBeanClass) throws IOException {
        //clear the comboBox list of MBean constructors
        constructorComboBox.removeAllItems();
        isValid = true;
        //clear information message
        stateLabel.setText( ""); // NOI18N
        
        mbeanClass = JavaModelHelper.findClassInProject(project, currentMBeanClass);
        isMBean = false;
        if (mbeanClass != null) {
            isMBean = JavaModelHelper.testMBeanCompliance(mbeanClass);
        }
 
        objectNameLabel.setEnabled(isMBean);
        objectNameTextField.setEnabled(isMBean);
        constructorLabel.setEnabled(isMBean);
        constructorComboBox.setEnabled(isMBean);
        if (isMBean) {
            String className = JavaModelHelper.getSimpleName(mbeanClass);
            String packageName = JavaModelHelper.getPackage(mbeanClass);
            objectNameTextField.setText(packageName + ":type=" + className);// NOI18N
            // NOI18N
            constructorsMap = JavaModelHelper.getConstructors(mbeanClass);    
            Object[] constructors = getConstructors(constructorsMap);
            if (constructors.length > 0) {
                constructorComboBox.setEnabled(true);
                for (int i = 0; i < constructors.length; i++) {
                    constructorComboBox.addItem(constructors[i]);
                }
                constructorComboBox.addItem(
                        bundle.getString("LBL_ConstructorNotGenerated")); // NOI18N
                //select first row
                constructorComboBox.setSelectedItem(0); // NOI18N
            } else if (JavaModelHelper.hasOnlyDefaultConstruct(mbeanClass)) {
                constructorComboBox.addItem(className + "()"); // NOI18N
                constructorComboBox.setSelectedItem(0); // NOI18N
            } else {
                isValid = false;
                constructorComboBox.setEnabled(false);
                stateLabel.setText(bundle.getString("LBL_ClassWithNoConstructor")); // NOI18N
            }
        }
        if ((!isMBean) && (!currentMBeanClass.equals(""))) { // NOI18N
            isValid = false;
            stateLabel.setText(bundle.getString("LBL_NotMBeanClass")); // NOI18N
        }
        btnOK.setEnabled(isAcceptable());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mbeanGroup = new javax.swing.ButtonGroup();
        northPanel = new javax.swing.JPanel();
        objectNameLabel = new javax.swing.JLabel();
        objectNameTextField = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        classNameLabel = new javax.swing.JLabel();
        mbeanClassLabel = new javax.swing.JLabel();
        userMBeanRadioButton = new javax.swing.JRadioButton();
        standardMBeanRadioButton = new javax.swing.JRadioButton();
        interfaceLabel = new javax.swing.JLabel();
        interfaceComboBox = new javax.swing.JComboBox();
        constructorLabel = new javax.swing.JLabel();
        constructorComboBox = new javax.swing.JComboBox();
        browseButton = new javax.swing.JButton();
        mbeanClassTextField = new javax.swing.JTextField();
        mbeanBrowseButton = new javax.swing.JButton();
        chooseLabel = new javax.swing.JLabel();
        addedInfosLabel = new javax.swing.JLabel();
        isMXBean = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.GridBagLayout());

        objectNameLabel.setLabelFor(objectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        northPanel.add(objectNameLabel, gridBagConstraints);

        objectNameTextField.setName("objectNameTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        northPanel.add(objectNameTextField, gridBagConstraints);

        stateLabel.setForeground(new java.awt.Color(0, 0, 128));
        stateLabel.setMinimumSize(new java.awt.Dimension(0, 20));
        stateLabel.setName("stateLabel"); // NOI18N
        stateLabel.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        northPanel.add(stateLabel, gridBagConstraints);

        classNameTextField.setName("classNameTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        northPanel.add(classNameTextField, gridBagConstraints);

        classNameLabel.setLabelFor(classNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 34, 0, 11);
        northPanel.add(classNameLabel, gridBagConstraints);

        mbeanClassLabel.setLabelFor(mbeanClassTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 34, 0, 11);
        northPanel.add(mbeanClassLabel, gridBagConstraints);

        mbeanGroup.add(userMBeanRadioButton);
        userMBeanRadioButton.setName("userMBeanRadioButton"); // NOI18N
        userMBeanRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userMBeanRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        northPanel.add(userMBeanRadioButton, gridBagConstraints);

        mbeanGroup.add(standardMBeanRadioButton);
        standardMBeanRadioButton.setName("standardMBeanRadioButton"); // NOI18N
        standardMBeanRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stabdardMBeanRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        northPanel.add(standardMBeanRadioButton, gridBagConstraints);

        interfaceLabel.setLabelFor(interfaceComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 34, 0, 11);
        northPanel.add(interfaceLabel, gridBagConstraints);

        interfaceComboBox.setMinimumSize(new java.awt.Dimension(270, 25));
        interfaceComboBox.setName("interfaceComboBox"); // NOI18N
        interfaceComboBox.setPreferredSize(new java.awt.Dimension(270, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        northPanel.add(interfaceComboBox, gridBagConstraints);

        constructorLabel.setLabelFor(constructorComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        northPanel.add(constructorLabel, gridBagConstraints);

        constructorComboBox.setName("constructorComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        northPanel.add(constructorComboBox, gridBagConstraints);

        browseButton.setText("jButton1");
        browseButton.setName("browseButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        northPanel.add(browseButton, gridBagConstraints);

        mbeanClassTextField.setName("mbeanClassTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        northPanel.add(mbeanClassTextField, gridBagConstraints);

        mbeanBrowseButton.setText("jButton1");
        mbeanBrowseButton.setName("mbeanBrowseButton"); // NOI18N
        mbeanBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mbeanBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        northPanel.add(mbeanBrowseButton, gridBagConstraints);

        chooseLabel.setMinimumSize(new java.awt.Dimension(0, 20));
        chooseLabel.setName("stateLabel"); // NOI18N
        chooseLabel.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        northPanel.add(chooseLabel, gridBagConstraints);

        addedInfosLabel.setMinimumSize(new java.awt.Dimension(0, 20));
        addedInfosLabel.setName("stateLabel"); // NOI18N
        addedInfosLabel.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 0, 12);
        northPanel.add(addedInfosLabel, gridBagConstraints);

        isMXBean.setText("jCheckBox1");
        isMXBean.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        isMXBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        isMXBean.setName("isMXBeanCheckBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 34, 0, 11);
        northPanel.add(isMXBean, gridBagConstraints);

        add(northPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    private void mbeanBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mbeanBrowseButtonActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_mbeanBrowseButtonActionPerformed
    
    private void stabdardMBeanRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stabdardMBeanRadioButtonActionPerformed
        updateState(null);
    }//GEN-LAST:event_stabdardMBeanRadioButtonActionPerformed
    
    private void userMBeanRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userMBeanRadioButtonActionPerformed
        String newMBeanClass = mbeanClassTextField.getText();
        updateState(newMBeanClass);
    }//GEN-LAST:event_userMBeanRadioButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addedInfosLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JComboBox constructorComboBox;
    private javax.swing.JLabel constructorLabel;
    private javax.swing.JComboBox interfaceComboBox;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JCheckBox isMXBean;
    private javax.swing.JButton mbeanBrowseButton;
    private javax.swing.JLabel mbeanClassLabel;
    private javax.swing.JTextField mbeanClassTextField;
    private javax.swing.ButtonGroup mbeanGroup;
    private javax.swing.JPanel northPanel;
    private javax.swing.JLabel objectNameLabel;
    private javax.swing.JTextField objectNameTextField;
    private javax.swing.JRadioButton standardMBeanRadioButton;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JRadioButton userMBeanRadioButton;
    // End of variables declaration//GEN-END:variables
    public void itemStateChanged(ItemEvent e) {
        String newMBeanClass = (String) mbeanClassTextField.getText();
        updateState(newMBeanClass);
    }
    
    public void insertUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }
    
    
    public void removeUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }
    
    public void changedUpdate(DocumentEvent e) {
        Document doc = e.getDocument();
        try {
            String newMBeanClass = doc.getText(0,doc.getLength());
            updateState(newMBeanClass);
        } catch (BadLocationException excep) {}
    }
}
