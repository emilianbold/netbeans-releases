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

import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Vladimir Kvasihn
 */
public class FriendClassImpl extends OffsetableDeclarationBase<CsmFriendClass> implements CsmFriendClass {
    private final String name;
    private final String[] nameParts;
    private final CsmUID<CsmClass> parentUID;
    
    public FriendClassImpl(AST ast, FileImpl file, CsmClass parent) {
        super(ast, file);
        this.parentUID = parent.getUID();
        AST qid = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        name = (qid == null) ? "" : AstRenderer.getQualifiedName(qid);
        nameParts = initNameParts(qid);
        registerInProject();
    }

    private void registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).registerDeclaration(this);
        }
    }

    public CsmClass getContainingClass() {
        return parentUID.getObject();
    }

    public CsmScope getScope() {
        return getContainingClass();
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        CsmClass cls = getContainingClass();
        String clsQName = cls.getQualifiedName();
	if( clsQName != null && clsQName.length() > 0 ) {
            return clsQName + "::" + getQualifiedNamePostfix(); // NOI18N
	}
        return getName();
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION;
    }

    public CsmClass getReferencedClass() {
        CsmObject o = resolve();
        return (o instanceof CsmClass) ? (CsmClass) o : (CsmClass) null;
    }
    
    private String[] initNameParts(AST qid) {
        if( qid != null ) {
            return AstRenderer.getNameTokens(qid);
        }
        return new String[0];
    }
    
    private CsmObject resolve() {
        Resolver resolver = ResolverFactory.createResolver(this);
        return resolver.resolve(nameParts);
    }

    public void dispose() {
        super.dispose();
	unregisterInProject();
    }

    private void unregisterInProject() {
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        PersistentUtils.writeStrings(this.nameParts, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.parentUID, output);    
    }


    public FriendClassImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, TextCache.getManager());
        this.parentUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
