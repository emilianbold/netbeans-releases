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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public abstract class CsmSelect {
    private static CsmSelect DEFAULT = new Default();

    public abstract CsmFilterBuilder getFilterBuilder();
    public abstract Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter);
    public abstract Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter);
    public abstract boolean hasDeclarations(CsmFile file);
    public abstract Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter);
    public abstract Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter);
    public abstract Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter);
    public abstract Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter);
    public abstract Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter);
    public abstract Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter);

    
    protected CsmSelect() {
    }
    
    /**
     * Static method to obtain the CsmSelect implementation.
     * @return the selector
     */
    public static synchronized CsmSelect getDefault() {
        return DEFAULT;
    }
    
    public static interface CsmFilter {
    }
    
    public static interface NameAcceptor {
        boolean accept(CharSequence name);
    }

    public static interface CsmFilterBuilder {
        CsmFilter createKindFilter(CsmDeclaration.Kind kinds[]);
        CsmFilter createNameFilter(String strPrefix, boolean match, boolean caseSensitive, boolean allowEmptyName);
        CsmFilter createOffsetFilter(int startOffset, int endOffset);
        CsmFilter createOffsetFilter(int innerOffset);
        CsmFilter createCompoundFilter(CsmFilter first, CsmFilter second);
        CsmFilter createNameFilter(NameAcceptor nameAcceptor);
    }

    /**
     * Implementation of the default selector
     */  
    private static final class Default extends CsmSelect {
        private final Lookup.Result<CsmSelect> res;
        private static final boolean FIX_SERVICE = true;
        private CsmSelect fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmSelect.class);
        }

        private CsmSelect getService(){
            CsmSelect service = fixedSelector;
            if (service == null) {
                for (CsmSelect selector : res.allInstances()) {
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
            CsmSelect service = getService();
            if (service != null) {
                return service.getFilterBuilder();
            }
            return null;
        }

        @Override
        public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getMacros(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getIncludes(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getDeclarations(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getClassMembers(cls, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getStaticVariables(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter) {
            CsmSelect service = getService();
            if (service != null) {
                return service.getStaticFunctions(file, filter);
            }
            return null;
        }

        @Override
        public boolean hasDeclarations(CsmFile file) {
            CsmSelect service = getService();
            if (service != null) {
                return service.hasDeclarations(file);
            }
            return file.getDeclarations().isEmpty();
        }
    }
}
