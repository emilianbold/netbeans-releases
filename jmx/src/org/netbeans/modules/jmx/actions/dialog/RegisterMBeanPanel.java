/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.actions.RegisterMBeanAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask which MBean to instantiate and register.
 * @author  tl156378
 */
public class RegisterMBeanPanel extends javax.swing.JPanel 
        implements ItemListener, DocumentListener {
    
    /** true if the current class have a getMBeanServer method */
    private boolean hasGetMBeanServMeth;
    /** class to add registration of MBean */
    private JavaClass currentClass;
    private JavaClass mbeanClass = null;
    
    private DefaultTableModel constructorsModel;
    private DefaultTableModel interfacesModel;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    private boolean isMBean = false;
    private boolean isExistingClass = false;
    private boolean standardMBeanIsSelected;
    
    /**
     * Returns the current Java class.
     * @return <CODE>JavaClass</CODE> current specified Java class
     */
    public JavaClass getJavaClass() {
        return currentClass;
    }
    
    /**
     * Returns the current user defined MBean class.
     * @return <CODE>JavaClass</CODE> specified MBean class
     */
    public JavaClass getMBeanClass() {
        return mbeanClass;
    }
    
    /**
     * Returns if the current user defined MBean class is StandardMBean class.
     * @return <CODE>boolean</CODE> true only if StandardMBean class is selected
     */
    public boolean standardMBeanSelected() {
        return standardMBeanIsSelected;
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
        return classNameTextField.getText();
    }
    
    /**
     * Returns the current user defined class name.
     * @return <CODE>String</CODE> specified interface name
     */
    public String getInterfaceName() {
        if (selectIntfCheckBox.isSelected())
            return (String) interfacesModel.getValueAt(
                    interfacesTable.getSelectedRow(),0);
        else
            return WizardConstants.NULL;
    }
    
    /**
     * Returns the current user defined MBean constructor signature.
     * @return <CODE>String</CODE> signature of choosed constructor
     */
    public String getConstructorSignature() {
        return (String) constructorsTable.getValueAt(
                constructorsTable.getSelectedRow(),0);
    }
    
    /** 
     * Creates new form RegisterMBeanPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public RegisterMBeanPanel(Node node) {
        bundle = NbBundle.getBundle(RegisterMBeanPanel.class);
        
        initComponents();
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        JavaClass[] mbeanClasses = WizardHelpers.getMBeanClasses(
                WizardHelpers.getProject(fo));
        
        // add StandardMBean class
        mbeanClassComboBox.addItem(WizardConstants.STANDARD_MBEAN_CLASS);
        
        for (int i = 0; i < mbeanClasses.length; i++) {
            mbeanClassComboBox.addItem(mbeanClasses[i].getName());
        }
        
        Resource rc = JavaModel.getResource(fo);
        currentClass = WizardHelpers.getJavaClass(rc,fo.getName());
        
        // init tags
        hasGetMBeanServMeth = Introspector.hasMBeanServMethod(currentClass);
        standardMBeanIsSelected = true;
        
        String[] columnNames = new String[] { bundle.getString("LBL_Constructor_column") }; // NOI18N
        String[][] cells = new String[][] {};
        constructorsModel = 
                new NonEditableTableModel(cells,columnNames);
        columnNames = new String[] { bundle.getString("LBL_Interface_column") }; // NOI18N
        interfacesModel = 
                new NonEditableTableModel(cells,columnNames);
        initTable(constructorsTable,constructorsModel);
        initTable(interfacesTable,interfacesModel);
                
        // init labels
        Mnemonics.setLocalizedText(mbeanClassLabel,
                     bundle.getString("LBL_MBean_Class")); // NOI18N
        Mnemonics.setLocalizedText(classNameLabel,
                     bundle.getString("LBL_Class")); // NOI18N
        Mnemonics.setLocalizedText(objectNameLabel,
                     bundle.getString("LBL_ObjectName")); // NOI18N
        Mnemonics.setLocalizedText(constructorsLabel,
                     bundle.getString("LBL_Constructor")); // NOI18N
        Mnemonics.setLocalizedText(selectIntfCheckBox,
                     bundle.getString("LBL_SelectMgtIntf")); // NOI18N
    }
    
    private void initTable(JTable table, TableModel model) {
        table.setModel(model);
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(18);
    }
    
    private boolean isAcceptable() {
        return (((constructorsModel.getRowCount() > 0) && 
                (!standardMBeanIsSelected) &&
                (constructorsTable.getSelectedRow() != -1) && isMBean) ||
                (isExistingClass && standardMBeanIsSelected));
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_RegisterMBeanAction.Title"); // NOI18N
        btnOK = new JButton(bundle.getString("LBL_OK")); //NOI18N
        btnOK.setEnabled(isAcceptable());
        
        //set listeners
        ((JTextField) mbeanClassComboBox.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(this);
        mbeanClassComboBox.addItemListener(this);
        classNameTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateInterfaces(classNameTextField.getText());
                } catch (BadLocationException excep) {}
            }
            
            public void removeUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateInterfaces(classNameTextField.getText());
                } catch (BadLocationException excep) {}
            }
            
            public void changedUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                try {
                    String newClassName = doc.getText(0,doc.getLength());
                    updateInterfaces(classNameTextField.getText());
                } catch (BadLocationException excep) {}
            }
        });
        
        //init state
        updateComponentsState();
        updateState((String) mbeanClassComboBox.getSelectedItem());
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(RegisterMBeanAction.class),
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    private void updateState(String currentMBeanClass) {
        if (!standardMBeanIsSelected && (currentMBeanClass.equals(
                WizardConstants.STANDARD_MBEAN_CLASS))) {
            standardMBeanIsSelected = true;
            updateComponentsState();
        } else if (standardMBeanIsSelected && (!currentMBeanClass.equals(
                WizardConstants.STANDARD_MBEAN_CLASS))) {
            standardMBeanIsSelected = false;
            updateComponentsState();
        }
        if (standardMBeanIsSelected)
            updateInterfaces(classNameTextField.getText());
        updateConstructors(currentMBeanClass);
    }
    
    private void updateComponentsState() {
        classNameLabel.setEnabled(standardMBeanIsSelected);
        classNameTextField.setEnabled(standardMBeanIsSelected);
        tabPane.setSelectedIndex(standardMBeanIsSelected ? 1 : 0);
        tabPane.setEnabledAt(0,!standardMBeanIsSelected);
        tabPane.setEnabledAt(1,standardMBeanIsSelected);
    }
    
    private void updateInterfaces(String className) {
        stateLabel.setText( ""); // NOI18N
        JavaModelPackage pkg = (JavaModelPackage) currentClass.refImmediatePackage();
        JavaClass clazz = (JavaClass) pkg.getJavaClass().resolve(className);
        if (standardMBeanIsSelected) {
                objectNameTextField.setText(WizardHelpers.reversePackageName(
                    WizardHelpers.getPackageName(className)) +
                    ":type=" + WizardHelpers.getClassName(className)); // NOI18N
        } 
        //clear the table
        interfacesModel.setRowCount(0);
        if ((clazz != null) && (!clazz.getClass().getName().startsWith(
                "org.netbeans.jmi.javamodel.UnresolvedClass"))) { // NOI18N
            String[] interfaces = WizardHelpers.getInterfaceNames(clazz);
            boolean hasIntf = (interfaces.length > 0);
            selectIntfCheckBox.setEnabled(hasIntf);
            selectIntfCheckBox.setSelected(hasIntf);
            interfacesScrollPane.setEnabled(hasIntf);
            if (hasIntf) {
                for (int i = 0; i < interfaces.length ; i++) {
                    interfacesModel.addRow(new String[] { interfaces[i] });
                }
                //select first row
                interfacesTable.setRowSelectionInterval(0, 0);
            }
            isExistingClass = true;
        } else {
            stateLabel.setText(bundle.getString("LBL_ClassNotExist")); // NOI18N
            selectIntfCheckBox.setEnabled(false);
            selectIntfCheckBox.setSelected(false);
            interfacesScrollPane.setEnabled(false);
            isExistingClass = false;
        }
        btnOK.setEnabled(isAcceptable());
    }
    
    private void updateConstructors(String currentMBeanClass) {
        //clear the table
        constructorsModel.setRowCount(0);
        //clear information message
        stateLabel.setText( ""); // NOI18N
        JavaModelPackage pkg = (JavaModelPackage) currentClass.refImmediatePackage();
        mbeanClass = (JavaClass) pkg.getJavaClass().resolve(currentMBeanClass);
        if ((mbeanClass != null) && (!mbeanClass.getClass().getName().startsWith(
                "org.netbeans.jmi.javamodel.UnresolvedClass"))) { // NOI18N
            if (!standardMBeanIsSelected) {
                objectNameTextField.setText(WizardHelpers.reversePackageName(
                    WizardHelpers.getPackageName(mbeanClass.getName())) +
                    ":type=" + mbeanClass.getSimpleName()); // NOI18N
            }     
            isMBean = Introspector.isMBeanClass(mbeanClass);
            Constructor[] constructors =
                    WizardHelpers.getConstructors(mbeanClass);
            if (constructors.length > 0) {
                for (int i = 0; i < constructors.length; i++) {
                    Constructor currentConstruct = constructors[i];
                    List params = currentConstruct.getParameters();
                    String construct = mbeanClass.getSimpleName() + "("; // NOI18N
                    for (Iterator<Parameter> it = params.iterator(); it.hasNext();) {
                        construct += WizardHelpers.getClassName(
                                it.next().getType().getName());
                        if (it.hasNext())
                            construct += ", "; // NOI18N
                    }
                    construct += ")"; // NOI18N
                    constructorsModel.addRow(new String[] { construct });
                }
                //select first row
                constructorsTable.setRowSelectionInterval(0, 0);
            } else {
                stateLabel.setText(bundle.getString("LBL_ClassWithNoConstructor")); // NOI18N
            }
            
        } else {
            isMBean = false;
        }
        if (!isMBean)
            stateLabel.setText(bundle.getString("LBL_NotMBeanClass")); // NOI18N
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

        objectNameLabel = new javax.swing.JLabel();
        objectNameTextField = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        mbeanClassComboBox = new javax.swing.JComboBox();
        classNameTextField = new javax.swing.JTextField();
        tabPane = new javax.swing.JTabbedPane();
        constructorPanel = new javax.swing.JPanel();
        constructorsLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        constructorsTable = new javax.swing.JTable();
        interfacePanel = new javax.swing.JPanel();
        interfacesScrollPane = new javax.swing.JScrollPane();
        interfacesTable = new javax.swing.JTable();
        selectIntfCheckBox = new javax.swing.JCheckBox();
        classNameLabel = new javax.swing.JLabel();
        mbeanClassLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(380, 300));
        objectNameLabel.setLabelFor(objectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(objectNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(objectNameTextField, gridBagConstraints);

        stateLabel.setForeground(java.awt.SystemColor.activeCaption);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 12, 12);
        add(stateLabel, gridBagConstraints);

        mbeanClassComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(mbeanClassComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(classNameTextField, gridBagConstraints);

        constructorPanel.setLayout(new java.awt.GridBagLayout());

        constructorPanel.setPreferredSize(new java.awt.Dimension(380, 300));
        constructorsLabel.setLabelFor(constructorsTable);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        constructorPanel.add(constructorsLabel, gridBagConstraints);

        constructorsTable.setBorder(new javax.swing.border.EtchedBorder());
        constructorsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        constructorsTable.setIntercellSpacing(new java.awt.Dimension(4, 1));
        constructorsTable.setMinimumSize(new java.awt.Dimension(60, 60));
        jScrollPane2.setViewportView(constructorsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 12, 12);
        constructorPanel.add(jScrollPane2, gridBagConstraints);

        tabPane.addTab(java.util.ResourceBundle.getBundle("org/netbeans/modules/jmx/actions/dialog/Bundle").getString("LBL_TAB_Constructor"), constructorPanel);

        interfacePanel.setLayout(new java.awt.GridBagLayout());

        interfacePanel.setPreferredSize(new java.awt.Dimension(380, 300));
        interfacesTable.setBorder(new javax.swing.border.EtchedBorder());
        interfacesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        interfacesTable.setIntercellSpacing(new java.awt.Dimension(4, 1));
        interfacesTable.setMinimumSize(new java.awt.Dimension(60, 60));
        interfacesScrollPane.setViewportView(interfacesTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 12, 12);
        interfacePanel.add(interfacesScrollPane, gridBagConstraints);

        selectIntfCheckBox.setSelected(true);
        selectIntfCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIntfCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        interfacePanel.add(selectIntfCheckBox, gridBagConstraints);

        tabPane.addTab(java.util.ResourceBundle.getBundle("org/netbeans/modules/jmx/actions/dialog/Bundle").getString("LBL_TAB_Interface"), interfacePanel);

        tabPane.setSelectedComponent(constructorPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(tabPane, gridBagConstraints);

        classNameLabel.setLabelFor(classNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(classNameLabel, gridBagConstraints);

        mbeanClassLabel.setLabelFor(mbeanClassComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(mbeanClassLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void selectIntfCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectIntfCheckBoxActionPerformed
        boolean toEnable = ((interfacesModel.getRowCount() > 0) && 
                (selectIntfCheckBox.isSelected()));
        interfacesScrollPane.setEnabled(toEnable);
        interfacesTable.setEnabled(toEnable);
    }//GEN-LAST:event_selectIntfCheckBoxActionPerformed
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JPanel constructorPanel;
    private javax.swing.JLabel constructorsLabel;
    private javax.swing.JTable constructorsTable;
    private javax.swing.JPanel interfacePanel;
    private javax.swing.JScrollPane interfacesScrollPane;
    private javax.swing.JTable interfacesTable;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox mbeanClassComboBox;
    private javax.swing.JLabel mbeanClassLabel;
    private javax.swing.JLabel objectNameLabel;
    private javax.swing.JTextField objectNameTextField;
    private javax.swing.JCheckBox selectIntfCheckBox;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTabbedPane tabPane;
    // End of variables declaration//GEN-END:variables
    
    public void itemStateChanged(ItemEvent e) {
        String newMBeanClass = (String) mbeanClassComboBox.getSelectedItem();
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
