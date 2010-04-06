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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Implements CsmUsingDeclaration
 * @author Vladimir Kvasihn
 */
public class UsingDeclarationImpl extends OffsetableDeclarationBase<CsmUsingDeclaration> 
        implements CsmUsingDeclaration, CsmMember, RawNamable, Disposable {

    private final CharSequence name;
    private final int startOffset;
    private final CharSequence[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmUID<CsmDeclaration> referencedDeclarationUID = null;
    private WeakReference<CsmDeclaration> refDeclaration;
    private int lastParseCount = -1;
    private final CsmUID<CsmScope> scopeUID;
    private final CsmVisibility visibility;
    
    public UsingDeclarationImpl(AST ast, CsmFile file, CsmScope scope, boolean global, CsmVisibility visibility) {
        super(ast, file);
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
        name = NameCache.getManager().getString(ast.getText());
        rawName = AstUtil.getRawNameInChildren(ast);
        if (!global) {
            Utils.setSelfUID(this);
        }
        this.visibility = visibility;
        // TODO: here we override startOffset which is not good because startPosition is now wrong
        AST child = ast.getFirstChild();
        if(child instanceof CsmAST) {
            startOffset = ((CsmAST)child).getOffset();
        } else {
            startOffset = getStartOffset();
        }
    }

    private CsmDeclaration renderReferencedDeclaration(Resolver resolver) {
        CsmDeclaration referencedDeclaration = null;
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
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(lastName, true, true, false);

                // we should try searching not only in namespace resolved found,
                // but in numspaces with the same name in required projects
                // iz #140787 cout, endl unresolved in some Loki files
                Collection<CsmNamespace> namespacesToSearch = new LinkedHashSet<CsmNamespace>();
                namespacesToSearch.add(namespace);
                CharSequence nspQName = namespace.getQualifiedName();
                final Collection<CsmProject> libraries;
                if (resolver != null) {
                    libraries = resolver.getLibraries();
                } else {
                    libraries = Resolver3.getSearchLibraries(prjBase);
                }
                for (CsmProject lib : libraries) {
                    CsmNamespace libNs = lib.findNamespace(nspQName);
                    if (libNs != null) {
                        namespacesToSearch.add(libNs);
                    }
                }

                outer:
                for (CsmNamespace curr : namespacesToSearch) {
                    Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(curr, filter);
                    while (it.hasNext()) {
                        CsmDeclaration elem = it.next();
                        if (CharSequences.comparator().compare(lastName,elem.getName())==0) {
                            if (!CsmKindUtilities.isExternVariable(elem)) {
                                referencedDeclaration = elem;
                                break outer;
                            } else {
                                bestChoice = elem;
                            }
                        }
                    }
                }

                // search for enumerators
                if (referencedDeclaration == null && bestChoice == null) {
                    CsmFilter filter2 = CsmSelect.getFilterBuilder().createKindFilter(new Kind[]{Kind.ENUM});
                    outer2:
                    for (CsmNamespace curr : namespacesToSearch) {
                        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(curr, filter2);
                        while (it.hasNext()) {
                            CsmDeclaration elem = it.next();
                            if (CsmKindUtilities.isEnum(elem)) {
                                CsmEnum e = (CsmEnum) elem;
                                for (CsmEnumerator enumerator : e.getEnumerators()) {
                                    if(lastName.toString().equals(enumerator.getName().toString())) {
                                        referencedDeclaration = enumerator;
                                        break outer2;
                                    }
                                }
                            }
                        }
                    }
                }

                referencedDeclaration = referencedDeclaration == null ? bestChoice : referencedDeclaration;
            }
            CsmClass cls = null;
            if(namespace == null && rawName.length > 1) {
                CharSequence[] partial = new CharSequence[rawName.length - 1];
                System.arraycopy(rawName, 0, partial, 0, rawName.length - 1);
                CsmObject result = ResolverFactory.createResolver(getContainingFile(), startOffset, resolver).resolve(partial, Resolver.CLASSIFIER);
                if (CsmKindUtilities.isClass(result)) {
                    cls = (CsmClass)result;
                }
            }
            if(cls != null && rawName.length > 0) {
                CharSequence lastName = rawName[rawName.length - 1];
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(lastName, true, true, false);
                Iterator<CsmMember> it = CsmSelect.getClassMembers(cls, filter);
                if (it.hasNext()) {
                    CsmMember member = it.next();
                    referencedDeclaration = member;
                }
            }

        }
        return referencedDeclaration;
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
            int newParseCount = FileImpl.getParseCount();
            if (lastParseCount != newParseCount) {
                _setReferencedDeclaration(null);
                referencedDeclaration = renderReferencedDeclaration(resolver);
                synchronized (this) {
                    lastParseCount = newParseCount;
                    _setReferencedDeclaration(referencedDeclaration);
                }
            }            
        }
        return referencedDeclaration;
    }
    
    private CsmDeclaration _getReferencedDeclaration() {
        CsmDeclaration referencedDeclaration = null;
        WeakReference<CsmDeclaration> aRefDeclaration = refDeclaration;
        if (aRefDeclaration != null) {
            referencedDeclaration =((Reference<CsmDeclaration>)aRefDeclaration).get();
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

    public CsmClass getContainingClass() {
        CsmScope scope = getScope();
        if(CsmKindUtilities.isClass(scope)) {
            return (CsmClass) scope;
        }
        return null;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public boolean isStatic() {
        return false;
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
        PersistentUtils.writeUTF(name, output);
        output.writeInt(this.startOffset);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedDeclarationUID, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);

        PersistentUtils.writeVisibility(this.visibility, output);
    }
    
    public UsingDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.startOffset = input.readInt();
        this.rawName = PersistentUtils.readStrings(input, NameCache.getManager());
        
        // read cached declaration
        this.referencedDeclarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);

        this.visibility = PersistentUtils.readVisibility(input);
    }
}
