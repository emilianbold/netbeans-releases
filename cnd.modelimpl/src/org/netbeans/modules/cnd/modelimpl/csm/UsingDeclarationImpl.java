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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmUsingDeclaration
 * @author Vladimir Kvasihn
 */
public class UsingDeclarationImpl extends OffsetableDeclarationBase<CsmUsingDeclaration> implements CsmUsingDeclaration, RawNamable {

    private final String name;
    private final int startOffset;
    private final String[] rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmDeclaration referencedDeclarationOLD = null;
    private CsmUID<CsmDeclaration> referencedDeclarationUID = null;
    
    public UsingDeclarationImpl(AST ast, CsmFile file) {
        super(ast, file);
        name = ast.getText();
        // TODO: here we override startOffset which is not good because startPosition is now wrong
        startOffset = ((CsmAST)ast.getFirstChild()).getOffset();
        rawName = AstUtil.getRawNameInChildren(ast);
    }
    
    public CsmDeclaration getReferencedDeclaration() {
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
                String[] partial = new String[rawName.length - 1];
                System.arraycopy(rawName, 0, partial, 0, rawName.length - 1);
                CsmObject result = ResolverFactory.createResolver(getContainingFile(), startOffset).resolve(partial);
                if (CsmKindUtilities.isNamespace(result)) {
                    String lastName = rawName[rawName.length - 1];
                    CsmDeclaration bestChoice = null;
                    for (CsmDeclaration elem : ((CsmNamespace)result).getDeclarations()) {
                        if (lastName.equals(elem.getName())) {
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
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            CsmDeclaration referencedDeclaration = UIDCsmConverter.UIDtoDeclaration(referencedDeclarationUID);
            // can be null if namespace was removed 
            return referencedDeclaration;
        } else {
            return this.referencedDeclarationOLD;
        }
    }    

    private void _setReferencedDeclaration(CsmDeclaration referencedDeclaration) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.referencedDeclarationUID = UIDCsmConverter.declarationToUID(referencedDeclaration);
            assert this.referencedDeclarationUID != null || referencedDeclaration == null;
        } else {
            this.referencedDeclarationOLD = referencedDeclaration;
        }
    }
    
    public int getStartOffset() {
        return startOffset;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.USING_DECLARATION;
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
    
    public CsmScope getScope() {
        //TODO: implement!
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        output.writeInt(this.startOffset);
        PersistentUtils.writeStrings(this.rawName, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedDeclarationUID, output);
    }
    
    public UsingDeclarationImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.startOffset = input.readInt();
        this.rawName = PersistentUtils.readStrings(input, TextCache.getManager());
        
        // read cached declaration
        this.referencedDeclarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }      
}
