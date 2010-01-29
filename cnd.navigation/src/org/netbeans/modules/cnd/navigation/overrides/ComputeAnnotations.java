/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.navigation.overrides;

import java.util.Collection;
import java.util.logging.Level;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * A class that computes annotations -
 * extracted from OverridesTaskFactory for the sake of testability
 * @author Vladimir Kvashin
 */
public class ComputeAnnotations {

    private ComputeAnnotations() {
    }

    public static void computeAnnotations(
            Collection<? extends CsmOffsetableDeclaration> toProcess,
            Collection<OverriddeAnnotation> toAdd, CsmFile file, StyledDocument doc, DataObject dobj) {

        for (CsmOffsetableDeclaration decl : toProcess) {
            if (CsmKindUtilities.isFunction(decl)) {
                OverriddeAnnotation anno = computeAnnotation((CsmFunction) decl, doc);
                if (anno != null) {
                    toAdd.add(anno);
                }
            } else if (CsmKindUtilities.isClass(decl)) {
                computeAnnotations(((CsmClass) decl).getMembers(), toAdd, file, doc, dobj);

            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                computeAnnotations(((CsmNamespaceDefinition) decl).getDeclarations(), toAdd, file, doc, dobj);
            }
        }
    }

    private static OverriddeAnnotation computeAnnotation(CsmFunction func, StyledDocument doc) {
        if (CsmKindUtilities.isMethod(func)) {
            CsmMethod meth = (CsmMethod) func;
            final Collection<? extends CsmMethod> baseMethods = CsmVirtualInfoQuery.getDefault().getBaseDeclaration(meth);
            final Collection<? extends CsmMethod> overriddenMethods = CsmVirtualInfoQuery.getDefault().getOverridenMethods(meth, false);
            if (OverriddeAnnotation.LOGGER.isLoggable(Level.FINEST)) {
                OverriddeAnnotation.LOGGER.log(Level.FINEST, "Found {0} base decls for {1}", new Object[]{baseMethods.size(), func});
                for (CsmMethod baseMethod : baseMethods) {
                    OverriddeAnnotation.LOGGER.log(Level.FINEST, "    {0}", baseMethod);
                }
            }
            if (!baseMethods.isEmpty()) {
                boolean itself = baseMethods.size() == 1 && baseMethods.iterator().next().equals(func);
                if (!itself) {
                    CsmMethod m = baseMethods.iterator().next(); //TODO: XXX
                    String desc = NbBundle.getMessage(OverridesTaskFactory.class, "LAB_Overrides", m.getQualifiedName().toString());
                    return new OverriddeAnnotation(doc, func,  OverriddeAnnotation.AnnotationType.OVERRIDES, desc, baseMethods);
                }
            }
            if (!overriddenMethods.isEmpty()) {
                String desc = NbBundle.getMessage(OverridesTaskFactory.class, "LAB_IsOverriden");
                return new OverriddeAnnotation(doc, func,  OverriddeAnnotation.AnnotationType.IS_OVERRIDDEN, desc, overriddenMethods);
            }
        }
        return null;
    }

}
