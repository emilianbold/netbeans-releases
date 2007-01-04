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
import org.netbeans.modules.cnd.api.model.*;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Implements CsmNamespaceDefinition
 * @author Vladimir Kvasihn
 */
public class NamespaceDefinitionImpl extends OffsetableDeclarationBase
    implements CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable {

    private List declarations = Collections.synchronizedList(new ArrayList());
    private String name;
    private NamespaceImpl namespace;
    
    public NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        super(ast, file);
        assert ast.getType() == CPPTokenTypes.CSM_NAMESPACE_DECLARATION;
        name = ast.getText();
        namespace = ((ProjectBase) file.getProject()).findNamespace(parent, name, true);
        if( namespace instanceof NamespaceImpl ) {
            ((NamespaceImpl) namespace).addNamespaceDefinition(this);
        }
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_DEFINITION;
    }
            
    public List getDeclarations() {
        return new ArrayList(declarations);
    }
    
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        declarations.add(decl);
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        declarations.remove(declaration);
    }

    public String getQualifiedName() {
        return getNamespace().getQualifiedName();
    }
    
//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }

    public CsmNamespace getNamespace() {
        return namespace;
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
        declarations.clear();    
    }
    
    
}
