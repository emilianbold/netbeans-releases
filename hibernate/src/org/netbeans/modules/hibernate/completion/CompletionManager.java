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
package org.netbeans.modules.hibernate.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class CompletionManager {

    private static final String MAPPING_TAG = "hibernate-mapping";
    private static final String CLASS_TAG = "class";
    private static final String ID_TAG = "id";
    private static final String GENERATOR_TAG = "generator";
    private static final String PROPERTY_TAG = "property";
    private static final String SCHEMA_ATTRIB = "schema";
    private static final String CATALOG_ATTRIB = "catalog";
    private static final String TABLE_ATTRIB = "table"; // table name
    private static final String PACKAGE_ATTRIB = "package";
    private static final String CLASS_ATTRIB = "class";
    private static final String NAME_ATTRIB = "name";
    private static final String TYPE_ATTRIB = "type";
    private static final String COLUMN_ATTRIB = "column";
    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private CompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for id generator
        String[] generatorClasses = new String[]{
            "increment", NbBundle.getMessage(CompletionManager.class, "INCREMENT_GENERATOR_DESC"), // NOI18N
            "identity", NbBundle.getMessage(CompletionManager.class, "IDENTITY_GENERATOR_DESC"), // NOI18N
            "sequence", NbBundle.getMessage(CompletionManager.class, "SEQUENCE_GENERATOR_DESC"), // NOI18N
            "hilo", NbBundle.getMessage(CompletionManager.class, "HILO_GENERATOR_DESC"), // NOI18N
            "seqhilo", NbBundle.getMessage(CompletionManager.class, "SEQHILO_GENERATOR_DESC"), // NOI18N
            "uuid", NbBundle.getMessage(CompletionManager.class, "UUID_GENERATOR_DESC"), // NOI18N
            "guid", NbBundle.getMessage(CompletionManager.class, "GUID_GENERATOR_DESC"), // NOI18N
            "native", NbBundle.getMessage(CompletionManager.class, "NATIVE_GENERATOR_DESC"), // NOI18N
            "assigned", NbBundle.getMessage(CompletionManager.class, "ASSIGNED_GENERATOR_DESC"), // NOI18N
            "select", NbBundle.getMessage(CompletionManager.class, "SELECT_GENERATOR_DESC"), // NOI18N
            "foreign", NbBundle.getMessage(CompletionManager.class, "FOREIGN_GENERATOR_DESC"), // NOI18N
            "sequence-identity", NbBundle.getMessage(CompletionManager.class, "SEQUENCE_IDENTITY_GENERATOR_DESC") // NOI18N
        };

        // Id generator classes
        AttributeValueCompletor completor = new AttributeValueCompletor(generatorClasses);
        registerCompletor(GENERATOR_TAG, CLASS_ATTRIB, completor);

        JavaClassCompletor javaClassCompletor = new JavaClassCompletor(false);
        registerCompletor(CLASS_TAG, NAME_ATTRIB, javaClassCompletor);

        JavaClassCompletor javaPackageCompletor = new JavaClassCompletor(true);
        registerCompletor(MAPPING_TAG, PACKAGE_ATTRIB, javaPackageCompletor);

        PropertyCompletor propertyCompletor = new PropertyCompletor();
        registerCompletor(PROPERTY_TAG, NAME_ATTRIB, propertyCompletor);
    }
    private static CompletionManager INSTANCE = new CompletionManager();

    public static CompletionManager getDefault() {
        return INSTANCE;
    }

    public void completeAttributeValues(CompletionResultSet resultSet, CompletionContext context) {
        String tagName = context.getTag().getNodeName();
        TokenItem attrib = ContextUtilities.getAttributeToken(context.getCurrentToken());
        String attribName = attrib != null ? attrib.getImage() : null;

        Completor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            resultSet.addAllItems(completor.doCompletion(context));
            if (completor.getAnchorOffset() != -1) {
                resultSet.setAnchorOffset(completor.getAnchorOffset());
            }
        }
    }

    public void completeAttributes(CompletionResultSet resultSet, CompletionContext context) {
    // TBD
    }

    public void completeElements(CompletionResultSet resultSet, CompletionContext context) {
    // TBD
    }

    private static abstract class Completor {

        private int anchorOffset = -1;

        public abstract List<HibernateMappingCompletionItem> doCompletion(CompletionContext context);

        protected void setAnchorOffset(int anchorOffset) {
            this.anchorOffset = anchorOffset;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }
    }

    private void registerCompletor(String tagName, String attribName,
            Completor completor) {
        completors.put(createRegisteredName(tagName, attribName), completor);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (nodeName != null && nodeName.trim().length() > 0) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (attributeName != null && attributeName.trim().length() > 0) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private Completor locateCompletor(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        return null;
    }

    /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    private static class AttributeValueCompletor extends Completor {

        private String[] itemTextAndDocs;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateMappingCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars)) {
                    HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
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
    private static class JavaClassCompletor extends Completor {

        private boolean packageOnly = false;

        public JavaClassCompletor(boolean packageOnly) {
            this.packageOnly = packageOnly;
        }

        public List<HibernateMappingCompletionItem> doCompletion(final CompletionContext context) {
            final List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = HibernateMappingCompletionItem.getJavaSource(doc);
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

        private void doNormalJavaCompletion(JavaSource js, final List<HibernateMappingCompletionItem> results,
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
                                HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createTypeItem(substitutionOffset,
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

        private void doSmartJavaCompletion(JavaSource js, final List<HibernateMappingCompletionItem> results,
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
                                TypeElement typeElement = eh.resolve(cc);
                                if (typeElement != null) {
                                    HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createTypeItem(substitutionOffset,
                                            typeElement, eh, cc.getElements().isDeprecated(typeElement), true);
                                    results.add(item);
                                }
                            }
                        }
                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }

        private void addPackages(ClassIndex ci, List<HibernateMappingCompletionItem> results, String typedPrefix, int substitutionOffset) {
            Set<String> packages = ci.getPackageNames(typedPrefix, true, EnumSet.allOf(SearchScope.class));
            for (String pkg : packages) {
                if (pkg.length() > 0) {
                    HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                    results.add(item);
                }
            }
        }
    }

    private static class PropertyCompletor extends Completor {

        public PropertyCompletor() {
        }

        @Override
        public List<HibernateMappingCompletionItem> doCompletion(final CompletionContext context) {
            final List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
            final int caretOffset = context.getCaretOffset();
            final String typedChars = context.getTypedPrefix();

            Document doc = context.getDocument();
            HibernateMappingDataObject mappingDataObject = (HibernateMappingDataObject)NbEditorUtilities.getDataObject(doc);
                    
            if (mappingDataObject == null) {
                return Collections.emptyList();
            }
            
            java.io.InputStream is = null;
            try {
                // Get the class name

                is = mappingDataObject.getEditorSupport().getInputStream();
                HibernateMapping mapping = HibernateMapping.createGraph(is);
                
                // Find out which class element we are working on
                List<String> elements = context.getAllElmentsToRoot();
                int numClassElem = 0;
                for( String elem : elements ) {
                    if( elem.equalsIgnoreCase("class")) { // NOI18N
                        numClassElem ++;
                    }
                }
                
                MyClass myClass = mapping.getMyClass(numClassElem);
                final String className = myClass.getAttributeValue("Name"); // NOI18N

                // Compile the class and find the fiels
                JavaSource classJavaSrc = HibernateMappingCompletionItem.getJavaSource(doc);
                classJavaSrc.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.ELEMENTS_RESOLVED);
                        ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                        TypeElement typeElem = cc.getElements().getTypeElement(className);

                        if (typeElem == null) {
                            return;
                        }

                        List<? extends Element> clsChildren = typeElem.getEnclosedElements();
                        for (Element clsChild : clsChildren) {
                            if (clsChild.getKind() == ElementKind.FIELD) {
                                VariableElement elem = (VariableElement) clsChild;
                                HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createClassPropertyItem(caretOffset - typedChars.length(),
                                        elem, ElementHandle.create(elem), cc.getElements().isDeprecated(clsChild));
                                results.add(item);
                            }
                        }
                    }
                }, true);

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }
    }
}
