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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver.class)
public class ClassifierResolverImpl extends CsmClassifierResolver {

    @Override
    public CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
        if (orig instanceof CsmOffsetable) {
            CsmProject project = contextFile.getProject();
            // we'd prefer to start from real project, not artificial one
            if (project != null && project.isArtificial()) {
                for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(contextFile, 0)) {
                    if (cu.getStartFile() != null) {
                        contextFile = cu.getStartFile();
                        break;
                    }
                }
            }
            return ResolverFactory.createResolver((CsmOffsetable) orig, contextFile).getOriginalClassifier(orig);
        }
        return orig;
    }

    @Override
    public boolean isForwardClass(CsmObject cls) {
        return CsmKindUtilities.isDeclaration(cls) && ForwardClass.isForwardClass((CsmDeclaration)cls);
    }

    @Override
    public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile file, boolean classesOnly) {
        CsmProject project = file.getProject();
        if (project == null) {
            return null;
        }
        AtomicBoolean visible = new AtomicBoolean(false);
        CsmClassifier result = findVisibleDeclaration(project, qualifiedName, file, visible, classesOnly);
        // we prefer to skip even visible class forward based classes
        if (!ForwardClass.isForwardClass(result) && visible.get()) {
            assert result != null : "how can visible be true without a result?";
            return result;
        }
        Collection<CsmProject> libraries = getLibraries(file, 0);
        // continue in libs
        for (Iterator iter = libraries.iterator(); iter.hasNext();) {
            visible.set(false);
            CsmProject lib = (CsmProject) iter.next();
            CsmClassifier visibleDecl = findVisibleDeclaration(lib, qualifiedName, file, visible, classesOnly);         
            // we prefer to skip even visible class forward based classes
            if (!ForwardClass.isForwardClass(visibleDecl) && visible.get()) {
                return visibleDecl;
            }
            if (result == null) {
                result = visibleDecl;
            }
        }
        return result;
    }

    private CsmClassifier findVisibleDeclaration(CsmProject project, CharSequence uniqueName,
            CsmFile file, AtomicBoolean visible, boolean classesOnly) {
        Collection<CsmClassifier> decls = project.findClassifiers(uniqueName);
        CsmClassifier first = null;
        for (CsmClassifier decl : decls) {
            if (!classesOnly || CsmKindUtilities.isClass(decl)) {
                if (first == null || ForwardClass.isForwardClass(first)) {
                    first = decl;
                }
                if (CsmIncludeResolver.getDefault().isObjectVisible(file, decl)) {
                    visible.set(true);
                    return decl;
                }
            }
        }
        return first;
    }

    private Collection<CsmProject> getLibraries(CsmFile file, int contextOffset) {
        CsmProject project = file.getProject();
        if (project == null) {
            return Collections.emptyList();
        }
        if (project.isArtificial()) {
            Collection<CsmProject> out = new HashSet<CsmProject>(2);
            for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(file, contextOffset)) {
                out.addAll(cu.getStartProject().getLibraries());
            }
            return out;
        } else {
            return project.getLibraries();
        }
    }
}
