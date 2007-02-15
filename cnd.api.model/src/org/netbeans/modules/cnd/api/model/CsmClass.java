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

import java.util.List;

/**
 * Represents class, struct, enum and union
 * @author Vladimir Kvashin
 *
 * TODO: think over how to represent built-in types
 */
public interface CsmClass extends CsmCompoundClassifier<CsmClass> {

//    public static class Kind extends TypeSafeEnum {
//
//        public Kind(String value) {
//            super(value);
//        }
//
//        public static final Kind CLASS = new Kind("class");
//        public static final Kind UNION = new Kind("union");
//        public static final Kind STRUCT = new Kind("struct");
//        
//    }
//    
//    CsmClass.Kind  getClassKind();
    
    /**
     * If this is nested class, returns the class,
     * in which this class resides, otherwise null *
     */
    CsmClass getContainingClass();
    
    /**
     * Returns immutable collection of this class' members
     * Members migt be:
     *	fields
     *	methods
     *	nested classes
     *	enumerations
     *	bit fields
     *	friends (?)
     *	typedefs
     *
     * TODO: collection of WHAT?
     */
    List/*<CsmMember>*/ getMembers();
    
    /** Returns the list of base classes */
    List/*<CsmInheritance>*/ getBaseClasses();
    
    /** 
     * Returns true if this class is template, otherwise false.
     * If isTemplate() returns true, this class is an instance of CsmTemplate
     */
    public boolean isTemplate();
    
    /**
     * Gets the offset of the class' open curly bracket.
     */
    int getLeftBracketOffset();
    
}
