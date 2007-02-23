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

import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;

/** Pull Up Refactoring implementation class.
 *
 * @author Jan Becicka, Martin Matula
 */
public final class PullUpRefactoring extends AbstractRefactoring {
    private static final MemberInfo[] EMPTY_MEMBERS = new MemberInfo[0];

    // parameters of the refactoring
    private TreePathHandle targetType;
    private MemberInfo[] members;
    
    /** Creates a new instance of PullUpRefactoring 
     * @param sourceType Type the members of which should be pulled up.
     */
    public PullUpRefactoring(Lookup sourceType) {
        super(sourceType);
    }
    

    /** Returns the type the members of which should be pulled up
     * by this refactoring.
     * @return Source of the members to be pulled up.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns target supertype to pull members up to.
     * @return Target supertype or null if no target supertype is set.
     */
    public TreePathHandle getTargetType() {
        return targetType;
    }

    /** Sets target supertype to pull members up to.
     * @param targetType Target supertype.
     */
    public void setTargetType(TreePathHandle targetType) {
        this.targetType = targetType;
    }

    /** Returns descriptors of the members to pull up.
     * @return Member descriptors.
     */
    public MemberInfo[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to pull up.
     * @param members Descriptors of members to be pulled up.
     */
    public void setMembers(MemberInfo[] members) {
        this.members = members;
    }
    
    // --- HELPER METHODS ------------------------------------------------------
    
//    /** Returns supertypes of the source type that the members could
//     * be pull up to.
//     * @return Supertypes available for pulling up members to.
//     */
//    public TreePathHandle[] collectSupertypes() {
//        if (supertypes == null) {
//            if (sourceType != null) {
//                List list = new ArrayList();
//                // collect all supertypes recursivelly using a helper method
//                collectSupertypes(sourceType, list, new HashSet());
//                supertypes = (JavaClass[]) list.toArray(new JavaClass[list.size()]);
//            } else {
//                supertypes = new JavaClass[0];
//            }
//        }
//        return supertypes;
//    }
//    
//    // helper method for collecting supertypes
//    private static void collectSupertypes(JavaClass type, List result, Set visited) {
//        JavaClass superClass = Utilities.getRealClass(type.getSuperClass());
//        ArrayList supertypes = new ArrayList();
//        
//        // get superclass (if not visited already)
//        if (superClass != null && visited.add(superClass)) {
//            supertypes.add(superClass);
//            // add it to the result set if its source is available
//            if (Utilities.isFromSource(superClass) && !CheckUtils.isFromLibrary(superClass.getResource())) {
//                result.add(superClass);
//            }
//        }
//        
//        // get all implemented super interfaces (if not visited already)
//        for (Iterator it = type.getInterfaces().iterator(); it.hasNext();) {
//            JavaClass ifc = Utilities.getRealClass((JavaClass) it.next());
//            if (visited.add(ifc)) {
//                supertypes.add(ifc);
//                // add it to the result set if its source is available
//                if (Utilities.isFromSource(ifc)) {
//                    result.add(ifc);
//                }
//            }
//        }
//        
//        // iterate through the collected direct supertypes
//        // and collect their supertypes recursivelly
//        // (this is done in a separate loop to preserve logical ordering
//        // of the supertypes)
//        for (Iterator it = supertypes.iterator(); it.hasNext();) {
//            collectSupertypes((JavaClass) it.next(), result, visited);
//        }
//    }
//    
   
    // --- HELPER CLASSES ------------------------------------------------------
    
    /** Class describing a member to be pulled up.
     */
    public static final class MemberInfo {
        public final TreePathHandle member;
        public final boolean makeAbstract;
        
        /** Creates a new instance of MemberInfo describing a method.
         * @param method Method to be pulled up.
         * @param makeAbstract Indicates whether the method should be made abstract
         *              in the supertype.
         */
        private MemberInfo(TreePathHandle member, boolean makeAbstract) {
            this.member = member;
            this.makeAbstract = makeAbstract;
        }

        /** Creates a new instance of MemberInfo describing an inner class
         * to be pulled up.
         * @param field Inner class to be pulled up.
         */
        public MemberInfo(TreePathHandle innerClass) {
            this(innerClass, false);
        }
        
//        /** Creates a new instance of MemberInfo describing a field 
//         * to be pulled up.
//         * @param field Field to be pulled up.
//         */
//        public MemberInfo(TreePathHandle field) {
//            this(field, false);
//        }
//        
//        /** Creates a new instance of MemberInfo describing an interface name
//         * from the implements clause that should be pulled up.
//         * @param interfaceName Interface name to be pulled up.
//         */
//        public MemberInfo(TreePathHandle interfaceName) {
//            this(interfaceName, false);
//        }
    }
}
