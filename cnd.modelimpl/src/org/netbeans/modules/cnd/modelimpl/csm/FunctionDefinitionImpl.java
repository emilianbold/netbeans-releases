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
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl extends FunctionImpl implements CsmFunctionDefinition {

    private CsmFunction declaration;
    private String name;
    private CsmCompoundStatement body;
    private List/*<CsmParameter>*/  parameters;
    
    public FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    public CsmCompoundStatement getBody() {
        if( body == null ) {
            body = AstRenderer.findCompoundStatement(getAst(), getContainingFile());
        }
        return body;
    }

    public CsmFunction getDeclaration() {
        if( declaration == null ) {
            String[] cnn = getClassOrNspNames();
            if( cnn != null ) {
                CsmObject o = ResolverFactory.createResolver(this).resolve(cnn);
                if( o instanceof CsmClass ) {
                    for( Iterator iter = ((CsmClass) o).getMembers().iterator(); iter.hasNext(); ) {
                        CsmMember mem = (CsmMember) iter.next();
                        if( mem.getKind() == CsmDeclaration.Kind.FUNCTION ) {
                            if( mem.getName().equals(getName()) ) {
				String sign = ((CsmFunction) mem).getSignature();
				if( sign != null && sign.equals(getSignature()) ) {
				    return (CsmFunction) mem;
				}
                            }
                        }
                    }
                }
                else if( o instanceof CsmNamespace ) {
                    
                }
                //CsmDeclaration decl = 
            }
            // TODO: implement searching for declaration somewhere in .h ???
            //else {             
            //}
        }
        return declaration;
    }    
    
    private String[] getClassOrNspNames() {
        AST qid = getQialifiedId();
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List/*<String>*/ l = new ArrayList/*<String>*/();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.ID ) {
                    if( token.getNextSibling() != null ) {
                        l.add(token.getText());
                    }
                }
            }
            return (String[]) l.toArray(new String[l.size()]);
        }
        return null;
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }
    
    public String getQualifiedName() {
        CsmFunction decl = getDeclaration();
        return decl == null ? "<unknown>" : decl.getQualifiedName();
    }

    public String getName() {
        if( name == null ) {
            AST qid = getQialifiedId();
            if( qid != null ) {
                for( AST n = qid.getFirstChild(); n != null; n = n.getNextSibling() ) {
                    name = n.getText();
                }
            }
        }
        return name;
    }
    
    public AST getQialifiedId() {
        for( AST t = getAst().getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                return t;
            }
        }
        return null;
    }

    public CsmScope getScope() {
        return getContainingFile();
    }

//    public List/*<CsmParameter>*/  getParameters() {
//        if( parameters == null ) {
//            AST ast = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_PARMLIST);
//            parameters = AstRenderer.renderParameters(ast, getContainingFile());
//        }
//        return parameters;
//    }

    public List getScopeElements() {
        List l = new ArrayList();
        l.addAll(getParameters());
        l.add(getBody());
        return l;
    }

    public CsmFunctionDefinition getDefinition() {
        return this;
    }
    
    
}

