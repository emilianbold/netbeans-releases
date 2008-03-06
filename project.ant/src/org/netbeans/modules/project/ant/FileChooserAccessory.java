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

package org.netbeans.modules.project.ant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Accessory allowing to choose how file is referenced from a project - relative
 * or absolute.
 * 
 * <p>The panel is used from two different places - FileChooser and 
 * RelativizeFilePathCustomizer.
 * 
 * @author David Konecny
 */
public class FileChooserAccessory extends javax.swing.JPanel 
        implements ActionListener, PropertyChangeListener {

    private File baseFolder;
    private final File sharedLibrariesFolder;
    private boolean copyAllowed;
    private JFileChooser chooser;
    private List<String> copiedRelativeFiles = null;
    /** In RelativizeFilePathCustomizer scenario this property holds preselected file */
    private File usetThisFileInsteadOfOneFromChooser = null;

    /**
     * Constructor for usage from RelativizeFilePathCustomizer.
     */
    public FileChooserAccessory(File baseFolder, File sharedLibrariesFolder, boolean copyAllowed, File selectedFile) {
        this(null, baseFolder, sharedLibrariesFolder, copyAllowed);
        usetThisFileInsteadOfOneFromChooser = selectedFile;
        enableAccessory(true);
        update(Collections.singletonList(usetThisFileInsteadOfOneFromChooser));
        
        //when deciding on predefined file, we can assume certain options to be preferable.
        if (CollocationQuery.areCollocated(baseFolder, selectedFile)) {
            rbRelative.setSelected(true);
        } else {
            if (copyAllowed) {
                rbCopy.setSelected(true);
            } else {
                rbAbsolute.setSelected(true);
            }
        }
    }
    
    /**
     * Constructor for usage from FileChooser.
     */
    public FileChooserAccessory(JFileChooser chooser, File baseFolder, File sharedLibrariesFolder, boolean copyAllowed) {
        assert baseFolder != null;
        assert !baseFolder.isFile();
        if (sharedLibrariesFolder != null) {
            assert !sharedLibrariesFolder.isFile() : true;
            if (!sharedLibrariesFolder.equals(FileUtil.normalizeFile(sharedLibrariesFolder))) {
                throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                    "normalized. Was "+sharedLibrariesFolder+" instead of "+
                    FileUtil.normalizeFile(sharedLibrariesFolder));  // NOI18N
            }
        }
        this.baseFolder = baseFolder;
        this.sharedLibrariesFolder = sharedLibrariesFolder;
        this.copyAllowed = copyAllowed && sharedLibrariesFolder != null;
        this.chooser = chooser;
        initComponents();
        //copyPanel.setVisible(copyAllowed);
        rbCopy.addActionListener(this);
        rbRelative.addActionListener(this);
        rbAbsolute.addActionListener(this);
        if (chooser != null) {
            chooser.addPropertyChangeListener(this);
        }
        if (sharedLibrariesFolder != null) {
            copyTo.setText(sharedLibrariesFolder.getAbsolutePath());
        }
        enableAccessory(false);
        if (!copyAllowed) {
            rbCopy.setVisible(false);
            copyTo.setVisible(false);
        }
    }

    public String[] getFiles() {
        assert isRelative();
        if (isCopy()) {
            return copiedRelativeFiles.toArray(new String[copiedRelativeFiles.size()]);
        } else {
            List<File> files = Arrays.asList(getSelectedFiles());
            List<String> l = getRelativeFiles(files);
            return l.toArray(new String[l.size()]);
        }
    }
    
    public boolean canApprove() {
        if (!isCopy()) {
            return true;
        }
        File f = FileUtil.normalizeFile(new File(copyTo.getText()));
        if (!f.getPath().equals(sharedLibrariesFolder.getPath()) && 
                !(f.getPath()).startsWith(sharedLibrariesFolder.getPath()+File.separatorChar)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    MessageFormat.format(NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.warning1"), // NOI18N
                    new Object[] {sharedLibrariesFolder.getPath()})));
                
            return false;
        }
        final File[] files = getSelectedFiles();
        if (f.exists()) {
            for (File file : files) {
                File testFile = new File(f, file.getName());
                if (testFile.exists()) {
                    if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.warning3")))) {
                        return false;
                    }
                    break;
                }
            }
        } else {
            if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    MessageFormat.format(NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.warning2"), // NOI18N
                    new Object[] {f.getPath()})))) {
                return false;
            }
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.warning4")))) { // NOI18N
                    return false;
                }
                break;
            }
        }
        
        return true;
    }
    
    public void copyFilesIfNecessary() throws IOException {
        if (!isCopy()) {
            return;
        }
        File f = FileUtil.normalizeFile(new File(copyTo.getText()));
        FileUtil.createFolder(f);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        List<File> selectedFiles = Arrays.asList(getSelectedFiles());
        copyFiles(selectedFiles, fo);
    }
    
    private void enableAccessory(boolean enable) {
        rbRelative.setEnabled(enable);
        rbCopy.setEnabled(enable && copyAllowed);
        rbAbsolute.setEnabled(enable);
        copyTo.setEnabled(enable);
        copyTo.setEditable(enable && rbCopy.isSelected());
    }
    
    private File[] getSelectedFiles() {
        if (usetThisFileInsteadOfOneFromChooser != null) {
            return new File[]{usetThisFileInsteadOfOneFromChooser};
        }
        if (chooser.isMultiSelectionEnabled()) {
            return chooser.getSelectedFiles();
        } else {
            if (chooser.getSelectedFile() != null) {
                return new File[] { chooser.getSelectedFile() };
            } else {
                return new File[0];
            }
        }
    }
    
    public boolean isRelative() {
        return (rbRelative.isEnabled() && rbRelative.isSelected()) || isCopy();
    }
    
    private boolean isCopy() {
        return rbCopy.isEnabled() && rbCopy.isSelected();
    }
    
    public void actionPerformed(ActionEvent e) {
        copyTo.setEditable(e.getSource() == rbCopy);
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (!(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName()) ||
             JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName()))) {
            return;
        }
        File[] files = getSelectedFiles();
        enableAccessory(files.length != 0);
        update(Arrays.asList(files));
    }
        
    private void update(List<File> files) {
        StringBuffer absolute = new StringBuffer();
        StringBuffer relative = new StringBuffer();
        boolean isRelative = true;
        for (File file : files) {
            if (absolute.length() != 0) {
                absolute.append(", ");
                relative.append(", ");
                if (file.getParentFile() != null && file.getParentFile().getParentFile() != null &&
                        file.getParentFile().getName().length() > 0) {
                }
            }
            absolute.append(file.getAbsolutePath());
            String s = PropertyUtils.relativizeFile(baseFolder, file);
            if (s == null) {
                isRelative = false;
            }
            relative.append(s);
        }
        rbRelative.setEnabled(isRelative && rbRelative.isEnabled());
        relativePath.setText(relative.toString());
        relativePath.setCaretPosition(0);
        relativePath.setToolTipText(relative.toString());
        if (!isRelative) {
            relativePath.setText("");
            relativePath.setToolTipText("");
        }
        absolutePath.setText(absolute.toString());
        absolutePath.setCaretPosition(0);
        absolutePath.setToolTipText(absolute.toString());
    }

    private List<String> getRelativeFiles(List<File> files) {
        List<String> fs = new ArrayList<String>();
        for (File file : files) {
            String s = PropertyUtils.relativizeFile(baseFolder, file);
            if (s != null) {
                fs.add(s);
            }
        }
        return fs;
    }

    private void copyFiles(List<File> files, FileObject newRoot) throws IOException {
        List<File> fs = new ArrayList<File>();
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            FileObject newFO;
            if (fo.isFolder()) {
                newFO = copyFolderRecursively(fo, newRoot);
            } else {
                FileObject foExists = newRoot.getFileObject(fo.getName(), fo.getExt());
                if (foExists != null) {
                    foExists.delete();
                }
                newFO = FileUtil.copyFile(fo, newRoot, fo.getName(), fo.getExt());
            }
            fs.add(FileUtil.toFile(newFO));
        }
        copiedRelativeFiles = getRelativeFiles(fs);
    }
    
    public static FileObject copyFolderRecursively(FileObject sourceFolder, FileObject destination) throws IOException {
        assert sourceFolder.isFolder() : sourceFolder;
        assert destination.isFolder() : destination;
        FileObject destinationSubFolder = destination.getFileObject(sourceFolder.getName());
        if (destinationSubFolder == null) {
            destinationSubFolder = destination.createFolder(sourceFolder.getName());
        }
        for (FileObject fo : sourceFolder.getChildren()) {
            if (fo.isFolder()) {
                copyFolderRecursively(fo, destinationSubFolder);
            } else {
                FileObject foExists = destinationSubFolder.getFileObject(fo.getName(), fo.getExt());
                if (foExists != null) {
                    foExists.delete();
                }
                FileObject newFO = FileUtil.copyFile(fo, destinationSubFolder, fo.getName(), fo.getExt());
            }
        }
        return destinationSubFolder;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        rbRelative = new javax.swing.JRadioButton();
        relativePath = new javax.swing.JTextField();
        rbCopy = new javax.swing.JRadioButton();
        copyTo = new javax.swing.JTextField();
        rbAbsolute = new javax.swing.JRadioButton();
        absolutePath = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(250, 139));
        setPreferredSize(new java.awt.Dimension(250, 139));

        buttonGroup1.add(rbRelative);
        rbRelative.setSelected(true);
        rbRelative.setText(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbRelative.text")); // NOI18N

        relativePath.setEditable(false);
        relativePath.setText(null);

        buttonGroup1.add(rbCopy);
        rbCopy.setText(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbCopy.text")); // NOI18N

        copyTo.setEditable(false);
        copyTo.setText(null);

        buttonGroup1.add(rbAbsolute);
        rbAbsolute.setText(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbAbsolute.text")); // NOI18N

        absolutePath.setEditable(false);
        absolutePath.setText(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rbRelative)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(relativePath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                    .add(rbCopy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(copyTo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                    .add(rbAbsolute)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(absolutePath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(rbRelative)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(relativePath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbCopy)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(copyTo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbAbsolute)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(absolutePath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField absolutePath;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField copyTo;
    private javax.swing.JRadioButton rbAbsolute;
    private javax.swing.JRadioButton rbCopy;
    private javax.swing.JRadioButton rbRelative;
    private javax.swing.JTextField relativePath;
    // End of variables declaration//GEN-END:variables

}
