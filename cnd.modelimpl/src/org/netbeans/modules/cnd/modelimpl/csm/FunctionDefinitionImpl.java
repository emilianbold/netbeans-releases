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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl<T> extends FunctionImplEx<T> implements CsmFunctionDefinition {

    private CsmUID<CsmFunction> declarationUID;
    private final CsmCompoundStatement body;
    private int parseCount;

    public FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope, boolean register, boolean global) throws AstRendererException {
        super(ast, file, scope, false, global);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        if (body == null) {
            throw new AstRendererException((FileImpl) file, getStartOffset(),
                    "Null body in function definition."); // NOI18N
        }
        if (register) {
            registerInProject();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }
    
    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    @Override
    public CsmFunction getDeclaration() {
        return getDeclaration(null);
    }

    public CsmFunction getDeclaration(Resolver parent) {
        CsmFunction declaration = _getDeclaration();
        if (declaration == null) {
            int newCount = FileImpl.getParseCount();
            if (newCount == parseCount) {
                return null;
            }
            _setDeclaration(null);
            declaration = findDeclaration(parent);
            _setDeclaration(declaration);
            parseCount = newCount;
        }
        return declaration;
    }

    private CsmFunction _getDeclaration() {
        CsmFunction decl = UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
        // null object is OK here, because of changed cached reference
        return decl;
    }

    private void _setDeclaration(CsmFunction decl) {
        this.declarationUID = UIDCsmConverter.declarationToUID(decl);
        assert this.declarationUID != null || decl == null;
    }

    // method try to find declaration in case class have exactly one cast operator with desired name
    private CsmDeclaration fixCastOperator(CsmClass owner) {
        CsmDeclaration candidate = null;
        String s1 = getName().toString();
        int i1 = s1.lastIndexOf("::"); // NOI18N
        if (i1 > 0) {
            s1 = "operator  " + s1.substring(i1 + 2); // NOI18N
        }
        Iterator<CsmMember> it = CsmSelect.getClassMembers(owner,
                CsmSelect.getFilterBuilder().createNameFilter("operator", false, true, false)); // NOI18N
        while (it.hasNext()) {
            CsmMember m = it.next();
            String s2 = m.getName().toString();
            int i2 = s2.lastIndexOf("::"); // NOI18N
            if (i2 > 0) {
                s2 = "operator  " + s2.substring(i2 + 2); // NOI18N
            }
            if (s1.equals(s2)) {
                if (candidate == null) {
                    candidate = m;
                } else {
                    candidate = null;
                    break;
                }
            }
        }
        return candidate;
    }

    private CsmFunction findDeclaration(Resolver parent) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        if (def == null) {
            CsmObject owner = findOwner(parent);
            if (owner instanceof CsmClass) {
                Iterator<CsmMember> it = CsmSelect.getClassMembers((CsmClass) owner,
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                def = findByName(it, getName());
                if (def == null && isOperator()) {
                    def = fixCastOperator((CsmClass)owner);
                }
            } else if (owner instanceof CsmNamespace) {
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace) owner),
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                def = findByName(it, getName());
            }
        }
        return (CsmFunction) def;
    }

    private static CsmFunction findByName(Iterator declarations, CharSequence name) {
        for (Iterator it = declarations; it.hasNext();) {
            Object o = it.next();
            if (CsmKindUtilities.isCsmObject(o) && CsmKindUtilities.isFunction((CsmObject) o)) {
                CsmFunction decl = (CsmFunction) o;
                if (decl.getName().equals(name)) {
                    return decl;
                }
            }
        }
        return null;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    protected String findQualifiedName() {
        CsmFunction declaration = _getDeclaration();
        if (declaration != null) {
            return declaration.getQualifiedName().toString();
        }
        return super.findQualifiedName();
    }

    @Override
    public CsmScope getScope() {
        return getContainingFile();
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);

        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }

    public FunctionDefinitionImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);

        // read cached declaration
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
