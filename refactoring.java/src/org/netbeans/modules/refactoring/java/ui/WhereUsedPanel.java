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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.*;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {
    
    private static final String PREF_SCOPE = "FindUsages-Scope";
    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N
    private final transient TreePathHandle element;
    private final transient ChangeListener parent;
//    private static final int MAX_NAME = 50;
    private static final int SCOPE_COMBOBOX_COLUMNS = 14;
    public static final String ELLIPSIS = "\u2026"; //NOI18N
    private Scope customScope;
    private boolean enableScope;

    private WhereUsedInnerPanel panel;
    private final WhereUsedPanelMethod methodPanel;
    private final WhereUsedPanelClass classPanel;
    private final WhereUsedPanelPackage packagePanel;
    private final WhereUsedPanelVariable variablePanel;
    
    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, TreePathHandle e, ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class,"LBL_WhereUsed")); // NOI18N
        this.element = e;
        this.parent = parent;
        this.enableScope = true;
        initComponents();
        methodPanel = new WhereUsedPanelMethod(parent, element);
        classPanel = new WhereUsedPanelClass(parent);
        packagePanel = new WhereUsedPanelPackage(parent);
        variablePanel = new WhereUsedPanelVariable(parent);
        panel = variablePanel;
        btnCustomScope.setAction(new ScopeAction(scope));
    }
    
    public Scope getCustomScope() {
        FileObject file = RefactoringUtils.getFileObject(element);
        Scope value = null;
        
        if(!enableScope) {
            return Scope.create(null, null, Arrays.asList(file));
        }

        switch (scope.getSelectedIndex()) {
            case 1:
                value = Scope.create(Arrays.asList(projectSources), null, null);
                break;
            case 2:
                NonRecursiveFolder nonRecursiveFolder = new NonRecursiveFolder() {
            @Override
                    public FileObject getFolder() {
                        return packageFolder;
                    }
                };
                value = Scope.create(null, Arrays.asList(nonRecursiveFolder), null);
                break;
            case 3:
                value = Scope.create(null, null, Arrays.asList(file));
                break;
            case 4:
                value = WhereUsedPanel.this.customScope;
                break;
        }
        return value;
    }

    private boolean initialized = false;
    private FileObject packageFolder = null;
    private FileObject[] projectSources = null;
    
    String getMethodDeclaringClass() {
        return methodPanel.getMethodDeclaringClass();
    }
    
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        JavaSource source = JavaSource.forFileObject(element.getFileObject());
        final Project p = FileOwnerQuery.getOwner(element.getFileObject());
        CancellableTask<CompilationController> task =new CancellableTask<CompilationController>() {
            @Override
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }
            
            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);

                final Element element = WhereUsedPanel.this.element.resolveElement(info);
                switch (element.getKind()) {
                    case CONSTRUCTOR:
                    case METHOD: {
                        panel = methodPanel;
                        break;
                    }
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case ANNOTATION_TYPE: {
                        panel = classPanel;
                        break;
                    }
                    case PACKAGE: {
                        panel = packagePanel;
                        break;
                    }
                    case FIELD:
                    case ENUM_CONSTANT:
                    default: {
                        panel = variablePanel;
                        break;
                    }
                }
                panel.initialize(element, info);

                TreePath path = WhereUsedPanel.this.element.resolve(info);
                final ExpressionTree packageName1 = path.getCompilationUnit().getPackageName();
                final String packageName = packageName1 == null? "<default package>" : packageName1.toString(); //NOI18N
                if(packageName1 == null) {
                    packageFolder = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findOwnerRoot(WhereUsedPanel.this.element.getFileObject());
                } else {
                    packageFolder = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(packageName.replaceAll("\\.", "/")); //NOI18N
                }
                
                final JLabel customScope;
                final JLabel currentFile;
                final JLabel currentPackage;
                final JLabel currentProject;
                final JLabel allProjects;
                if (p != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(FileOwnerQuery.getOwner(WhereUsedPanel.this.element.getFileObject()));
                    
                    SourceGroup[] sources = ProjectUtils.getSources(pi.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    projectSources = new FileObject[sources.length];
                    for (int i = 0; i < sources.length; i++) {
                        projectSources[i] = sources[i].getRootFolder();
                    }
                    
                    DataObject currentFileDo = null;
                    try {
                        currentFileDo = DataObject.find(WhereUsedPanel.this.element.getFileObject());
                    } catch (DataObjectNotFoundException ex) {
                    } // Not important, only for Icon.
                    customScope = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CustomScope"), pi.getIcon(), SwingConstants.LEFT); //NOI18N
                    currentFile = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentFile", WhereUsedPanel.this.element.getFileObject().getNameExt()), currentFileDo != null ? new ImageIcon(currentFileDo.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)) : pi.getIcon(), SwingConstants.LEFT); //NOI18N
                    currentPackage = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentPackage", packageName), ImageUtilities.loadImageIcon(PACKAGE, false), SwingConstants.LEFT); //NOI18N
                    currentProject = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_CurrentProject", pi.getDisplayName()), pi.getIcon(), SwingConstants.LEFT); //NOI18N
                    allProjects = new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_AllProjects"), pi.getIcon(), SwingConstants.LEFT); //NOI18N
                } else {
                    customScope = null;
                    currentFile = null;
                    currentPackage = null;
                    currentProject = null;
                    allProjects = null;
                }

                if((element.getKind().equals(ElementKind.LOCAL_VARIABLE) || element.getKind().equals(ElementKind.PARAMETER))
                        || element.getModifiers().contains(Modifier.PRIVATE)) {
                    enableScope = false;
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        innerPanel.removeAll();
                        innerPanel.add(panel, BorderLayout.CENTER);
                        innerPanel.invalidate();
                        panel.setVisible(true);

                        if(enableScope && currentProject!=null) {
                            scope.setModel(new DefaultComboBoxModel(new Object[]{allProjects, currentProject, currentPackage, currentFile, customScope }));
                            int defaultItem = (Integer) RefactoringModule.getOption("whereUsed.scope", 0); // NOI18N
                            WhereUsedPanel.this.customScope = readScope();
                            if(defaultItem == 4 && WhereUsedPanel.this.customScope !=null &&
                                    WhereUsedPanel.this.customScope.getFiles().isEmpty() &&
                                    WhereUsedPanel.this.customScope.getFolders().isEmpty() &&
                                    WhereUsedPanel.this.customScope.getSourceRoots().isEmpty()) {
                                scope.setSelectedIndex(0);
                            } else {
                                scope.setSelectedIndex(defaultItem);
                            }
                            scope.setRenderer(new JLabelRenderer());
                        } else {
                            scopePanel.setVisible(false);
                        }
                        validate();
                    }
                });
            }};
            try {
                source.runUserActionTask(task, true);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            initialized = true;
    }
    private static class JLabelRenderer extends JLabel implements ListCellRenderer, UIResource {
        public JLabelRenderer () {
            setOpaque(true);
        }
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                setText(((JLabel)value).getText());
                setIcon(((JLabel)value).getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

    static abstract class WhereUsedInnerPanel extends JPanel {
        abstract boolean isSearchInComments();
        abstract void initialize(Element element, CompilationController info);
    }

    private class ScopeAction extends AbstractAction {
        private final JComboBox scope;

        private ScopeAction(JComboBox scope) {
            this.scope = scope;
            this.putValue(NAME, ELLIPSIS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Scope customScope = getCustomScope();
            
            customScope = JavaScopeBuilder.open(NbBundle.getMessage(WhereUsedPanel.class, "DLG_CustomScope"), customScope); //NOI18N
            if (customScope != null) {
                WhereUsedPanel.this.customScope = customScope;
                scope.setSelectedIndex(4);
                storeScope(customScope);
            }
        }
    }
    
    private void storeScope(Scope customScope) {
        try {
            storeFileList(customScope.getSourceRoots(), "sourceRoot" ); //NOI18N
            storeFileList(customScope.getFolders(), "folder" ); //NOI18N
            storeFileList(customScope.getFiles(), "file" ); //NOI18N
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Scope readScope() {
        try {
            if (NbPreferences.forModule(JavaScopeBuilder.class).nodeExists(PREF_SCOPE)) { //NOI18N
                return Scope.create(
                        loadFileList("sourceRoot", FileObject.class), //NOI18N
                        loadFileList("folder", NonRecursiveFolder.class), //NOI18N
                        loadFileList("file", FileObject.class)); //NOI18N
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private <T> List<T> loadFileList(String basekey, Class<T> type) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(JavaScopeBuilder.class).node(PREF_SCOPE).node(basekey);
        List<T> toRet = new LinkedList<T>();
        for (String key : pref.keys()) {
            final String url = pref.get(key, null);
            if (url != null && !url.isEmpty()) {
                try {
                    final FileObject f = URLMapper.findFileObject(new URL(url));
                    if (f != null && f.isValid()) {
                        if (type.isAssignableFrom(FileObject.class)) {
                            toRet.add((T) f);
                        } else {
                            toRet.add((T) new NonRecursiveFolder() {

                                @Override
                                public FileObject getFolder() {
                                    return f;
                                }
                            });
                        }
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return toRet;
    }
    
    private void storeFileList(Set files, String basekey) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(WhereUsedPanel.class).node(PREF_SCOPE).node(basekey);
        assert files != null;
        pref.clear();
        int count = 0;
        for (Object next : files) {
            try {
                if (next instanceof FileObject) {
                    pref.put(basekey + count++, ((FileObject) next).getURL().toExternalForm());
                } else {
                    pref.put(basekey + count++, ((NonRecursiveFolder) next).getFolder().getURL().toExternalForm());
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        pref.flush();
    }
    
//    static String getHeader(Element call, CompilationInfo info) {
//        String result = ElementHeaders.getHeader(call, info, ElementHeaders.NAME + ElementHeaders.PARAMETERS);
//        if (result.length() > MAX_NAME) {
//            result = result.substring(0,MAX_NAME-1) + "..."; // NOI18N
//        }
//        return UIUtilities.htmlize(result);
//    }
    
    public TreePathHandle getMethodHandle() {
        return methodPanel.getMethodHandle();
    }
    
    @Override
    public void requestFocus() {
        super.requestFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        scopePanel = new javax.swing.JPanel();
        scopeLabel = new javax.swing.JLabel();
        scope = new javax.swing.JComboBox();
        btnCustomScope = new javax.swing.JButton();
        innerPanel = new javax.swing.JPanel();

        scopeLabel.setLabelFor(scope);
        org.openide.awt.Mnemonics.setLocalizedText(scopeLabel, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_Scope")); // NOI18N

        ((javax.swing.JTextField) scope.getEditor().getEditorComponent()).setColumns(SCOPE_COMBOBOX_COLUMNS);
        scope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scopeActionPerformed(evt);
            }
        });

        btnCustomScope.setText(ELLIPSIS);

        javax.swing.GroupLayout scopePanelLayout = new javax.swing.GroupLayout(scopePanel);
        scopePanel.setLayout(scopePanelLayout);
        scopePanelLayout.setHorizontalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scopeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scope, 0, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCustomScope)
                .addContainerGap())
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(scopePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(scopeLabel))
                    .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnCustomScope)
                        .addComponent(scope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        scope.getAccessibleContext().setAccessibleDescription("N/A");

        innerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scopePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scopePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void scopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeActionPerformed
    RefactoringModule.setOption("whereUsed.scope", scope.getSelectedIndex()); // NOI18N
}//GEN-LAST:event_scopeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomScope;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JComboBox scope;
    private javax.swing.JLabel scopeLabel;
    private javax.swing.JPanel scopePanel;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        return methodPanel.isMethodFromBaseClass();
    }
    
    public boolean isMethodOverriders() {
        return methodPanel.isMethodOverriders();
    }

    public boolean isMethodFindUsages() {
        return methodPanel.isMethodFindUsages();
    }

    public boolean isClassSubTypes() {
        return classPanel.isClassSubTypes();
    }
    
    public boolean isClassSubTypesDirectOnly() {
        return classPanel.isClassSubTypesDirectOnly();
    }

    public boolean isClassFindUsages() {
        return classPanel.isClassFindUsages();
    }
    
    public boolean isSearchInComments() {
        return panel.isSearchInComments();
    }

    @Override
    public Component getComponent() {
        return this;
    }
}

