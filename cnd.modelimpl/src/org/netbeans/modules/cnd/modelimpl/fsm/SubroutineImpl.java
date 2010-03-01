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

package org.netbeans.modules.cnd.modelimpl.fsm;

import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmFunction.OperatorKind;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author nk220367
 */
public class SubroutineImpl <T> extends OffsetableDeclarationBase<T>
        implements CsmFunctionDefinition, Disposable, RawNamable {

    private final CharSequence name;
    private final CharSequence[] rawName;
    private CsmUID<CsmScope> scopeUID;

    private final DummyParametersListImpl parameterList;

    public SubroutineImpl(String name, CsmFile file, int startOffset, int endOffset, CsmType type, CsmScope scope,
            DummyParametersListImpl parameterList) {
        super(file, startOffset, endOffset);

        this.name = QualifiedNameCache.getManager().getString(name);
        rawName = new CharSequence[1];
        rawName[0] = this.name;
        this.parameterList = parameterList;

        try {
            _setScope(scope);
        } catch (AstRendererException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    public CharSequence getQualifiedName() {
        return name;
    }

    public CharSequence getName() {
        return name;
    }

    public CsmScope getScope() {
        return _getScope();
    }

    public CharSequence getDeclarationText() {
        return name;
    }

    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    public CsmFunction getDeclaration() {
        return this;
    }

    public boolean isOperator() {
        return false;
    }

    public OperatorKind getOperatorKind() {
        return OperatorKind.NONE;
    }

    public boolean isInline() {
        return false;
    }

    public CsmType getReturnType() {
        return TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
    }

    public CsmFunctionParameterList getParameterList() {
        return null;
    }

    public Collection<CsmParameter> getParameters() {
        return (parameterList == null) ? Collections.<CsmParameter>emptyList() : parameterList.getParameters();
    }

    public CharSequence getSignature() {
        return ""; // NOI18N
    }

    public Collection<CsmScopeElement> getScopeElements() {
        return Collections.<CsmScopeElement>emptyList();
    }

    public CharSequence[] getRawName() {
        return rawName;
    }

    private void _setScope(CsmScope scope) throws AstRendererException {
        // for functions declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        }
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        return scope;
    }

    public CsmCompoundStatement getBody() {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
//        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();

        PersistentUtils.writeParameterList(this.parameterList, output);
        PersistentUtils.writeStrings(this.rawName, output);

//        // not null UID
//        assert !CHECK_SCOPE || this.scopeUID != null;
        factory.writeUID(this.scopeUID, output);

//        PersistentUtils.writeUTF(this.signature, output);
//        output.writeByte(flags);
    }

    public SubroutineImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
//        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameterList = (DummyParametersListImpl)PersistentUtils.readParameterList(input);


        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());

        this.scopeUID = factory.readUID(input);
//        // not null UID
//        assert !CHECK_SCOPE || this.scopeUID != null;
//        this.scopeRef = null;
//
//        this.signature = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
//        this.flags = input.readByte();
    }
}

