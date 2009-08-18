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
package org.netbeans.modules.vmd.midp.propertyeditors.resource.elements;

import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class ImageEditorElement extends PropertyEditorResourceElement implements Runnable, CleanUp {

    private static final String[] EXTENSIONS = {"png", "gif", "jpg", "jpeg"}; // NOI18N
    private long componentID;
    private boolean doNotFireEvent;
    //private Project project;
    private String lastDir;
    private ImagePreview imagePreview;
    private Image image;
    private DefaultComboBoxModel comboBoxModel;
    private Map<String, FileObject> paths;
    private final AtomicBoolean requiresModelUpdate = new AtomicBoolean(false);
    private DesignComponentWrapper wrapper;
    private WeakReference<DesignDocument> documentReferences;

    

    public ImageEditorElement() {
        paths = new HashMap<String, FileObject>();
        comboBoxModel = new DefaultComboBoxModel();
        initComponents();
        progressBar.setVisible(false);
        imagePreview = new ImagePreview();
        previewPanel.add(imagePreview, BorderLayout.CENTER);
    }

    public void clean(DesignComponent component) {
        
        imagePreview = null;
        image = null;
        comboBoxModel = null;
        if (paths != null) {
            paths.clear();
            paths = null;
        }
        wrapper = null;
        documentReferences = null;
        this.removeAll();
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return ImageCD.TYPEID;
    }

    public List<String> getPropertyValueNames() {
        return Arrays.asList(ImageCD.PROP_RESOURCE_PATH);
    }

    @Override
    public void setDesignComponent(DesignComponent component) {
        init(component.getDocument());
        super.setDesignComponent(component);
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        this.wrapper = wrapper;

        if (documentReferences == null || documentReferences.get() == null) {
            return;
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
                    PropertyValue propertyValue = component.readProperty(ImageCD.PROP_RESOURCE_PATH);
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
                if (ImageCD.PROP_RESOURCE_PATH.equals(propertyName)) {
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
            text = ""; // NOI18N
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

    @SuppressWarnings(value = "unchecked") // NOI18N
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

    public void setAllEnabled(boolean isEnabled) {
        pathLabel.setEnabled(isEnabled);
        pathTextComboBox.setEnabled(isEnabled);
        previewLabel.setEnabled(isEnabled);
        previewPanel.setEnabled(isEnabled);
        widthLabel.setEnabled(isEnabled);
        widthTextField.setEnabled(isEnabled);
        heightLabel.setEnabled(isEnabled);
        heightTextField.setEnabled(isEnabled);
        sizeLabel.setEnabled(isEnabled);
        sizeTextField.setEnabled(isEnabled);
        chooserButton.setEnabled(isEnabled);
    }

    public void updateModel(DesignDocument document) {
        boolean isEnabled = pathTextComboBox.isEnabled();
        pathTextComboBox.setEnabled(false);
        doNotFireEvent = true;
        comboBoxModel.removeAllElements();
        doNotFireEvent = false;
        paths.clear();

        Map<FileObject, String> fileMap = MidpProjectSupport.getImagesForProject(document, false);
        for (Entry<FileObject, String> entry : fileMap.entrySet()) {
            checkFile(entry.getKey(), entry.getValue());
        }

        if (isEnabled) {
            pathTextComboBox.setEnabled(true);
        }
    }

    private void checkFile(FileObject fo, String relativePath) {
        for (String ext : EXTENSIONS) {
            if (ext.equals(fo.getExt().toLowerCase())) {
                String path = convertFile(fo, relativePath, false);
                if (path != null) {
                    addImage(path);
                }
                break;
            }
        }
    }

    private void updatePreview() {
        String relativePath = (String) pathTextComboBox.getSelectedItem();
        FileObject fo = paths.get(relativePath);
        BufferedImage bufferedImage = null;
        try {
            if (fo != null) {
                bufferedImage = ImageIO.read(fo.getInputStream());
            }
        } catch (IOException ex) {
            System.out.print(ex.getMessage());
        }

        if (bufferedImage != null) {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            widthTextField.setText(String.valueOf(width));
            heightTextField.setText(String.valueOf(height));

            StringBuffer str = new StringBuffer();
            str.append(fo.getSize());
            str.append(' '); // NOI18N
            str.append(NbBundle.getMessage(ImageEditorElement.class, "LBL_Size_Bytes")); // NOI18N
            sizeTextField.setText(str.toString());

            image = bufferedImage;
        } else {
            image = null;
            widthTextField.setText(null);
            heightTextField.setText(null);
            sizeTextField.setText(null);
        }

        previewPanel.invalidate();
        previewPanel.validate();
        previewPanel.repaint();
    }

    private FileObject getSourceFolder() {
        if (documentReferences != null && documentReferences.get() == null) {
            return null;
        }
        Project project = ProjectUtils.getProject(documentReferences.get());
        if (project == null) {
            if (documentReferences != null && documentReferences.get() != null) {
                project = ProjectUtils.getProject(documentReferences.get());
            }
            if (project == null) {
                throw Debug.illegalState("Current project is null"); // NOI18N
            }
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

    public void init(DesignDocument document) {
        documentReferences = new WeakReference<DesignDocument>(document);
    }

    public void run() {
        if (documentReferences == null || documentReferences.get() == null) {
            return;
        }

        final DesignDocument document = documentReferences.get();

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
       
        wrapper = null;
        super.removeNotify();
    }

    private static class ImageFilter extends FileFilter {

        private String description;

        public ImageFilter() {
            description = NbBundle.getMessage(ImageEditorElement.class, "DISP_Image_Files"); // NOI18N
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }

            String extension = getExtension(file);
            for (String ext : EXTENSIONS) {
                if (ext.equals(extension)) {
                    return true;
                }
            }
            return false;
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

    private class ImagePreview extends JPanel {

        private static final int BORDER_EDGE_LENGTH = 10;
        private static final int IMAGE_GAP = 10;

        @Override
        public void paint(Graphics g) {
            if (image != null) {
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                int previewWidth = imagePreview.getWidth() - IMAGE_GAP;
                int previewHeight = imagePreview.getHeight() - IMAGE_GAP;
                if (width > previewWidth || height > previewHeight) {
                    g.drawImage(image, IMAGE_GAP, IMAGE_GAP, previewWidth, previewHeight, 0, 0, width, height, null);
                } else {
                    int xOffset = (previewWidth - width + IMAGE_GAP) / 2;
                    int yOffset = (previewHeight - height + IMAGE_GAP) / 2;
                    g.drawImage(image, xOffset, yOffset, null);
                }
            }

            // paint the border
            g.setColor(Color.BLACK);
            final int rightX = getWidth() - 1;
            final int bottomY = getHeight() - 1;
            // top left
            g.drawLine(0, 0, 0, BORDER_EDGE_LENGTH);
            g.drawLine(0, 0, BORDER_EDGE_LENGTH, 0);
            // top right
            g.drawLine(rightX, 0, rightX, BORDER_EDGE_LENGTH);
            g.drawLine(rightX, 0, rightX - BORDER_EDGE_LENGTH, 0);
            // bottom left
            g.drawLine(0, bottomY, 0, bottomY - BORDER_EDGE_LENGTH);
            g.drawLine(0, bottomY, BORDER_EDGE_LENGTH, bottomY);
            // bottom right
            g.drawLine(rightX, bottomY, rightX, bottomY - BORDER_EDGE_LENGTH);
            g.drawLine(rightX, bottomY, rightX - BORDER_EDGE_LENGTH, bottomY);
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
        sizeLabel = new javax.swing.JLabel();
        sizeTextField = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();

        pathLabel.setLabelFor(pathTextComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(pathLabel, org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.pathLabel.text")); // NOI18N
        pathLabel.setEnabled(false);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.previewLabel.text")); // NOI18N
        previewLabel.setEnabled(false);

        previewPanel.setEnabled(false);
        previewPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                previewPanelComponentResized(evt);
            }
        });
        previewPanel.setLayout(new java.awt.BorderLayout());

        widthLabel.setLabelFor(widthTextField);
        org.openide.awt.Mnemonics.setLocalizedText(widthLabel, org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.widthLabel.text")); // NOI18N
        widthLabel.setEnabled(false);

        widthTextField.setEditable(false);
        widthTextField.setEnabled(false);

        heightLabel.setLabelFor(heightTextField);
        org.openide.awt.Mnemonics.setLocalizedText(heightLabel, org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.heightLabel.text")); // NOI18N
        heightLabel.setEnabled(false);

        heightTextField.setEditable(false);
        heightTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(chooserButton, org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.chooserButton.text")); // NOI18N
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

        sizeLabel.setLabelFor(sizeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(sizeLabel, org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ImageEditorElement.sizeLabel.text")); // NOI18N
        sizeLabel.setEnabled(false);

        sizeTextField.setEditable(false);
        sizeTextField.setEnabled(false);

        progressBar.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pathLabel)
                .addContainerGap(287, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(heightLabel)
                                    .add(widthLabel)
                                    .add(sizeLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(widthTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .add(heightTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .add(sizeTextField)))
                            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(pathTextComboBox, 0, 261, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooserButton))
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
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(sizeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3))
                    .add(previewLabel)
                    .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
        );

        widthTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSN_Width")); // NOI18N
        widthTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSD_Width")); // NOI18N
        heightTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSN_Height")); // NOI18N
        heightTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSD_Height")); // NOI18N
        chooserButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSN_Browse")); // NOI18N
        chooserButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSD_Browse")); // NOI18N
        pathTextComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ASCN_ImagePath")); // NOI18N
        pathTextComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ASCD_ImagePath")); // NOI18N
        sizeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSN_Size")); // NOI18N
        sizeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImageEditorElement.class, "ACSD_Size")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        Project project = ProjectUtils.getProject(documentReferences.get());
        JFileChooser chooser = new JFileChooser(lastDir != null ? lastDir : project.getProjectDirectory().getPath());
        chooser.setFileFilter(new ImageFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(chooser.getSelectedFile()));
            lastDir = chooser.getSelectedFile().getParentFile().getPath();
            String relativePath = convertFile(fo, null, true);
            if (relativePath != null) {
                setText(relativePath);
                pathTextComboBoxActionPerformed(null);//GEN-LAST:event_chooserButtonActionPerformed
            } else {
                String message = NbBundle.getMessage(ImageEditorElement.class, "MSG_FILE_EXIST"); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }
    }

    private void pathTextComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathTextComboBoxActionPerformed
//        if (isShowing() && !doNotFireEvent) {//GEN-LAST:event_pathTextComboBoxActionPerformed
        if (!doNotFireEvent) {
            String text = (String) pathTextComboBox.getSelectedItem();
            fireElementChanged(componentID, ImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(text != null ? text : "")); // NOI18N
            updatePreview();
        }
        this.repaint();
    }

    private void previewPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_previewPanelComponentResized
        updatePreview();//GEN-LAST:event_previewPanelComponentResized
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooserButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JComboBox pathTextComboBox;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JTextField sizeTextField;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables
}