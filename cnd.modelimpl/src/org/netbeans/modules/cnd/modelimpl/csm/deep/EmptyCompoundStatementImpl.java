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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;

/**
 * empty compound statement. Used for incorrect/uncompleted code 
 * to present i.e. functions body
 * @author Vladimir Voskresensky
 */
public class EmptyCompoundStatementImpl extends StatementBase implements CsmCompoundStatement {
    
    public EmptyCompoundStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        ast.setFirstChild(null);
    }
    
    public List<CsmStatement> getStatements() {
        return Collections.<CsmStatement>emptyList();
    }
    
    public CsmStatement.Kind getKind() {
        return Kind.COMPOUND;
    }
    
    public List<CsmScopeElement> getScopeElements() {
        return Collections.<CsmScopeElement>emptyList();
    }
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }
    
    public EmptyCompoundStatementImpl(DataInput input) throws IOException {
        super(input);
    }     
}
