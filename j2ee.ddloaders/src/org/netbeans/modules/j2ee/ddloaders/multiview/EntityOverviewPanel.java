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

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EntityOverviewForm;
import org.netbeans.modules.xml.multiview.ItemComboBoxHelper;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.tools.generator.ValidatingTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class EntityOverviewPanel extends EntityOverviewForm {

    private EjbJarMultiViewDataObject dataObject;
    private static final String COMPOUND = "compound"; //NOI18N

    /**
     * Creates new form EntityOverviewForm
     */
    public EntityOverviewPanel(SectionNodeView sectionNodeView, final Entity entity, final EntityHelper entityHelper) {
        super(sectionNodeView);
        dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();

        JTextField ejbNameTextField = getEjbNameTextField();
        JTextField persistenceTypeTextField = getPersistenceTypeTextField();
        JTextField abstractSchemaNameTextField = getAbstractSchemaNameTextField();
        JLabel primaryKeyFieldLabel = getPrimaryKeyFieldLabel();
        final JComboBox primaryKeyFieldComboBox = getPrimaryKeyFieldComboBox();
        final JComboBox primaryKeyClassComboBox = getPrimaryKeyClassComboBox();
        final JTextField primaryKeyClassTextField = getPrimaryKeyClassTextField();
        final JCheckBox reentrantCheckBox = getReentrantCheckBox();

        new ItemEditorHelper(ejbNameTextField, new TextItemEditorModel(dataObject, false) {
            protected String getValue() {
                return entity.getEjbName();
            }

            protected void setValue(String value) {
                entity.setEjbName(value);
            }
        });

        persistenceTypeTextField.setEnabled(false);
        String persistenceType = entity.getPersistenceType();
        boolean isCmp = Entity.PERSISTENCE_TYPE_CONTAINER.equals(persistenceType);
        persistenceTypeTextField.setText(persistenceType + ((isCmp ? " (CMP)" : " (BMP)")));    //NOI18N

        new ItemEditorHelper(abstractSchemaNameTextField, new TextItemEditorModel(dataObject, false) {
            protected String getValue() {
                return entity.getAbstractSchemaName();
            }

            protected void setValue(String value) {
                entity.setAbstractSchemaName(value);
            }
        });

        if (isCmp) {
            primaryKeyFieldLabel.setVisible(true);
            primaryKeyFieldComboBox.setVisible(true);
            primaryKeyFieldComboBox.setVisible(true);
            primaryKeyClassTextField.setVisible(false);

            primaryKeyFieldComboBox.addItem(COMPOUND);
            CmpField[] cmpFields = entity.getCmpField();
            for (int i = 0; i < cmpFields.length; i++) {
                CmpField cmpField = cmpFields[i];
                primaryKeyFieldComboBox.addItem(cmpField.getFieldName());
                primaryKeyFieldComboBox.setEditor(new ValidatingTextField());
            }
            new ItemComboBoxHelper(primaryKeyFieldComboBox, dataObject) {

                public String getItemValue() {
                    return entity.getPrimkeyField();
                }

                public void setItemValue(String value) {
                    entity.setPrimkeyField(value);
                }
            };
            primaryKeyFieldComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = primaryKeyFieldComboBox.getSelectedIndex();
                    if (selectedIndex == 0) {
                        primaryKeyClassComboBox.setEnabled(true);
                        primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
                    } else {
                        primaryKeyClassComboBox.setEnabled(false);
                        CmpField cmpField = entity.getCmpField(selectedIndex - 1);
                        CmpFieldHelper helper = new CmpFieldHelper(entityHelper,
                                cmpField);
                        entity.setPrimKeyClass(helper.getType());
                    }
                    primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
                    dataObject.modelUpdatedFromUI();
                }
            });
            primaryKeyClassComboBox.setEnabled(primaryKeyFieldComboBox.getSelectedIndex() == 0);

            primaryKeyClassComboBox.addItem("boolean");             //NOI18N
            primaryKeyClassComboBox.addItem("byte");                //NOI18N
            primaryKeyClassComboBox.addItem("char");                //NOI18N
            primaryKeyClassComboBox.addItem("double");              //NOI18N
            primaryKeyClassComboBox.addItem("float");               //NOI18N
            primaryKeyClassComboBox.addItem("int");                 //NOI18N
            primaryKeyClassComboBox.addItem("long");                //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Boolean");   //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Byte");      //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Character"); //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Double");    //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Float");     //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Integer");   //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Long");      //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Object");    //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.String");    //NOI18N
            primaryKeyClassComboBox.addItem("java.math.BigDecimal");//NOI18N

            new ItemComboBoxHelper(primaryKeyClassComboBox, dataObject) {

                public String getItemValue() {
                    return entity.getPrimKeyClass();
                }

                public void setItemValue(String value) {
                    entity.setPrimKeyClass(value);
                }
            };

            primaryKeyClassComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dataObject.modelUpdatedFromUI();
                }
            });

        } else {
            primaryKeyFieldLabel.setVisible(false);
            primaryKeyFieldComboBox.setVisible(false);
            primaryKeyFieldComboBox.setVisible(false);
            primaryKeyClassTextField.setVisible(true);

            new ItemEditorHelper(primaryKeyClassTextField, new TextItemEditorModel(dataObject, false) {
                protected String getValue() {
                    return entity.getPrimKeyClass();
                }

                protected void setValue(String value) {
                    entity.setPrimKeyClass(value);
                }
            });
        }
        reentrantCheckBox.setSelected(entity.isReentrant());
        reentrantCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                entity.setReentrant(reentrantCheckBox.isSelected());
                dataObject.modelUpdatedFromUI();
            }
        });
    }

}
