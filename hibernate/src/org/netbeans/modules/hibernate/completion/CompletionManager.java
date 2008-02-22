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
    private static final String SET_TAG = "set";
    private static final String KEY_TAG = "key";
    private static final String ONE_TO_MANY_TAG = "one-to-many";
    private static final String DISCRIMINATOR_TAG = "discriminator";
    private static final String COMPOSITE_ID_TAG = "composite-id";
    private static final String KEY_PROPERTY_TAG = "key-property";
    private static final String KEY_MANY_TO_ONE_TAG = "key-many-to-one";
    private static final String VERSION_TAG = "version";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String MANY_TO_ONE_TAG = "many-to-one";
    private static final String ONE_TO_ONE_TAG = "one-to-one";
    private static final String COMPONENT_TAG = "component";
    private static final String SUBCLASS_TAG = "subclass";
    private static final String JOINED_SUBCLASS_TAG = "joined-subclass";
    private static final String UNION_SUBCLASS_TAG = "union-subclass";
    private static final String JOIN_TAG = "join";
    private static final String COLUMN_TAG = "column";
    private static final String IMPORT_TAG = "import";
    private static final String ANY_TAG = "any";
    private static final String MAP_TAG = "map";
    private static final String LIST_TAG = "list";
    private static final String LIST_INDEX_TAG = "list-index";
    private static final String INDEX_TAG = "index";
    private static final String MAP_KEY_TAG = "map-key";
    private static final String ELEMENT_TAG = "element";
    private static final String MANY_TO_MANY_TAG = "many-to-many";
    private static final String TABLE_ATTRIB = "table"; // table name
    private static final String PACKAGE_ATTRIB = "package";
    private static final String CLASS_ATTRIB = "class";
    private static final String NAME_ATTRIB = "name";
    private static final String TYPE_ATTRIB = "type";
    private static final String COLUMN_ATTRIB = "column";
    private static final String EXTENDS_ATTRIB = "extends";
    private static final String PERSISTER_ATTRIB = "persister";
    private static final String CASCADE_ATTRIB = "cascade";
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

        // Completion items for Hibernate type
        String[] hibernateTypes = new String[]{
            "big_decimal", NbBundle.getMessage(CompletionManager.class, "BIG_DECIMAL_DESC"), // NOI18N
            "big_integer", NbBundle.getMessage(CompletionManager.class, "BIG_INTEGER_DESC"), // NOI18N
            "binary", NbBundle.getMessage(CompletionManager.class, "BINARY_DESC"), // NOI18N
            "blob", NbBundle.getMessage(CompletionManager.class, "BLOB_DESC"), // NOI18N
            "boolean", NbBundle.getMessage(CompletionManager.class, "BOOLEAN_DESC"), // NOI18N
            "byte", NbBundle.getMessage(CompletionManager.class, "BYTE_DESC"), // NOI18N
            "calendar", NbBundle.getMessage(CompletionManager.class, "CALENDAR_DESC"), // NOI18N
            "calendar_date", NbBundle.getMessage(CompletionManager.class, "CALENDAR_DATE_DESC"), // NOI18N
            "character", NbBundle.getMessage(CompletionManager.class, "CHARACTER_DESC"), // NOI18N
            "class", NbBundle.getMessage(CompletionManager.class, "CLASS_DESC"), // NOI18N
            "clob", NbBundle.getMessage(CompletionManager.class, "CLOB_DESC"), // NOI18N
            "currency", NbBundle.getMessage(CompletionManager.class, "CURRENCY_DESC"), // NOI18N
            "date", NbBundle.getMessage(CompletionManager.class, "DATE_DESC"), // NOI18N
            "double", NbBundle.getMessage(CompletionManager.class, "DOUBLE_DESC"), // NOI18N
            "float", NbBundle.getMessage(CompletionManager.class, "FLOAT_DESC"), // NOI18N
            "imm_binary", NbBundle.getMessage(CompletionManager.class, "IMM_BINARY_DESC"), // NOI18N
            "imm_calendar", NbBundle.getMessage(CompletionManager.class, "IMM_CALENDAR_DESC"), // NOI18N
            "imm_calendar_date", NbBundle.getMessage(CompletionManager.class, "IMM_CALENDAR_DATE_DESC"), // NOI18N
            "imm_date", NbBundle.getMessage(CompletionManager.class, "IMM_DATE_DESC"), // NOI18N
            "imm_serializable", NbBundle.getMessage(CompletionManager.class, "IMM_SERIALIZABLE_DESC"), // NOI18N
            "imm_time", NbBundle.getMessage(CompletionManager.class, "IMM_TIME_DESC"), // NOI18N
            "imm_timestamp", NbBundle.getMessage(CompletionManager.class, "IMM_TIMESTAMP_DESC"), // NOI18N
            "integer", NbBundle.getMessage(CompletionManager.class, "INTEGER_DESC"), // NOI18N
            "locale", NbBundle.getMessage(CompletionManager.class, "LOCALE_DESC"), // NOI18N
            "long", NbBundle.getMessage(CompletionManager.class, "LONG_DESC"), // NOI18N
            "serializable", NbBundle.getMessage(CompletionManager.class, "SERIALIZABLE_DESC"), // NOI18N
            "short", NbBundle.getMessage(CompletionManager.class, "SHORT_DESC"), // NOI18N
            "string", NbBundle.getMessage(CompletionManager.class, "STRING_DESC"), // NOI18N
            "text", NbBundle.getMessage(CompletionManager.class, "TEXT_DESC"), // NOI18N
            "time", NbBundle.getMessage(CompletionManager.class, "TIME_DESC"), // NOI18N
            "timestamp", NbBundle.getMessage(CompletionManager.class, "TIMESTAMP_DESC"), // NOI18N
            "timezone", NbBundle.getMessage(CompletionManager.class, "TIMEZONE_DESC"), // NOI18N,
            "true_false", NbBundle.getMessage(CompletionManager.class, "TRUE_FALSE_DESC"), // NOI18N
            "yes_no", NbBundle.getMessage(CompletionManager.class, "YES_NO_DESC") // NOI18N
        };

        String[] cascadeStyles = new String[]{
            "none", null, // NOI18N
            "all", null, // NOI18N
            "delete", null, // NOI18N
            "delete-orphan", null, // NOI18N
            "evict", null, // NOI18N
            "refresh", null, // NOI18N
            "lock", null, // NOI18N
            "merge", null, // NOI18N
            "persist", null, // NOI18N
            "replicate", null, // NOI18N
            "save-update", null // NOI18N
        };

        // Items for package attribute in the root element
        JavaClassCompletor javaPackageCompletor = new JavaClassCompletor(true);
        registerCompletor(MAPPING_TAG, PACKAGE_ATTRIB, javaPackageCompletor);

        // Items for Id generator classes
        AttributeValueCompletor generatorCompletor = new AttributeValueCompletor(generatorClasses);
        registerCompletor(GENERATOR_TAG, CLASS_ATTRIB, generatorCompletor);

        // Items for Hibernate type 
        AttributeValueCompletor typeCompletor = new AttributeValueCompletor(hibernateTypes);
        registerCompletor(PROPERTY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(ID_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(DISCRIMINATOR_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(KEY_PROPERTY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(VERSION_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(ELEMENT_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(MAP_KEY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(INDEX_TAG, TYPE_ATTRIB, typeCompletor);

        // Items for classes to be mapped
        JavaClassCompletor javaClassCompletor = new JavaClassCompletor(false);
        registerCompletor(CLASS_TAG, NAME_ATTRIB, javaClassCompletor);
        registerCompletor(ONE_TO_MANY_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(COMPOSITE_ID_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(KEY_MANY_TO_ONE_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(MANY_TO_ONE_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(ONE_TO_ONE_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(COMPONENT_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(SUBCLASS_TAG, NAME_ATTRIB, javaClassCompletor);
        registerCompletor(SUBCLASS_TAG, EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(JOINED_SUBCLASS_TAG, NAME_ATTRIB, javaClassCompletor);
        registerCompletor(JOINED_SUBCLASS_TAG, EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(JOINED_SUBCLASS_TAG, PERSISTER_ATTRIB, javaClassCompletor);
        registerCompletor(UNION_SUBCLASS_TAG, NAME_ATTRIB, javaClassCompletor);
        registerCompletor(UNION_SUBCLASS_TAG, EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(UNION_SUBCLASS_TAG, PERSISTER_ATTRIB, javaClassCompletor);
        registerCompletor(IMPORT_TAG, CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(MANY_TO_MANY_TAG, CLASS_ATTRIB, javaClassCompletor);

        // Items for properties to be mapped
        PropertyCompletor propertyCompletor = new PropertyCompletor();
        registerCompletor(PROPERTY_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(ID_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(SET_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(COMPOSITE_ID_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(KEY_PROPERTY_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(KEY_MANY_TO_ONE_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(VERSION_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(TIMESTAMP_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(MANY_TO_ONE_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(ONE_TO_ONE_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(COMPONENT_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(ANY_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(MAP_TAG, NAME_ATTRIB, propertyCompletor);
        registerCompletor(LIST_TAG, NAME_ATTRIB, propertyCompletor);

        // Items for database tables to be mapped to
        DatabaseTableCompletor databaseTableCompletor = new DatabaseTableCompletor();
        registerCompletor(CLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(SET_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(JOINED_SUBCLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(UNION_SUBCLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(JOIN_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(MAP_TAG, TABLE_ATTRIB, databaseTableCompletor);

        // Items for database columns to be mapped to
        DatabaseTableColumnCompletor databaseColumnCompletor = new DatabaseTableColumnCompletor();
        registerCompletor(PROPERTY_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(ID_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(KEY_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(DISCRIMINATOR_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(KEY_PROPERTY_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(KEY_MANY_TO_ONE_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(VERSION_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(TIMESTAMP_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(MANY_TO_ONE_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(COLUMN_TAG, NAME_ATTRIB, databaseColumnCompletor);
        registerCompletor(LIST_INDEX_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(INDEX_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(MAP_KEY_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(ELEMENT_TAG, COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(MANY_TO_MANY_TAG, COLUMN_ATTRIB, databaseColumnCompletor);

        // Items for cascade attribute
        CascadeStyleCompletor cascadeStyleCompletor = new CascadeStyleCompletor(cascadeStyles);
        registerCompletor(MANY_TO_ONE_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(ONE_TO_ONE_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(ANY_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(MAP_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
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
                if (itemTextAndDocs[i].startsWith(typedChars.trim())) {
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
     * A  completor for completing the cascade attribute with cascade styles
     * 
     */
    private static class CascadeStyleCompletor extends Completor {

        private String[] itemTextAndDocs;

        public CascadeStyleCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateMappingCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
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
                    HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createCascadeStyleItem(caretOffset - styleName.length(),
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

        private void doSmartJavaCompletion(final JavaSource js, final List<HibernateMappingCompletionItem> results,
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
                                HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createClassPropertyItem(caretOffset - typedChars.length(), elem, ElementHandle.create(elem), cc.getElements().isDeprecated(clsChild));
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

    private static class DatabaseTableCompletor extends Completor {

        public DatabaseTableCompletor() {

        }

        @Override
        public List<HibernateMappingCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            // TODO: call Vadiraj's API to get the database tables
            // For now: hard code them so that I can test my completor
            List<String> tableNames = new ArrayList<String>();
            tableNames.add("PERSON");
            tableNames.add("TRIP");
            tableNames.add("TRIPTYPE");
            tableNames.add("FLIGHT");
            tableNames.add("HOTEL");
            tableNames.add("CARRENTAL");
            tableNames.add("VALIDATION_TABLE");

            for (String tableName : tableNames) {
                HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createDatabaseTableItem(
                        caretOffset - typedChars.length(), tableName);
                results.add(item);
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }
    }

    private static class DatabaseTableColumnCompletor extends Completor {

        public DatabaseTableColumnCompletor() {

        }

        @Override
        public List<HibernateMappingCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateMappingCompletionItem> results = new ArrayList<HibernateMappingCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            final String tableName = HibernateCompletionEditorUtil.getTableName(context.getTag());

            if (tableName == null) {
                return Collections.emptyList();
            }

            // For now: hard code them for the PERSON table so that I can test my completor
            List<String> columnNamesPerson = new ArrayList<String>();
            columnNamesPerson.add("PERSONID");
            columnNamesPerson.add("NAME");
            columnNamesPerson.add("JOBTITLE");
            columnNamesPerson.add("FREQUENTFLYER");
            columnNamesPerson.add("LASTUPDATED");

            List<String> columnNamesTrip = new ArrayList<String>();
            columnNamesTrip.add("TRIPID");
            columnNamesTrip.add("PERSONID");
            columnNamesTrip.add("DEPDATE");
            columnNamesTrip.add("DEPCITY");
            columnNamesTrip.add("DESTCITY");
            columnNamesTrip.add("TRIPTYPEID");
            columnNamesTrip.add("LASTUPDATED");

            List<String> columnNames = null;
            if (tableName.equalsIgnoreCase("PERSON")) {
                columnNames = columnNamesPerson;
            } else {
                columnNames = columnNamesTrip;
            }

            for (String columnName : columnNames) {
                boolean pk = false;
                if ((tableName.equalsIgnoreCase("PERSON") && columnName.equals("PERSONID")) ||
                        (tableName.equalsIgnoreCase("TRIP") && columnName.equals("TRIPID"))) {
                    pk = true;
                }

                HibernateMappingCompletionItem item = HibernateMappingCompletionItem.createDatabaseColumnItem(
                        caretOffset - typedChars.length(), columnName, pk);
                results.add(item);
            }


            setAnchorOffset(context.getCurrentToken().getOffset() + 1);

            return results;
        }
    }
}
