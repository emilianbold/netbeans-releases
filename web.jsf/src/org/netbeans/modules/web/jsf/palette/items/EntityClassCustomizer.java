/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * 
 * @author  Po-Ting Wu
 */
public final class EntityClassCustomizer extends javax.swing.JPanel {
    private static final ResourceBundle bundle = NbBundle.getBundle(EntityClassCustomizer.class);

    private Dialog dialog = null;
    private boolean dialogOK = false;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;

    private final EntityClass entityClass;
    private final JTextComponent target;
    private final FileObject targetFileObject;
    private final boolean hasModuleJsf;
    private final Vector<String> beans = new Vector<String>();
            
    public EntityClassCustomizer(EntityClass entityClass, JTextComponent target) {
        this.entityClass = entityClass;
        this.target = target;
        targetFileObject = EntityClass.getFO(target);
        hasModuleJsf = JSFConfigUtilities.hasJsfFramework(targetFileObject);
        
        initComponents();
        Mnemonics.setLocalizedText(empty, bundle.getString("LBL_Empty" + entityClass.getName())); // NOI18N
        Mnemonics.setLocalizedText(fromBean, bundle.getString("LBL_FromEntity" + entityClass.getName())); // NOI18N
        empty.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Empty" + entityClass.getName())); // NOI18N
        empty.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Empty" + entityClass.getName())); // NOI18N
        fromBean.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Generated" + entityClass.getName())); // NOI18N
        fromBean.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Generated" + entityClass.getName())); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Jsf" + entityClass.getName())); // NOI18N
        if (!"Form".equals(entityClass.getName())) { // NOI18N
            jLabel2.setVisible(false);
            edit.setVisible(false);
            detail.setVisible(false);
        }
        
        entityClassName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate (DocumentEvent documentEvent) { updateBeanName(); } 
            public void removeUpdate (DocumentEvent documentEvent) { updateBeanName(); } 
            public void changedUpdate(DocumentEvent documentEvent) { updateBeanName(); }
        });
        
        managedBeanName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate (DocumentEvent documentEvent) { checkStatus(); } 
            public void removeUpdate (DocumentEvent documentEvent) { checkStatus(); } 
            public void changedUpdate(DocumentEvent documentEvent) { checkStatus(); }
        });
        
        initBeans();
    }
    
    private void initBeans() {
        if (targetFileObject == null) {
            return;
        }

        WebModule webModule = WebModule.getWebModule(targetFileObject);
        ArrayList<ManagedBean> items = (ArrayList<ManagedBean>) JSFBeanCache.getBeans(webModule);
        for (ManagedBean item : items) {
            beans.add(item.getManagedBeanName());
        }

        managedBeanComboBox.setModel(new DefaultComboBoxModel(beans));
    }
    
    public boolean showDialog() {
        dialogOK = false;
        String displayName = bundle.getString("NAME_jsp-Jsf" + entityClass.getName()); // NOI18N
        descriptor = new DialogDescriptor
                (this, bundle.getString("LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent actionEvent) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
                     }
                }
        );
        statusLine = descriptor.createNotificationLineSupport();
        checkStatus();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        String className = entityClassName.getText();
        entityClass.setBean(className);
        
        int formType = empty.isSelected() ? EntityClass.FORM_TYPE_EMPTY
                                          : detail.isSelected() ? EntityClass.FORM_TYPE_DETAIL : EntityClass.FORM_TYPE_NEW;
        entityClass.setFormType(formType);

        if (formType == EntityClass.FORM_TYPE_EMPTY)
            return;
        
        if (managedBeanCreate.isSelected()) {
            String beanName = managedBeanName.getText();
            entityClass.setVariable(beanName);
            entityClass.addManagedBean(targetFileObject, beanName, className);
        } else if (managedBeanRegistered.isSelected()) {
            entityClass.setVariable((String) managedBeanComboBox.getSelectedItem());
        } else {
            entityClass.setVariable("anInstanceOf." + className); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        populate = new javax.swing.ButtonGroup();
        viewType = new javax.swing.ButtonGroup();
        managedBeanType = new javax.swing.ButtonGroup();
        empty = new javax.swing.JRadioButton();
        fromBean = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        entityClassName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        managedBeanRegistered = new javax.swing.JRadioButton();
        managedBeanCreate = new javax.swing.JRadioButton();
        managedBeanNo = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        edit = new javax.swing.JRadioButton();
        detail = new javax.swing.JRadioButton();
        managedBeanComboBox = new javax.swing.JComboBox();
        managedBeanName = new javax.swing.JTextField();

        jFileChooser1.setCurrentDirectory(null);

        populate.add(empty);
        empty.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/palette/items/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(empty, bundle.getString("LBL_EmptyForm")); // NOI18N
        empty.setMargin(new java.awt.Insets(0, 0, 0, 0));
        empty.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectItemStateChanged(evt);
            }
        });

        populate.add(fromBean);
        org.openide.awt.Mnemonics.setLocalizedText(fromBean, bundle.getString("LBL_FromEntityForm")); // NOI18N
        fromBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromBean.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectItemStateChanged(evt);
            }
        });

        jLabel1.setLabelFor(entityClassName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_GetProperty_Bean")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, bundle.getString("LBL_Browse")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("LBL_ManagedBean")); // NOI18N

        managedBeanType.add(managedBeanRegistered);
        org.openide.awt.Mnemonics.setLocalizedText(managedBeanRegistered, bundle.getString("LBL_ManagedBean_Registered")); // NOI18N
        managedBeanRegistered.setMargin(new java.awt.Insets(0, 0, 0, 0));
        managedBeanRegistered.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectItemStateChanged(evt);
            }
        });

        managedBeanType.add(managedBeanCreate);
        managedBeanCreate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(managedBeanCreate, bundle.getString("LBL_ManagedBean_Create")); // NOI18N
        managedBeanCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        managedBeanCreate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectItemStateChanged(evt);
            }
        });

        managedBeanType.add(managedBeanNo);
        org.openide.awt.Mnemonics.setLocalizedText(managedBeanNo, bundle.getString("LBL_ManagedBean_No")); // NOI18N
        managedBeanNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        managedBeanNo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("LBL_From_Fields")); // NOI18N

        viewType.add(edit);
        org.openide.awt.Mnemonics.setLocalizedText(edit, bundle.getString("LBL_View_Edit")); // NOI18N
        edit.setMargin(new java.awt.Insets(0, 0, 0, 0));

        viewType.add(detail);
        detail.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(detail, bundle.getString("LBL_View_Detail")); // NOI18N
        detail.setMargin(new java.awt.Insets(0, 0, 0, 0));

        managedBeanComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(managedBeanNo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                        .add(343, 343, 343))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(detail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 168, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(edit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(managedBeanCreate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 209, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(13, 13, 13))
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(managedBeanRegistered, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(19, 19, 19)))
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(managedBeanName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                    .add(managedBeanComboBox, 0, 232, Short.MAX_VALUE)))
                            .add(entityClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(entityClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(managedBeanCreate)
                    .add(managedBeanName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(managedBeanComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(managedBeanRegistered))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(managedBeanNo)
                .add(21, 21, 21)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(detail)
                    .add(jLabel2)
                    .add(edit))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_EntytyClass")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_EntytyClass")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Browse")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Browse")); // NOI18N
        managedBeanRegistered.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ManagedBean_Registered")); // NOI18N
        managedBeanRegistered.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBean_Registered")); // NOI18N
        managedBeanCreate.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ManagedBean_Create")); // NOI18N
        managedBeanCreate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBean_Create")); // NOI18N
        managedBeanNo.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ManagedBean_No")); // NOI18N
        managedBeanNo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBean_No")); // NOI18N
        edit.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Editable")); // NOI18N
        edit.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Editable")); // NOI18N
        detail.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ReadOnly")); // NOI18N
        detail.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ReadOnly")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(empty))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(fromBean))
                    .add(layout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .add(empty)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromBean)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        empty.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_EmptyForm")); // NOI18N
        empty.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_EmptyForm")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_GeneratedForm")); // NOI18N
        fromBean.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_GeneratedForm")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JsfForm")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ClasspathInfo cpInfo = ClasspathInfo.create(targetFileObject);
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {                                            
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            entityClassName.setText(handle.getQualifiedName());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void selectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectItemStateChanged
        checkStatus();
    }//GEN-LAST:event_selectItemStateChanged

    /**
     * Update the managed bean name when modifying the entity class name
     * & automatically select the managed bean name if it's already there.
     */
    private void updateBeanName() {
        String name = entityClassName.getText();
        int index = name.lastIndexOf(".");
        if (index != -1) {
            name = name.substring(index+1);
        }

        managedBeanName.setText(name);

        for (String beanName : beans) {
            if (name.equals(beanName)) {
                managedBeanRegistered.setSelected(true);
                managedBeanComboBox.setSelectedItem(beanName);
                return;
            }
        }
    }
    
    /**
     * Enabling or disabling the UI depends on whether it's editable or not
     * & issue mesgs when the values are not acceptable.
     */
    private void checkStatus() {
        if (empty.isSelected()) {
            entityClassName.setEnabled(false);
            jButton1.setEnabled(false);
            managedBeanCreate.setEnabled(false);
            managedBeanName.setEnabled(false);
            managedBeanRegistered.setEnabled(false);
            managedBeanComboBox.setEnabled(false);
            managedBeanNo.setEnabled(false);
            detail.setEnabled(false);
            edit.setEnabled(false);
        } else {
            entityClassName.setEnabled(true);
            jButton1.setEnabled(true);
            managedBeanCreate.setEnabled(true);
            managedBeanName.setEnabled(managedBeanCreate.isSelected());
            managedBeanRegistered.setEnabled(beans.size() > 0);
            managedBeanComboBox.setEnabled(managedBeanRegistered.isSelected() && beans.size() > 0);
            managedBeanNo.setEnabled(true);
            detail.setEnabled(true);
            edit.setEnabled(true);
        }

        boolean validClassName = false;
        try {
            validClassName = empty.isSelected() || entityClass.classExists(targetFileObject, entityClassName.getText());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        boolean validBeanName = empty.isSelected() || !managedBeanCreate.isSelected() || validBean(targetFileObject, managedBeanName.getText());

        descriptor.setValid(hasModuleJsf && validClassName && validBeanName);
        statusLine.clearMessages();
        if (!hasModuleJsf) {
            statusLine.setErrorMessage(bundle.getString("MSG_NoJSF")); //NOI18N
        } else if (!validClassName) {
            if (entityClassName.getText().length() < 1)
                statusLine.setInformationMessage(bundle.getString("MSG_EmptyClassName"));  //NOI18N
            else
                statusLine.setErrorMessage(bundle.getString("MSG_InvalidClassName"));  //NOI18N
        } else if (!validBeanName) {
            if (managedBeanName.getText().length() < 1)
                statusLine.setInformationMessage(bundle.getString("MSG_EmptyBeanName"));  //NOI18N
            else
                statusLine.setErrorMessage(bundle.getString("MSG_ExistBeanName"));  //NOI18N
        }
    }
    
    public boolean validBean(FileObject referenceFO, String name) {
        if (name.length() == 0) {
            return false;
        }

        for (String beanName : beans) {
            if (name.equals(beanName)) {
                return false;
            }
        }

        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton detail;
    private javax.swing.JRadioButton edit;
    private javax.swing.JRadioButton empty;
    private javax.swing.JTextField entityClassName;
    private javax.swing.JRadioButton fromBean;
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JComboBox managedBeanComboBox;
    private javax.swing.JRadioButton managedBeanCreate;
    private javax.swing.JTextField managedBeanName;
    private javax.swing.JRadioButton managedBeanNo;
    private javax.swing.JRadioButton managedBeanRegistered;
    private javax.swing.ButtonGroup managedBeanType;
    private javax.swing.ButtonGroup populate;
    private javax.swing.ButtonGroup viewType;
    // End of variables declaration//GEN-END:variables
}
