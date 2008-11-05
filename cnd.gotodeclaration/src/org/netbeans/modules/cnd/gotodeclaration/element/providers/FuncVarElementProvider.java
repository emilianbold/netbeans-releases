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

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author vk155633
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider.class)
public class FuncVarElementProvider extends BaseProvider implements ElementProvider {

    private static class FuncVarDelegate extends ProviderDelegate {

        public String name() {
            return "C/C++ Functions and Variables"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(FuncVarElementProvider.class, "FUNCVAR_PROVIDER_DISPLAY_NAME"); // NOI18N
        }

        protected void processProject(CsmProject project, ResultSet result, CsmSelect.CsmFilter filter) {
            if( TRACE ) System.err.printf("FuncVarElementProvider.processProject %s\n", project.getName());
            processNamespace(project.getGlobalNamespace(), result, filter);
        }

        private void processNamespace(CsmNamespace nsp, ResultSet result, CsmSelect.CsmFilter filter) {
            if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
            Iterator<CsmOffsetableDeclaration> iter  = CsmSelect.getDefault().getDeclarations(nsp, filter);
            while( iter.hasNext() ) {
                if( isCancelled() ) {
                    return;
                }
                processDeclaration(iter.next(), result);
            }
            for( CsmNamespace child : nsp.getNestedNamespaces() ) {
                if( isCancelled() ) {
                    return;
                }
                processNamespace(child, result, filter);
            }
        }

        private void processDeclaration(CsmDeclaration decl, ResultSet result) {
            CsmFunctionDefinition fdef = null;
            switch (decl.getKind()) {
                case FUNCTION_DEFINITION:
                    fdef = (CsmFunctionDefinition) decl;
                    result.add(new FunctionElementDescriptor(fdef));
                    break;
                case FUNCTION:
                    CsmFunction fdecl = (CsmFunction) decl;
                    fdef = fdecl.getDefinition();
                    if( fdef == null || fdef.equals(fdecl) ) {
                        result.add(new FunctionElementDescriptor((CsmFunction) decl));
                    }
                    break;
                case VARIABLE:
                    result.add(new VariableElementDescriptor((CsmVariable) decl));
                    break;
                case CLASS:
                case UNION:
                case STRUCT:
                case ENUM:
                case TYPEDEF:
                case BUILT_IN:
                case ENUMERATOR:
                case MACRO:
                case VARIABLE_DEFINITION:
                case TEMPLATE_SPECIALIZATION:
                case ASM:
                case TEMPLATE_DECLARATION:
                case NAMESPACE_DEFINITION:
                case NAMESPACE_ALIAS:
                case USING_DIRECTIVE:
                case USING_DECLARATION:
                case CLASS_FORWARD_DECLARATION:
                case CLASS_FRIEND_DECLARATION:
                    break;
            }
        }


    }
    
    @Override
    protected ProviderDelegate createDelegate() {
        return new FuncVarDelegate();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(FuncVarElementProvider.class, "FUNCVAR_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    public String name() {
        return "C/C++ Functions and Variables"; // NOI18N
    }
    
}
