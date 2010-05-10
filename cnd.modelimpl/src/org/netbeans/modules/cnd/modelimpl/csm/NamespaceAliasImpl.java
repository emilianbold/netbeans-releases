/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;

/**
 * Implements CsmNamespaceAlias
 * @author Vladimir Kvasihn
 */
public class NamespaceAliasImpl extends OffsetableDeclarationBase<CsmNamespaceAlias> implements CsmNamespaceAlias, RawNamable {

    private final CharSequence alias;
    private final CharSequence namespace;
    private final CharSequence[] rawName;
    
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;

    private CsmUID<CsmScope> scopeUID = null;
    
    public NamespaceAliasImpl(AST ast, CsmFile file, CsmScope scope, boolean global) {
        super(ast, file);
        _setScope(scope);
        rawName = createRawName(ast);
        alias = NameCache.getManager().getString(ast.getText());
        AST token = ast.getFirstChild();
        while( token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL ) {
            token = token.getNextSibling();
        }
        StringBuilder sb = new StringBuilder();
        if( token == null ) {
            if( FileImpl.reportErrors ) {
                int ln = ast.getLine();
                int col = ast.getColumn();
                AST child = ast.getFirstChild();
                if( child != null ) {
                    ln = child.getLine();
                    col = child.getColumn();
                }
                System.err.println("Corrupted AST for namespace alias in " + 
                file.getAbsolutePath() + ' ' + ln + ":" + col); // NOI18N
            }
            namespace = CharSequences.empty();
        }
        else {
            for( token = token.getNextSibling() ; token != null; token = token.getNextSibling() ) {
                sb.append(token.getText());
            }
            namespace = QualifiedNameCache.getManager().getString(sb.toString());
        }
        if (!global) {
            Utils.setSelfUID(this);
        }
    }

    private void _setScope(CsmScope scope) {
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
        assert (scopeUID != null || scope == null);
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        return scope;
    }

    public CsmNamespace getReferencedNamespace() {
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver2"))
        //assert ResolverFactory.resolver != 2;
        CsmNamespace res = ((ProjectBase)(getContainingFile().getProject())).findNamespace(namespace, true);
        if(res == null) {
            CsmScope scope = getScope();
            if(scope instanceof CsmNamespace) {
                StringBuilder sb = new StringBuilder(((CsmNamespace)scope).getQualifiedName());
                sb.append("::"); // NOI18N
                sb.append(namespace);
                res = ((ProjectBase)(getContainingFile().getProject())).findNamespace(sb, true);
            }
        }
        return res;
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_ALIAS;
    }

    public CharSequence getAlias() {
        return alias;
    }

    public CharSequence getName() {
        return getAlias();
    }
    
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmNamespaceDefinition) ) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if( scopeQName != null && scopeQName.length() > 0 ) {
                return CharSequences.create(scopeQName.toString() + "::" + getQualifiedNamePostfix()); // NOI18N
            }
        }
        return getName();
    }
    
    private static CharSequence[] createRawName(AST node) {
        AST token = node.getFirstChild();
        while( token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL ) {
            token = token.getNextSibling();
        }
        if( token != null ) {
            token = token.getNextSibling();
            if( token != null && token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                return AstUtil.getRawName(token.getFirstChild());
            }
        }
        return new CharSequence[0];
    }

    public CharSequence[] getRawName() {
        return rawName;
    }
    
    @Override
    public String toString() {
        return "" + getKind() + ' ' + alias + '=' + namespace /*+ " rawName=" + Utils.toString(getRawName())*/; // NOI18N
    }
    
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public void dispose() {
        super.dispose();
        CsmScope scope = _getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.alias != null;
        PersistentUtils.writeUTF(alias, output);
        assert this.namespace != null;
        PersistentUtils.writeUTF(namespace, output);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
    }
    
    public NamespaceAliasImpl(DataInput input) throws IOException {
        super(input);
        this.alias = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.alias != null;
        this.namespace = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.namespace != null;
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }    
}
