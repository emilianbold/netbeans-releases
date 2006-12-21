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
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * CsmFunction + CsmMember implementation
 * @author Vladimir Kvashin
 */
public class MethodImpl extends FunctionImpl implements CsmMethod {

    //private ClassImpl containingClass;
    private CsmVisibility visibility;
    private boolean _static = false;
    private boolean _abstract = false;
    
//    public MethodImpl(ClassImpl cls, CsmVisibility visibility, String name, int start, int end) {
//        super(name, cls.getContainingFile(), start, end);
//        init(cls, visibility);
//    }

    public MethodImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        super(ast, cls.getContainingFile(), cls);
        init(cls, visibility);
        //this(cls, visibility, AstUtil.findId(ast), 0, 0);
        //setAst(ast);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                setStatic(true);                    
                break;
            }
        }
    }
    
//    /** overrides FunctionImpl.registerInProject */
//    protected void registerInProject() {
//        // do NOT register in project - only in class!
//        //super.registerInProject();
//    }
    
    private void init(ClassImpl cls, CsmVisibility visibility) {
        //this.containingClass = cls;
        this.visibility = visibility;
    }

    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
        //return containingClass;
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

    // getScope is defined in parent class
//    public CsmScope getScope() {
//        return getContainingClass();
//    }

    public boolean isConst() {
	return super.isConst();
    }
}

