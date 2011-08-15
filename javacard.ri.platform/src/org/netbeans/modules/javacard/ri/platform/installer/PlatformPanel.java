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

import java.awt.Component;
import java.awt.EventQueue;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public class PlatformPanel extends javax.swing.JPanel implements FocusListener, DocumentListener {

    private final ChangeSupport supp = new ChangeSupport(this);
    private final PlatformValidator validator;
    private FileObject baseDir;

    public PlatformPanel(FileObject fo) {
        this.baseDir = fo;
        validator = new PlatformValidatorImpl(fo);
        initComponents();
        displayNameField.addFocusListener(this);
        infoField.addFocusListener(this);
        displayNameField.setEnabled(false);
        displayNameField.getDocument().addDocumentListener(this);
        locationField.setText(baseDir == null ? "" : baseDir.getPath()); //NOI18N
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.SettingUpJavaCardPlatform"); //NOI18N
        infoField.getCaret().setVisible(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (baseDir != null) {
            if (!validator.hasRun()) {
                validatePlatform();
            }
        } else {
            enableControls(false);
            setProblem (NbBundle.getMessage(PlatformPanel.class,
                    "MSG_NO_EMULATOR", baseDir.toString())); //NOI18N
        }
    }

    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    public void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }

    boolean isProblem() {
        return problemLbl.getText().trim().length() > 0;
    }

    void setDisplayName(String nm) {
        displayNameField.setText(nm);
    }

    void setProblem(String txt) {
        boolean wasProblem = isProblem();
        txt = txt == null ? "" : txt;
        problemLbl.setText(txt);
        if (wasProblem != isProblem()) {
            fireChange();
        }
    }

    void enableControls(boolean val) {
        for (Component c : getComponents()) {
            if (c != jProgressBar1 && c != infoField && c != problemLbl) {
                c.setEnabled(val);
            }
        }
        change();
    }

    private void change() {
        String key = null;
        String name = displayNameField.getText().trim();
        if (validator.failed()) {
            String msg = validator.failMessage();
            if (msg != null) {
                setProblem(msg);
                return;
            }
            key = "MSG_BAD_PLATFORM"; //NOI18N
        } else if (validator.isRunning()) {
            key = "MSG_LOADING"; //NOI18N
        } else if (name.length() == 0) {
            key = "MSG_NO_NAME"; //NOI18N
        } else if (name.contains("/") || name.contains("\\") || name.contains(":") || //NOI18N
                name.contains(";") || name.contains(File.separator) || //NOI18N
                name.contains (File.pathSeparator)) {
            //The name will be used as a filename.  Disallow known path and path separator characters
            key = "MSG_NO_SLASHES"; //NOI18N
        } else if (platformFileExists(name)) {
            key = "MSG_PLATFORM_EXISTS";
        }
        String path = baseDir == null ? "" : baseDir.getPath();
        String msg = key == null ? null : NbBundle.getMessage(PlatformPanel.class, key, path);
        setProblem(msg);
    }

    private boolean platformFileExists (String name) {
        String nm = name.replace (' ', '_') + "." + JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION;
        final FileObject platformsFolder = FileUtil.getConfigFile(
                CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
        FileObject platformFile = platformsFolder.getFileObject(nm);
        return (nm != null && platformFile != null);
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

        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        versionLabel = new javax.swing.JLabel();
        infoPane = new javax.swing.JScrollPane();
        infoField = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        problemLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();

        displayNameLabel.setDisplayedMnemonic('D');
        displayNameLabel.setLabelFor(displayNameField);
        displayNameLabel.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameLabel.text")); // NOI18N

        displayNameField.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameField.text")); // NOI18N
        displayNameField.setToolTipText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.displayNameField.toolTipText")); // NOI18N

        versionLabel.setDisplayedMnemonic('V');
        versionLabel.setLabelFor(infoField);
        versionLabel.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.versionLabel.text")); // NOI18N

        infoField.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        infoField.setColumns(20);
        infoField.setEditable(false);
        infoField.setLineWrap(true);
        infoField.setRows(5);
        infoField.setWrapStyleWord(true);
        infoPane.setViewportView(infoField);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jProgressBar1.setIndeterminate(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jProgressBar1, gridBagConstraints);

        problemLbl.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        problemLbl.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.problemLbl.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 180;
        gridBagConstraints.ipady = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(problemLbl, gridBagConstraints);

        jLabel1.setDisplayedMnemonic('L');
        jLabel1.setLabelFor(locationField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.jLabel1.text")); // NOI18N

        locationField.setEditable(false);
        locationField.setText(org.openide.util.NbBundle.getMessage(PlatformPanel.class, "PlatformPanel.locationField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(displayNameLabel)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                            .addComponent(displayNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
                    .addComponent(versionLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayNameLabel)
                    .addComponent(displayNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(versionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoPane, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextArea infoField;
    private javax.swing.JScrollPane infoPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel problemLbl;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    public void focusGained(FocusEvent e) {
        if (e.getComponent() instanceof JTextComponent) {
            ((JTextComponent) e.getComponent()).selectAll();
        }
    }

    public String getDisplayName() {
        return displayNameField.getText();
    }

    public void focusLost(FocusEvent e) {
        if (displayNameField == e.getOppositeComponent()) {
            change();
        }
    }

    private void validatePlatform() {
        if (!validator.hasRun()) {
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
            enableControls(false);
            validator.start();
            invalidate();
            revalidate();
            repaint();
        }
    }
    volatile boolean failed;

    public void insertUpdate(DocumentEvent e) {
        change();
        firePropertyChange("displayName", null, getDisplayName()); //NOI18N
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }
    PlatformInfo platformInfo;

    PlatformInfo getPlatformInfo() {
        return platformInfo;
    }

    private final class PlatformValidatorImpl extends PlatformValidator {

        PlatformValidatorImpl(FileObject baseDir) {
            super(baseDir);
        }

        @Override
        void onStart() {
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
            enableControls(false);
            setProblem(NbBundle.getMessage(PlatformValidatorImpl.class,"MSG_VALIDATING")); //NOI18N
            invalidate();
            revalidate();
            repaint();
        }

        @Override
        void onFail(Exception e) {
            assert EventQueue.isDispatchThread();
            infoField.setText(getStandardOutput() + "\n" + getErrorOutput()); //NOI18N
            setProblem(e.getMessage());
            failMessage = e.getLocalizedMessage();
            e.printStackTrace();
        }

        @Override
        void onSucceed(String stdout) {
            assert !EventQueue.isDispatchThread();
            platformInfo = getPlatformInfo(stdout, getPlatformProps());
            PlatformPanel.this.fireChange();
        }

        @Override
        void onDone() {
            assert EventQueue.isDispatchThread();
            enableControls(true);
            if (platformInfo != null) {
                displayNameField.setText(platformInfo.getName());
            }
            infoField.setText(getStandardOutput());
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
            invalidate();
            revalidate();
            repaint();
            change();
        }
    }

    PlatformInfo getPlatformInfo(String out, EditableProperties props) {
        return new PlatformInfo (props);
    }
}