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
import org.netbeans.modules.xml.multiview.ItemCheckBoxHelper;
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
    private Entity entity;

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

        addRefreshable(new ItemEditorHelper(ejbNameTextField, new TextItemEditorModel(dataObject, false) {
            protected String getValue() {
                return entity.getEjbName();
            }

            protected void setValue(String value) {
                entity.setEjbName(value);
            }
        }));

        persistenceTypeTextField.setEnabled(false);
        this.entity = entity;
        String persistenceType = this.entity.getPersistenceType();
        boolean isCmp = Entity.PERSISTENCE_TYPE_CONTAINER.equals(persistenceType);
        persistenceTypeTextField.setText(persistenceType + ((isCmp ? " (CMP)" : " (BMP)")));    //NOI18N

        addRefreshable(new ItemEditorHelper(abstractSchemaNameTextField, new TextItemEditorModel(dataObject, false) {
            protected String getValue() {
                return entity.getAbstractSchemaName();
            }

            protected void setValue(String value) {
                entity.setAbstractSchemaName(value);
            }
        }));

        if (isCmp) {
            primaryKeyFieldLabel.setVisible(true);
            primaryKeyFieldComboBox.setVisible(true);
            primaryKeyClassComboBox.setVisible(true);
            primaryKeyClassTextField.setVisible(false);

            primaryKeyFieldComboBox.addItem(Utils.getBundleMessage("LBL_Compound_PK"));
            CmpField[] cmpFields = entity.getCmpField();
            for (int i = 0; i < cmpFields.length; i++) {
                CmpField cmpField = cmpFields[i];
                primaryKeyFieldComboBox.addItem(cmpField.getFieldName());
                primaryKeyFieldComboBox.setEditor(new ValidatingTextField());
            }
            addRefreshable(new ItemComboBoxHelper(dataObject, primaryKeyFieldComboBox) {
                public String getItemValue() {
                    return entity.getPrimkeyField();
                }

                public void setItemValue(String value) {
                    entity.setPrimkeyField(value);
                }
            });
            primaryKeyFieldComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = primaryKeyFieldComboBox.getSelectedIndex();
                    if (selectedIndex == 0) {
                        primaryKeyClassComboBox.setEnabled(true);
                        primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
                    } else {
                        primaryKeyClassComboBox.setEnabled(false);
                        CmpField cmpField = entity.getCmpField(selectedIndex - 1);
                        CmpFieldHelper helper = new CmpFieldHelper(entityHelper, cmpField);
                        entityHelper.setPrimKeyClass(helper.getType());
                    }
                    primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
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

            addRefreshable(new ItemComboBoxHelper(dataObject, primaryKeyClassComboBox) {
                public String getItemValue() {
                    return entity.getPrimKeyClass();
                }

                public void setItemValue(String value) {
                    if (Utils.isValidPackageName(value)) {
                        entity.setPrimKeyClass(value);
                    } else {
                        primaryKeyClassComboBox.setSelectedItem(getItemValue());
                    }
                }
            });

        } else {
            primaryKeyFieldLabel.setVisible(false);
            primaryKeyFieldComboBox.setVisible(false);
            primaryKeyClassComboBox.setVisible(false);
            primaryKeyClassTextField.setVisible(true);

            addRefreshable(new ItemEditorHelper(primaryKeyClassTextField, new TextItemEditorModel(dataObject, false) {
                protected String getValue() {
                    return entity.getPrimKeyClass();
                }

                protected void setValue(String value) {
                    entity.setPrimKeyClass(value);
                }
            }));
        }
        addRefreshable(new ItemCheckBoxHelper(dataObject, getReentrantCheckBox()) {
            public boolean getItemValue() {
                return entity.isReentrant();
            }

            public void setItemValue(boolean value) {
                entity.setReentrant(value);
            }
        });
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public void refreshView() {
        super.refreshView();
        getReentrantCheckBox().setSelected(entity.isReentrant());
    }
}
