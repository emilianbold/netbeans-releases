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
    
    // only one of namespaceOLD/namespaceUID must be used (based on USE_REPOSITORY)
    private final NamespaceImpl namespaceOLD;
    private final CsmUID<CsmNamespace> namespaceUID;
    
    public NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        super(ast, file);
        assert ast.getType() == CPPTokenTypes.CSM_NAMESPACE_DECLARATION;
        name = ast.getText();
        NamespaceImpl nsImpl = ((ProjectBase) file.getProject()).findNamespace(parent, name, true);
        
        // set parent ns, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY) {
            namespaceUID = UIDCsmConverter.namespaceToUID(nsImpl);
            assert namespaceUID != null;
            this.namespaceOLD = null;
        } else {
            this.namespaceOLD = nsImpl;
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
            List<CsmDeclaration> decls = UIDCsmConverter.UIDsToDeclarations(declarations);
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
            if (true) RepositoryUtils.remove(uid);
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
    }

    private NamespaceImpl _getNamespaceImpl() {
        if (TraceFlags.USE_REPOSITORY) {
            NamespaceImpl impl = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(namespaceUID);
            assert impl != null;
            return impl;
        } else {
            return namespaceOLD;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);  
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output);
        factory.writeUID(this.namespaceUID, output);
        assert this.name != null;
        output.writeUTF(this.name);
    }  
    
    public NamespaceDefinitionImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = (List<CsmUID<CsmDeclaration>>) factory.readUIDCollection(Collections.synchronizedList(new ArrayList<CsmUID<CsmDeclaration>>()), input);
        this.namespaceUID = factory.readUID(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        
        assert TraceFlags.USE_REPOSITORY;
        this.declarationsOLD = null;
        this.namespaceOLD = null;
    }      
}
