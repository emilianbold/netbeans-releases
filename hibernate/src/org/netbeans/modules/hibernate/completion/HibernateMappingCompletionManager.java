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

import org.netbeans.modules.hibernate.editor.ContextUtilities;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.TokenItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.NbBundle;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class HibernateMappingCompletionManager {

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
    private static final String ID_TYPE_ATTRIB = "id-type";
    
    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private HibernateMappingCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for id generator
        String[] generatorClasses = new String[]{
            "increment", NbBundle.getMessage(HibernateMappingCompletionManager.class, "INCREMENT_GENERATOR_DESC"), // NOI18N
            "identity", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IDENTITY_GENERATOR_DESC"), // NOI18N
            "sequence", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQUENCE_GENERATOR_DESC"), // NOI18N
            "hilo", NbBundle.getMessage(HibernateMappingCompletionManager.class, "HILO_GENERATOR_DESC"), // NOI18N
            "seqhilo", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQHILO_GENERATOR_DESC"), // NOI18N
            "uuid", NbBundle.getMessage(HibernateMappingCompletionManager.class, "UUID_GENERATOR_DESC"), // NOI18N
            "guid", NbBundle.getMessage(HibernateMappingCompletionManager.class, "GUID_GENERATOR_DESC"), // NOI18N
            "native", NbBundle.getMessage(HibernateMappingCompletionManager.class, "NATIVE_GENERATOR_DESC"), // NOI18N
            "assigned", NbBundle.getMessage(HibernateMappingCompletionManager.class, "ASSIGNED_GENERATOR_DESC"), // NOI18N
            "select", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SELECT_GENERATOR_DESC"), // NOI18N
            "foreign", NbBundle.getMessage(HibernateMappingCompletionManager.class, "FOREIGN_GENERATOR_DESC"), // NOI18N
            "sequence-identity", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQUENCE_IDENTITY_GENERATOR_DESC") // NOI18N
         // NOI18N
        };

        // Completion items for Hibernate type
        String[] hibernateTypes = new String[]{
            "big_decimal", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BIG_DECIMAL_DESC"), // NOI18N
            "big_integer", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BIG_INTEGER_DESC"), // NOI18N
            "binary", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BINARY_DESC"), // NOI18N
            "blob", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BLOB_DESC"), // NOI18N
            "boolean", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BOOLEAN_DESC"), // NOI18N
            "byte", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BYTE_DESC"), // NOI18N
            "calendar", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CALENDAR_DESC"), // NOI18N
            "calendar_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CALENDAR_DATE_DESC"), // NOI18N
            "character", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CHARACTER_DESC"), // NOI18N
            "class", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CLASS_DESC"), // NOI18N
            "clob", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CLOB_DESC"), // NOI18N
            "currency", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CURRENCY_DESC"), // NOI18N
            "date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "DATE_DESC"), // NOI18N
            "double", NbBundle.getMessage(HibernateMappingCompletionManager.class, "DOUBLE_DESC"), // NOI18N
            "float", NbBundle.getMessage(HibernateMappingCompletionManager.class, "FLOAT_DESC"), // NOI18N
            "imm_binary", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_BINARY_DESC"), // NOI18N
            "imm_calendar", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_CALENDAR_DESC"), // NOI18N
            "imm_calendar_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_CALENDAR_DATE_DESC"), // NOI18N
            "imm_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_DATE_DESC"), // NOI18N
            "imm_serializable", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_SERIALIZABLE_DESC"), // NOI18N
            "imm_time", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_TIME_DESC"), // NOI18N
            "imm_timestamp", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_TIMESTAMP_DESC"), // NOI18N
            "integer", NbBundle.getMessage(HibernateMappingCompletionManager.class, "INTEGER_DESC"), // NOI18N
            "locale", NbBundle.getMessage(HibernateMappingCompletionManager.class, "LOCALE_DESC"), // NOI18N
            "long", NbBundle.getMessage(HibernateMappingCompletionManager.class, "LONG_DESC"), // NOI18N
            "serializable", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SERIALIZABLE_DESC"), // NOI18N
            "short", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SHORT_DESC"), // NOI18N
            "string", NbBundle.getMessage(HibernateMappingCompletionManager.class, "STRING_DESC"), // NOI18N
            "text", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TEXT_DESC"), // NOI18N
            "time", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIME_DESC"), // NOI18N
            "timestamp", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIMESTAMP_DESC"), // NOI18N
            "timezone", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIMEZONE_DESC"), // NOI18N,
            "true_false", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TRUE_FALSE_DESC"), // NOI18N
            "yes_no", NbBundle.getMessage(HibernateMappingCompletionManager.class, "YES_NO_DESC") // NOI18N
         // NOI18N
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
        Completor.JavaClassCompletor javaPackageCompletor = new Completor.JavaClassCompletor(true);
        registerCompletor(MAPPING_TAG, PACKAGE_ATTRIB, javaPackageCompletor);

        // Items for Id generator classes
        Completor.AttributeValueCompletor generatorCompletor = new Completor.AttributeValueCompletor(generatorClasses);
        registerCompletor(GENERATOR_TAG, CLASS_ATTRIB, generatorCompletor);

        // Items for Hibernate type 
        Completor.AttributeValueCompletor typeCompletor = new Completor.AttributeValueCompletor(hibernateTypes);
        registerCompletor(PROPERTY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(ID_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(DISCRIMINATOR_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(KEY_PROPERTY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(VERSION_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(ELEMENT_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(MAP_KEY_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(INDEX_TAG, TYPE_ATTRIB, typeCompletor);
        registerCompletor(ANY_TAG, ID_TYPE_ATTRIB, typeCompletor);

        // Items for classes to be mapped
        Completor.JavaClassCompletor javaClassCompletor = new Completor.JavaClassCompletor(false);
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
        Completor.PropertyCompletor propertyCompletor = new Completor.PropertyCompletor();
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
        Completor.DatabaseTableCompletor databaseTableCompletor = new Completor.DatabaseTableCompletor();
        registerCompletor(CLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(SET_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(JOINED_SUBCLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(UNION_SUBCLASS_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(JOIN_TAG, TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(MAP_TAG, TABLE_ATTRIB, databaseTableCompletor);

        // Items for database columns to be mapped to
        Completor.DatabaseTableColumnCompletor databaseColumnCompletor = new Completor.DatabaseTableColumnCompletor();
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
        Completor.CascadeStyleCompletor cascadeStyleCompletor = new Completor.CascadeStyleCompletor(cascadeStyles);
        registerCompletor(MANY_TO_ONE_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(ONE_TO_ONE_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(ANY_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(MAP_TAG, CASCADE_ATTRIB, cascadeStyleCompletor);
    }
    private static HibernateMappingCompletionManager INSTANCE = new HibernateMappingCompletionManager();

    public static HibernateMappingCompletionManager getDefault() {
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
}
