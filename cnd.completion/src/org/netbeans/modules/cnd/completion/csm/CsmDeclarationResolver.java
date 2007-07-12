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

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFunction;

/**
 *
 * @author vv159170
 */
public class CsmDeclarationResolver {
    
    /** Creates a new instance of CsmDeclarationResolver */
    private CsmDeclarationResolver() {
    }

    // ==================== help static methods ================================
    
    public static CsmDeclaration findDeclaration(CsmObject obj) {
        if (obj == null) {
            return null;
        }
        CsmClassifier clazz = null;
        if (CsmKindUtilities.isVariable(obj)) {
            CsmVariable var = (CsmVariable)obj;
            // pass for further handling as type object
            obj = var.getType();
        }
        if (CsmKindUtilities.isType(obj)) { 
//            clazz = ((CsmType)obj).getClassifier();
        } else if (CsmKindUtilities.isClassForwardDeclaration(obj)) {
            clazz = ((CsmClassForwardDeclaration)obj).getCsmClass();
        } else if (CsmKindUtilities.isClass(obj)) {
            clazz = (CsmClassifier)obj;
        } else if (CsmKindUtilities.isInheritance(obj)) {
            clazz = ((CsmInheritance)obj).getCsmClassifier();
        }
        
        return clazz;
    }  
    
    public static CsmDeclaration findTopFileDeclaration(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findTopFileDeclaration";
        List/*<CsmDeclaration>*/ decls = file.getDeclarations();
        for (Iterator it = decls.iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            assert (decl != null) : "can't be null declaration";
            if (CsmOffsetUtilities.isInObject(decl, offset)) {
                // we are inside declaration
                return decl;
            }
        }
        return null;
    }
    
    public static CsmObject findInnerFileObject(CsmFile file, int offset, CsmContext context) {
        assert (file != null) : "can't be null file in findTopFileDeclaration";
        // add file scope to context
        CsmContextUtilities.updateContext(file, offset, context);
        // check file declarations
        CsmObject lastObject = findInnerDeclaration(file.getDeclarations().iterator(), context, offset);
        // check includes if needed
        lastObject = lastObject != null ? lastObject : CsmOffsetUtilities.findObject(file.getIncludes(), context, offset);
        // check macros if needed
        lastObject = lastObject != null ? lastObject : CsmOffsetUtilities.findObject(file.getMacros(), context, offset);
        return lastObject;
    }
    
    private static CsmDeclaration findInnerDeclaration(final Iterator<? extends CsmDeclaration> it, final CsmContext context, final int offset) {
        CsmDeclaration innerDecl = null;
        if (it != null) {
            // continue till has next and not yet found
            while (it.hasNext()) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                assert (decl != null) : "can't be null declaration";
                if (CsmOffsetUtilities.isInObject(decl, offset)) {
                    if (!CsmKindUtilities.isFunction(decl) || CsmOffsetUtilities.isInFunctionScope((CsmFunction)decl, offset)) {
                        // add declaration scope to context
                        CsmContextUtilities.updateContext(decl, offset, context);
                        // we are inside declaration, but try to search deeper
                        innerDecl = findInnerDeclaration(decl, offset, context);
                    } else {
                        context.setLastObject(decl);
                    }
                    innerDecl = innerDecl != null ? innerDecl : decl;
                    // we can break loop, because list of declarations is sorted
                    // by offset and we found already one of container declaration
                    break;
                }
            }
        }
        return innerDecl;
    }
        
    // must check before call, that offset is inside outDecl
    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(outDecl, offset)) : "must be in outDecl object!";
        Iterator<? extends CsmDeclaration> it = null;
        if (CsmKindUtilities.isNamespace(outDecl)) { 
            CsmNamespace ns = (CsmNamespace)outDecl;
            it = ns.getDeclarations().iterator();
        } else if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            List list = cl.getMembers();
            if (cl.getFriends().size() > 0) {
                // combine friends with members for search
                list.addAll(cl.getFriends());
            }
            it = list.iterator();
        } else if (CsmKindUtilities.isEnum(outDecl)) {
            CsmEnum en = (CsmEnum)outDecl;
            it = en.getEnumerators().iterator();
        }
        return findInnerDeclaration(it, context, offset);
    }     
}
