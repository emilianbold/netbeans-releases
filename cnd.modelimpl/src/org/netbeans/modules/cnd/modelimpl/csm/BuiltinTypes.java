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

import java.io.DataOutput;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.ObjectBasedUID;

/**
 * Implementation for built-in types
 * @author Vladimir Kvasihn
 */
public class BuiltinTypes {

    private static class BuiltinImpl implements CsmBuiltIn {

        private final String name;
        private final CsmUID<CsmBuiltIn> uid;
        
        private BuiltinImpl(String name) {
            this.name = TextCache.getString(name);
            if (TraceFlags.USE_REPOSITORY) {
                this.uid = new BuiltInUID(this);
            } else {
                this.uid = null;
            }
        }
        
        public String getQualifiedName() {
            return getName();
        }

        public String getUniqueName() {
            return getKind().toString() + OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR +  getQualifiedName();
        }
        
        public String getName() {
            assert name != null && name.length() > 0;
            return name;
        }

        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.BUILT_IN;
        }

        public CsmScope getScope() {
            // TODO: builtins shouldn't be declarations! snd thus shouldn't be ScopeElements!
            return null;
        }
        
        public CsmUID<CsmBuiltIn> getUID() {
            return uid;
        }
        
        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }            
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            BuiltinImpl other = (BuiltinImpl)obj;
            return name.equals(other.name);
        }
    }
    
    private static Map<String, CsmBuiltIn> types = new HashMap();
    
    public static CsmBuiltIn getBuiltIn(AST ast) {
        assert ast.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN;
        StringBuffer sb = new StringBuffer();
        // TODO: take synonims into account!!!
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(token.getText());
        }
        return getBuiltIn(sb.toString());
    }
    
    public static CsmBuiltIn getBuiltIn(String text) {
        CsmBuiltIn builtIn = (CsmBuiltIn) types.get(text);
        if( builtIn == null ) {
            builtIn = new BuiltinImpl(text);
            types.put(TextCache.getString(text), builtIn);
        }
        return builtIn;
    }

    public static ObjectBasedUID readUID(DataInput aStream) throws IOException {
        String name = aStream.readUTF(); // no need for text manager
        CsmBuiltIn builtIn = BuiltinTypes.getBuiltIn(name);
        BuiltInUID anUID = (BuiltInUID) builtIn.getUID();
        assert anUID != null;
        return anUID;
    }

    /**
     * UID for CsmBuiltIn
     */    
    public static final class BuiltInUID extends ObjectBasedUID<CsmBuiltIn> {
        private BuiltInUID(CsmBuiltIn decl) {
            super(decl);
        }
        
        public String toString() {
            String retValue = "<BUILT-IN UID> " + super.toString(); // NOI18N
            return retValue;
        } 

        public void write(DataOutput output) throws IOException {
            BuiltinImpl ref = (BuiltinImpl) getObject();
            assert ref != null;
            output.writeUTF(ref.getName());
        }
    }     
}
