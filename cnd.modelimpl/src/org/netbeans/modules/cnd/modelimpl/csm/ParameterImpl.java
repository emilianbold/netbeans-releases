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

/**
 * Implements CsmParameter
 * @author Vladimir Kvashin
 */
public final class ParameterImpl extends VariableImpl<CsmParameter> implements CsmParameter {
    private final boolean varArg;

    public ParameterImpl(AST ast, CsmFile file, CsmType type, String name, CsmScope scope) {
        super(ast, file, type, name, scope, false);
        varArg = ast.getType() == CPPTokenTypes.ELLIPSIS;
    }

    public boolean isVarArgs() {
        return varArg;
    }
    
    public String getDisplayText() {
	return isVarArgs() ? "..." : super.getDisplayText(); //NOI18N
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);      
        output.writeBoolean(this.varArg);
        // write UID for unnamed parameter
        if (getName().length() == 0) {
            super.writeUID(output);
        }
    }  
    
    public ParameterImpl(DataInput input) throws IOException {
        super(input);
        this.varArg = input.readBoolean();
        // restore UID for unnamed parameter
        if (getName().length() == 0) {
            super.readUID(input);
        }        
    } 
}
