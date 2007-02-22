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
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * @author Vladimir Kvasihn
 */
public final class DestructorDefinitionImpl extends FunctionDefinitionImpl {
    public DestructorDefinitionImpl(AST ast, CsmFile file) {
        super(ast, file, null);
    }

    public CsmType getReturnType() {
        return NoType.instance();
    }
    
    protected String initName(AST node) {
        AST token = node.getFirstChild();
        if( token != null && token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            token = token.getNextSibling();
            if( token != null && token.getType() == CPPTokenTypes.TILDE ) {
                token = token.getNextSibling();
                if( token != null && token.getType() == CPPTokenTypes.ID ) {
                    return "~" + token.getText(); // NOI18N
                }
            }
        }
        return "~"; // NOI18N
    }
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }
    
    public DestructorDefinitionImpl(DataInput input) throws IOException {
        super(input);
    }     
}
