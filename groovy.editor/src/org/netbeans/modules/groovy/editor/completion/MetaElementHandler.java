/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.editor.completion;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;

/**
 *
 * @author Petr Hejl
 */
// FIXME this should somehow use compilation classpath
public final class MetaElementHandler {

    private static final Logger LOG = Logger.getLogger(GroovyElementHandler.class.getName());

    private final CompilationInfo info;

    private MetaElementHandler(CompilationInfo info) {
        this.info = info;
    }

    public static MetaElementHandler forCompilationInfo(CompilationInfo info) {
        return new MetaElementHandler(info);
    }

    public Map<MethodSignature, ? extends CompletionItem> getMethods(String className,
            String prefix, int anchor) {

        Class clz;

        try {
            clz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.FINEST, "Class.forName() failed: {0}", e.getMessage()); // NOI18N
            return Collections.emptyMap();
        }

        MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

        if (metaClz != null) {

            Map<MethodSignature, CompletionItem.MetaMethodItem> result = new HashMap<MethodSignature, CompletionItem.MetaMethodItem>();

            LOG.log(Level.FINEST, "Adding groovy methods --------------------------"); // NOI18N
            for (Object method : metaClz.getMetaMethods()) {
                LOG.log(Level.FINEST, method.toString());
                populateProposal(clz, method, prefix, anchor, result);
            }

            return result;
        }
        return Collections.emptyMap();
    }

    private void populateProposal(Class clz, Object method, String prefix, int anchor,
            Map<MethodSignature, CompletionItem.MetaMethodItem> methodList) {

        if (method != null && (method instanceof MetaMethod)) {
            MetaMethod mm = (MetaMethod) method;

            if (mm.getName().startsWith(prefix)) {
                LOG.log(Level.FINEST, "Found matching method: {0}", mm.getName()); // NOI18N

                CompletionItem.MetaMethodItem item =
                        new CompletionItem.MetaMethodItem(clz, mm, anchor, true);
                addOrReplaceItem(methodList, item);
            }

        }
    }

    // FIXME cleanup
    private void addOrReplaceItem(Map<MethodSignature, CompletionItem.MetaMethodItem> methodItemList, CompletionItem.MetaMethodItem itemToStore) {

        // if we have a method in-store which has the same name and same signature
        // then replace it if we have a method with a higher distance to the super-class.
        // For example: toString() is defined in java.lang.Object and java.lang.String
        // therefore take the one from String.

        MetaMethod methodToStore = itemToStore.getMethod();
        int toStoreDistance = methodToStore.getDeclaringClass().getSuperClassDistance();

        for (CompletionItem.MetaMethodItem methodItem : methodItemList.values()) {
            MetaMethod listMethod = methodItem.getMethod();

            // FIXME return types subtype
            if (listMethod.getName().equals(methodToStore.getName())
                    /*&& listMethod.isSame(methodToStore)*/ && isSame(listMethod, methodToStore)) {

                if (listMethod.getReturnType().isAssignableFrom(methodToStore.getReturnType())
                        && listMethod.getDeclaringClass().getSuperClassDistance() <= toStoreDistance) {
                    LOG.log(Level.FINEST, "Remove existing method: {0}", methodToStore.getName()); // NOI18N
                    methodItemList.remove(getSignature(listMethod));
                    break; // it's unlikely that we have more then one Method with a smaller distance
                } else {
                    LOG.log(Level.FINEST, "Not removing existing method: {0}", listMethod.getName()); // NOI18N
                    return;
                }
            }
        }

        methodItemList.put(getSignature(methodToStore), itemToStore);
    }

    private static boolean isSame(MetaMethod listMethod, MetaMethod methodToStore) {
        if (!listMethod.getName().equals(methodToStore.getName())) {
            return false;
        }
        int mask = java.lang.reflect.Modifier.PRIVATE | java.lang.reflect.Modifier.PROTECTED
                | java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC;
        if ((listMethod.getModifiers() & mask) != (methodToStore.getModifiers() & mask)) {
            return false;
        }
        if (!isSame(listMethod.getParameterTypes(), methodToStore.getParameterTypes())) {
            return false;
        }

        return true;
    }

    private static boolean isSame(CachedClass[] parameters1, CachedClass[] parameters2) {
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
}
