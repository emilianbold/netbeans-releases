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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils.Public;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils.Static;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.Property;
import org.netbeans.modules.spring.beans.editor.PropertyFinder;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Node;

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
                final JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(context.getDocument());
                if (js == null) {
                    return;
                }

                final String typedPrefix = context.getTypedPrefix();
                final String pNamespacePrefix = context.getDocumentContext().getNamespacePrefix(ContextUtilities.P_NAMESPACE);
                final int substitutionOffset = context.getCaretOffset() - typedPrefix.length();
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        String className = new BeanClassFinder(context.getTag(), context.getDocument()).findImplementationClass();
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

    private static interface CompletorFactory {
        Completor createCompletor();
    }
    
    private static abstract class Completor {

        private int anchorOffset = -1;
        
        public abstract List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context);

        protected void setAnchorOffset(int anchorOffset) {
            this.anchorOffset = anchorOffset;
        } 
        
        public int getAnchorOffset() {
            return anchorOffset;
        }
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

    private static class BeansRefCompletor extends Completor {

        final private boolean includeGlobal;

        public BeansRefCompletor(boolean includeGlobal) {
            this.includeGlobal = includeGlobal;
        }

        @Override
        public List<SpringXMLConfigCompletionItem> doCompletion(final CompletionContext context) {
            Document doc = context.getDocument();
            final FileObject fo = NbEditorUtilities.getFileObject(doc);
            if (fo == null) {
                return Collections.emptyList();
            }
            SpringConfigModel model = SpringConfigModel.forFileObject(fo);
            if (model == null) {
                return Collections.emptyList();
            }
            final List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            final String prefix = context.getTypedPrefix();
       
            final List<String> cNames = new ArrayList<String>();
            // get current bean parameters
            if(SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), "id")) { // NOI18N
                String cId = SpringXMLConfigEditorUtils.getAttribute(context.getTag(), "id"); // NOI18N
                cNames.add(cId);
            }
            if(SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), "name")) { // NOI18N
                List<String> names = StringUtils.tokenize(
                        SpringXMLConfigEditorUtils.getAttribute(context.getTag(), "name"), 
                        SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS); // NOI18N
                cNames.addAll(names);
            }
            
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans sb) {
                        List<SpringBean> beans = includeGlobal ? sb.getBeans() : sb.getBeans(FileUtil.toFile(fo));
                        Map<String, SpringBean> name2Bean = getName2Beans(beans, includeGlobal); // if local beans, then add only bean ids;
                        for(String beanName : name2Bean.keySet()) {
                            if(!beanName.startsWith(prefix) || cNames.contains(beanName)) {
                                continue;
                            }
                            SpringBean bean = name2Bean.get(beanName);
                            SpringXMLConfigCompletionItem item = 
                                    SpringXMLConfigCompletionItem.createBeanRefItem(context.getCurrentToken().getOffset() + 1, 
                                    beanName, bean, fo);
                            results.add(item);
                        }
                    }

                    private Map<String, SpringBean> getName2Beans(List<SpringBean> beans, boolean addNames) {
                        Map<String, SpringBean> name2Bean = new HashMap<String, SpringBean>();
                        for (SpringBean bean : beans) {
                            String beanId = bean.getId();
                            if (beanId != null) {
                                name2Bean.put(beanId, bean);
                            }
                            if (addNames) {
                                List<String> beanNames = bean.getNames();
                                for (String beanName : beanNames) {
                                    name2Bean.put(beanName, bean);
                                }
                            }
                        }

                        return name2Bean;
                    }
                });
                
                setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            return results;
        }
    }
    
    private static class AttributeValueCompletorFactory implements CompletorFactory {

        private String[] itemTextAndDocs;

        public AttributeValueCompletorFactory(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }
        
        public Completor createCompletor() {
            return new AttributeValueCompletor(itemTextAndDocs);
        }
    }

    private static class GenericCompletorFactory implements CompletorFactory {

        private Class<? extends Completor> clazz;

        public GenericCompletorFactory(Class<? extends Completor> clazz) {
            this.clazz = clazz;
        }
        
        public Completor createCompletor() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            return null;
        }
    }
    
    private static class BeansRefCompletorFactory implements CompletorFactory {

        private boolean includeGlobal;

        public BeansRefCompletorFactory(boolean includeGlobal) {
            this.includeGlobal = includeGlobal;
        }
        
        public Completor createCompletor() {
            return new BeansRefCompletor(includeGlobal);
        }
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

        public List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context) {
            List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars)) {
                    SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }
    
    private static class JavaClassCompletor extends Completor {

        public JavaClassCompletor() {
        }

        public List<SpringXMLConfigCompletionItem> doCompletion(final CompletionContext context) {
            final List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(doc);
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

        private void doNormalJavaCompletion(JavaSource js, final List<SpringXMLConfigCompletionItem> results, 
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                    int index = substitutionOffset;
                    String packName = typedPrefix;
                    int dotIndex = typedPrefix.lastIndexOf('.'); // NOI18N
                    if (dotIndex != -1) {
                        index += (dotIndex + 1);  // NOI18N
                        packName = typedPrefix.substring(0, dotIndex);
                    }
                    addPackages(ci, results, typedPrefix, index);

                    PackageElement pkgElem = cc.getElements().getPackageElement(packName);
                    if (pkgElem == null) {
                        return;
                    }
                    
                    // get this as well as non-static inner classes
                    List<TypeElement> tes = new TypeScanner().scan(pkgElem);
                    for (TypeElement te : tes) {
                        if (ElementUtilities.getBinaryName(te).startsWith(typedPrefix)) {
                            SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createTypeItem(substitutionOffset,
                                    te, ElementHandle.create(te), cc.getElements().isDeprecated(te), false);
                            results.add(item);
                        }
                    }

                    setAnchorOffset(index);
                }
            }, true);
        }
        
        private void doSmartJavaCompletion(final JavaSource js, final List<SpringXMLConfigCompletionItem> results, 
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getJavaSource().getClasspathInfo().getClassIndex();
                    // add packages
                    addPackages(ci, results, typedPrefix, substitutionOffset);
                    
                    // add classes 
                    Set<ElementHandle<TypeElement>> matchingTypes = ci.getDeclaredTypes(typedPrefix, 
                            NameKind.CASE_INSENSITIVE_PREFIX, EnumSet.allOf(SearchScope.class));
                    for (ElementHandle<TypeElement> eh : matchingTypes) {
                        if (eh.getKind() == ElementKind.CLASS) {
                            LazyTypeCompletionItem item = LazyTypeCompletionItem.create(substitutionOffset, eh, js);
                            results.add(item);
                        }
                    }
                }
            }, true);
            
            setAnchorOffset(substitutionOffset);
        }
        
        private static boolean isAccessibleClass(TypeElement te) {
            NestingKind nestingKind = te.getNestingKind();
            return (nestingKind == NestingKind.TOP_LEVEL) 
                    || (nestingKind == NestingKind.MEMBER && te.getModifiers().contains(Modifier.STATIC));
        }
        
        private void addPackages(ClassIndex ci, List<SpringXMLConfigCompletionItem> results, String typedPrefix, int substitutionOffset) {
            Set<String> packages = ci.getPackageNames(typedPrefix, true, EnumSet.allOf(SearchScope.class));
            for (String pkg : packages) {
                if (pkg.length() > 0) {
                    SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                    results.add(item);
                }
            }
        }
        
        private static final class TypeScanner extends ElementScanner6<List<TypeElement>, Void> {

            public TypeScanner() {
                super(new ArrayList<TypeElement>());
            }
            
            @Override
            public List<TypeElement> visitType(TypeElement typeElement, Void arg) {
                if(typeElement.getKind() == ElementKind.CLASS && isAccessibleClass(typeElement)) {
                    DEFAULT_VALUE.add(typeElement);
                }
                return super.visitType(typeElement, arg);
            }
            
        }
    }
    
    private static abstract class JavaMethodCompletor extends Completor {

        @Override
        public List<SpringXMLConfigCompletionItem> doCompletion(final CompletionContext context) {
            final List<SpringXMLConfigCompletionItem> results = new  ArrayList<SpringXMLConfigCompletionItem>();
            try {
                final String classBinaryName = getTypeName(context);
                final Public publicFlag = getPublicFlag(context);
                final Static staticFlag = getStaticFlag(context);
                final int argCount = getArgCount(context);
                
                if (classBinaryName == null || classBinaryName.equals("")) { // NOI18N
                    return Collections.emptyList();
                }
                Document doc = context.getDocument();

                final JavaSource javaSource = SpringXMLConfigEditorUtils.getJavaSource(doc);
                if (javaSource == null) {
                    return Collections.emptyList();
                }

                javaSource.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement classElem = SpringXMLConfigEditorUtils.findClassElementByBinaryName(classBinaryName, controller);
                        if (classElem == null) {
                            return;
                        }
                        
                        ElementUtilities eu = controller.getElementUtilities();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {

                            public boolean accept(Element e, TypeMirror type) { 
                                // XXX : display methods of java.lang.Object? 
                                // Displaying them adds unnecessary clutter in the completion window
                                if (e.getKind() == ElementKind.METHOD) {
                                    TypeElement te = (TypeElement) e.getEnclosingElement();
                                    if(te.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                                        return false;
                                    }

                                    // match name
                                    if(!e.getSimpleName().toString().startsWith(context.getTypedPrefix())) {
                                        return false;
                                    }
                                    
                                    ExecutableElement method = (ExecutableElement) e;
                                    // match argument count
                                    if(argCount != -1 && method.getParameters().size() != argCount) {
                                        return false;
                                    }
                                
                                    // match static
                                    if (staticFlag != Static.DONT_CARE) {
                                        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                                        if ((isStatic && staticFlag == Static.NO) || (!isStatic && staticFlag == Static.YES)) {
                                            return false;
                                        }
                                    }
                                    
                                    // match public
                                    if (publicFlag != Public.DONT_CARE) {
                                        boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);
                                        if ((isPublic && publicFlag == Public.NO) || (!isPublic && publicFlag == Public.YES)) {
                                            return false;
                                        }
                                    }
                                    
                                    return true;
                                }
                                
                                return false;
                            }
                        };

                        int substitutionOffset = context.getCurrentToken().getOffset() + 1;
                        Iterable<? extends Element> methods = eu.getMembers(classElem.asType(), acceptor);
                        
                        methods = filter(methods);
                        
                        for (Element e : methods) {
                            SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createMethodItem(
                                    substitutionOffset, (ExecutableElement) e, e.getEnclosingElement() != classElem,
                                    controller.getElements().isDeprecated(e));
                            results.add(item);
                        }
                        
                        setAnchorOffset(substitutionOffset);

                    }
                }, false);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;

        }
        
        /**
         * Should the method be public
         */
        protected abstract Public getPublicFlag(CompletionContext context);
        
        /**
         * Should the method be static
         */
        protected abstract Static getStaticFlag(CompletionContext context);

        /**
         * Number of arguments of the method
         */
        protected abstract int getArgCount(CompletionContext context);

        /**
         * Binary name of the class which should be searched for methods
         */
        protected abstract String getTypeName(CompletionContext context);

        /**
         * Post process applicable methods, for eg. return only those
         * methods which return do not return void
         */
        protected Iterable<? extends Element> filter(Iterable<? extends Element> methods) {
            return methods;
        }
    }
    
    private static class InitDestroyMethodCompletor extends JavaMethodCompletor {

        public InitDestroyMethodCompletor() {
        }

        @Override
        protected Public getPublicFlag(CompletionContext context) {
            return Public.DONT_CARE;
        }

        @Override
        protected Static getStaticFlag(CompletionContext context) {
            return Static.NO;
        }

        @Override
        protected int getArgCount(CompletionContext context) {
            return 0;
        }

        @Override
        protected String getTypeName(CompletionContext context) {
            Node tag = context.getTag();
            return new BeanClassFinder(tag, context.getDocument()).findImplementationClass();
        }
    }
    
    private static class FactoryMethodCompletor extends JavaMethodCompletor {

        public FactoryMethodCompletor() {
        }

        private Static staticFlag = Static.YES;
        
        @Override
        protected Public getPublicFlag(CompletionContext context) {
            return Public.DONT_CARE;
        }

        @Override
        protected Static getStaticFlag(CompletionContext context) {
            return staticFlag;
        }

        @Override
        protected int getArgCount(CompletionContext context) {
            return -1;
        }

        @Override
        protected String getTypeName(CompletionContext context) {
            Node tag = context.getTag();
            SpringBean mergedBean = SpringXMLConfigEditorUtils.getMergedBean(tag, context.getDocument());
            if(mergedBean == null) {
                return null;
            }
            
            final String[] className = {mergedBean.getClassName()};

            // if factory-bean has been defined, resolve it and get it's class name
            if (mergedBean.getFactoryBean() != null) {
                final String factoryBeanName = mergedBean.getFactoryBean();
                FileObject fo = NbEditorUtilities.getFileObject(context.getDocument());
                if (fo == null) {
                    return null;
                }
                SpringConfigModel model = SpringConfigModel.forFileObject(fo);
                try {
                    model.runReadAction(new Action<SpringBeans>() {

                        public void run(SpringBeans beans) {
                            SpringBean bean = beans.findBean(factoryBeanName);
                            if (bean == null) {
                                className[0] = null;
                                return;
                            }
                            className[0] = bean.getClassName();
                        }
                    });
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                    className[0] = null;
                }

                staticFlag = Static.NO;
            }
            
            return className[0];
        }

        @Override
        protected Iterable<? extends Element> filter(Iterable<? extends Element> methods) {
            List<ExecutableElement> ret = new ArrayList<ExecutableElement>();
            for(Element e : methods) {
                ExecutableElement method = (ExecutableElement) e;
                if(method.getReturnType().getKind() != TypeKind.VOID) {
                    ret.add(method);
                }
            }
            
            return ret;
        }
    }
    
    private static class PropertyCompletor extends Completor {

        public PropertyCompletor() {
        }

        @Override
        public List<SpringXMLConfigCompletionItem> doCompletion(final CompletionContext context) {
            final List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            final String propertyPrefix = context.getTypedPrefix();
            final JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(context.getDocument());
            if (js == null) {
                return Collections.emptyList();
            }
            
            try {
                // traverse the properties
                final int dotIndex = propertyPrefix.lastIndexOf("."); // NOI18N
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        String className = new BeanClassFinder(
                                SpringXMLConfigEditorUtils.getBean(context.getTag()), 
                                context.getDocument()).findImplementationClass();
                        if (className == null) {
                            return;
                        }
                        TypeElement te = SpringXMLConfigEditorUtils.findClassElementByBinaryName(className, cc);
                        if (te == null) {
                            return;
                        }
                        TypeMirror startType = te.asType();
                        ElementUtilities eu = cc.getElementUtilities();
                        
                        // property chain
                        if(dotIndex != -1) {
                            String getterChain = propertyPrefix.substring(0, dotIndex);
                            StringTokenizer tokenizer = new StringTokenizer(getterChain, "."); // NOI18N
                            while (tokenizer.hasMoreTokens() && startType != null) {
                                String propertyName = tokenizer.nextToken();
                                Property[] props = new PropertyFinder(startType, propertyName, eu).findProperties();
                                
                                // no matching element found
                                if (props.length == 0 || props[0].getGetter() == null) {
                                    startType = null;
                                    break;
                                }

                                TypeMirror retType = props[0].getGetter().getReturnType();
                                if (retType.getKind() == TypeKind.DECLARED) {
                                    startType = retType;
                                } else {
                                    startType = null;
                                }
                            }
                        }
                        
                        if(startType == null) {
                            return;
                        }
                        
                        String setterPrefix = "";
                        if(dotIndex != propertyPrefix.length() - 1) {
                            setterPrefix = propertyPrefix.substring(dotIndex + 1);
                        }
                        
                        Property[] props = new PropertyFinder(startType, setterPrefix, eu).findProperties();
                        int substitutionOffset = context.getCurrentToken().getOffset() + 1;
                        if(dotIndex != -1) {
                            substitutionOffset += dotIndex + 1;
                        }
                        
                        for (Property prop : props) {
                            if(prop.getSetter() == null) {
                                continue;
                            }
                            results.add(SpringXMLConfigCompletionItem.createPropertyItem(substitutionOffset, prop));
                        }
                        
                        setAnchorOffset(substitutionOffset);
                    }
                }, false);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            return results;
        }
    }
    
    private static class PNamespaceBeanRefCompletor extends Completor {

        public PNamespaceBeanRefCompletor() {
        }
        
        @Override
        public List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context) {
            TokenItem attribToken = ContextUtilities.getAttributeToken(context.getCurrentToken());
            if (attribToken == null) {
                return Collections.emptyList();
            }

            String attribName = attribToken.getImage();
            if (!ContextUtilities.isPNamespaceName(context.getDocumentContext(), attribName)) {
                return Collections.emptyList();
            }

            if (!attribName.endsWith("-ref")) { // NOI18N
                return Collections.emptyList();

            }

            // XXX: Ideally find out the property name and it's expected type
            // to list bean proposals intelligently
            BeansRefCompletor beansRefCompletor = new BeansRefCompletor(true);
            return beansRefCompletor.doCompletion(context);
        }
        
    }

    private static class ResourceCompletor extends Completor {

        public ResourceCompletor() {
        }

        public List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context) {
            List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            Document doc = context.getDocument();
            FileObject fileObject = NbEditorUtilities.getFileObject(doc).getParent();
            String typedChars = context.getTypedPrefix();

            int lastSlashIndex = typedChars.lastIndexOf("/"); // NOI18N
            String prefix = typedChars;

            if (lastSlashIndex != -1) {
                String pathStr = typedChars.substring(0, typedChars.lastIndexOf("/")); // NOI18N
                fileObject = fileObject.getFileObject(pathStr);
                if (lastSlashIndex != typedChars.length() - 1) {
                    prefix = typedChars.substring(Math.min(typedChars.lastIndexOf("/") + 1, // NOI18N
                            typedChars.length() - 1));
                } else {
                    prefix = "";
                }
            }

            if (fileObject == null) {
                return Collections.emptyList();
            }

            if (prefix == null) {
                prefix = "";
            }

            
            Enumeration<? extends FileObject> folders = fileObject.getFolders(false);
            while (folders.hasMoreElements()) {
                FileObject fo = folders.nextElement();
                if (fo.getName().startsWith(prefix)) {
                    results.add(SpringXMLConfigCompletionItem.createFolderItem(context.getCaretOffset() - prefix.length(),
                            fo));
                }
            }


            Enumeration<? extends FileObject> files = fileObject.getData(false);
            while (files.hasMoreElements()) {
                FileObject fo = files.nextElement();
                if (fo.getName().startsWith(prefix) && SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                    results.add(SpringXMLConfigCompletionItem.createSpringXMLFileItem(context.getCaretOffset() - prefix.length(), fo));
                }
            }

            setAnchorOffset(context.getCaretOffset() - prefix.length());
            
            return results;
        }
    }
}
