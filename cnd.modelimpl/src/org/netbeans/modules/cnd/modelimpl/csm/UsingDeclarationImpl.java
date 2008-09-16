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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
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
public class UsingDeclarationImpl extends OffsetableDeclarationBase<CsmUsingDeclaration> 
        implements CsmUsingDeclaration, RawNamable, Disposable {

    private final CharSequence name;
    private final int startOffset;
    private final CharSequence[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmUID<CsmDeclaration> referencedDeclarationUID = null;
    private WeakReference<CsmDeclaration> refDeclaration;
    private boolean lastResolveFalure;
    private final CsmUID<CsmScope> scopeUID;
    
    public UsingDeclarationImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file);
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
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
        if (referencedDeclaration == null && ! lastResolveFalure) {
            _setReferencedDeclaration(null);
            if (rawName != null) {
                ProjectBase prjBase = (ProjectBase)getProject();
                CsmNamespace namespace = null;
                if (rawName.length == 1) {
                    namespace = prjBase.getGlobalNamespace();
                } else if (rawName.length > 1) {
                    CharSequence[] partial = new CharSequence[rawName.length - 1];
                    System.arraycopy(rawName, 0, partial, 0, rawName.length - 1);
                    CsmObject result = ResolverFactory.createResolver(getContainingFile(), startOffset, resolver).resolve(partial, Resolver.NAMESPACE);
                    if (CsmKindUtilities.isNamespace(result)) {
                        namespace = (CsmNamespace)result;
                    }
                }
                if (namespace != null) {
                    CharSequence lastName = rawName[rawName.length - 1];
                    CsmDeclaration bestChoice = null;
                    CsmFilter filter = CsmSelect.getDefault().getFilterBuilder().createNameFilter(lastName.toString(), true, true, false);

                    // we should try searching not only in namespace resolved found,
                    // but in numspaces with the same name in required projects
                    // iz #140787 cout, endl unresolved in some Loki files
                    Collection<CsmNamespace> namespacesToSearch = new ArrayList<CsmNamespace>();
                    namespacesToSearch.add(namespace);
                    CharSequence nspQName = namespace.getQualifiedName();
                    for (CsmProject lib : getProject().getLibraries()) {
                        CsmNamespace libNs = lib.findNamespace(nspQName);
                        if (libNs != null) {
                            namespacesToSearch.add(libNs);
                        }
                    }

                    outer:
                    for (CsmNamespace curr : namespacesToSearch) {
                        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(curr, filter);
                        while (it.hasNext()) {
                            CsmDeclaration elem = it.next();
                            if (CharSequenceKey.Comparator.compare(lastName,elem.getName())==0) {
                                if (!CsmKindUtilities.isExternVariable(elem)) {
                                    referencedDeclaration = elem;
                                    break outer;
                                } else {
                                    bestChoice = elem;
                                }
                            }
                        }
                    }
                    referencedDeclaration = referencedDeclaration == null ? bestChoice : referencedDeclaration;
                }
            }
            _setReferencedDeclaration(referencedDeclaration);
            lastResolveFalure = referencedDeclaration == null;
        }
        return referencedDeclaration;
    }
    
    private CsmDeclaration _getReferencedDeclaration() {
        CsmDeclaration referencedDeclaration = null;
        if (refDeclaration != null) {
            referencedDeclaration =((Reference<CsmDeclaration>)refDeclaration).get();
        }
        if (referencedDeclaration == null) {
            referencedDeclaration = UIDCsmConverter.UIDtoDeclaration(referencedDeclarationUID);
            refDeclaration = new WeakReference<CsmDeclaration>(referencedDeclaration);
        }
        // can be null if namespace was removed 
        return referencedDeclaration;
    }    

    private void _setReferencedDeclaration(CsmDeclaration referencedDeclaration) {
        if (referencedDeclaration != null) {
            refDeclaration = new WeakReference<CsmDeclaration>(referencedDeclaration);
        } else {
            refDeclaration = null;
        }
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
        return  UIDCsmConverter.UIDtoScope(this.scopeUID);
    }

    @Override
    public void dispose() {
        super.dispose();
        CsmScope scope = getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
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
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
    }
    
    @SuppressWarnings("unchecked")
    public UsingDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = NameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this.startOffset = input.readInt();
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached declaration
        this.referencedDeclarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }      
}
