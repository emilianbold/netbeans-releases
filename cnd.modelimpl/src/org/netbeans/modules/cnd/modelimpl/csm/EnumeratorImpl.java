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

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * CsmEnumerator implementation
 * @author Vladimir Kvashin
 */
public final class EnumeratorImpl extends OffsetableDeclarationBase<CsmEnumerator> implements CsmEnumerator {
    private final String name;
    
    // only one of enumerationRef/enumerationUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)    
    private final CsmEnum enumerationRef;    
    private final CsmUID<CsmEnum> enumerationUID;

    public EnumeratorImpl(AST ast, EnumImpl enumeration) {
        super(ast, enumeration.getContainingFile());
        this.name = ast.getText();
        
        // set parent enum, do it in constructor to have final fields
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
            this.enumerationRef = null;
        } else {
            this.enumerationRef = enumeration;
            this.enumerationUID = null;
        }
        
        enumeration.addEnumerator(this);
    }
    
    public String getName() {
        return name;
    }

    public CsmExpression getExplicitValue() {
        return null;
    }

    public CsmEnum getEnumeration() {
        return _getEnumeration();
    }
    
    public CsmScope getScope() {
        return getEnumeration();
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUMERATOR;
    }

    public String getQualifiedName() {
	    return _getEnumeration().getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N    
    }

    private CsmEnum _getEnumeration() {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            CsmEnum enumaration = UIDCsmConverter.UIDtoDeclaration(enumerationUID);
            assert (enumaration != null || enumerationUID == null) : "null object for UID " + enumerationUID;
            return enumaration;             
        } else {
            return enumerationRef;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        CsmUID<CsmEnum> writeEnumerationUID;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            writeEnumerationUID = this.enumerationUID;
        } else {
            // save reference
            assert this.enumerationRef != null;
            writeEnumerationUID = UIDCsmConverter.declarationToUID(this.enumerationRef);
        }        
        // not null UID
        assert writeEnumerationUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(writeEnumerationUID, output);
    }
    
    public EnumeratorImpl(DataInput input) throws IOException {
        super(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        CsmUID<CsmEnum> readEnumerationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert readEnumerationUID != null;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            this.enumerationUID = readEnumerationUID;
            
            this.enumerationRef = null;
        } else {
            // restore reference
            this.enumerationRef = UIDCsmConverter.UIDtoDeclaration(readEnumerationUID);
            assert this.enumerationRef != null || readEnumerationUID == null : "no object for UID " + readEnumerationUID;
            
            this.enumerationUID = null;
        }
        
        assert TraceFlags.USE_REPOSITORY;
    }
}
