/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.CmpRelationshipsForm;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.SourceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author pfiala
 */
class CmpRelationshipsDialogHelper {
    private static final String[] FILED_TYPE_ITEMS = new String[]{"java.util.Collection", "java.util.Set"};//NOI18N

    private static boolean lastGetter = true;
    private static boolean lastSetter = true;
    private static boolean lastGetter2 = true;
    private static boolean lastSetter2 = true;

    private final FileObject ejbJarFile;
    private final EjbJar ejbJar;

    private JTextField relationshipNameTextField;
    private JTextArea descriptionTextArea;
    private JTextField roleNameTextField;
    private JComboBox ejbComboBox;
    private JRadioButton multiplicityManyRadioButton;
    private JRadioButton multiplicityOneRadioButton;
    private JCheckBox cascadeDeleteCheckBox;
    private JCheckBox createCmrFieldCheckBox;
    private JTextField fieldNameTextField;
    private JComboBox fieldTypeComboBox;
    private JTextField roleNameTextField2;
    private JComboBox ejbComboBox2;
    private JRadioButton multiplicityManyRadioButton2;
    private JRadioButton multiplicityOneRadioButton2;
    private JCheckBox cascadeDeleteCheckBox2;
    private JCheckBox createCmrFieldCheckBox2;
    private JTextField fieldNameTextField2;
    private JComboBox fieldTypeComboBox2;
    private JCheckBox getterCheckBox;
    private JCheckBox setterCheckBox;
    private JCheckBox getterCheckBox2;
    private JCheckBox setterCheckBox2;

    private String origEjbName2;
    private String origEjbName;
    private MethodElement origLocalGetterMethod;
    private MethodElement origLocalSetterMethod;
    private MethodElement origLocalGetterMethod2;
    private MethodElement origLocalSetterMethod2;
    private ClassElement origLocalInterface2;
    private ClassElement origLocalInterface;
    private String origFieldName;
    private String origFieldType;
    private String origFieldName2;
    private String origFieldType2;

    public CmpRelationshipsDialogHelper(FileObject ejbJarFile, EjbJar ejbJar) {
        this.ejbJarFile = ejbJarFile;
        this.ejbJar = ejbJar;
    }

    public boolean showCmpRelationshipsDialog(String title, EjbRelation relation) {
        CmpRelationshipsForm form = initForm();

        Vector entityNames = getEntities();

        ejbComboBox.setModel(new DefaultComboBoxModel(entityNames));
        ejbComboBox2.setModel(new DefaultComboBoxModel(entityNames));

        fieldTypeComboBox.setModel(new DefaultComboBoxModel(FILED_TYPE_ITEMS));
        fieldTypeComboBox2.setModel(new DefaultComboBoxModel(FILED_TYPE_ITEMS));

        RelationshipHelper helper;
        if (relation != null) {
            helper = new RelationshipHelper(relation);
            populateFormFields(helper);
        } else {
            helper = null;
        }

        RelationshipDialogActionListener listener = new RelationshipDialogActionListener();
        multiplicityOneRadioButton.addActionListener(listener);
        multiplicityManyRadioButton.addActionListener(listener);
        multiplicityManyRadioButton2.addActionListener(listener);
        multiplicityOneRadioButton2.addActionListener(listener);
        createCmrFieldCheckBox.addActionListener(listener);
        createCmrFieldCheckBox2.addActionListener(listener);

        listener.updateForm();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(form, title);
        dialogDescriptor.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            if (helper == null) {
                helper = new RelationshipHelper(ejbJar.getSingleRelationships());
            }
            processResult(helper);
            return true;
        } else {
            return false;
        }
    }

    private Vector getEntities() {
        Vector entityNames = new Vector();
        Entity[] entities = ejbJar.getEnterpriseBeans().getEntity();
        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];
            if (entity.getLocal() != null) {
                entityNames.add(entity.getEjbName());
            }
        }
        return entityNames;
    }

    private void processResult(RelationshipHelper helper) {
        String ejbName = (String) ejbComboBox.getSelectedItem();
        String ejbName2 = (String) ejbComboBox2.getSelectedItem();
        String relationName = relationshipNameTextField.getText().trim();
        if (relationName.length() == 0) {
            relationName = ejbName + "-" + ejbName2; //NOI18N
        }
        String roleName = roleNameTextField.getText().trim();
        if (roleName.length() == 0) {
            roleName = ejbName;
        }
        String roleName2 = roleNameTextField2.getText().trim();
        if (roleName2.length() == 0) {
            roleName2 = ejbName2;
        }

        helper.setRelationName(relationName);
        helper.setDescription(descriptionTextArea.getText().trim());

        helper.setEjbName(ejbName);
        helper.setRoleName(ejbName);
        helper.setMultiple(multiplicityManyRadioButton.isSelected());
        helper.setCascadeDelete(cascadeDeleteCheckBox.isSelected());
        String fieldName;
        String fieldType;
        if (createCmrFieldCheckBox.isSelected()) {
            fieldName = fieldNameTextField.getText().trim();
            fieldType = (String) fieldTypeComboBox.getSelectedItem();
            helper.setCmrField(fieldName, fieldType);
        } else {
            fieldName = null;
            fieldType = null;
            helper.setCmrField(null);
        }

        helper.setEjbName2(ejbName2);
        helper.setRoleName2(ejbName2);
        helper.setMultiple2(multiplicityManyRadioButton2.isSelected());
        helper.setCascadeDelete2(cascadeDeleteCheckBox2.isSelected());
        String fieldName2;
        String fieldType2;
        if (createCmrFieldCheckBox2.isSelected()) {
            fieldName2 = fieldNameTextField2.getText().trim();
            fieldType2 = (String) fieldTypeComboBox2.getSelectedItem();
            helper.setCmrField2(fieldName2, fieldType2);
        } else {
            fieldName2 = null;
            fieldType2 = null;
            helper.setCmrField2(null);
        }
        boolean getter = getterCheckBox.isSelected();
        boolean setter = setterCheckBox.isSelected();
        boolean origGetter = origLocalGetterMethod != null;
        boolean origSetter = origLocalSetterMethod != null;
        if (origEjbName != ejbName || origFieldName != fieldName || getter != origGetter || setter != origSetter) {
            if(origGetter) {
                try {
                    origLocalInterface.removeMethod(origLocalGetterMethod);
                } catch (SourceException e) {
                    Utils.notifyError(e);
                }
            }
            if(origSetter) {
                try {
                    origLocalInterface.removeMethod(origLocalSetterMethod);
                } catch (SourceException e) {
                    Utils.notifyError(e);
                }
            }
            if(getter || setter) {
                Entity entity = getEntity(ejbName);
                ClassElement beanClass = Utils.getBeanClass(ejbJarFile, entity);
                MethodElement getterMethod = Utils.getGetterMethod(beanClass, fieldName);
                MethodElement setterMethod = Utils.getSetterMethod(beanClass, fieldName, getterMethod);
                ClassElement localInterface = Utils.getBusinessInterface(entity.getLocal(), ejbJarFile, beanClass);
                if(getter) {
                    try {
                        localInterface.addMethod(getterMethod);
                    } catch (SourceException e) {
                        Utils.notifyError(e);
                    }
                }
                if(setter) {
                    try {
                        localInterface.addMethod(setterMethod);
                    } catch (SourceException e) {
                        Utils.notifyError(e);
                    }
                }

            }


        }
    }

    private void populateFormFields(RelationshipHelper helper) {
        relationshipNameTextField.setText(helper.getRelationName());
        descriptionTextArea.setText(helper.getDescription());

        roleNameTextField.setText(helper.getRoleName());
        origEjbName = helper.getEjbName();
        ejbComboBox.setSelectedItem(origEjbName);
        if (helper.isMultiple()) {
            multiplicityManyRadioButton.setSelected(true);
        } else {
            multiplicityOneRadioButton.setSelected(true);
        }
        cascadeDeleteCheckBox.setSelected(helper.isCascadeDelete());

        CmrField field = helper.getCmrField();
        if (field == null) {
            origFieldName = null;
            origFieldType = null;
            createCmrFieldCheckBox.setSelected(false);
            fieldNameTextField.setText(null);
            fieldTypeComboBox.setSelectedItem(null);
        } else {
            origFieldName = field.getCmrFieldName();
            Entity entity = getEntity(origEjbName);
            ClassElement beanClass = Utils.getBeanClass(ejbJarFile, entity);
            MethodElement getterMethod = Utils.getGetterMethod(beanClass, origFieldName);
            MethodElement setterMethod = Utils.getSetterMethod(beanClass, origFieldName, getterMethod);
            origLocalInterface = Utils.getBusinessInterface(entity.getLocal(), ejbJarFile, beanClass);
            origLocalGetterMethod = Utils.getBusinessMethod(origLocalInterface, getterMethod);
            origLocalSetterMethod = Utils.getBusinessMethod(origLocalInterface, setterMethod);
            lastGetter = origLocalGetterMethod != null;
            lastSetter = origLocalSetterMethod != null;
            getterCheckBox.setSelected(lastGetter);
            setterCheckBox.setSelected(lastSetter);


            createCmrFieldCheckBox.setSelected(true);
            fieldNameTextField.setText(field.getCmrFieldName());
            origFieldType = field.getCmrFieldType();
            fieldTypeComboBox.setSelectedItem(origFieldType);
        }

        roleNameTextField2.setText(helper.getRoleName2());
        origEjbName2 = helper.getEjbName2();
        ejbComboBox2.setSelectedItem(origEjbName2);
        if (helper.isMultiple2()) {
            multiplicityManyRadioButton2.setSelected(true);
        } else {
            multiplicityOneRadioButton2.setSelected(true);
        }
        cascadeDeleteCheckBox2.setSelected(helper.isCascadeDelete2());
        CmrField field2 = helper.getCmrField2();
        if (field2 == null) {
            origFieldName2 = null;
            origFieldType2 = null;
            createCmrFieldCheckBox2.setSelected(false);
            fieldNameTextField2.setText(null);
            fieldTypeComboBox2.setSelectedItem(null);
        } else {
            origFieldName2 = field2.getCmrFieldName();
            Entity entity = getEntity(origEjbName2);
            ClassElement beanClass = Utils.getBeanClass(ejbJarFile, entity);
            MethodElement getterMethod = Utils.getGetterMethod(beanClass, origFieldName2);
            MethodElement setterMethod = Utils.getSetterMethod(beanClass, origFieldName2, getterMethod);
            origLocalInterface2 = Utils.getBusinessInterface(entity.getLocal(), ejbJarFile, beanClass);
            origLocalGetterMethod2 = Utils.getBusinessMethod(origLocalInterface2, getterMethod);
            origLocalSetterMethod2 = Utils.getBusinessMethod(origLocalInterface2, setterMethod);
            lastGetter2 = origLocalGetterMethod2 != null;
            lastSetter2 = origLocalSetterMethod2 != null;
            getterCheckBox2.setSelected(lastGetter2);
            setterCheckBox2.setSelected(lastSetter2);

            createCmrFieldCheckBox2.setSelected(true);
            fieldNameTextField2.setText(field2.getCmrFieldName());
            origFieldType2 = field2.getCmrFieldType();
            fieldTypeComboBox2.setSelectedItem(origFieldType2);
        }
    }

    private Entity getEntity(String origEjbName) {
        Entity[] entities = ejbJar.getEnterpriseBeans().getEntity();
        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];
            if (origEjbName.equals(entity.getEjbName())) {
                return entity;
            }
        }
        return null;
    }

    private CmpRelationshipsForm initForm() {
        CmpRelationshipsForm form = new CmpRelationshipsForm();
        relationshipNameTextField = form.getRelationshipNameTextField();
        descriptionTextArea = form.getDescriptionTextArea();

        roleNameTextField = form.getRoleNameTextField();
        ejbComboBox = form.getEjbComboBox();
        multiplicityManyRadioButton = form.getMultiplicityManyRadioButton();
        multiplicityOneRadioButton = form.getMultiplicityOneRadioButton();
        cascadeDeleteCheckBox = form.getCascadeDeleteCheckBox();
        createCmrFieldCheckBox = form.getCreateCmrFieldCheckBox();
        fieldNameTextField = form.getFieldNameTextField();
        fieldTypeComboBox = form.getFieldTypeComboBox();

        roleNameTextField2 = form.getRoleNameTextField2();
        ejbComboBox2 = form.getEjbComboBox2();
        multiplicityManyRadioButton2 = form.getMultiplicityManyRadioButton2();
        multiplicityOneRadioButton2 = form.getMultiplicityOneRadioButton2();
        cascadeDeleteCheckBox2 = form.getCascadeDeleteCheckBox2();
        createCmrFieldCheckBox2 = form.getCreateCmrFieldCheckBox2();
        fieldNameTextField2 = form.getFieldNameTextField2();
        fieldTypeComboBox2 = form.getFieldTypeComboBox2();

        getterCheckBox = form.getGetterCheckBox();
        setterCheckBox = form.getSetterCheckBox();
        getterCheckBox2 = form.getGetterCheckBox2();
        setterCheckBox2 = form.getSetterCheckBox2();
        return form;
    }

    private class RelationshipDialogActionListener implements ActionListener {
        String lastFieldName = fieldNameTextField.getText().trim();
        String lastFieldType = (String) fieldTypeComboBox.getSelectedItem();
        String lastFieldName2 = fieldNameTextField2.getText().trim();
        String lastFieldType2 = (String) fieldTypeComboBox2.getSelectedItem();
        boolean lastCreateField = createCmrFieldCheckBox.isSelected();
        boolean lastCreateField2 = createCmrFieldCheckBox2.isSelected();

        public void actionPerformed(ActionEvent e) {
            boolean createField = createCmrFieldCheckBox.isSelected();
            if (createField != lastCreateField) {
                updateCreateFieldInfo(createField);
            }
            updateCreateFieldType(createField);
            boolean createField2 = createCmrFieldCheckBox2.isSelected();
            if (createField2 != lastCreateField2) {
                updateCreateFieldInfo2(createField2);
            }
            updateCreateFieldType2(createField2);
        }

        private void updateCreateFieldType2(boolean createField2) {
            String fieldType2 = (String) fieldTypeComboBox2.getSelectedItem();
            if (createField2 && multiplicityManyRadioButton2.isSelected()) {
                if (fieldType2 == null) {
                    fieldTypeComboBox2.setSelectedItem(lastFieldType2);
                }
                fieldTypeComboBox2.setEnabled(true);
            } else {
                if (fieldType2 != null) {
                    lastFieldType2 = fieldType2;
                }
                fieldTypeComboBox2.setSelectedItem(null);
                fieldTypeComboBox2.setEnabled(false);
            }
        }

        private void updateCreateFieldType(boolean createField) {
            String fieldType = (String) fieldTypeComboBox.getSelectedItem();
            if (createField && multiplicityManyRadioButton.isSelected()) {
                if (fieldType == null) {
                    fieldTypeComboBox.setSelectedItem(lastFieldType);
                }
                fieldTypeComboBox.setEnabled(true);
            } else {
                if (fieldType != null) {
                    lastFieldType = fieldType;
                }
                fieldTypeComboBox.setSelectedItem(null);
                fieldTypeComboBox.setEnabled(false);
            }
        }

        private void updateCreateFieldInfo2(boolean createField2) {
            lastCreateField2 = createField2;
            if (createField2) {
                fieldNameTextField2.setText(lastFieldName2);
                fieldNameTextField2.setEnabled(true);
                getterCheckBox2.setSelected(lastGetter2);
                getterCheckBox2.setEnabled(true);
                setterCheckBox2.setSelected(lastSetter2);
                setterCheckBox2.setEnabled(true);
            } else {
                lastFieldName2 = fieldNameTextField2.getText().trim();
                lastGetter2 = getterCheckBox2.isSelected();
                lastSetter2 = setterCheckBox2.isSelected();
                fieldNameTextField2.setText(null);
                fieldNameTextField2.setEnabled(false);
                getterCheckBox2.setSelected(false);
                getterCheckBox2.setEnabled(false);
                setterCheckBox2.setSelected(false);
                setterCheckBox2.setEnabled(false);
            }
        }

        private void updateCreateFieldInfo(boolean createField) {
            lastCreateField = createField;
            if (createField) {
                fieldNameTextField.setText(lastFieldName);
                fieldNameTextField.setEnabled(true);
                getterCheckBox.setSelected(lastGetter);
                getterCheckBox.setEnabled(true);
                setterCheckBox.setSelected(lastSetter);
                setterCheckBox.setEnabled(true);
            } else {
                lastFieldName = fieldNameTextField.getText().trim();
                lastGetter = getterCheckBox.isSelected();
                lastSetter = setterCheckBox.isSelected();
                fieldNameTextField.setText(null);
                fieldNameTextField.setEnabled(false);
                getterCheckBox.setSelected(false);
                getterCheckBox.setEnabled(false);
                setterCheckBox.setSelected(false);
                setterCheckBox.setEnabled(false);
            }
        }

        public void updateForm() {
            updateCreateFieldInfo(lastCreateField);
            updateCreateFieldType(lastCreateField);
            updateCreateFieldInfo2(lastCreateField2);
            updateCreateFieldType2(lastCreateField2);
        }
    }
}
