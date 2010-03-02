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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.loaders.DataObject;

/**
 * A class that computes annotations -
 * extracted from OverridesTaskFactory for the sake of testability
 * @author Vladimir Kvashin
 */
public class ComputeAnnotations {

    public static ComputeAnnotations getInstance(CsmFile csmFile, StyledDocument doc, DataObject dobj) {
        return new ComputeAnnotations(csmFile, doc, dobj);
    }

    private final CsmProject csmProject;
    private final CsmFile csmFile;
    private final StyledDocument doc;
    private final DataObject dobj;

    private ComputeAnnotations(CsmFile csmFile, StyledDocument doc, DataObject dobj) {
       this.csmFile = csmFile;
       this.csmProject = csmFile.getProject();
       this.doc = doc;
       this.dobj = dobj;
    }

    /*package*/ void computeAnnotations(Collection<BaseAnnotation> toAdd) {
        computeAnnotations(csmFile.getDeclarations(), toAdd);
    }

    private void computeAnnotations(
            Collection<? extends CsmOffsetableDeclaration> toProcess,
            Collection<BaseAnnotation> toAdd) {

        for (CsmOffsetableDeclaration decl : toProcess) {
            if (CsmKindUtilities.isFunction(decl)) {
                computeAnnotation((CsmFunction) decl, toAdd);
            } else if (CsmKindUtilities.isClass(decl)) {
                computeAnnotation((CsmClass) decl, toAdd);
                computeAnnotations(((CsmClass) decl).getMembers(), toAdd);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                computeAnnotations(((CsmNamespaceDefinition) decl).getDeclarations(), toAdd);
            }
        }
    }

    private void computeAnnotation(CsmFunction func, Collection<BaseAnnotation> toAdd) {
        if (CsmKindUtilities.isMethod(func)) {
            CsmMethod meth = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration(func);
            final Collection<? extends CsmMethod> baseMethods = CsmVirtualInfoQuery.getDefault().getFirstBaseDeclarations(meth);
            Collection<? extends CsmMethod> overriddenMethods;
            if (!baseMethods.isEmpty() || CsmVirtualInfoQuery.getDefault().isVirtual(meth)) {
                overriddenMethods = CsmVirtualInfoQuery.getDefault().getOverridenMethods(meth, false);
            } else {
                overriddenMethods = Collections.<CsmMethod>emptyList();
            }
            if (BaseAnnotation.LOGGER.isLoggable(Level.FINEST)) {
                BaseAnnotation.LOGGER.log(Level.FINEST, "Found {0} base decls for {1}", new Object[]{baseMethods.size(), toString(func)});
                for (CsmMethod baseMethod : baseMethods) {
                    BaseAnnotation.LOGGER.log(Level.FINEST, "    {0}", toString(baseMethod));
                }
            }
            baseMethods.remove(meth);
            if (!baseMethods.isEmpty() || !overriddenMethods.isEmpty()) {
                toAdd.add(new OverrideAnnotation(doc, func, baseMethods, overriddenMethods));
            }
        }
    }

    private void computeAnnotation(CsmClass cls, Collection<BaseAnnotation> toAdd) {
        Collection<CsmReference> subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false);
        if (!subRefs.isEmpty()) {
            Collection<CsmClass> subClasses = new ArrayList<CsmClass>(subRefs.size());
            for (CsmReference ref : subRefs) {
                CsmObject obj = ref.getReferencedObject();
                CndUtils.assertTrue(obj == null || (obj instanceof CsmClass), "getClassifier() should return either null or CsmClass"); //NOI18N
                if (obj instanceof CsmClass) {
                    subClasses.add((CsmClass) obj);
                }
            }
            if (!subClasses.isEmpty()) {
                toAdd.add(new InheritAnnotation(doc, cls, subClasses));
            }
        }
    }

    private static CharSequence toString(CsmFunction func) {
        StringBuilder sb = new StringBuilder();
        sb.append(func.getClass().getSimpleName());
        sb.append(' ');
        sb.append(func.getQualifiedName());
        sb.append(" ["); // NOI18N
        sb.append(func.getContainingFile().getName());
        sb.append(':');
        sb.append(func.getStartPosition().getLine());
        sb.append(']');
        return sb;
    }
}
