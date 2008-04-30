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

import org.netbeans.modules.spring.beans.completion.completors.InitDestroyMethodCompletor;
import org.netbeans.modules.spring.beans.completion.completors.PNamespaceBeanRefCompletor;
import org.netbeans.modules.spring.beans.completion.completors.ResourceCompletor;
import org.netbeans.modules.spring.beans.completion.completors.PropertyCompletor;
import org.netbeans.modules.spring.beans.completion.completors.FactoryMethodCompletor;
import org.netbeans.modules.spring.beans.completion.completors.JavaClassCompletor;
import org.netbeans.modules.spring.beans.completion.completors.GenericCompletorFactory;
import org.netbeans.modules.spring.beans.completion.completors.BeansRefCompletorFactory;
import org.netbeans.modules.spring.beans.completion.completors.AttributeValueCompletorFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class CompletorRegistry {

    private static Map<String, CompletorFactory> completorFactories = new HashMap<String, CompletorFactory>();

    private CompletorRegistry() {
        setupCompletors();
    }

    private void setupCompletors() {

        String[] defaultAutoWireItems = new String[]{
            "no", NbBundle.getMessage(CompletorRegistry.class, "DESC_autowire_no"), // NOI18N
            "byName", NbBundle.getMessage(CompletorRegistry.class, "DESC_autowire_byName"), // NOI18N
            "byType", NbBundle.getMessage(CompletorRegistry.class, "DESC_autowire_byType"), // NOI18N
            "constructor", NbBundle.getMessage(CompletorRegistry.class, "DESC_autowire_constructor"), // NOI18N
            "autodetect", NbBundle.getMessage(CompletorRegistry.class, "DESC_autowire_autodetect") // NOI18N
         // NOI18N
        };
        AttributeValueCompletorFactory completorFactory = new AttributeValueCompletorFactory(defaultAutoWireItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_AUTOWIRE, completorFactory);
        
        String[] autoWireItems = new String[defaultAutoWireItems.length + 2];
        System.arraycopy(defaultAutoWireItems, 0, autoWireItems, 0, defaultAutoWireItems.length);
        autoWireItems[defaultAutoWireItems.length] = "default"; // NOI18N
        autoWireItems[defaultAutoWireItems.length + 1] = null; // XXX: Documentation
        completorFactory = new AttributeValueCompletorFactory(autoWireItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.AUTOWIRE, completorFactory);
        
        String[] primaryItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(primaryItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.PRIMARY, completorFactory);
        
        String[] defaultLazyInitItems = new String[]{
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultLazyInitItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_LAZY_INIT, completorFactory);
        
        String[] lazyInitItems = new String[] {
            defaultLazyInitItems[0], defaultLazyInitItems[1],
            defaultLazyInitItems[2], defaultLazyInitItems[3],
            "default", null // XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(lazyInitItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.LAZY_INIT, completorFactory);
        
        String[] defaultMergeItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultMergeItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_MERGE, completorFactory);
        
        String[] defaultDepCheckItems = new String[] {
            "none", NbBundle.getMessage(CompletorRegistry.class, "DESC_def_dep_check_none"), // NOI18N
            "simple", NbBundle.getMessage(CompletorRegistry.class, "DESC_def_dep_check_simple"), // NOI18N
            "objects", NbBundle.getMessage(CompletorRegistry.class, "DESC_def_dep_check_objects"), // NOI18N
            "all", NbBundle.getMessage(CompletorRegistry.class, "DESC_def_dep_check_all"), // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultDepCheckItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_DEPENDENCY_CHECK, completorFactory);

        String[] depCheckItems = new String[defaultDepCheckItems.length + 2];
        System.arraycopy(defaultDepCheckItems, 0, depCheckItems, 0, defaultDepCheckItems.length);
        depCheckItems[defaultDepCheckItems.length] = "default"; // NOI18N
        depCheckItems[defaultDepCheckItems.length + 1] = null; // XXX Documentation
        completorFactory = new AttributeValueCompletorFactory(depCheckItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.DEPENDENCY_CHECK, completorFactory);
        
        String[] abstractItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(abstractItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.ABSTRACT, completorFactory);
        
        String[] autowireCandidateItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
            "default", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(autowireCandidateItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.AUTOWIRE_CANDIDATE, completorFactory);
        
        String[] mergeItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
            "default", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(mergeItems);
        registerCompletorFactory(BeansElements.LIST, BeansAttributes.MERGE, completorFactory);
        registerCompletorFactory(BeansElements.SET, BeansAttributes.MERGE, completorFactory);
        registerCompletorFactory(BeansElements.MAP, BeansAttributes.MERGE, completorFactory);
        registerCompletorFactory(BeansElements.PROPS, BeansAttributes.MERGE, completorFactory);
        
        registerCompletorFactory(BeansElements.IMPORT, BeansAttributes.RESOURCE, new GenericCompletorFactory(ResourceCompletor.class));

        GenericCompletorFactory javaClassCompletorFactory = new GenericCompletorFactory(JavaClassCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.CLASS, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.LIST, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.MAP, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.MAP, BeansAttributes.KEY_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.SET, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.VALUE, BeansAttributes.TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.TYPE, javaClassCompletorFactory);
        
        BeansRefCompletorFactory beansRefCompletorFactory = new BeansRefCompletorFactory(true);
        registerCompletorFactory(BeansElements.ALIAS, BeansAttributes.NAME, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.PARENT, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.DEPENDS_ON, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.FACTORY_BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.REF, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.IDREF, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.ENTRY, BeansAttributes.KEY_REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.ENTRY, BeansAttributes.VALUE_REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.PROPERTY, BeansAttributes.REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.LOOKUP_METHOD, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.REPLACED_METHOD, BeansAttributes.REPLACER, beansRefCompletorFactory);
        
        beansRefCompletorFactory = new BeansRefCompletorFactory(false);
        registerCompletorFactory(BeansElements.REF, BeansAttributes.LOCAL, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.IDREF, BeansAttributes.LOCAL, beansRefCompletorFactory);
        
        GenericCompletorFactory javaMethodCompletorFactory = new GenericCompletorFactory(InitDestroyMethodCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.INIT_METHOD, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.DESTROY_METHOD, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.LOOKUP_METHOD, BeansAttributes.NAME, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.REPLACED_METHOD, BeansAttributes.NAME, javaMethodCompletorFactory);
        
        javaMethodCompletorFactory = new GenericCompletorFactory(FactoryMethodCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.FACTORY_METHOD, javaMethodCompletorFactory);
        
        GenericCompletorFactory propertyCompletorFactory = new GenericCompletorFactory(PropertyCompletor.class);
        registerCompletorFactory(BeansElements.PROPERTY, BeansAttributes.NAME, propertyCompletorFactory);
        
        GenericCompletorFactory pNamespaceBeanRefCompletorFactory 
                = new GenericCompletorFactory(PNamespaceBeanRefCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, null, pNamespaceBeanRefCompletorFactory);
    }
    private static CompletorRegistry INSTANCE = new CompletorRegistry();

    public static CompletorRegistry getDefault() {
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
        if(tagName.equals(BeansElements.BEAN) && ContextUtilities.isPNamespaceAdded(context.getDocumentContext())) {
            try {
                final JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
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
                        TypeElement te = JavaUtils.findClassElementByBinaryName(className, cc);
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
