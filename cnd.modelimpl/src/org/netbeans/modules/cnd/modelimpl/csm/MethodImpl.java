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
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * CsmFunction + CsmMember implementation
 * @author Vladimir Kvashin
 */
public class MethodImpl<T> extends FunctionImpl<T> implements CsmMethod<T> {

    private final CsmVisibility visibility;
    private boolean _static = false;
    private boolean _abstract = false;

    public MethodImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        this(ast, cls, visibility, true);
    }
    
    protected MethodImpl(AST ast, ClassImpl cls, CsmVisibility visibility, boolean register) {
        super(ast, cls.getContainingFile(), cls, false);
        this.visibility = visibility;
        //this(cls, visibility, AstUtil.findId(ast), 0, 0);
        //setAst(ast);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                setStatic(true);                    
                break;
            }
        }
        if (register) {
            registerInProject();
        }
    }

    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public boolean isStatic() {
        return _static;
    }
    
    public boolean isAbstract() {
        return _abstract;
    }
    
    public void setAbstract(boolean _abstract) {
        this._abstract = _abstract;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static;
    }
    
    public boolean isExplicit() {
        //TODO: implement!!
        return false;
    }
    
    public boolean isVirtual() {
        //TODO: implement!
        return false;
    }

    public boolean isConst() {
	return super.isConst();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
        output.writeBoolean(this._abstract);
        output.writeBoolean(this._static);
    }
    
    public MethodImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
        this._abstract = input.readBoolean();
        this._static = input.readBoolean();
    }      
}

