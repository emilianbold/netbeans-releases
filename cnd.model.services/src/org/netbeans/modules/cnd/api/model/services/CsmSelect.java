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

package org.netbeans.modules.cnd.api.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmSelectProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class CsmSelect {

    private static CsmSelectProvider DEFAULT = new Default();

    public static CsmFilterBuilder getFilterBuilder() {
        return getDefault().getFilterBuilder();
    }

    public static Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
        return getDefault().getMacros(file, filter);
    }

    public static Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
        return getDefault().getIncludes(file, filter);
    }

    public static boolean hasDeclarations(CsmFile file) {
        return getDefault().hasDeclarations(file);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
        return getDefault().getDeclarations(file, filter);
    }

    public static Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter)  {
        return getDefault().getStaticVariables(file, filter);
    }

    public static Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter)  {
        return getDefault().getStaticFunctions(file, filter);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter)  {
        return getDefault().getDeclarations(namespace, filter);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter)  {
        return getDefault().getDeclarations(namespace, filter);
    }

    public static Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter)  {
        return getDefault().getClassMembers(cls, filter);
    }

    public static Iterator<CsmFunction> getFunctions(CsmProject project, CharSequence qualifiedName) {
        // ensure that qName does NOT start with "::"
        if (qualifiedName.length() > 1 && qualifiedName.charAt(0) == ':' && qualifiedName.charAt(1) == ':') {
            qualifiedName = qualifiedName.subSequence(2, qualifiedName.length());
        }
        Collection<CsmFunction> result = new ArrayList<CsmFunction>();
	getFunctions(project, qualifiedName, result, new LinkedHashSet<CsmProject>());
	return result.iterator();
    }

    // NB: qName does NOT start with "::"
    private static void getFunctions(CsmProject project, CharSequence qName,
            Collection<CsmFunction> result, Collection<CsmProject> processedProjects) {
        if (!processedProjects.contains(project)) {
            processedProjects.add(project);
            // find "::" in Name
            int pos = -1;
            for (int i = qName.length()-2; i > 1; i--) {
                if (qName.charAt(i) == ':' && qName.charAt(i+1) == ':') { //NOI18N
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                // qName resides in global namespace
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION,
                         CsmDeclaration.Kind.FUNCTION_FRIEND,CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(qName, true, true, false));
                getFunctions(CsmSelect.getDeclarations(project.getGlobalNamespace(), filter), result);
            } else {
                // split qName into owner name and function name
                CharSequence ownerQName = qName.subSequence(0, pos);
                CharSequence funcName = qName.subSequence(pos+2, qName.length());
                CsmNamespace nsp = project.findNamespace(ownerQName);
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION,
                         CsmDeclaration.Kind.FUNCTION_FRIEND,CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(funcName, true, true, false));
                if (nsp != null) {
                    getFunctions(CsmSelect.getDeclarations(nsp, filter), result);
                }
                for (CsmClassifier cls : project.findClassifiers(ownerQName)) {
                    if (CsmKindUtilities.isClass(cls)) {
                        getFunctions(CsmSelect.getClassMembers((CsmClass) cls, filter), result);
                    }
                }
            }
            for (CsmProject lib : project.getLibraries()) {
                getFunctions(lib, qName, result, processedProjects);
            }
        }
    }

    private static void getFunctions(Iterator<? extends CsmOffsetableDeclaration> iter, Collection<CsmFunction> result) {
        while (iter.hasNext()) {
            CsmOffsetableDeclaration decl = iter.next();
            if (CsmKindUtilities.isFunction(decl)) {
                result.add((CsmFunction) decl);
            }
        }
    }

    private CsmSelect() {
    }
    
    /**
     * Static method to obtain the CsmSelect implementation.
     * @return the selector
     */
    private static CsmSelectProvider getDefault() {
        return DEFAULT;
    }
    
    public static interface CsmFilter {
    }
    
    public static interface NameAcceptor {
        boolean accept(CharSequence name);
    }

    public static interface CsmFilterBuilder {
        CsmFilter createKindFilter(CsmDeclaration.Kind ... kinds);
        CsmFilter createNameFilter(CharSequence strPrefix, boolean match, boolean caseSensitive, boolean allowEmptyName);
        CsmFilter createOffsetFilter(int startOffset, int endOffset);
        CsmFilter createOffsetFilter(int innerOffset);
        CsmFilter createCompoundFilter(CsmFilter first, CsmFilter second);
        CsmFilter createNameFilter(NameAcceptor nameAcceptor);
    }

    /**
     * Implementation of the default selector
     */  
    private static final class Default implements CsmSelectProvider {
        private final Lookup.Result<CsmSelectProvider> res;
        private static final boolean FIX_SERVICE = true;
        private CsmSelectProvider fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmSelectProvider.class);
        }

        private CsmSelectProvider getService(){
            CsmSelectProvider service = fixedSelector;
            if (service == null) {
                for (CsmSelectProvider selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    fixedSelector = service;
                }
            }
            return service;
        }
        
        @Override
        public CsmFilterBuilder getFilterBuilder() {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getFilterBuilder();
            }
            return null;
        }

        @Override
        public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getMacros(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getIncludes(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getClassMembers(cls, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getStaticVariables(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getStaticFunctions(file, filter);
            }
            return null;
        }

        @Override
        public boolean hasDeclarations(CsmFile file) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.hasDeclarations(file);
            }
            return file.getDeclarations().isEmpty();
        }
    }
}
