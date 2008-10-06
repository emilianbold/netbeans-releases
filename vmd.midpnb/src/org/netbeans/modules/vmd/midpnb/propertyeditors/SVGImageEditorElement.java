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
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.microedition.m2g.SVGImage;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorMessageAwareness;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGImageComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGImageEditorElement extends PropertyEditorResourceElement implements Runnable, CleanUp {

    private static final String EXTENSION = "svg"; // NOI18N
    private long componentID;
    private boolean doNotFireEvent;
    private Project project;
    private String lastDir;
    private SVGImageComponent imageView;
    private DefaultComboBoxModel comboBoxModel;
    private Map<String, FileObject> paths;
    private final AtomicBoolean requiresModelUpdate = new AtomicBoolean(false);
    private DesignComponentWrapper wrapper;
    private PropertyEditorMessageAwareness messageAwareness;

    public void clean(DesignComponent component) {
        project = null;
        imageView = null;
        comboBoxModel = null;
        if (paths != null ) {
            paths.clear();
            paths = null;
        }
        wrapper = null;
        messageAwareness = null;
        chooserButton = null;
        heightLabel = null;
        heightTextField = null;
        pathLabel = null;
        pathTextComboBox = null;
        previewLabel = null;
        previewPanel = null;
        progressBar = null;
        widthLabel = null;
        widthTextField = null;
        this.removeAll();
    }

    public SVGImageEditorElement() {
        paths = new HashMap<String, FileObject>();
        comboBoxModel = new DefaultComboBoxModel();
        initComponents();
        progressBar.setVisible(false);
        imageView = new SVGImageComponent();
        previewPanel.add(imageView, BorderLayout.CENTER);
    }

    @Override
    public void setPropertyEditorMessageAwareness(PropertyEditorMessageAwareness messageAwareness) {
        this.messageAwareness = messageAwareness;
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
        this.wrapper = wrapper;
        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            project = ProjectUtils.getProject(document);
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

    @Override
    public boolean isPostSetValueSupported(final DesignComponent component) {
        final boolean[] retValue = new boolean[1];
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                retValue[0] = component.getDocument().getDescriptorRegistry().isInHierarchy(SVGMenuCD.TYPEID, component.getType()) && component.readProperty(SVGMenuCD.PROP_ELEMENTS).getArray().size() == 0;
            }
        });

        return retValue[0];
    }

    @Override
    public void postSetValue(final DesignComponent parentComponent, final DesignComponent childComponent) {
        final FileObject[] svgImageFileObject = new FileObject[1];
        childComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                PropertyValue propertyValue = childComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                    Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(parentComponent.getDocument(), MidpTypes.getString(propertyValue));
                    Iterator<FileObject> iterator = images.keySet().iterator();
                    svgImageFileObject[0] = iterator.hasNext() ? iterator.next() : null;
                }
            }
        });

        parseSVGImageItems(svgImageFileObject[0], parentComponent);
    }

    private void parseSVGImageItems(FileObject imageFO, DesignComponent parentComponent) {
        if (imageFO == null) {
            return;
        }
        SVGComponentImageParser parser = SVGComponentImageParser.getParserByComponent(parentComponent);
        if (parser == null) {
            return;
        }

        InputStream inputStream = null;
        try {
            inputStream = imageFO.getInputStream();
            if (inputStream != null) {
                parser.parse(inputStream, parentComponent);
            }
        } catch (FileNotFoundException ex) {
            Debug.warning(ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    Debug.warning(ioe);
                }
            }
        }
    }

    private void setText(String text) {
        if (text == null) {
            text = ""; // NOI18N
        }

        addImage(text, true);
    }

    private void addImage(String path, boolean selectImage) {
        doNotFireEvent = true;
        if (comboBoxModel.getIndexOf(path) == -1) {
            comboBoxModel.addElement(path);
            sortComboBoxContent();
        }
        if (selectImage) {
            pathTextComboBox.setSelectedItem(path);
            updatePreview();
        }
        doNotFireEvent = false;
    }

    @SuppressWarnings(value = "unchecked")
    private void sortComboBoxContent() {
        int size = pathTextComboBox.getItemCount();
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(pathTextComboBox.getItemAt(i));
        }
        Collections.sort(list, StringComparator.instance);
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

    private void updateModel(DesignDocument document) {
        boolean isEnabled = pathTextComboBox.isEnabled();
        pathTextComboBox.setEnabled(false);
        doNotFireEvent = true;
        comboBoxModel.removeAllElements();
        doNotFireEvent = false;
        paths.clear();

        Map<FileObject, String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(document, Collections.<String>singleton(EXTENSION));
        for (Entry<FileObject, String> entry : fileMap.entrySet()) {
            checkFile(entry.getKey(), entry.getValue());
        }

        if (isEnabled) {
            pathTextComboBox.setEnabled(true);
        }
    }

    private void checkFile(FileObject fo, String relativePath) {
        if (EXTENSION.equals(fo.getExt().toLowerCase())) {
            String path = convertFile(fo, relativePath, false);
            if (path != null) {
                addImage(path, false);
            }
        }
    }

    private void updatePreview() {
        String relativePath = (String) pathTextComboBox.getSelectedItem();
        FileObject fo = paths.get(relativePath);
        SVGImage svgImage = null;
        try {
            if (fo != null) {
                svgImage = Util.createSVGImage(fo, true);
            }
            if (messageAwareness != null) {
                messageAwareness.clearErrorStatus();
            }
        } catch (IOException e) {
            Debug.warning(e);
            if (messageAwareness != null) {
                messageAwareness.displayWarning(NbBundle.getMessage(SVGImageEditorElement.class, "MSG_SVG_Image_Not_SVG_Tiny")); // NOI18N
            }
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

    private String convertFile(FileObject fo, String relPath, boolean needCopy) {
        String relativePath;
        FileObject sourceFolder = getSourceFolder();
        String sourcePath = FileUtil.toFile(sourceFolder).getAbsolutePath();

        File file = FileUtil.toFile(fo);
        if (file == null) {
            // abstract FO - zip/jar...
            if (!fo.getPath().startsWith("/", 0)) {
                relativePath = "/" + fo.getPath(); // NOI18N
            } else {
                relativePath = fo.getPath();
            }
        } else {
            String fullPath = file.getAbsolutePath();
            if (fullPath.contains(sourcePath)) {
                // file is inside sources
                fullPath = fo.getPath();
                int i = fullPath.indexOf(sourcePath) + sourcePath.length() + 1;
                if (!fullPath.substring(i).startsWith("/")) { //NOI18N
                    relativePath = "/" + fullPath.substring(i); //NOI18N
                } else {
                    relativePath = fullPath.substring(i);
                }
            } else if (needCopy) {
                // somewhere outside sources - need to copy (export image)
                File possible = new File(sourcePath + File.separator + fo.getNameExt());
                if (possible.exists()) {
                    // file exists, do not convert
                    return null;
                }

                try {
                    fo = fo.copy(sourceFolder, fo.getName(), fo.getExt());
                } catch (IOException ex) {
                    Debug.warning("SVGImageEditorElement.convertFile()", "can't copy file", fullPath, ex); // NOI18N
                }
                relativePath = "/" + fo.getNameExt(); // NOI18N
            } else {
                // somewhere outside sources, no need to copy - folder attached to resources
                relativePath = relPath;
            }
        }
        paths.put(relativePath, fo);

        return relativePath;
    }

    public void run() {
        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            updateModel(document);
        }

        showProgressBar(false);
        setDesignComponentWrapper(wrapper);
        requiresModelUpdate.set(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (requiresModelUpdate.getAndSet(true)) {
            return;
        }

        showProgressBar(true);
        new Thread(this).start();
    }

    private void showProgressBar(final boolean isShowing) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                progressBar.setVisible(isShowing);
            }
        });
    }

    @Override
    public void removeNotify() {
        paths.clear();
        project = null;
        wrapper = null;
        super.removeNotify();
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        progressBar = new javax.swing.JProgressBar();

        pathLabel.setLabelFor(pathTextComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(pathLabel, org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.pathLabel.text")); // NOI18N
        pathLabel.setEnabled(false);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.previewLabel.text")); // NOI18N
        previewLabel.setEnabled(false);

        previewPanel.setEnabled(false);
        previewPanel.setLayout(new java.awt.BorderLayout());

        widthLabel.setLabelFor(widthTextField);
        org.openide.awt.Mnemonics.setLocalizedText(widthLabel, org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.widthLabel.text")); // NOI18N
        widthLabel.setEnabled(false);

        widthTextField.setEditable(false);
        widthTextField.setEnabled(false);

        heightLabel.setLabelFor(heightTextField);
        org.openide.awt.Mnemonics.setLocalizedText(heightLabel, org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.heightLabel.text")); // NOI18N
        heightLabel.setEnabled(false);

        heightTextField.setEditable(false);
        heightTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(chooserButton, org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ImageEditorElement.chooserButton.text")); // NOI18N
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

        progressBar.setIndeterminate(true);

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
                        .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(heightLabel)
                                    .add(widthLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(pathTextComboBox, 0, 246, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooserButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pathTextComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chooserButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(widthLabel)
                            .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(heightLabel)
                            .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(previewLabel)
                    .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
        );

        widthTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSN_Width")); // NOI18N
        widthTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSD_Width")); // NOI18N
        heightTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSN_Height")); // NOI18N
        heightTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSD_Height")); // NOI18N
        chooserButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSN_Browse")); // NOI18N
        chooserButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSD_Browse")); // NOI18N
        pathTextComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSN_ImagePath")); // NOI18N
        pathTextComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGImageEditorElement.class, "ACSD_ImagePath")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        JFileChooser chooser = new JFileChooser(lastDir != null ? lastDir : project.getProjectDirectory().getPath());
        chooser.setFileFilter(new ImageFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(chooser.getSelectedFile()));
            lastDir = chooser.getSelectedFile().getParentFile().getPath();
            String relativePath = convertFile(fo, null, true);
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
        if (!doNotFireEvent) {
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
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables
}