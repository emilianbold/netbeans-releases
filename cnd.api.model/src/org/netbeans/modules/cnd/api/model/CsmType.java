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

package org.netbeans.modules.cnd.api.model;

/**
 * Represents type
 *
 * Comments about offsetable part and text of type objects:
 * type has start-end around it's classifier part, while return full text,
 * i.e int a[5],b;
 *     ^ ^
 *     | |
 *   st  end
 * for variable a getText() returns "int[5]"
 * for variable b getText() returns "int"
 * @author Vladimir Kvashin
 */
public interface CsmType extends CsmObject, CsmOffsetable {

//    /**
//     * Returns true if this type is a template instantiation
//     */
//    boolean isTemplateInstantiation();
    
    /** gets classifier this type references to */
    CsmClassifier getClassifier();
    
    /** array depth, i.e. 2 for "int[][]", 1 for "int[]", 0 for "int" */
    int getArrayDepth();
    
    boolean isPointer();
    
    /** if this is a pointer, returns the number of asterisks */
    int getPointerDepth();
    
    boolean isReference();
    
    boolean isConst();
    
    // TODO: [] and * are the same? 
    // is there a connection between isPointer() and isReference()
    
    // TODO: how to get from CsmType (int[][]) CsmType (int[]) ?
    
    // TODO: how to get from CsmType (int*) CsmType (int**) ?
    
    /** 
     * checks wether type is reference to built-in type or not
     * @param resolveTypeChain if true then resolve all typedefs (slow down)
     */
    boolean isBuiltInBased(boolean resolveTypeChain);
}
