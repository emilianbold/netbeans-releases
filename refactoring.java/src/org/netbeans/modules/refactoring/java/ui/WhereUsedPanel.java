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
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.netbeans.modules.refactoring.api.Scope;
import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import javax.lang.model.element.Modifier;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.plaf.UIResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;


/**
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N
    private final transient TreePathHandle element;
    private  TreePathHandle newElement;
    private final transient ChangeListener parent;
    private static final int MAX_NAME = 50;
    private static final int SCOPE_COMBOBOX_COLUMNS = 14;
    public static final String ELLIPSIS = "\u2026"; //NOI18N
    private Scope customScope;
    private boolean enableScope;
    
    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, TreePathHandle e, ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class,"LBL_WhereUsed")); // NOI18N
        this.element = e;
        this.parent = parent;
        this.enableScope = true;
        initComponents();
        btnCustomScope.setAction(new ScopeAction(scope));
    }
    
    public Scope getCustomScope() {
        FileObject file = RetoucheUtils.getFileObject(element);
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
    private String methodDeclaringSuperClass = null;
    private String methodDeclaringClass = null;
    private FileObject packageFolder = null;
    private FileObject[] projectSources = null;
    
    String getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }
    
    private Collection getOverriddenMethods(ExecutableElement m, CompilationInfo info) {
        return RetoucheUtils.getOverridenMethods(m, info);
    }
    
    public void initialize() {
        if (initialized) return;
        JavaSource source = JavaSource.forFileObject(element.getFileObject());
        final Project p = FileOwnerQuery.getOwner(element.getFileObject());
        CancellableTask<CompilationController> task =new CancellableTask<CompilationController>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                String m_isBaseClassText = null;
                final String labelText;
                Set<Modifier> modif = new HashSet<Modifier>();
                
                final Element element = WhereUsedPanel.this.element.resolveElement(info);
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) element;
                    modif = method.getModifiers();
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_MethodUsages", getHeader(method, info), getSimpleName(method.getEnclosingElement())); // NOI18N
                    
                    methodDeclaringClass = getSimpleName(method.getEnclosingElement());
                    Collection overridens = getOverriddenMethods(method, info);
                    if (!overridens.isEmpty()) {
                        ExecutableElement el = (ExecutableElement) overridens.iterator().next();                        
                        assert el!=null;
                        m_isBaseClassText =
                                new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOfBaseClass")).format( //NOI18N
                                new Object[] {
                            methodDeclaringSuperClass = getSimpleName((el).getEnclosingElement())
                        }
                        );
                        newElement = TreePathHandle.create(el, info);

                    }
                } else if (element.getKind().isClass() || element.getKind().isInterface()) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ClassUsages", element.getSimpleName()); // NOI18N
                } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_ConstructorUsages", getHeader(element,info), getSimpleName(element.getEnclosingElement())); // NOI18N
                } else if (element.getKind().isField()) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_FieldUsages", element.getSimpleName(), getSimpleName(element.getEnclosingElement())); // NOI18N
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_PackageUsages", element.getSimpleName()); // NOI18N
                } else {
                    labelText = NbBundle.getMessage(WhereUsedPanel.class, "DSC_VariableUsages", element.getSimpleName()); // NOI18N
                }

                TreePath path = WhereUsedPanel.this.element.resolve(info);
                final ExpressionTree packageName1 = path.getCompilationUnit().getPackageName();
                final String packageName = packageName1 == null? "<default package>" : packageName1.toString(); //NOI18N
                if(packageName1 == null) {
                    packageFolder = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findOwnerRoot(WhereUsedPanel.this.element.getFileObject());
                } else {
                    packageFolder = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(packageName.replaceAll("\\.", "/")); //NOI18N
                }
                
                final Set<Modifier> modifiers = modif;
                final String isBaseClassText = m_isBaseClassText;

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

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        remove(classesPanel);
                        remove(methodsPanel);
                        label.setText(labelText);
                        if (element instanceof ExecutableElement) {
                            add(methodsPanel, BorderLayout.CENTER);
                            methodsPanel.setVisible(true);
                            m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
                            m_overriders.setVisible(! (element.getEnclosingElement().getModifiers().contains(Modifier.FINAL) || modifiers.contains(Modifier.FINAL) || modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE) || element.getKind() == ElementKind.CONSTRUCTOR));
                            if (methodDeclaringSuperClass != null ) {
                                m_isBaseClass.setVisible(true);
                                m_isBaseClass.setSelected(true);
                                Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
                            } else {
                                m_isBaseClass.setVisible(false);
                                m_isBaseClass.setSelected(false);
                            }
                        } else if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.INTERFACE)) {
                            add(classesPanel, BorderLayout.CENTER);
                            classesPanel.setVisible(true);
                        } else {
                            remove(classesPanel);
                            remove(methodsPanel);
                            c_subclasses.setVisible(false);
                            m_usages.setVisible(false);
                            c_usages.setVisible(false);
                            c_directOnly.setVisible(false);
                        }
                        ElementKind kind = element.getKind();
                        if((kind.equals(ElementKind.LOCAL_VARIABLE) || kind.equals(ElementKind.PARAMETER))
                                || element.getModifiers().contains(Modifier.PRIVATE)) {
                            enableScope = false;
                        }
                        if(enableScope && currentProject!=null) {
                            scope.setModel(new DefaultComboBoxModel(new Object[]{allProjects, currentProject, currentPackage, currentFile, customScope }));
                            int defaultItem = (Integer) RefactoringModule.getOption("whereUsed.scope", 0); // NOI18N
                            scope.setSelectedIndex(defaultItem);
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
                throw (RuntimeException) new RuntimeException().initCause(ioe);
            }
            initialized = true;
    }
    private static class JLabelRenderer extends JLabel implements ListCellRenderer, UIResource {
        public JLabelRenderer () {
            setOpaque(true);
        }
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
            }
        }
    }
    
    private String getSimpleName(Element clazz) {
        return clazz.getSimpleName().toString();
        //return NbBundle.getMessage(WhereUsedPanel.class, "LBL_AnonymousClass"); // NOI18N
    }
    
    private String getHeader(Element call, CompilationInfo info) {
        String result = ElementHeaders.getHeader(call, info, ElementHeaders.NAME + ElementHeaders.PARAMETERS);
        if (result.length() > MAX_NAME) {
            result = result.substring(0,MAX_NAME-1) + "..."; // NOI18N
        }
        return RetoucheUtils.htmlize(result);
    }
    
    public TreePathHandle getBaseMethod() {
        return newElement;
    }
    
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
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        methodsPanel = new javax.swing.JPanel();
        m_isBaseClass = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        m_overriders = new javax.swing.JCheckBox();
        m_usages = new javax.swing.JCheckBox();
        classesPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        c_subclasses = new javax.swing.JRadioButton();
        c_usages = new javax.swing.JRadioButton();
        c_directOnly = new javax.swing.JRadioButton();
        commentsPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();
        scopePanel = new javax.swing.JPanel();
        scopeLabel = new javax.swing.JLabel();
        scope = new javax.swing.JComboBox();
        btnCustomScope = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        methodsPanel.setLayout(new java.awt.GridBagLayout());

        m_isBaseClass.setSelected(true);
        m_isBaseClass.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_isBaseClassStateChanged(evt);
            }
        });
        m_isBaseClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_isBaseClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_isBaseClass, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        m_isBaseClass.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_isBaseClass")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(m_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindOverridingMethods")); // NOI18N
        m_overriders.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_overridersStateChanged(evt);
            }
        });
        m_overriders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_overridersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_overriders, gridBagConstraints);
        m_overriders.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_overriders")); // NOI18N

        m_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(m_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        m_usages.setMargin(new java.awt.Insets(10, 2, 2, 2));
        m_usages.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_usagesStateChanged(evt);
            }
        });
        m_usages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_usagesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_usages, gridBagConstraints);
        m_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

        add(methodsPanel, java.awt.BorderLayout.CENTER);

        classesPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        classesPanel.add(jPanel2, gridBagConstraints);

        buttonGroup.add(c_subclasses);
        org.openide.awt.Mnemonics.setLocalizedText(c_subclasses, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindAllSubtypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_subclasses, gridBagConstraints);
        c_subclasses.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_subclasses")); // NOI18N

        buttonGroup.add(c_usages);
        c_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(c_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        c_usages.setMargin(new java.awt.Insets(4, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_usages, gridBagConstraints);
        c_usages.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_usages")); // NOI18N

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_directOnly")); // NOI18N

        add(classesPanel, java.awt.BorderLayout.CENTER);

        commentsPanel.setLayout(new java.awt.BorderLayout());
        commentsPanel.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        commentsPanel.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(commentsPanel, java.awt.BorderLayout.NORTH);

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
                .addComponent(scope, 0, 150, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCustomScope)
                .addContainerGap())
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(scopeLabel)
            .addComponent(scope)
            .addComponent(btnCustomScope)
        );

        scope.getAccessibleContext().setAccessibleDescription("N/A");

        add(scopePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

private void scopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeActionPerformed
    RefactoringModule.setOption("whereUsed.scope", scope.getSelectedIndex()); // NOI18N
}//GEN-LAST:event_scopeActionPerformed

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b); // NOI18N
    }//GEN-LAST:event_searchInCommentsItemStateChanged

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_isBaseClassActionPerformed

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_overridersActionPerformed

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_usagesActionPerformed

private void m_isBaseClassStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_isBaseClassStateChanged
        parent.stateChanged(null);
}//GEN-LAST:event_m_isBaseClassStateChanged

private void m_overridersStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_overridersStateChanged
        parent.stateChanged(null);
}//GEN-LAST:event_m_overridersStateChanged

private void m_usagesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_usagesStateChanged
        parent.stateChanged(null);
}//GEN-LAST:event_m_usagesStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomScope;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel commentsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox m_isBaseClass;
    private javax.swing.JCheckBox m_overriders;
    private javax.swing.JCheckBox m_usages;
    private javax.swing.JPanel methodsPanel;
    private javax.swing.JComboBox scope;
    private javax.swing.JLabel scopeLabel;
    private javax.swing.JPanel scopePanel;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        return m_isBaseClass.isSelected();
    }
    
    public boolean isMethodOverriders() {
        return m_overriders.isSelected();
    }
    
    public boolean isClassSubTypes() {
        return c_subclasses.isSelected();
    }
    
    public boolean isClassSubTypesDirectOnly() {
        return c_directOnly.isSelected();
    }
    
    public boolean isMethodFindUsages() {
        return m_usages.isSelected();
    }
    
    public boolean isClassFindUsages() {
        return c_usages.isSelected();
    }
    
//    public Dimension getPreferredSize() {
//        Dimension orig = super.getPreferredSize();
//        return new Dimension(orig.width + 30 , orig.height + 30);
//    }
    
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    public Component getComponent() {
        return this;
    }
}

