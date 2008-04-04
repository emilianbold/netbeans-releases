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

package org.netbeans.modules.spring.beans.completion;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.Property;
import org.netbeans.modules.spring.beans.editor.PropertyFinder;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class CompletionManager {

    private static final String BEAN_TAG = "bean"; // NOI18N
    private static final String ALIAS_TAG = "alias"; // NOI18N
    private static final String BEANS_TAG = "beans"; // NOI18N
    private static final String LIST_TAG = "list"; // NOI18N
    private static final String SET_TAG = "set"; // NOI18N
    private static final String MAP_TAG = "map"; // NOI18N
    private static final String PROPS_TAG = "props"; // NOI18N
    private static final String IMPORT_TAG = "import"; // NOI18N
    private static final String VALUE_TAG = "value"; // NOI18N
    private static final String CONSTRUCTOR_ARG_TAG = "constructor-arg"; // NOI18N
    private static final String REF_TAG = "ref"; // NOI18N
    private static final String IDREF_TAG = "idref"; // NOI18N
    private static final String ENTRY_TAG = "entry"; // NOI18N
    private static final String PROPERTY_TAG = "property"; // NOI18N
    private static final String LOOKUP_METHOD_TAG = "lookup-method"; // NOI18N
    private static final String REPLACED_METHOD_TAG = "replaced-method";  // NOI18N
    private static final String DEPENDS_ON_ATTRIB = "depends-on"; // NOI18N
    private static final String PARENT_ATTRIB = "parent"; // NOI18N
    private static final String FACTORY_BEAN_ATTRIB = "factory-bean"; // NOI18N
    private static final String NAME_ATTRIB = "name"; // NOI18N
    private static final String DEFAULT_LAZY_INIT_ATTRIB = "default-lazy-init"; // NOI18N
    private static final String AUTOWIRE_ATTRIB = "autowire"; // NOI18N
    private static final String DEFAULT_MERGE_ATTRIB = "default-merge"; // NOI18N
    private static final String DEFAULT_DEPENDENCY_CHECK_ATTRIB = "default-dependency-check"; // NOI18N
    private static final String DEFAULT_AUTOWIRE_ATTRIB = "default-autowire"; // NOI18N
    private static final String DEPENDENCY_CHECK_ATTRIB = "dependency-check"; // NOI18N
    private static final String LAZY_INIT_ATTRIB = "lazy-init"; // NOI18N
    private static final String ABSTRACT_ATTRIB = "abstract"; // NOI18N
    private static final String AUTOWIRE_CANDIDATE_ATTRIB = "autowire-candidate"; // NOI18N
    private static final String MERGE_ATTRIB = "merge"; // NOI18N
    private static final String RESOURCE_ATTRIB = "resource"; // NOI18N
    private static final String INIT_METHOD_ATTRIB = "init-method"; // NOI18N
    private static final String DESTROY_METHOD_ATTRIB = "destroy-method"; // NOI18N
    private static final String CLASS_ATTRIB = "class"; // NOI18N
    private static final String VALUE_TYPE_ATTRIB = "value-type"; // NOI18N
    private static final String KEY_TYPE_ATTRIB = "key-type"; // NOI18N
    private static final String TYPE_ATTRIB = "type"; // NOI18N
    private static final String REF_ATTRIB = "ref"; // NOI18N
    private static final String BEAN_ATTRIB = "bean"; // NOI18N
    private static final String LOCAL_ATTRIB = "local"; // NOI18N
    private static final String KEY_REF_ATTRIB = "key-ref"; // NOI18N
    private static final String VALUE_REF_ATTRIB = "value-ref"; // NOI18N
    private static final String REPLACER_ATTRIB = "replacer";  // NOI18N
    private static final String FACTORY_METHOD_ATTRIB = "factory-method"; // NOI18N
    private static Map<String, CompletorFactory> completorFactories = new HashMap<String, CompletorFactory>();

    private CompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        String[] defaultAutoWireItems = new String[]{
            "no", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_no"), // NOI18N
            "byName", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_byName"), // NOI18N
            "byType", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_byType"), // NOI18N
            "constructor", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_constructor"), // NOI18N
            "autodetect", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_autodetect") // NOI18N
        };
        AttributeValueCompletorFactory completorFactory = new AttributeValueCompletorFactory(defaultAutoWireItems);
        registerCompletorFactory(BEANS_TAG, DEFAULT_AUTOWIRE_ATTRIB, completorFactory);
        
        String[] autoWireItems = new String[defaultAutoWireItems.length + 2];
        System.arraycopy(defaultAutoWireItems, 0, autoWireItems, 0, defaultAutoWireItems.length);
        autoWireItems[defaultAutoWireItems.length] = "default"; // NOI18N
        autoWireItems[defaultAutoWireItems.length + 1] = null; // XXX: Documentation
        completorFactory = new AttributeValueCompletorFactory(autoWireItems);
        registerCompletorFactory(BEAN_TAG, AUTOWIRE_ATTRIB, completorFactory);
        
        String[] defaultLazyInitItems = new String[]{
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultLazyInitItems);
        registerCompletorFactory(BEANS_TAG, DEFAULT_LAZY_INIT_ATTRIB, completorFactory);
        
        String[] lazyInitItems = new String[] {
            defaultLazyInitItems[0], defaultLazyInitItems[1],
            defaultLazyInitItems[2], defaultLazyInitItems[3],
            "default", null // XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(lazyInitItems);
        registerCompletorFactory(BEAN_TAG, LAZY_INIT_ATTRIB, completorFactory);
        
        String[] defaultMergeItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultMergeItems);
        registerCompletorFactory(BEANS_TAG, DEFAULT_MERGE_ATTRIB, completorFactory);
        
        String[] defaultDepCheckItems = new String[] {
            "none", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_none"), // NOI18N
            "simple", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_simple"), // NOI18N
            "objects", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_objects"), // NOI18N
            "all", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_all"), // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultDepCheckItems);
        registerCompletorFactory(BEANS_TAG, DEFAULT_DEPENDENCY_CHECK_ATTRIB, completorFactory);

        String[] depCheckItems = new String[defaultDepCheckItems.length + 2];
        depCheckItems[defaultDepCheckItems.length] = "default"; // NOI18N
        depCheckItems[defaultDepCheckItems.length + 1] = null; // XXX Documentation
        completorFactory = new AttributeValueCompletorFactory(depCheckItems);
        registerCompletorFactory(BEAN_TAG, DEPENDENCY_CHECK_ATTRIB, completorFactory);
        
        String[] abstractItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(abstractItems);
        registerCompletorFactory(BEAN_TAG, ABSTRACT_ATTRIB, completorFactory);
        
        String[] autowireCandidateItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
            "default", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(autowireCandidateItems);
        registerCompletorFactory(BEAN_TAG, AUTOWIRE_CANDIDATE_ATTRIB, completorFactory);
        
        String[] mergeItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
            "default", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(mergeItems);
        registerCompletorFactory(LIST_TAG, MERGE_ATTRIB, completorFactory);
        registerCompletorFactory(SET_TAG, MERGE_ATTRIB, completorFactory);
        registerCompletorFactory(MAP_TAG, MERGE_ATTRIB, completorFactory);
        registerCompletorFactory(PROPS_TAG, MERGE_ATTRIB, completorFactory);
        
        registerCompletorFactory(IMPORT_TAG, RESOURCE_ATTRIB, new GenericCompletorFactory(ResourceCompletor.class));

        GenericCompletorFactory javaClassCompletorFactory = new GenericCompletorFactory(JavaClassCompletor.class);
        registerCompletorFactory(BEAN_TAG, CLASS_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(LIST_TAG, VALUE_TYPE_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(MAP_TAG, VALUE_TYPE_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(MAP_TAG, KEY_TYPE_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(SET_TAG, VALUE_TYPE_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(VALUE_TAG, TYPE_ATTRIB, javaClassCompletorFactory);
        registerCompletorFactory(CONSTRUCTOR_ARG_TAG, TYPE_ATTRIB, javaClassCompletorFactory);
        
        BeansRefCompletorFactory beansRefCompletorFactory = new BeansRefCompletorFactory(true);
        registerCompletorFactory(ALIAS_TAG, NAME_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(BEAN_TAG, PARENT_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(BEAN_TAG, DEPENDS_ON_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(BEAN_TAG, FACTORY_BEAN_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(CONSTRUCTOR_ARG_TAG, REF_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(REF_TAG, BEAN_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(IDREF_TAG, BEAN_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(ENTRY_TAG, KEY_REF_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(ENTRY_TAG, VALUE_REF_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(PROPERTY_TAG, REF_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(LOOKUP_METHOD_TAG, BEAN_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(REPLACED_METHOD_TAG, REPLACER_ATTRIB, beansRefCompletorFactory);
        
        beansRefCompletorFactory = new BeansRefCompletorFactory(false);
        registerCompletorFactory(REF_TAG, LOCAL_ATTRIB, beansRefCompletorFactory);
        registerCompletorFactory(IDREF_TAG, LOCAL_ATTRIB, beansRefCompletorFactory);
        
        GenericCompletorFactory javaMethodCompletorFactory = new GenericCompletorFactory(InitDestroyMethodCompletor.class);
        registerCompletorFactory(BEAN_TAG, INIT_METHOD_ATTRIB, javaMethodCompletorFactory);
        registerCompletorFactory(BEAN_TAG, DESTROY_METHOD_ATTRIB, javaMethodCompletorFactory);
        registerCompletorFactory(LOOKUP_METHOD_TAG, NAME_ATTRIB, javaMethodCompletorFactory);
        registerCompletorFactory(REPLACED_METHOD_TAG, NAME_ATTRIB, javaMethodCompletorFactory);
        
        javaMethodCompletorFactory = new GenericCompletorFactory(FactoryMethodCompletor.class);
        registerCompletorFactory(BEAN_TAG, FACTORY_METHOD_ATTRIB, javaMethodCompletorFactory);
        
        GenericCompletorFactory propertyCompletorFactory = new GenericCompletorFactory(PropertyCompletor.class);
        registerCompletorFactory(PROPERTY_TAG, NAME_ATTRIB, propertyCompletorFactory);
        
        GenericCompletorFactory pNamespaceBeanRefCompletorFactory 
                = new GenericCompletorFactory(PNamespaceBeanRefCompletor.class);
        registerCompletorFactory(BEAN_TAG, null, pNamespaceBeanRefCompletorFactory);
    }
    private static CompletionManager INSTANCE = new CompletionManager();

    public static CompletionManager getDefault() {
        return INSTANCE;
    }

    public void completeAttributeValues(CompletionResultSet resultSet, CompletionContext context) {
        String tagName = context.getTag().getNodeName();
        TokenItem attrib = ContextUtilities.getAttributeToken(context.getCurrentToken());
        String attribName = attrib != null ? attrib.getImage() : null;

        CompletorFactory completorFactory = locateCompletorFactory(tagName, attribName);
        if (completorFactory != null) {
            Completor completor = completorFactory.createCompletor();
            resultSet.addAllItems(completor.doCompletion(context));
            if(completor.getAnchorOffset() != -1) {
                resultSet.setAnchorOffset(completor.getAnchorOffset());
            }
        }
    }

    public void completeAttributes(final CompletionResultSet resultSet, final CompletionContext context) {
        String tagName = context.getTag().getNodeName();
        if(tagName.equals(BEAN_TAG) && ContextUtilities.isPNamespaceAdded(context.getDocumentContext())) {
            try {
                final JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(context.getFileObject());
                if (js == null) {
                    return;
                }

                final String typedPrefix = context.getTypedPrefix();
                final String pNamespacePrefix = context.getDocumentContext().getNamespacePrefix(ContextUtilities.P_NAMESPACE);
                final int substitutionOffset = context.getCaretOffset() - typedPrefix.length();
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        String className = new BeanClassFinder(SpringXMLConfigEditorUtils.getTagAttributes(context.getTag()), 
                                context.getFileObject()).findImplementationClass();
                        if (className == null) {
                            return;
                        }
                        TypeElement te = SpringXMLConfigEditorUtils.findClassElementByBinaryName(className, cc);
                        if (te == null) {
                            return;
                        }
                        ElementUtilities eu = cc.getElementUtilities();
                        Property[] props = new PropertyFinder(te.asType(), "", eu).findProperties(); // NOI18N
                        for (Property prop : props) {
                            if(prop.getSetter() == null) {
                                continue;
                            } 
                            String attribName = pNamespacePrefix + ":" + prop.getName(); // NOI18N
                            if (!context.getExistingAttributes().contains(attribName) && attribName.startsWith(typedPrefix)) {
                                SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createPropertyAttribItem(substitutionOffset,
                                        attribName, prop);
                                resultSet.addItem(item);
                            }
                            attribName += "-ref"; // NOI18N
                            if (!context.getExistingAttributes().contains(attribName) && attribName.startsWith(typedPrefix)) {
                                SpringXMLConfigCompletionItem refItem = SpringXMLConfigCompletionItem.createPropertyAttribItem(substitutionOffset,
                                        attribName, prop); // NOI18N
                                resultSet.addItem(refItem);
                            }
                        }
                    }
                }, true);
                
                resultSet.setAnchorOffset(substitutionOffset);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void completeElements(CompletionResultSet resultSet, CompletionContext context) {
        // TBD
    }

    private void registerCompletorFactory(String tagName, String attribName,
            CompletorFactory completorFactory) {
        completorFactories.put(createRegisteredName(tagName, attribName), completorFactory);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(nodeName)) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (StringUtils.hasText(attributeName)) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private CompletorFactory locateCompletorFactory(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }
        
        key = createRegisteredName(nodeName, null);
        if(completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }

        return null;
    }
}
