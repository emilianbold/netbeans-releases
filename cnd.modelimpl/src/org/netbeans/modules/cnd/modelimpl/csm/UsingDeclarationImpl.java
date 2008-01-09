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
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Implements CsmUsingDeclaration
 * @author Vladimir Kvasihn
 */
public class UsingDeclarationImpl extends OffsetableDeclarationBase<CsmUsingDeclaration> implements CsmUsingDeclaration, RawNamable {

    private final CharSequence name;
    private final int startOffset;
    private final CharSequence[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmUID<CsmDeclaration> referencedDeclarationUID = null;
    
    public UsingDeclarationImpl(AST ast, CsmFile file) {
        super(ast, file);
        name = NameCache.getManager().getString(ast.getText());
        // TODO: here we override startOffset which is not good because startPosition is now wrong
        startOffset = ((CsmAST)ast.getFirstChild()).getOffset();
        rawName = AstUtil.getRawNameInChildren(ast);
    }
    
    public CsmDeclaration getReferencedDeclaration() {
        return getReferencedDeclaration(null);
    }   

    public CsmDeclaration getReferencedDeclaration(Resolver resolver) {
        // TODO: process preceding aliases
        // TODO: process non-class elements
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver"))
        CsmDeclaration referencedDeclaration = _getReferencedDeclaration();
        if (referencedDeclaration == null) {
            _setReferencedDeclaration(null);
            ProjectBase prjBase = ((ProjectBase)getProject());
            referencedDeclaration = prjBase.findClassifier(name, true);
            if (referencedDeclaration == null && rawName != null && rawName.length > 1) {
                // resolve all before last ::
                CharSequence[] partial = new CharSequence[rawName.length - 1];
                System.arraycopy(rawName, 0, partial, 0, rawName.length - 1);
                CsmObject result = ResolverFactory.createResolver(getContainingFile(), startOffset, resolver).resolve(partial, Resolver.NAMESPACE);
                if (CsmKindUtilities.isNamespace(result)) {
                    CharSequence lastName = rawName[rawName.length - 1];
                    CsmDeclaration bestChoice = null;
                    for (CsmDeclaration elem : ((CsmNamespace)result).getDeclarations()) {
                        if (CharSequenceKey.Comparator.compare(lastName,elem.getName())==0) {
                            if (!CsmKindUtilities.isExternVariable(elem)) {
                                referencedDeclaration = elem;
                                break;
                            } else {
                                bestChoice = elem;
                            }
                        }
                    }
                    referencedDeclaration = referencedDeclaration == null ? bestChoice : referencedDeclaration;
                }
            }
            _setReferencedDeclaration(referencedDeclaration);                
        }
        return referencedDeclaration;
    }
    
    private CsmDeclaration _getReferencedDeclaration() {
        CsmDeclaration referencedDeclaration = UIDCsmConverter.UIDtoDeclaration(referencedDeclarationUID);
        // can be null if namespace was removed 
        return referencedDeclaration;
    }    

    private void _setReferencedDeclaration(CsmDeclaration referencedDeclaration) {
        this.referencedDeclarationUID = UIDCsmConverter.declarationToUID(referencedDeclaration);
        assert this.referencedDeclarationUID != null || referencedDeclaration == null;
    }
    
    @Override
    public int getStartOffset() {
        return startOffset;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.USING_DECLARATION;
    }
    
    public CharSequence getName() {
        return name;
    }
    
    public CharSequence getQualifiedName() {
        return getName();
    }
    
    public CharSequence[] getRawName() {
        return rawName;
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
        assert this.name != null;
        output.writeUTF(this.name.toString());
        output.writeInt(this.startOffset);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedDeclarationUID, output);
    }
    
    public UsingDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = NameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this.startOffset = input.readInt();
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached declaration
        this.referencedDeclarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }      
}
