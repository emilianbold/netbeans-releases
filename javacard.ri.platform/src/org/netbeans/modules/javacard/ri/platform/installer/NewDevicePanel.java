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
package org.netbeans.modules.javacard.ri.platform.installer;

import org.netbeans.modules.javacard.common.KeysAndValues;
import org.netbeans.modules.javacard.common.GuiUtils;
import java.awt.BorderLayout;
import javax.swing.text.Document;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.conversion.Converter;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class NewDevicePanel extends javax.swing.JPanel {

    private final DevicePropertiesPanel pnl = new DevicePropertiesPanel();

    @SuppressWarnings("unchecked")
    public NewDevicePanel(FileObject targetFolder) {
        initComponents();
        jPanel1.add(pnl, BorderLayout.CENTER);
        ValidationGroup group = pnl.getValidationGroup();
        Converter <String, Document> c = Converter.find(String.class, Document.class);
        Validator<Document> v = c.convert(
                ValidatorUtils.merge(StringValidators.REQUIRE_NON_EMPTY_STRING,
                StringValidators.MAY_NOT_START_WITH_DIGIT, StringValidators.REQUIRE_VALID_FILENAME),
                new NonDuplicateFileValidator(targetFolder));

        group.add (displayNameField, v);
        displayNameField.setName (jLabel1.getText());
        GuiUtils.prepareContainer(this);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.AddNewDevice"); //NOI18N
    }

    public SwingValidationGroup getValidationGroup() {
        return pnl.getValidationGroup();
    }

    private static final class NonDuplicateFileValidator extends AbstractValidator<String> {
        private final FileObject targetFolder;
        NonDuplicateFileValidator (FileObject targetFolder) {
            super(String.class);
            this.targetFolder = targetFolder;
        }

        @Override public void validate(Problems p, String compName, String model) {
            boolean result = targetFolder.getFileObject(model.trim(), JCConstants.JAVACARD_DEVICE_FILE_EXTENSION) == null;
            if (!result) {
                p.add(NbBundle.getMessage(NewDevicePanel.class,
                    "ERR_DEVICE_EXISTS", model)); //NOI18N);
            }
        }

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
        displayNameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewDevicePanel.class, "NewDevicePanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 5);
        add(jLabel1, gridBagConstraints);

        displayNameField.setText(org.openide.util.NbBundle.getMessage(NewDevicePanel.class, "NewDevicePanel.displayNameField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 5, 12);
        add(displayNameField, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables


    public void read (KeysAndValues<?> data) {
        pnl.read(data);
    }

    public void write (KeysAndValues<?> data) {
        pnl.write(data);
        data.put(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME, displayNameField.getText());
    }

    public String getDeviceName() {
        return displayNameField.getText().trim();
    }
}
