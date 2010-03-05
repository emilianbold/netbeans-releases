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

package org.netbeans.modules.spring.webmvc.editor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author alexeybutenko
 */
public class AddBeanPanelVisual extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    private AddBeanPanel panel;
    private FileObject fileObject;
    /** Creates new form AddBeanPanelVisual */
    public AddBeanPanelVisual(AddBeanPanel panel) {
        this.panel = panel;
        fileObject = NbEditorUtilities.getFileObject(panel.getDocument());
        initComponents();

        idTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }

        });

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
    }


    public boolean showDialog() {
        String id = panel.getId();
        String className = panel.getClassName();
        if (className !=null) {
            classNameTextField.setEditable(false);
        } else {
            className ="";  //NOI18N
        }
        idTextField.setText(id);
        classNameTextField.setText(className);
        String displayName = ""; // NOI18N
        try {
            displayName = NbBundle.getMessage(AddBeanPanelVisual.class, "TTL_Add_Bean_Panel");
        }
        catch (Exception e) {}
        descriptor = new DialogDescriptor
                (this, displayName, true,   // NOI18N
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            collectInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
                     }

                  }
        );
        statusLine = descriptor.createNotificationLineSupport();
        validateInput();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();

        return dialogOK;

    }

    private void collectInput() {
        panel.setClassName(classNameTextField.getText());
        panel.setId(idTextField.getText());
    }


    private void validateInput() {
        if (descriptor == null)
            return;
        if (idTextField.getText().length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddBeanPanelVisual.class,"Error_Empty_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }

        if (idExist()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_not_uniq_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (classNameTextField.getText().length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddBeanPanelVisual.class,"Error_Empty_Class")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (!validClass()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_No_Such_class")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (beanExist()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_Bean_Already_exist")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        statusLine.clearMessages();
        descriptor.setValid(true);
    }

    private boolean idExist() {
        SpringScope scope = SpringScope.getSpringScope(fileObject);
        final String id = idTextField.getText();
        final boolean[] found = {false};
        for (SpringConfigModel model: scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans beans) {
                        SpringBean bean = beans.findBean(id);
                        if (bean !=null)
                            found[0]=true;
                    }
                });
                if (found[0]) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return found[0];
    }

    private boolean beanExist() {
        final boolean[] found = {false};
        SpringScope scope = SpringScope.getSpringScope(fileObject);
        final String className = classNameTextField.getText();
        if (className == null || "".equals(className)) {
            return false;
        }
        for (SpringConfigModel model: scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans beans) {
                        for (SpringBean bean : beans.getBeans()){
                            if (className.equals(bean.getClassName())) {
                                found[0]=true;
                                break;
                            }
                        }
                    }
                });
                if (found[0]) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return found[0];
    }

    private boolean validClass() {
        final boolean[] result ={false};
        final String name = classNameTextField.getText();
        JavaSource js = JavaSource.create(ClasspathInfo.create(fileObject));
        if (js ==null) {
            return result[0];
        }
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws Exception {
                    result[0] = parameter.getElements().getTypeElement(name) != null;
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result[0];
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(420, 120));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(idTextField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);

        idTextField.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.idTextField.text")); // NOI18N
        idTextField.setMinimumSize(new java.awt.Dimension(200, 27));
        idTextField.setPreferredSize(new java.awt.Dimension(450, 27));
        idTextField.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(idTextField, gridBagConstraints);

        jLabel2.setLabelFor(classNameTextField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel2, gridBagConstraints);

        classNameTextField.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.classNameTextField.text")); // NOI18N
        classNameTextField.setPreferredSize(new java.awt.Dimension(450, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(classNameTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextField idTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

}
