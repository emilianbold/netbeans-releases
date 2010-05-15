/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.properties;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JList;
import org.netbeans.modules.compapp.configextension.handlers.model.Handler;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.compapp.configextension.handlers.model.BeanIntrospector;
import org.netbeans.modules.compapp.configextension.handlers.model.HandlerParameter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Custom editor for the handler chain property.
 *
 * @author jqian
 */
public class HandlerChainCustomEditor extends javax.swing.JPanel
        implements EnhancedCustomPropertyEditor {

    private List<Handler> handlers = new ArrayList<Handler>();
    private boolean editable;
    private List<String> handlerBaseClassNames;
    private DefaultListModel listModel;

    /**
     * @param editable              whether the editor is editable or not
     * @param handlerBaseClassNames a list of fully qualified class names
     *                              for the handler base classes
     */
    public HandlerChainCustomEditor(boolean editable, 
            List<String> handlerBaseClassNames,
            List<Handler> handlers) {

        initComponents();

        listModel = new DefaultListModel();
        listHandlerChain.setModel(listModel);

        this.editable = editable;
        this.handlerBaseClassNames = handlerBaseClassNames;

        btnAdd.setEnabled(editable);
        handlerPanel.setEditable(editable);
        ListCellRenderer listCellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Handler handler = (Handler) value;
                return super.getListCellRendererComponent(
                        list, handler.getName(), index, isSelected, cellHasFocus);
            }
        };
        listHandlerChain.setCellRenderer(listCellRenderer);

        setHandlers(handlers);

        selectFirstHandler();
    }

    public List<Handler> getPropertyValue() throws IllegalStateException {
        return handlers;
    }

    private void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
        listModel.clear();
        for (Handler handler : handlers) {
            listModel.addElement(handler);
        }
    }

    private void selectFirstHandler() {
        if (handlers != null && handlers.size() > 0) {
            listHandlerChain.setSelectedIndex(0);
        } else {
            listHandlerChainValueChanged(null);
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

        topPanel = new javax.swing.JPanel();
        btnMoveUp = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        lblHandlerChain = new javax.swing.JLabel();
        btnMoveDown = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listHandlerChain = new javax.swing.JList();
        btnRemove = new javax.swing.JButton();
        bottomPanel = new javax.swing.JPanel();
        handlerPanel = new org.netbeans.modules.compapp.configextension.handlers.properties.HandlerPanel();

        setLayout(new java.awt.BorderLayout());

        btnMoveUp.setText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnMoveUp.text")); // NOI18N
        btnMoveUp.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnMoveUp.tooltipText")); // NOI18N
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });

        btnAdd.setText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnAdd.text")); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnAdd.tooltipText")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        lblHandlerChain.setLabelFor(listHandlerChain);
        lblHandlerChain.setText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.lblHandlerChain.text")); // NOI18N

        btnMoveDown.setText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnMoveDown.text")); // NOI18N
        btnMoveDown.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnMoveDown.tooltipText")); // NOI18N
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });

        listHandlerChain.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listHandlerChainValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listHandlerChain);

        btnRemove.setText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnRemove.text")); // NOI18N
        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(HandlerChainCustomEditor.class, "HandlerChainCustomEditor.btnRemove.tooltipText")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .add(topPanelLayout.createSequentialGroup()
                        .add(btnAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnMoveUp)
                        .add(6, 6, 6)
                        .add(btnMoveDown))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblHandlerChain))
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblHandlerChain)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnMoveDown)
                    .add(btnMoveUp)
                    .add(btnRemove)
                    .add(btnAdd))
                .add(23, 23, 23)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(topPanel, java.awt.BorderLayout.NORTH);

        org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(handlerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(handlerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(bottomPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(
                NbBundle.getMessage(HandlerChainCustomEditor.class,
                "HandlerChainCustomEditor.MSG_ChooseJavaProjectOrDirectoryOrJarFile")); // NOI18N
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(DirAndJarFileFilter.getInstance());
        int ret = fileChooser.showOpenDialog(this);

        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            List<Handler> newHandlers = new ArrayList<Handler>();
            List<String> jarPaths = new ArrayList<String>();
            String projectPath = null;

            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isDirectory()) { 
                if (HandlerUtils.isJ2SEProjectDir(selectedFile)) {
                    projectPath = selectedFile.getCanonicalPath();

                    // build the Java project
                    if (!HandlerUtils.buildJ2SEProject(projectPath)) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                NbBundle.getMessage(HandlerChainCustomEditor.class,
                                "HandlerChainCustomEditor.MSG_FailToBuildProject", // NOI18N
                                projectPath),
                                NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }

                    // add dist/*.jar
                    File projectDistDir = new File(projectPath + "/dist/"); // NOI18N
                    List<File> distJars = new ArrayList<File>();
                    HandlerUtils.getJars(projectDistDir, distJars, false);
                    assert distJars.size() > 0;
                    for (File file : distJars) {
                        jarPaths.add(file.getAbsolutePath());
                    }

                    // add dist/lib/*.jar
                    File projectLibDir = new File(projectPath + "/dist/lib/"); // NOI18N
                    if (projectLibDir.exists()) {
                        List<File> libraries = new ArrayList<File>();
                        HandlerUtils.getJars(projectLibDir, libraries, true);

                        if (libraries.size() > 0) {
                            LibrarySelectionPanel librarySelectionPanel =
                                    new LibrarySelectionPanel(libraries);

                            Object[] options = new Object[]{
                                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
                            };
                            DialogDescriptor desc = new DialogDescriptor(librarySelectionPanel,
                                    NbBundle.getMessage(LibrarySelectionPanel.class,
                                    "LibrarySelectionPanel.dialogtitle"), // NOI18N
                                    true, options, options[0],
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null, null);
                            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                            dlg.setVisible(true);

                            if (desc.getValue() != options[0]) { // cancel
                                return;
                            } else {
                                List<String> selectedLibraries =
                                        librarySelectionPanel.getSelectedLibaries();
                                jarPaths.addAll(selectedLibraries);
                            }
                        }
                    }

                } else { 
                    // The seleceted file is a regular directory.
                    // Find all the jars recursively.
                    List<File> jarList = new ArrayList<File>();
                    HandlerUtils.getJars(selectedFile, jarList, true);

                    for (File jar : jarList) {
                        jarPaths.add(jar.getCanonicalPath());
                    }

                    if (jarPaths.size() == 0) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                NbBundle.getMessage(HandlerChainCustomEditor.class,
                                "HandlerChainCustomEditor.MSG_NoJarFoundUnderDirectory", // NOI18N
                                selectedFile),
                                NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                }
            } else {
                jarPaths.add(selectedFile.getCanonicalPath());
            }

            List<Class> classes = null;
            try {
                classes = BeanIntrospector.getSubClasses(jarPaths,
                        handlerBaseClassNames, false);
            } catch (NoClassDefFoundError err) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(HandlerChainCustomEditor.class,
                        "HandlerChainCustomEditor.MSG_IntrospectionFailure_NoClassDefinitionFound", // NOI18N
                        err.getMessage()),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            for (Class clazz : classes) {
                Handler handler = new Handler();
                handler.setName(clazz.getSimpleName());
                handler.setClassName(clazz.getName());
                handler.setProjectPath(projectPath);
                handler.setJarPaths(jarPaths);
                handler.setParameters(BeanIntrospector.getParameters(clazz, true));

                newHandlers.add(handler);
            }

            if (newHandlers.size() == 0) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(HandlerChainCustomEditor.class,
                        "HandlerChainCustomEditor.MSG_NoHandlerImplFound", // NOI18N
                        projectPath),
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
//            } else {
//                String newHandlerNamesInHTML = "<UL>"; // NOI18N
//                for (Handler handler : newHandlers) {
//                    newHandlerNamesInHTML += "<LI>"; // NOI18N
//                    newHandlerNamesInHTML += handler.getClassName();
//                }
//                newHandlerNamesInHTML += "</UL>"; // NOI18N
//                NotifyDescriptor d = new NotifyDescriptor.Message(
//                        NbBundle.getMessage(HandlerChainCustomEditor.class,
//                        "HandlerChainCustomEditor.MSG_HandlerImplFound", // NOI18N
//                        newHandlers.size(),
//                        selectedFile,
//                        newHandlerNamesInHTML),
//                        NotifyDescriptor.INFORMATION_MESSAGE);
//                DialogDisplayer.getDefault().notify(d);
            }

            handlers.addAll(newHandlers);

            for (Handler h : newHandlers) {
                listModel.addElement(h);
            }

            if (newHandlers.size() > 0) {
                // select the first added one
                listHandlerChain.setSelectedValue(newHandlers.get(0), true);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } 
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed

        List<Object> list = Arrays.asList(listHandlerChain.getSelectedValues());

        handlers.removeAll(list);

        for (Object handler : list) {
            listModel.removeElement(handler);
        }

        selectFirstHandler();
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void listHandlerChainValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listHandlerChainValueChanged

        if (evt != null && evt.getValueIsAdjusting()) {
            return;
        }

        int min = -1;
        int max = -1;
        if (evt != null) {
            min = listHandlerChain.getMinSelectionIndex();
            max = listHandlerChain.getMaxSelectionIndex();
        }

        btnRemove.setEnabled(editable && min != -1);
        btnMoveUp.setEnabled(editable && min > 0);
        btnMoveDown.setEnabled(editable && min != -1 && max < handlers.size() - 1);

        handlerPanel.setHandler((Handler) listHandlerChain.getSelectedValue());
    }//GEN-LAST:event_listHandlerChainValueChanged

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        int[] indices = listHandlerChain.getSelectedIndices();

        for (int i = 0; i < indices.length; i++) {
            Handler handler = handlers.remove(indices[i]);
            indices[i]--;
            handlers.add(indices[i], handler);
        }

        setHandlers(handlers);
        listHandlerChain.setSelectedIndices(indices);
    }//GEN-LAST:event_btnMoveUpActionPerformed

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        int[] indices = listHandlerChain.getSelectedIndices();

        for (int i = indices.length - 1; i >= 0; i--) {  // swap in decreasing order
            Handler handler = handlers.remove(indices[i]);
            indices[i]++;
            handlers.add(indices[i], handler);
        }

        setHandlers(handlers);
        listHandlerChain.setSelectedIndices(indices);
    }//GEN-LAST:event_btnMoveDownActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnRemove;
    private org.netbeans.modules.compapp.configextension.handlers.properties.HandlerPanel handlerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblHandlerChain;
    private javax.swing.JList listHandlerChain;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}

class DirAndJarFileFilter extends FileFilter {

    private static FileFilter instance;

    public static FileFilter getInstance() {
        if (instance == null) {
            instance = new DirAndJarFileFilter();
        }
        return instance;
    }

    private DirAndJarFileFilter() {
    }

    public boolean accept(File f) {
        return f != null &&
                (f.isDirectory() || f.getName().endsWith(".jar")); // NOI18N
    }

    public String getDescription() {
        return "Jar files (*.jar)";
    }
}
