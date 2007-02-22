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

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmUsingDirective
 * @author Vladimir Kvasihn
 */
public class UsingDirectiveImpl extends OffsetableDeclarationBase<CsmUsingDirective> implements CsmUsingDirective, RawNamable {

    private final String name;
    private final int startOffset;
    private final String[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmNamespace referencedNamespaceOLD = null;
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;
    
    public UsingDirectiveImpl(AST ast, CsmFile file) {
        super(ast, file);
        // TODO: here we override startOffset which is not good because startPosition is now wrong
        startOffset = ((CsmAST)ast.getFirstChild()).getOffset();
        rawName = AstUtil.getRawNameInChildren(ast);
        
        name = ast.getText();
    }
    
    public CsmNamespace getReferencedNamespace() {
        // TODO: process preceding aliases
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver2"))
        if (ResolverFactory.resolver != 2)
            return ((ProjectBase) getContainingFile().getProject()).findNamespace(name, true);
        else {
            CsmNamespace referencedNamespace = _getReferencedNamespace();
            if (referencedNamespace == null) {
                _setReferencedNamespace(null);
                CsmObject result = ResolverFactory.createResolver(
                        getContainingFile(),
                        startOffset).
                        resolve(name);
                if (result != null && result instanceof CsmNamespaceDefinition) {
                    referencedNamespace = ((CsmNamespaceDefinition)result).getNamespace();
                    _setReferencedNamespace(referencedNamespace);
                }
            }
            return referencedNamespace;
        }
    }
    
    private CsmNamespace _getReferencedNamespace() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmNamespace referencedNamespace = UIDCsmConverter.UIDtoNamespace(referencedNamespaceUID);
            assert referencedNamespace != null || referencedNamespaceUID == null;
            return referencedNamespace;
        } else {
            return this.referencedNamespaceOLD;
        }
    }    

    private void _setReferencedNamespace(CsmNamespace referencedNamespace) {
        if (TraceFlags.USE_REPOSITORY) {
            this.referencedNamespaceUID = UIDCsmConverter.namespaceToUID(referencedNamespace);
            assert this.referencedNamespaceUID != null || referencedNamespace == null;
        } else {
            this.referencedNamespaceOLD = referencedNamespace;
        }
    }
    
    public int getStartOffset() {
        return startOffset;
    }
 
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.USING_DIRECTIVE;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return getName();
    }
    
    public String[] getRawName() {
        return rawName;
    }
    
    public String toString() {
        return "" + getKind() + ' ' + name /*+ " rawName=" + Utils.toString(getRawName())*/; // NOI18N
    }
    
    public CsmScope getScope() {
        //TODO: implement!
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.writeUTF(this.name);
        output.writeInt(this.startOffset);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
    }
    
    public UsingDirectiveImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        this.startOffset = input.readInt();
        this.rawName = PersistentUtils.readStrings(input, TextCache.getManager());
        
        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }  
    
}
