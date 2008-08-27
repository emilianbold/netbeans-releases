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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.microedition.m2g.SVGImage;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorMessageAwareness;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGFormImageParser;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGImageComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class SVGFormEditorElement extends PropertyEditorResourceElement implements Runnable {

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
    private WeakReference<DesignDocument> documentReferences;
    private Map<String, String> pathMap;

    public SVGFormEditorElement() {
        paths = new HashMap<String, FileObject>();
        comboBoxModel = new DefaultComboBoxModel();
        initComponents();
        progressBar.setVisible(false);
        imageView = new SVGImageComponent();
        previewPanel.add(imageView, BorderLayout.CENTER);
        jTable1.setModel(new Model());
        pathMap = new HashMap<String, String>();
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

    @Override
    public void preResetToDefaultValue(final DesignComponent component) {
        if (component.getType() != SVGFormCD.TYPEID) {
            return;
        }
        component.getDocument().getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                Collection<DesignComponent> svgComponents = new ArrayList<DesignComponent>(component.getComponents());
                component.resetToDefault(SVGFormCD.PROP_COMPONENTS);
                component.getDocument().deleteComponents(svgComponents);
            }
        });
        super.preResetToDefaultValue(component);
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        this.wrapper = wrapper;

        if (documentReferences == null || documentReferences.get() == null) {
            return;
        }
        final DesignDocument document = documentReferences.get();
        project = ProjectUtils.getProject(document);


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
                retValue[0] = component.getDocument().getDescriptorRegistry().isInHierarchy(SVGFormCD.TYPEID, component.getType());
            }
        });

        return retValue[0];
    }

    @Override
    public void postSetValue(final DesignComponent parentComponent, final DesignComponent childComponent) {

        final FileObject[] svgImageFileObject = new FileObject[1];
        final Boolean[] parseIt = new Boolean[1];
        parentComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
//                DesignComponent childComponent = parentComponent.readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
//                if (childComponent == null) {
//                    return;
//                }

                PropertyValue propertyValue = childComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                    //String svgImagePath = MidpTypes.getString(propertyValue);
                    Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(parentComponent.getDocument(), MidpTypes.getString(propertyValue));
                    Iterator<FileObject> iterator = images.keySet().iterator();
                    svgImageFileObject[0] = iterator.hasNext() ? iterator.next() : null;
                    parseIt[0] = Boolean.TRUE;
                }
                DesignComponent oldComponent = parentComponent.readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
                if (oldComponent == childComponent && svgImageFileObject[0] != null) {
                    parseIt[0] = Boolean.FALSE;
                }
            }
        });

        if (parseIt[0] != null && parseIt[0]) {
            parseSVGImageItems(svgImageFileObject[0], parentComponent);
        }
    }

    @Override
    public  void nullValueSet(final DesignComponent svgForm) {
        svgForm.getDocument().getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                SVGFormSupport.removeAllSVGFormComponents(svgForm);
            }
        });
    }
    
    private void parseSVGImageItems(FileObject imageFO, final DesignComponent parentComponent) {
        if (imageFO == null) {
            nullValueSet(parentComponent);
            return;
        }
        final SVGComponentImageParser[] svgComponentImageParser = new SVGComponentImageParser[1];
        parentComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                svgComponentImageParser[0] = SVGComponentImageParser.getParserByComponent(parentComponent);
            }
        });

        SVGComponentImageParser parser = svgComponentImageParser[0];
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
                messageAwareness.displayWarning(NbBundle.getMessage(SVGFormEditorElement.class, "MSG_SVG_Image_Not_SVG_Tiny")); // NOI18N
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
            updateSVGModelTable(null);
        }
        imageView.setImage(svgImage);

        previewPanel.invalidate();
        previewPanel.validate();
        previewPanel.repaint();
        try {
            InputStream is = null;
            if (fo != null) {
                is = fo.getInputStream();
                updateSVGModelTable(is);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
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
        String sourcePath = sourceFolder.getPath();

        File file = FileUtil.toFile(fo);
        if (file == null) {
            // abstract FO - zip/jar...
            relativePath = "/" + fo.getPath(); // NOI18N
        } else {
            String fullPath = file.getAbsolutePath();
            if (fullPath.contains(sourcePath)) {
                // file is inside sources
                fullPath = fo.getPath();
                int i = fullPath.indexOf(sourcePath) + sourcePath.length();
                relativePath = fullPath.substring(i);
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

        if (documentReferences == null || documentReferences.get() == null) {
            return;
        }

        final DesignDocument document = documentReferences.get();

        project = ProjectUtils.getProject(document);

        updateModel(document);

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
                progressBar.setVisible(
                        isShowing);
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
            description = NbBundle.getMessage(SVGFormEditorElement.class, "DISP_SVG_Image_Files"); // NOI18N
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

    @Override
    public void setDesignDocument(DesignDocument document) {
        documentReferences = new WeakReference<DesignDocument>(document);
        super.setDesignDocument(document);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelImageFile = new javax.swing.JPanel();
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
        jPanelSVGComponents = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        org.openide.awt.Mnemonics.setLocalizedText(pathLabel, org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ImageEditorElement.pathLabel.text")); // NOI18N
        pathLabel.setEnabled(false);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ImageEditorElement.previewLabel.text")); // NOI18N
        previewLabel.setEnabled(false);

        previewPanel.setEnabled(false);
        previewPanel.setLayout(new java.awt.BorderLayout());

        widthLabel.setText(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ImageEditorElement.widthLabel.text")); // NOI18N
        widthLabel.setEnabled(false);

        widthTextField.setEditable(false);
        widthTextField.setEnabled(false);

        heightLabel.setText(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ImageEditorElement.heightLabel.text")); // NOI18N
        heightLabel.setEnabled(false);

        heightTextField.setEditable(false);
        heightTextField.setEnabled(false);

        chooserButton.setText(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ImageEditorElement.chooserButton.text")); // NOI18N
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

        org.jdesktop.layout.GroupLayout jPanelImageFileLayout = new org.jdesktop.layout.GroupLayout(jPanelImageFile);
        jPanelImageFile.setLayout(jPanelImageFileLayout);
        jPanelImageFileLayout.setHorizontalGroup(
            jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 367, Short.MAX_VALUE)
            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanelImageFileLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanelImageFileLayout.createSequentialGroup()
                            .add(pathLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 298, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelImageFileLayout.createSequentialGroup()
                            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanelImageFileLayout.createSequentialGroup()
                                    .add(previewLabel)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(jPanelImageFileLayout.createSequentialGroup()
                                            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(heightLabel)
                                                .add(widthLabel))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(pathTextComboBox, 0, 321, Short.MAX_VALUE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(chooserButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap()))
        );
        jPanelImageFileLayout.setVerticalGroup(
            jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 200, Short.MAX_VALUE)
            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanelImageFileLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(pathLabel)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(chooserButton)
                        .add(pathTextComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanelImageFileLayout.createSequentialGroup()
                            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(widthLabel)
                                .add(widthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jPanelImageFileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(heightLabel)
                                .add(heightTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                        .add(previewLabel)
                        .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                    .addContainerGap()))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "SVGFormEditorElement.jPanelImageFile.TabConstraints.tabTitle"), jPanelImageFile); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ACSN_RecognizedComponents")); // NOI18N
        jTable1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ACSD_RecognizedComponents")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelSVGComponentsLayout = new org.jdesktop.layout.GroupLayout(jPanelSVGComponents);
        jPanelSVGComponents.setLayout(jPanelSVGComponentsLayout);
        jPanelSVGComponentsLayout.setHorizontalGroup(
            jPanelSVGComponentsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
        );
        jPanelSVGComponentsLayout.setVerticalGroup(
            jPanelSVGComponentsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "SVGFormEditorElement.jPanelSVGComponents.TabConstraints.tabTitle"), jPanelSVGComponents); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 388, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 8, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 8, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 241, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 6, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 7, Short.MAX_VALUE)))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ACSN_TabbedPane")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGFormEditorElement.class, "ACSD_TabbedPane")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        JFileChooser chooser = new JFileChooser(lastDir != null ? lastDir : project.getProjectDirectory().getPath());
        chooser.setFileFilter(new ImageFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(chooser.getSelectedFile()));//GEN-LAST:event_chooserButtonActionPerformed
            lastDir = chooser.getSelectedFile().getParentFile().getPath();
            String relativePath = convertFile(fo, null, true);
            if (relativePath != null) {
                setText(relativePath);
                pathMap.put(relativePath, fo.getPath());
                pathTextComboBoxActionPerformed(null);
            } else {
                String message = NbBundle.getMessage(SVGFormEditorElement.class, "MSG_FILE_EXIST"); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }
    }

    private void pathTextComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathTextComboBoxActionPerformed
        if (!doNotFireEvent) {//GEN-LAST:event_pathTextComboBoxActionPerformed
            String text = (String) pathTextComboBox.getSelectedItem();
            fireElementChanged(componentID, SVGImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(text != null ? text : "")); // NOI18N
            updatePreview();
        }
    }                                                
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooserButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JPanel jPanelImageFile;
    private javax.swing.JPanel jPanelSVGComponents;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JComboBox pathTextComboBox;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables

    private void updateSVGModelTable(InputStream inputStrem) {
        
        if (inputStrem != null) {
            jTable1.setModel(new Model(inputStrem));
        } else {
            jTable1.setModel(new Model());
        }
        
    }
    // End of variables declaration
    private class Model implements TableModel {

        private String COLUMN_NAME_I = "SVG Component Type"; //TODO Localization
        private String COLUMN_NAME_II = "SVG Component ID"; //TODO Localization
        private String[][] values;

        public Model(InputStream inputStream) {
          this.values = SVGFormImageParser.getComponentsInformation(inputStream);
        }
        
        public Model() {
            this.values = null;
        }


        public int getRowCount() {
            if (values == null)
                return 0;
            return values.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return COLUMN_NAME_I;
            } else {
                return COLUMN_NAME_II;
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (this.values != null) {// && columnIndex < values.length && rowIndex < 2) {
                return this.values[rowIndex][columnIndex];
            }
            return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }
    }
}