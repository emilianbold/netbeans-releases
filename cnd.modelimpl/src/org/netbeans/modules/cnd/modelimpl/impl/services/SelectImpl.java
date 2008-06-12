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

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 *
 * @author Alexander Simon
 */
public class SelectImpl extends CsmSelect {

    @Override
    public CsmFilterBuilder getFilterBuilder() {
        return new FilterBuilder();
    }

    @Override
    public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return ((FileImpl)file).getMacros(filter);
        }
        return file.getMacros().iterator();
    }

    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter) {
        if (namespace instanceof NamespaceImpl){
            return ((NamespaceImpl)namespace).getDeclarations(filter);
        }
        return namespace.getDeclarations().iterator();
    }

    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
        if (namespace instanceof NamespaceDefinitionImpl){
            return ((NamespaceDefinitionImpl)namespace).getDeclarations(filter);
        }
        return namespace.getDeclarations().iterator();
    }

    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return ((FileImpl)file).getDeclarations(filter);
        }
        return file.getDeclarations().iterator();
    }

    private static interface Filter extends CsmFilter, UIDFilter {
    }
    
    static class FilterBuilder implements CsmFilterBuilder {
        public CsmFilter createKindFilter(final CsmDeclaration.Kind[] kinds) {
            return new Filter(){
                public boolean accept(CsmUID uid) {
                    CsmDeclaration.Kind kind = UIDUtilities.getKind(uid);
                    if (kind != null) {
                        for(CsmDeclaration.Kind k : kinds){
                            if (k == kind){
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
        }

        public CsmFilter createNameFilter(final String strPrefix, final boolean match, final boolean caseSensitive, final boolean allowEmptyName) {
            return new Filter(){
                public boolean accept(CsmUID uid) {
                    CharSequence name = UIDUtilities.getName(uid);
                    if (name != null) {
                        if (allowEmptyName && name.length() == 0) {
                            return true;
                        }
                        return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive);
                    }
                    return false;
                }
            };
        }

        public CsmFilter createCompoundFilter(final CsmFilter first, final CsmFilter second) {
            return new Filter(){
                public boolean accept(CsmUID uid) {
                    return ((UIDFilter)first).accept(uid) && ((UIDFilter)second).accept(uid);
                }
            };
        }

        public CsmFilter createNameFilter(final NameAcceptor nameAcceptor) {
            return new Filter(){
                public boolean accept(CsmUID uid) {
                    CharSequence name = UIDUtilities.getName(uid);
                    return nameAcceptor.accept(name);
                }
            };
        }
    }

}
