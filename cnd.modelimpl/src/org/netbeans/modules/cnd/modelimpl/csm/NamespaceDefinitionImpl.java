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
    private final NamespaceImpl namespaceRef;
    private final CsmUID<CsmNamespace> namespaceUID;
    
    public NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        super(ast, file);
        assert ast.getType() == CPPTokenTypes.CSM_NAMESPACE_DECLARATION;
        name = ast.getText();
        NamespaceImpl nsImpl = ((ProjectBase) file.getProject()).findNamespaceCreateIfNeeded(parent, name);
        
        // set parent ns, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
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
            return new ArrayList(declarationsOLD);
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
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        for( Iterator iter = getDeclarations().iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if( o  instanceof Disposable ) {
                ((Disposable) o).dispose();
            }
        }
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmUID<CsmDeclaration>> uids = new ArrayList(declarations);
            RepositoryUtils.remove(uids);
            declarations.clear();            
        } else {
            declarationsOLD.clear();
        }  
        NamespaceImpl ns = _getNamespaceImpl();
        assert ns != null;
        ns.removeNamespaceDefinition(this);
    }

    private NamespaceImpl _getNamespaceImpl() {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            NamespaceImpl impl = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(namespaceUID);
            assert impl != null || namespaceUID == null : "null object for UID " + namespaceUID;
            return impl;
        } else {
            return namespaceRef;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);  
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output, true);
        
        CsmUID<CsmNamespace> writeNamespaceUID;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            writeNamespaceUID = this.namespaceUID;
        } else {
            // save reference
            assert this.namespaceRef != null;
            writeNamespaceUID = UIDCsmConverter.namespaceToUID(this.namespaceRef);        
        }
        // not null
        assert writeNamespaceUID != null;
        factory.writeUID(writeNamespaceUID, output);
        
        assert this.name != null;
        output.writeUTF(this.name);
    }  
    
    public NamespaceDefinitionImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = factory.readUIDCollection(Collections.synchronizedList(new ArrayList<CsmUID<CsmDeclaration>>()), input);
        
        CsmUID<CsmNamespace> readParentUID = factory.readUID(input);
        // not null UID
        assert readParentUID != null;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            this.namespaceUID = readParentUID;
            
            this.namespaceRef = null;
        } else {
            // restore reference
            this.namespaceRef = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(readParentUID);
            assert this.namespaceRef != null || readParentUID == null : "no object for UID " + readParentUID;
            
            this.namespaceUID = null;
        }    
        
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        
        assert TraceFlags.USE_REPOSITORY;
        this.declarationsOLD = null;
    }      
}
