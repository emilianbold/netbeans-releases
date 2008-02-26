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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.hibernate.service.TableColumn;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Various completor for code completing XML tags and attributes in Hibername 
 * configuration and mapping file
 * 
 * @author Dongmei Cao
 */
public abstract class Completor {

    private int anchorOffset = -1;

    public abstract List<HibernateCompletionItem> doCompletion(CompletionContext context);

    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    public static class AttributeValueCompletor extends Completor {

        private String[] itemTextAndDocs;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars.trim())) {
                    HibernateCompletionItem item = HibernateCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }

    /**
     * A  completor for completing the cascade attribute with cascade styles
     * 
     */
    public static class CascadeStyleCompletor extends Completor {

        private String[] itemTextAndDocs;

        public CascadeStyleCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            String styleName = null;
            if (typedChars.contains(",")) {
                int index = typedChars.lastIndexOf(",");
                styleName = typedChars.substring(index + 1);
            } else {
                styleName = typedChars;
            }

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(styleName.trim())) {
                    HibernateCompletionItem item = HibernateCompletionItem.createCascadeStyleItem(caretOffset - styleName.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }

    /**
     * For Java class items
     */
    public static class JavaClassCompletor extends Completor {

        private boolean packageOnly = false;

        public JavaClassCompletor(boolean packageOnly) {
            this.packageOnly = packageOnly;
        }

        public List<HibernateCompletionItem> doCompletion(final CompletionContext context) {
            final List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = HibernateCompletionEditorUtil.getJavaSource(doc);
                if (js == null) {
                    return Collections.emptyList();
                }

                if (typedChars.contains(".") || typedChars.equals("")) { // Switch to normal completion
                    doNormalJavaCompletion(js, results, typedChars, context.getCurrentToken().getOffset() + 1);
                } else { // Switch to smart class path completion
                    doSmartJavaCompletion(js, results, typedChars, context.getCurrentToken().getOffset() + 1);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;
        }

        private void doNormalJavaCompletion(JavaSource js, final List<HibernateCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                    int index = substitutionOffset;
                    String packName = typedPrefix;
                    String classPrefix = "";
                    int dotIndex = typedPrefix.lastIndexOf('.'); // NOI18N
                    if (dotIndex != -1) {
                        index += (dotIndex + 1);  // NOI18N
                        packName = typedPrefix.substring(0, dotIndex);
                        classPrefix = (dotIndex + 1 < typedPrefix.length()) ? typedPrefix.substring(dotIndex + 1) : "";
                    }
                    addPackages(ci, results, typedPrefix, index);

                    PackageElement pkgElem = cc.getElements().getPackageElement(packName);
                    if (pkgElem == null) {
                        return;
                    }

                    if (!packageOnly) {
                        List<? extends Element> pkgChildren = pkgElem.getEnclosedElements();
                        for (Element pkgChild : pkgChildren) {
                            if ((pkgChild.getKind() == ElementKind.CLASS) && pkgChild.getSimpleName().toString().startsWith(classPrefix)) {
                                TypeElement typeElement = (TypeElement) pkgChild;
                                HibernateCompletionItem item = HibernateCompletionItem.createTypeItem(substitutionOffset,
                                        typeElement, ElementHandle.create(typeElement),
                                        cc.getElements().isDeprecated(pkgChild), false);
                                results.add(item);
                            }
                        }
                    }

                    setAnchorOffset(index);
                }
            }, true);
        }

        private void doSmartJavaCompletion(final JavaSource js, final List<HibernateCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                    // add packages
                    addPackages(ci, results, typedPrefix, substitutionOffset);

                    if (!packageOnly) {
                        // add classes 
                        Set<ElementHandle<TypeElement>> matchingTypes = ci.getDeclaredTypes(typedPrefix,
                                NameKind.CASE_INSENSITIVE_PREFIX, EnumSet.allOf(SearchScope.class));
                        for (ElementHandle<TypeElement> eh : matchingTypes) {
                            if (eh.getKind() == ElementKind.CLASS) {
                                if (eh.getKind() == ElementKind.CLASS) {
                                    LazyTypeCompletionItem item = LazyTypeCompletionItem.create(substitutionOffset, eh, js);
                                    results.add(item);
                                }
                            }
                        }
                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }

        private void addPackages(ClassIndex ci, List<HibernateCompletionItem> results, String typedPrefix, int substitutionOffset) {
            Set<String> packages = ci.getPackageNames(typedPrefix, true, EnumSet.allOf(SearchScope.class));
            for (String pkg : packages) {
                if (pkg.length() > 0) {
                    HibernateCompletionItem item = HibernateCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                    results.add(item);
                }
            }
        }
    }

    public static class PropertyCompletor extends Completor {

        public PropertyCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(final CompletionContext context) {

            final List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            final int caretOffset = context.getCaretOffset();
            final String typedChars = context.getTypedPrefix();

            final String className = HibernateCompletionEditorUtil.getClassName(context.getTag());
            if (className == null) {
                return Collections.emptyList();
            }

            try {
                // Compile the class and find the fiels
                JavaSource classJavaSrc = HibernateCompletionEditorUtil.getJavaSource(context.getDocument());
                classJavaSrc.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElem = cc.getElements().getTypeElement(className);

                        if (typeElem == null) {
                            return;
                        }

                        List<? extends Element> clsChildren = typeElem.getEnclosedElements();
                        for (Element clsChild : clsChildren) {
                            if (clsChild.getKind() == ElementKind.FIELD) {
                                VariableElement elem = (VariableElement) clsChild;
                                HibernateCompletionItem item = HibernateCompletionItem.createClassPropertyItem(caretOffset - typedChars.length(), elem, ElementHandle.create(elem), cc.getElements().isDeprecated(clsChild));
                                results.add(item);
                            }
                        }
                    }
                }, true);


            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }
    }

    public static class DatabaseTableCompletor extends Completor {

        public DatabaseTableCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            List<String> tableNames = getDatabaseTableNamesForThisMappingFile(context);

            for (String tableName : tableNames) {
                HibernateCompletionItem item = HibernateCompletionItem.createDatabaseTableItem(
                        caretOffset - typedChars.length(), tableName);
                results.add(item);
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }

        private List<String> getDatabaseTableNamesForThisMappingFile(CompletionContext context) {
            List<String> tableNames = new ArrayList<String>();
            FileObject mappingFile = org.netbeans.modules.editor.NbEditorUtilities.getFileObject(context.getDocument());
            org.netbeans.api.project.Project enclosingProject = org.netbeans.api.project.FileOwnerQuery.getOwner(
                    mappingFile);
            org.netbeans.modules.hibernate.service.HibernateEnvironment env = enclosingProject.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
            tableNames = env.getDatabaseTables(mappingFile);
            return tableNames;
        }
    }

    public static class DatabaseTableColumnCompletor extends Completor {

        public DatabaseTableColumnCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            final String tableName = HibernateCompletionEditorUtil.getTableName(context.getTag());

            if (tableName == null) {
                return Collections.emptyList();
            }

            List<TableColumn> tableColumns = getColumnsForTable(context, tableName);

            for (TableColumn tableColumn : tableColumns) {
                HibernateCompletionItem item = HibernateCompletionItem.createDatabaseColumnItem(
                        caretOffset - typedChars.length(), tableColumn.getColumnName(), tableColumn.isPrimaryKey());
                results.add(item);
            }


            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }

        private List<TableColumn> getColumnsForTable(CompletionContext context, String tableName) {
            List<TableColumn> tableColumns = new ArrayList<TableColumn>();
            FileObject currentFileObject = org.netbeans.modules.editor.NbEditorUtilities.getFileObject(context.getDocument());
            org.netbeans.api.project.Project enclosingProject = org.netbeans.api.project.FileOwnerQuery.getOwner(
                    currentFileObject);
            org.netbeans.modules.hibernate.service.HibernateEnvironment env = enclosingProject.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
            tableColumns = env.getColumnsForTable(tableName, currentFileObject);
            return tableColumns;
        }
    }
    
        /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    public static class HbPropertyNameCompletor extends Completor {

        private String[] itemTextAndDocs;

        public HbPropertyNameCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            
            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars.trim()) 
                        || itemTextAndDocs[i].startsWith( "hibernate." + typedChars.trim()) ) { // NOI18N
                    HibernateCompletionItem item = HibernateCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }

    public static class HbMappingFileCompletor extends Completor {

        public HbMappingFileCompletor() {
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            String[] mappingFiles = getMappingFilesFromProject(context);

            for (int i = 0; i < mappingFiles.length; i++) {
                if (mappingFiles[i].startsWith(typedChars.trim())) {
                    HibernateCompletionItem item =
                            HibernateCompletionItem.createHbMappingFileItem(caretOffset - typedChars.length(),
                            mappingFiles[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
        
        // Gets the list of mapping files from HibernateEnvironment.
        private String[] getMappingFilesFromProject(CompletionContext context) {
            ArrayList<String> mappingFiles = new ArrayList<String>();
            org.netbeans.api.project.Project enclosingProject = org.netbeans.api.project.FileOwnerQuery.getOwner(
                    org.netbeans.modules.editor.NbEditorUtilities.getFileObject(context.getDocument())
                    );
            org.netbeans.modules.hibernate.service.HibernateEnvironment env = enclosingProject.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
            ArrayList<FileObject> mappingFileObjects = env.getAllHibernateMappingFileObjects(enclosingProject);
            for(FileObject fo : mappingFileObjects) {
                mappingFiles.add(fo.getPath());
            }
            return mappingFiles.toArray(new String[]{});
        }
    }
}
