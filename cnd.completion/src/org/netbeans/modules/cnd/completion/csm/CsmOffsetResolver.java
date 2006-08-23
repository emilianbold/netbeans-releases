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

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * resolve file objects under offset
 * @author vv159170
 */
public class CsmOffsetResolver {
    private CsmFile file;
    
    /** Creates a new instance of CsmOffsetResolver */
    public CsmOffsetResolver () {
    }

    public CsmOffsetResolver (CsmFile file) {
        this.file = file;
    }
    
    public CsmFile getFile () {
        return file;
    }

    public void setFile (CsmFile file) {
        this.file = file;
    }

    // =================== resolve object under offset ============================
    
    public CsmObject findObject(int offset) {
        return findObject(file, offset);
    }
    
    // ==================== help methods =======================================

    public static CsmObject findObject(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findObject";
        // not interested in context, only object under offset
        CsmObject last = findObjectWithContext(file, offset, null);
        return last;
    }
    
    public static CsmObject findObjectWithContext(CsmFile file, int offset, CsmContext context) {
        assert (file != null) : "can't be null file in findObject";
        CsmObject last = null;
        if (context == null) {
            // create dummy context
            context = new CsmContext(offset);
        }
        CsmDeclaration decl = CsmDeclarationResolver.findInnerFileDeclaration(file, offset, context);
        last = decl;
        // for functions search deeper
        if (CsmKindUtilities.isFunction(decl)) {
            CsmFunction fun = (CsmFunction)decl;
            // check if offset in return value
            CsmType retType = fun.getReturnType();
            if (CsmOffsetUtilities.isInObject(retType, offset)) {
                return retType;
            }
            // check if offset in parameters
            List/*<CsmParameter>*/ params = fun.getParameters();
            CsmObject csmObj = CsmOffsetUtilities.findObject(params, offset);
            if (csmObj != null) {
                return csmObj;
            }            
            // for function definition search deeper in body's statements
            if (CsmKindUtilities.isFunctionDefinition(decl)) {
                last = null;
                CsmFunctionDefinition funDef = (CsmFunctionDefinition)decl;
                if (CsmOffsetUtilities.isInObject(funDef.getBody(), offset) &&
                        // offset is in body, try to find inners statement
                        CsmStatementResolver.findInnerObject(funDef.getBody(), offset, context)) {
                    // if found exact object => return it, otherwise return last found scope
                    last = (context.getLastObject() != null) ? context.getLastObject() : context.getLastScope();
                }
            }
        } else if (CsmKindUtilities.isClass(decl)) {
            // check if in inheritance part
            CsmClass clazz = (CsmClass)decl;
            List/*<CsmInheritance>*/ inherits = clazz.getBaseClasses();
            for (int i = 0; i < inherits.size(); i++) {
                CsmInheritance inh = (CsmInheritance)inherits.get(i);
                if (CsmOffsetUtilities.isInObject(inh, offset)) {
                    CsmContextUtilities.updateContextObject(inh, offset, context);
                    last = inh;
                    break;
                } 
//                if (CsmOffsetUtilities.isAfterObject(inh, offset)) {
//                    break;
//                }
            }
        }
        return last;
    }    
    
    public static CsmContext findContext(CsmFile file, int offset) {
        CsmContext context = new CsmContext(offset);
        findObjectWithContext(file, offset, context);
        return context;
    }    
}
