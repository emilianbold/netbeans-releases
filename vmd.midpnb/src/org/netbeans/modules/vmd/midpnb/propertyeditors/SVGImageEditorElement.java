/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.microedition.m2g.SVGImage;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGImageComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGImageEditorElement extends PropertyEditorResourceElement {

    private static final String EXTENSION = "svg"; // NOI18N
    
    private long componentID;
    private boolean doNotFireEvent;
    private Project project;
    private String lastDir;
    private SVGImageComponent imageView;
    private DefaultComboBoxModel comboBoxModel;

    public SVGImageEditorElement() {
        comboBoxModel = new DefaultComboBoxModel();
        initComponents();
        imageView = new SVGImageComponent();
        previewPanel.add(imageView, BorderLayout.CENTER);
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return SVGImageCD.TYPEID;
    }

    public List<String> getPropertyValueNames() {
        return Arrays.asList(SVGImageCD.PROP_RESOURCE_PATH);
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            Project oldProject = project;
            project = ProjectUtils.getProject(document);
            if (!project.equals(oldProject)) {
                updateModel();
            }
        }

        if (wrapper == null) {
            // UI stuff
            setText(null);
            setAllEnabled(false);
            return;
        }

        this.componentID = wrapper.getComponentID();
        final String[] _pathText = new String[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) {
            // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue propertyValue = component.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        _pathText[0] = MidpTypes.getString(propertyValue);
                    }
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (SVGImageCD.PROP_RESOURCE_PATH.equals(propertyName)) {
                    _pathText[0] = MidpTypes.getString(propertyValue);
                }
            }
        }

        // UI stuff
        setAllEnabled(true);
        setText(_pathText[0]);
    }

    private void setText(String text) {
        if (text == null) {
            text = "";
        }

        addImage(text);
    }
    
    private void addImage(String path) {
        doNotFireEvent = true;
        if (comboBoxModel.getIndexOf(path) == -1) {
            comboBoxModel.addElement(path);
            sortComboBoxContent();
        }
        pathTextComboBox.setSelectedItem(path);
        doNotFireEvent = false;
        updatePreview();
    }

    @SuppressWarnings(value = "unchecked")
    private void sortComboBoxContent() {
        int size = pathTextComboBox.getItemCount();
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(pathTextComboBox.getItemAt(i));
        }
        Collections.sort(list);
        pathTextComboBox.removeAllItems();
        for (Object object : list) {
            pathTextComboBox.addItem(object);
        }
    }

    void setAllEnabled(boolean isEnabled) {
        pathLabel.setEnabled(isEnabled);
        pathTextComboBox.setEnabled(isEnabled);
        previewLabel.setEnabled(isEnabled);
        previewPanel.setEnabled(isEnabled);
        widthLabel.setEnabled(isEnabled);
        widthTextField.setEnabled(isEnabled);
        heightLabel.setEnabled(isEnabled);
        heightTextField.setEnabled(isEnabled);
        chooserButton.setEnabled(isEnabled);
    }

    private void updateModel() {
        comboBoxModel.removeAllElements();
        FileObject sourceFolder = getSourceFolder();
        searchImagesInDirectory(sourceFolder);
        for (Entry e : ClassPath.getClassPath(sourceFolder, ClassPath.COMPILE).entries()) {
            if (e.getRoot() == null) {
                continue;
            }
            if (e.getRoot().getName().trim().length() > 0) {
                searchImagesInDirectory(e.getRoot());
            }
        }
    }

    private void searchImagesInDirectory(FileObject dir) {
        for (FileObject fo : dir.getChildren()) {
            if (fo.isFolder()) {
                searchImagesInDirectory(fo);
            } else {
                if (EXTENSION.equals(fo.getExt().toLowerCase())) {
                    String path = convertFile(fo);
                    if (path != null) {
                        addImage(path);
                    }
                }
            }
        }
    }

    private void updatePreview() {
        String relativePath = (String) pathTextComboBox.getSelectedItem();
        String path = getSourceFolder().getPath() + relativePath;
        SVGImage svgImage = null;
        try {
            FileObject svgFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
            if (svgFileObject != null) {
                svgImage = Util.createSVGImage(svgFileObject, true);
            }
        } catch (IOException e) {
        }

        if (svgImage != null) {
            int width = svgImage.getViewportWidth();
            int height = svgImage.getViewportHeight();
            widthTextField.setText(String.valueOf(width));
            heightTextField.setText(String.valueOf(height));
        } else {
            widthTextField.setText(null);
            heightTextField.setText(null);
        }
        imageView.setImage(svgImage);

        previewPanel.invalidate();
        previewPanel.validate();
        previewPanel.repaint();
    }

    private FileObject getSourceFolder() {
        if (project == null) {
            throw Debug.illegalState("Current project is null"); // NOI18N
        }
        String projectID = ProjectUtils.getProjectID(project);
        return ProjectUtils.getSourceGroups(projectID).iterator().next().getRootFolder();
    }

    private String convertFile(FileObject file) {
        String fullPath = file.getPath();
        FileObject sourceFolder = getSourceFolder();
        String sourcePath = sourceFolder.getPath();

        if (!fullPath.contains(sourcePath)) {
            File possible = new File(sourcePath + File.separator + file.getNameExt());
            if (possible.exists()) {
                return null;
            } else {
                try {
                    file = file.copy(sourceFolder, file.getName(), file.getExt());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        fullPath = file.getPath();
        int i = fullPath.indexOf(sourcePath) + sourcePath.length();
        return fullPath.substring(i);
    }

    private static class ImageFilter extends FileFilter {

        private String description;

        public ImageFilter() {
            description = NbBundle.getMessage(SVGImageEditorElement.class, "DISP_SVG_Image_Files"); // NOI18N
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }

            String extension = getExtension(file);
            return EXTENSION.equals(extension);
        }

        public String getDescription() {
            return description;
        }

        private static String getExtension(File file) {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf('.'); // NOI18N
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        pathLabel = new javax.swing.JLabel();
        previewLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        heightLabel = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        chooserButton = new javax.swing.JButton();
        pathTextComboBox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(pathLabel, org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.pathLabel.text")); // NOI18N
        pathLabel.setEnabled(false);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.previewLabel.text")); // NOI18N
        previewLabel.setEnabled(false);

        previewPanel.setEnabled(false);
        previewPanel.setLayout(new java.awt.BorderLayout());

        widthLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.widthLabel.text")); // NOI18N
        widthLabel.setEnabled(false);

        widthTextField.setEditable(false);
        widthTextField.setEnabled(false);

        heightLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.heightLabel.text")); // NOI18N
        heightLabel.setEnabled(false);

        heightTextField.setEditable(false);
        heightTextField.setEnabled(false);

        chooserButton.setText(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.chooserButton.text")); // NOI18N
        chooserButton.setEnabled(false);
        chooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooserButtonActionPerformed(evt);
            }
        });

        pathTextComboBox.setEditable(true);
        pathTextComboBox.setModel(comboBoxModel);
        pathTextComboBox.setEnabled(false);
        pathTextComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathTextComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addContainerGap(272, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(heightLabel)
                            .add(widthLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(pathTextComboBox, 0, 295, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooserButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chooserButton)
                    .add(pathTextComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(widthLabel)
                            .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(heightLabel)
                            .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(previewLabel)
                    .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        JFileChooser chooser = new JFileChooser(lastDir != null ? lastDir : project.getProjectDirectory().getPath());
        chooser.setFileFilter(new ImageFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(chooser.getSelectedFile()));
            lastDir = chooser.getSelectedFile().getParentFile().getPath();
            String relativePath = convertFile(fo);
            if (relativePath != null) {
                setText(relativePath);
                pathTextComboBoxActionPerformed(null);
            } else {
                String message = NbBundle.getMessage(SVGImageEditorElement.class, "MSG_FILE_EXIST"); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }
    }//GEN-LAST:event_chooserButtonActionPerformed

    private void pathTextComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathTextComboBoxActionPerformed
        if (isShowing() && !doNotFireEvent) {
            String text = (String) pathTextComboBox.getSelectedItem();
            fireElementChanged(componentID, SVGImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(text != null ? text : "")); // NOI18N
            updatePreview();
       }
    }//GEN-LAST:event_pathTextComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooserButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JComboBox pathTextComboBox;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables
}
