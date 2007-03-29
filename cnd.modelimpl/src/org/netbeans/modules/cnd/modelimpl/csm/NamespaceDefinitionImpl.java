/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import java.util.*;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmNamespaceDefinition
 * @author Vladimir Kvasihn
 */
public final class NamespaceDefinitionImpl extends OffsetableDeclarationBase<CsmNamespaceDefinition>
    implements CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable {

    private List declarationsOLD = Collections.synchronizedList(new ArrayList());
    private List<CsmUID<CsmDeclaration>> declarations = Collections.synchronizedList(new ArrayList<CsmUID<CsmDeclaration>>());
    
    private final String name;
    
    // only one of namespaceRef/namespaceUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private /*final*/ NamespaceImpl namespaceRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmNamespace> namespaceUID;
    
    public NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        super(ast, file);
        assert ast.getType() == CPPTokenTypes.CSM_NAMESPACE_DECLARATION;
        name = ast.getText();
        NamespaceImpl nsImpl = ((ProjectBase) file.getProject()).findNamespaceCreateIfNeeded(parent, name);
        
        // set parent ns, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            namespaceUID = UIDCsmConverter.namespaceToUID(nsImpl);
            assert namespaceUID != null;
            this.namespaceRef = null;
        } else {
            this.namespaceRef = nsImpl;
            this.namespaceUID = null;
        }
        
        if( nsImpl instanceof NamespaceImpl ) {
            nsImpl.addNamespaceDefinition(this);
        }
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_DEFINITION;
    }
            
    public List/*<CsmDeclaration>*/ getDeclarations() {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmDeclaration> decls;
            synchronized (declarations) {
                decls = UIDCsmConverter.UIDsToDeclarations(declarations);
            }
            return decls;
        } else {
            synchronized (declarationsOLD) {
                return new ArrayList(declarationsOLD);
            }
        }
    }
    
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.USE_REPOSITORY) {        
            CsmUID<CsmDeclaration> uid = RepositoryUtils.put(decl);
            assert uid != null;
            declarations.add(uid);
            // update repository
            RepositoryUtils.put(this);
        } else {
            declarationsOLD.add(decl);
        }
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(declaration);
            assert uid != null;
            boolean res = declarations.remove(uid);
            assert res;
            RepositoryUtils.remove(uid);
            // update repository
            RepositoryUtils.put(this);
        } else {
            declarationsOLD.remove(declaration);
        }
    }

    public String getQualifiedName() {
        return getNamespace().getQualifiedName();
    }
    
//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }

    public CsmNamespace getNamespace() {
        return _getNamespaceImpl();
    }

    public String getName() {
        return name;
    }

    public CsmScope getScope() {
        return getContainingFile();
    }

    public void dispose() {
        super.dispose();
        onDispose();
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        if (TraceFlags.USE_REPOSITORY) {
            List/*<CsmDeclaration>*/ decls;
            List<CsmUID<CsmDeclaration>> uids;
            synchronized (declarations) {
                decls = getDeclarations();
                uids = declarations;
                declarations  = Collections.synchronizedList(new ArrayList<CsmUID<CsmDeclaration>>());
            }
            Utils.disposeAll(decls);            
            RepositoryUtils.remove(uids);                      
        } else {
            List/*<CsmDeclaration>*/ decls;
            synchronized (declarationsOLD) {
                decls = getDeclarations();
                declarationsOLD  = Collections.synchronizedList(new ArrayList());
            }
            Utils.disposeAll(decls);
        }  
        NamespaceImpl ns = _getNamespaceImpl();
        assert ns != null;
        ns.removeNamespaceDefinition(this);
    }

    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.namespaceRef = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            assert this.namespaceRef != null || this.namespaceUID == null : "no object for UID " + this.namespaceUID;
        }
    }
    
    private NamespaceImpl _getNamespaceImpl() {
        NamespaceImpl impl = this.namespaceRef;
        if (impl == null) {
            if (TraceFlags.USE_REPOSITORY) {
                impl = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
                assert impl != null || this.namespaceUID == null : "null object for UID " + this.namespaceUID;
            }
        }
        return impl;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);  
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output, true);
        
        // not null
        assert this.namespaceUID != null;
        factory.writeUID(this.namespaceUID, output);
        
        assert this.name != null;
        output.writeUTF(this.name);
    }  
    
    public NamespaceDefinitionImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = factory.readUIDCollection(Collections.synchronizedList(new ArrayList<CsmUID<CsmDeclaration>>()), input);
        
        this.namespaceUID = factory.readUID(input);
        // not null UID
        assert this.namespaceUID != null;
        this.namespaceRef = null;    
        
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        
        assert TraceFlags.USE_REPOSITORY;
        this.declarationsOLD = null;
    }      
}
