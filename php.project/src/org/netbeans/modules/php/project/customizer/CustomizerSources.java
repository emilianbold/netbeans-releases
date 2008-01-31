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

package org.netbeans.modules.php.project.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.ui.SourceRootsUi;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 *
 * @author  ads
 */
class CustomizerSources extends JPanel {

    private static final String TIP_FULL_SOURCE_PATH = "TIP_SourcePath"; // NOI18N
    private static final String LBL_SELECT_SOURCE_FOLDER = "LBL_Select_Source_Folder_Title"; // NOI18N
    private static final String BROWSE = "BROWSE"; // NOI18N
    private static final long serialVersionUID = 3571079774390893812L;


    CustomizerSources(PhpProjectProperties uiProperties) {
        initComponents();
        init();
        load(uiProperties);
    }

    private void init(){
        //text field is not editable. But we set it's BG color to look as it is editable.
        // To show that it can ber changed (at least using button)
        mySourceFolder.setBackground(getTextFieldBgColor());
        
        myEncoding.setModel(new EncodingModel());
        myEncoding.setRenderer(new EncodingRenderer());
        

        myEncoding.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }            
        });
        
    }
    
    private void handleEncodingChange () {
            Charset enc = (Charset) myEncoding.getSelectedItem();
            String encName;
            if (enc != null) {
                encName = enc.name();
            }
            else {
                encName = myOriginalEncoding;
            }
            getProperties().setProperty(PhpProject.SOURCE_ENCODING, encName);
    }

    private void load(PhpProjectProperties uiProperties) {
        myProps = uiProperties;

        PhpProject project = getProperties().getProject();

        // load project path
        FileObject projectObject = project.getProjectDirectory();
        String projectPath = FileUtil.toFile(projectObject).getAbsolutePath();
        myProjectFolder.setText(projectPath);
        myProjectFolder.setToolTipText(projectPath);

        // set source path
        setSources(project);

        // set encoding
        setEncoding(uiProperties);
    }
    private void setEncoding(PhpProjectProperties properties) {
        Charset select = Charset.defaultCharset();
        myOriginalEncoding = properties.getProperty(PhpProject.SOURCE_ENCODING);
        
        if (myOriginalEncoding != null) {
            if (myEncoding.getModel() instanceof EncodingModel) {
                
                EncodingModel model = (EncodingModel) myEncoding.getModel();
                Charset existing = model.getElementByName(myOriginalEncoding);
                if (existing != null) {
                    select = existing;
                } else {
                    select = new UnknownCharset(myOriginalEncoding);
                    model.addElement(select);
                }
            }
        }
        myEncoding.setSelectedItem(select);
        
    }

    private void setSources(PhpProject project) {
        /*
         * XXX : here is only first source root is used!
         */
        FileObject[] sources = Utils.getSourceObjects(project);
        if (sources == null || sources.length == 0) {
            return;
        }
        myLastUsedSourceDir = sources[0];
        viewSourcesRoot(sources[0]);
    }

    private void viewSourcesRoot(FileObject sourceFolder) {
        FileObject projectFolder = getProperties().getProject().getProjectDirectory();
        String sourceFullPath = FileUtil.toFile(sourceFolder).getAbsolutePath();

        if (FileUtil.isParentOf(projectFolder, sourceFolder)) {
            mySourceFolder.setText(FileUtil.getRelativePath(projectFolder, sourceFolder));
        //} else if (projectFolder.equals(sourceFolder)) {
        //    mySourceFolder.setText("."); // NOI18N
        } else {
            mySourceFolder.setText(sourceFullPath);
        }

        String message = NbBundle.getMessage(CustomizerSources.class, TIP_FULL_SOURCE_PATH);
        String tip = MessageFormat.format(message, sourceFullPath);
        mySourceFolder.setToolTipText(tip);
    }


    private PhpProjectProperties getProperties() {
        return myProps;
    }

    private String getMessage(String key, Object... args) {
        String message = null;
        if (args.length > 0) {
            message = MessageFormat.format(NbBundle.getMessage(CustomizerSources.class, key), args);
        } else {
            message = NbBundle.getMessage(CustomizerSources.class, key);
        }
        return message;
    }

    private void sourceFolderChanged() {
        String sourceFolder = mySourceFolder.getText();


        if (sourceFolder != null) {
            FileObject fileObject = getProperties().getProject().getHelper().resolveFileObject(sourceFolder);
            if (fileObject != null && fileObject.isValid()) {
                getProperties().setProperty(PhpProject.SRC, sourceFolder);
            }
        }

        //getProperties().setProperty(PhpProject.SRC, sourceFolder);
    }

    private Color getTextFieldBgColor(){
        JTextField tf = new JTextField();
        tf.setEditable(true);
        tf.setEnabled(true);
        return tf.getBackground();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myProjectFolderLabel = new javax.swing.JLabel();
        myProjectFolder = new javax.swing.JTextField();
        mySourceFolderLabel = new javax.swing.JLabel();
        mySourceFolder = new javax.swing.JTextField();
        myBrowse = new javax.swing.JButton();
        myEncodingLabel = new javax.swing.JLabel();
        myEncoding = new javax.swing.JComboBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(myProjectFolderLabel, bundle.getString("LBL_CstmzProjectLabel")); // NOI18N

        myProjectFolder.setEditable(false);

        mySourceFolderLabel.setLabelFor(mySourceFolder);
        org.openide.awt.Mnemonics.setLocalizedText(mySourceFolderLabel, bundle.getString("LBL_CstmzSourceLabel")); // NOI18N

        mySourceFolder.setEditable(false);
        mySourceFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mySourceFolderActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myBrowse, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse_Btn")); // NOI18N
        myBrowse.setActionCommand(BROWSE);
        myBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doBrowse(evt);
            }
        });

        myEncodingLabel.setLabelFor(myEncoding);
        org.openide.awt.Mnemonics.setLocalizedText(myEncodingLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N

        myEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(myProjectFolderLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myProjectFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mySourceFolderLabel)
                            .add(myEncodingLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(mySourceFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myBrowse)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myProjectFolderLabel)
                    .add(myProjectFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mySourceFolderLabel)
                    .add(myBrowse)
                    .add(mySourceFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myEncodingLabel))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        myProjectFolderLabel.getAccessibleContext().setAccessibleName(bundle.getString("A11_CstmzProjectFolder")); // NOI18N
        myProjectFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_CstmzProjectFolderField")); // NOI18N
        mySourceFolderLabel.getAccessibleContext().setAccessibleName(bundle.getString("A11_CstmzSourceFolder")); // NOI18N
        mySourceFolder.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_CstmzSourceFolderField")); // NOI18N
        myBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "A11_Browse_Btn")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void doBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doBrowse
    String command = evt.getActionCommand();

    if (BROWSE.equals(command)) {
        /*
         * XXX : here is only first source root is used!
         */
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(getMessage(LBL_SELECT_SOURCE_FOLDER));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File projectDir = FileUtil.toFile(getProperties().getProject().getProjectDirectory());
        File curDir = null;
        if (myLastUsedSourceDir != null) {
            curDir = FileUtil.toFile(myLastUsedSourceDir);
        }
        if (curDir == null) {
            curDir = projectDir;
        }
        if (curDir != null) {
            chooser.setCurrentDirectory(curDir);
        }

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File sourceDir = chooser.getSelectedFile();
            if (sourceDir != null) {
                File normSourceDir = FileUtil.normalizeFile(sourceDir);
                File normProjectDir = FileUtil.normalizeFile(projectDir);
                if (SourceRootsUi.isRootNotOccupied(normSourceDir, normProjectDir)) {
                    this.myLastUsedSourceDir = FileUtil.toFileObject(normSourceDir);
                    viewSourcesRoot(myLastUsedSourceDir);
                } else {
                    SourceRootsUi.showSourceUsedDialog(normSourceDir);
                }
            }
        }
        sourceFolderChanged();
    }
}//GEN-LAST:event_doBrowse

private void mySourceFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mySourceFolderActionPerformed
    sourceFolderChanged();
}//GEN-LAST:event_mySourceFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton myBrowse;
    private javax.swing.JComboBox myEncoding;
    private javax.swing.JLabel myEncodingLabel;
    private javax.swing.JTextField myProjectFolder;
    private javax.swing.JLabel myProjectFolderLabel;
    private javax.swing.JTextField mySourceFolder;
    private javax.swing.JLabel mySourceFolderLabel;
    // End of variables declaration//GEN-END:variables

    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {

        Collection<Charset> elements 
                = Charset.availableCharsets().values();
        
        public EncodingModel() {
            for (Charset c : elements) {
                addElement(c);
            }
            setSelectedItem(Charset.defaultCharset());
        }

        public Charset getElementByName(String charsetName) {
            if (charsetName == null) {
                return null;
            }

            Charset result = null;
            for (Charset c : elements) {
                if (c.name().equals(charsetName)) {
                    result = c;
                    break;
                }
            }
            return result;
        }
    }

    private static class UnknownCharset extends Charset {

        UnknownCharset(String name) {
            super(name, new String[0]);
        }

        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }
    
    private PhpProjectProperties myProps;

    private FileObject myLastUsedSourceDir;

    private String myOriginalEncoding;
}
