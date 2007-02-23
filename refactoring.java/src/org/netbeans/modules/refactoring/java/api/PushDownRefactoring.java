/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
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
package org.netbeans.modules.refactoring.java.api;

import java.util.Collection;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;

/**
 * Push Down Refactoring implementation class.
 *
 * @author Jan Becicka, Pavel Flaska
 */
public class PushDownRefactoring extends AbstractRefactoring {
    
    private static final MemberInfo[] EMPTY_MEMBERS = new MemberInfo[0];
    private MemberInfo[] members;
    
    /** Creates a new instance of PushDownRefactoring */
    public PushDownRefactoring(Lookup sourceType) {
        super(sourceType);
    }
    
    
    /** Returns descriptors of the members to pull up.
     * @return Member descriptors.
     */
    public MemberInfo[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to pull up.
     * @members Descriptors of members to be pulled up.
     */
    public void setMembers(MemberInfo[] members) {
        this.members = members;
    }
    
//    public JavaClass[] collectSubtypes() {
//        if (subtypes == null) {
//            if (sourceType != null) {
//                Collection c = sourceType.findSubTypes(false);
//                subtypes = (JavaClass[]) c.toArray(new JavaClass[c.size()]);
//            } else {
//                subtypes = new JavaClass[0];
//            }
//        }
//        return subtypes;
//    }
    
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    /** Class describing a member to be pushed down.
     */
    public static final class MemberInfo {
        public final TreePathHandle member;
        public final boolean makeAbstract;
        
        /** Creates a new instance of MemberInfo describing a method.
         * @param method Method to be pulled up.
         * @param makeAbstract Indicates whether the method should be made abstract
         *              in the supertype.
         */
        public MemberInfo(TreePathHandle method, boolean makeAbstract) {
            this.member = method;
            this.makeAbstract = makeAbstract;
        }
        
        /** Creates a new instance of MemberInfo describing a field 
         * to be pulled up.
         * @param field Field to be pulled up.
         */
        public MemberInfo(TreePathHandle innerClass) {
            this(innerClass, false);
        }
        
//        /** Creates a new instance of MemberInfo describing a field 
//         * to be pulled up.
//         * @param field Field to be pulled up.
//         */
//        public MemberInfo(Field field) {
//            this(field, false);
//        }
//        
//        /** Creates a new instance of MemberInfo describing an interface name
//         * from the implements clause that should be pulled up.
//         * @param interfaceName Interface name to be pulled up.
//         */
//        public MemberInfo(MultipartId interfaceName) {
//            this(interfaceName, false);
//        }
    }
}
