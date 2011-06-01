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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Convenience methods for dealing with {@link ELVariableResolver}s.
 */
public final class ELVariableResolvers {

    private ELVariableResolvers() {
    }

    /**
     * Gets the FQN class of the bean identified by {@code beanName}.
     * @param beanName
     * @param context
     * @return the FQN of the bean or {@code null}.
     */
    public static String findBeanClass(String beanName, FileObject context) {
        for (ELVariableResolver resolver : getResolvers()) {
            String beanClass = resolver.getBeanClass(beanName, context);
            if (beanClass != null) {
                return beanClass;
            }
        }
        return null;
    }

    /**
     * Gets the name of the bean of the given {@code clazz}.
     * @param clazz the FQN class of the bean.
     * @param context
     * @return the bean name or {@code null}.
     */
    public static String findBeanName(String clazz, FileObject context) {
        for (ELVariableResolver resolver : getResolvers()) {
            String beanName = resolver.getBeanName(clazz, context);
            if (beanName != null) {
                return beanName;
            }
        }
        return null;
    }

//    /**
//     * Gets the AST node of the expression referred by the EL identifier
//     * at the given {@code offset}.
//     * @param snapshot
//     * @param offset
//     * @return the node or {@code null}.
//     */
//    public static Node getReferredExpression(Snapshot snapshot, int offset) {
//        for (ELVariableResolver resolver : getResolvers()) {
//            String expression = resolver.getReferredExpression(snapshot, offset);
//            if (expression != null) {
//                try {
//                    return ELParser.parse(expression);
//                } catch (ELException e) {
//                    // not valid EL
//            }
//        }
//        }
//        return null;
//    }

    public static List<ELVariableResolver.VariableInfo> getManagedBeans(FileObject context) {
        List<ELVariableResolver.VariableInfo> result = new ArrayList<ELVariableResolver.VariableInfo>();
        for (ELVariableResolver resolver : getResolvers()) {
            result.addAll(resolver.getManagedBeans(context));
        }
        return result;
    }

    public static List<ELVariableResolver.VariableInfo> getVariables(Snapshot snapshot, int offset) {
        List<ELVariableResolver.VariableInfo> result = new ArrayList<ELVariableResolver.VariableInfo>();
        for (ELVariableResolver resolver : getResolvers()) {
            result.addAll(resolver.getVariables(snapshot, offset));
        }
        return result;
    }
    
//    public static List<ELVariableResolver.VariableInfo> getVariables(Snapshot snapshot, int offset, ELTypeUtilities typeUtilities) {
//        List<ELVariableResolver.VariableInfo> result = new ArrayList<ELVariableResolver.VariableInfo>();
//        for (ELVariableResolver resolver : getResolvers()) {
//            Collection<ELVariableResolver.VariableInfo> vinfos = resolver.getVariables(snapshot, offset);
//            //resove the unresolved variables (they do not have the clazz field properly set, jut the
//            //expression
//            for(ELVariableResolver.VariableInfo vi : vinfos) {
//                if(vi.clazz == null) {
//                    //unresolved
//                    ELElement element;
//                    try {
//                        Node node = ELParser.parse(vi.expression);
//                        element = ELElement.valid(node, vi.expression, OffsetRange.NONE, snapshot);
//                    } catch (ELException ex) {
//                        ELElement errorElement = ELElement.error(ex, vi.expression, OffsetRange.NONE, snapshot);
//                        ELSanitizer sanitizer = new ELSanitizer(errorElement);
//                        element = sanitizer.sanitized();
//                    }
//                    typeUtilities.resolveElement(element, node);
//                }
//            }
//
//            result.addAll(vinfos);
//        }
//        return result;
//    }

    public static List<ELVariableResolver.VariableInfo> getBeansInScope(String scope, Snapshot context) {
        List<ELVariableResolver.VariableInfo> result = new ArrayList<ELVariableResolver.VariableInfo>();
        for (ELVariableResolver resolver : getResolvers()) {
            result.addAll(resolver.getBeansInScope(scope, context));
        }
        return result;
    }

    public static List<ELVariableResolver.VariableInfo> getRawObjectProperties(String name, Snapshot context) {
        List<ELVariableResolver.VariableInfo> result = new ArrayList<ELVariableResolver.VariableInfo>();
        for (ELVariableResolver resolver : getResolvers()) {
            result.addAll(resolver.getRawObjectProperties(name, context));
        }
        return result;
    }

    private static Collection<? extends ELVariableResolver> getResolvers() {
        return Lookup.getDefault().lookupAll(ELVariableResolver.class);
    }

}
