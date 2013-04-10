/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.groovy.editor.completion.provider;

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.java.Utilities;

/**
 *
 * @author Petr Hejl
 */
// FIXME this should somehow use compilation classpath
public final class MetaElementHandler {

    private static final Logger LOG = Logger.getLogger(MetaElementHandler.class.getName());

    private final ParserResult info;

    private MetaElementHandler(ParserResult info) {
        this.info = info;
    }

    public static MetaElementHandler forCompilationInfo(ParserResult info) {
        return new MetaElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    // FIXME SPI to plug here for Grails dynamic methods
    public Map<FieldSignature, ? extends CompletionItem> getFields(String className,
            String prefix, int anchor) {

        final Class clazz = loadClass(className);
        if (clazz != null) {
            final MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(clazz);

            if (metaClass != null) {
                Map<FieldSignature, CompletionItem.FieldItem> result = new HashMap<FieldSignature, CompletionItem.FieldItem>();

                LOG.log(Level.FINEST, "Adding groovy methods --------------------------"); // NOI18N
                for (Object field : metaClass.getProperties()) {
                    LOG.log(Level.FINEST, field.toString());
                    MetaProperty prop = (MetaProperty) field;
                    if (prop.getName().startsWith(prefix)) {
                        result.put(new FieldSignature(prop.getName()), new CompletionItem.FieldItem(
                                prop.getName(), prop.getModifiers(), anchor, info, prop.getType().getSimpleName()));
                    }
                }

                return result;
            }
        }
        
        return Collections.emptyMap();
    }
    
    public Map<MethodSignature, ? extends CompletionItem> getMethods(String className,
            String prefix, int anchor, boolean nameOnly) {

        final Class clz = loadClass(className);
        if (clz != null) {
            final MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                Map<MethodSignature, CompletionItem.MetaMethodItem> result = new HashMap<MethodSignature, CompletionItem.MetaMethodItem>();

                LOG.log(Level.FINEST, "Adding groovy methods --------------------------"); // NOI18N
                for (MetaMethod method : metaClz.getMetaMethods()) {
                    populateProposal(clz, method, prefix, anchor, result, nameOnly);
                }

                return result;
            }
        
        }
        return Collections.emptyMap();
    }
    
    private Class loadClass(String className) {
        try {
            // FIXME should be loaded by classpath classloader
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.FINE, "Class.forName() failed: {0}", e.getMessage()); // NOI18N
            return null;
        } catch (NoClassDefFoundError err) {
            LOG.log(Level.FINE, "Class.forName() failed: {0}", err.getMessage()); // NOI18N
            return null;
        }
    }

    private void populateProposal(Class clz, MetaMethod method, String prefix, int anchor,
            Map<MethodSignature, CompletionItem.MetaMethodItem> methodList, boolean nameOnly) {

        if (method.getName().startsWith(prefix)) {
            LOG.log(Level.FINEST, "Found matching method: {0}", method.getName()); // NOI18N

            addOrReplaceItem(methodList, new CompletionItem.MetaMethodItem(clz, method, anchor, true, nameOnly));
        }
    }

    private void addOrReplaceItem(Map<MethodSignature, CompletionItem.MetaMethodItem> methodItemList,
            CompletionItem.MetaMethodItem itemToStore) {

        // if we have a method in-store which has the same name and same signature
        // then replace it if we have a method with a higher distance to the super-class.
        // For example: toString() is defined in java.lang.Object and java.lang.String
        // therefore take the one from String.

        MetaMethod methodToStore = itemToStore.getMethod();

        for (CompletionItem.MetaMethodItem methodItem : methodItemList.values()) {
            MetaMethod currentMethod = methodItem.getMethod();

            if (isSameMethod(currentMethod, methodToStore)) {
                if (isBetterDistance(currentMethod, methodToStore)) {
                    methodItemList.remove(getSignature(currentMethod));
                    methodItemList.put(getSignature(methodToStore), itemToStore);
                }
                return;
            }
        }

        // We don't have method with the same signature yet
        methodItemList.put(getSignature(methodToStore), itemToStore);
    }

    private static boolean isSameMethod(MetaMethod currentMethod, MetaMethod methodToStore) {
        if (!currentMethod.getName().equals(methodToStore.getName())) {
            return false;
        }
        
        int mask = java.lang.reflect.Modifier.PRIVATE |
                   java.lang.reflect.Modifier.PROTECTED |
                   java.lang.reflect.Modifier.PUBLIC |
                   java.lang.reflect.Modifier.STATIC;
        if ((currentMethod.getModifiers() & mask) != (methodToStore.getModifiers() & mask)) {
            return false;
        }
        
        if (!isSameParams(currentMethod.getParameterTypes(), methodToStore.getParameterTypes())) {
            return false;
        }
        
        return true;
    }

    private static boolean isSameParams(CachedClass[] parameters1, CachedClass[] parameters2) {
        if (parameters1.length == parameters2.length) {
            for (int i = 0, size = parameters1.length; i < size; i++) {
                if (parameters1[i] != parameters2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private MethodSignature getSignature(MetaMethod method) {
        String[] parameters = new String[method.getParameterTypes().length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Utilities.translateClassLoaderTypeName(method.getParameterTypes()[i].getName());
        }

        return new MethodSignature(method.getName(), parameters);
    }
    
    private boolean isBetterDistance(MetaMethod currentMethod, MetaMethod methodToStore) {
        // In some cases (e.g. #206610) there is the same distance between java.lang.Object and some
        // other interface java.util.Map and in such cases we always want to prefer the interface over
        // the java.lang.Object
        if ("java.lang.Object".equals(currentMethod.getDeclaringClass().getName())) {
            return true;
        }
        if ("java.lang.Object".equals(methodToStore.getDeclaringClass().getName())) {
            return false;
        }
        
        
        if (currentMethod.getDeclaringClass().getSuperClassDistance() <= methodToStore.getDeclaringClass().getSuperClassDistance()) {
            return true;
        }
        return false;
    }
}
