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
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * CsmVariable + CsmMember implementation
 * @author Vladimir Kvashin
 */
public final class FieldImpl extends VariableImpl<CsmField> implements CsmField {

    private final CsmVisibility visibility;
    
    public FieldImpl(AST ast, CsmFile file, CsmType type, String name, ClassImpl cls, CsmVisibility visibility) {
        super(ast, file, type, name, /*cls*/null, false);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    setStatic(true);
                    break;
            }
        }
        this.visibility = visibility;
	setScope(cls);
    }
    
    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);      
        PersistentUtils.writeVisibility(this.visibility, output);
    }  
    
    public FieldImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
    }     
}
