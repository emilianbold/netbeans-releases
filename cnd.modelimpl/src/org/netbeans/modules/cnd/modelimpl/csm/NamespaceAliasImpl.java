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
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * Implements CsmNamespaceAlias
 * @author Vladimir Kvasihn
 */
public class NamespaceAliasImpl extends OffsetableDeclarationBase<CsmNamespaceAlias> implements CsmNamespaceAlias, RawNamable {

    private final CharSequence alias;
    private final CharSequence namespace;
    private final CharSequence[] rawName;
    
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;
    
    public NamespaceAliasImpl(AST ast, CsmFile file) {
        super(ast, file);
        rawName = createRawName(ast);
        alias = QualifiedNameCache.getManager().getString(ast.getText());
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
            namespace = CharSequenceKey.empty();
        }
        else {
            for( token = token.getNextSibling() ; token != null; token = token.getNextSibling() ) {
                sb.append(token.getText());
            }
            namespace = QualifiedNameCache.getManager().getString(sb.toString());
        }
    }

    public CsmNamespace getReferencedNamespace() {
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver2"))
        //assert ResolverFactory.resolver != 2;
        return ((ProjectBase)(getContainingFile().getProject())).findNamespace(namespace, true);
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
        return getName();
    }
    
    private static String[] createRawName(AST node) {
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
        return new String[0];
    }

    public CharSequence[] getRawName() {
        return rawName;
    }
    
    @Override
    public String toString() {
        return "" + getKind() + ' ' + alias + '=' + namespace /*+ " rawName=" + Utils.toString(getRawName())*/; // NOI18N
    }
    
    public CsmScope getScope() {
        //TODO: implement!
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.alias != null;
        output.writeUTF(this.alias.toString());
        assert this.namespace != null;
        output.writeUTF(this.namespace.toString());
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
    }
    
    @SuppressWarnings("unchecked")
    public NamespaceAliasImpl(DataInput input) throws IOException {
        super(input);
        this.alias = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.alias != null;
        this.namespace = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.namespace != null;
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }    
}
