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

import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * CsmInheritance implementation
 * @author Vladimir Kvashin
 */
public class InheritanceImpl extends OffsetableBase implements CsmInheritance {

    private CsmVisibility visibility;
    private boolean virtual;
    private CsmClass ancestorCache;
    private String ancestorName;
    
    public InheritanceImpl(AST ast, CsmFile file) {
        super(ast, file);
        render(ast);
    }

    public boolean isVirtual() {
        return virtual;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public CsmClass getCsmClass() {
        if (ancestorCache == null || !ancestorCache.isValid())
        {
            ancestorCache = (CsmClass)getContainingFile().getProject().findClassifier(ancestorName);
        }
        return ancestorCache;
    }
    
    private void render(AST node) {
        visibility = CsmVisibility.PRIVATE;
        for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_private:
                    visibility = CsmVisibility.PRIVATE;
                    break;
                case CPPTokenTypes.LITERAL_public:
                    visibility = CsmVisibility.PUBLIC;
                    break;
                case CPPTokenTypes.LITERAL_protected:
                    visibility = CsmVisibility.PROTECTED;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    virtual = true;
                    break;
                case CPPTokenTypes.ID:
                    List l = new ArrayList();
                    for( ; token != null; token = token.getNextSibling() ) {
                        switch( token.getType() ) {
                            case CPPTokenTypes.ID:
                                l.add(token.getText());
                                break;
                            case CPPTokenTypes.SCOPE:
                                break;
                            default:
                                // here can be "<", ">" and other template stuff
                        }
                    }
                    //CsmObject o = ResolverFactory.createResolver(this).resolve(new String[] { token.getText() } );
                    CsmObject o = ResolverFactory.createResolver(this).resolve((String[]) l.toArray(new String[l.size()]));
                    if( o instanceof CsmClass ) {
                        ancestorCache = (CsmClass)o;
                        ancestorName = ancestorCache.getQualifiedName();
                    }
                    else 
                    {
                        if (Diagnostic.DEBUG && !(o instanceof CsmNamespace))
                            System.out.println( "Unknown token instead of Namespace/Class: " + token.getText());
                    }
                    return; // it's definitely the last!; besides otherwise we get NPE in for 
                    //break;
            }
        }
    }
}
