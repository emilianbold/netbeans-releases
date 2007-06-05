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

import org.netbeans.api.java.source.ElementHandle;
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
    private ElementHandle targetType;
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
    public ElementHandle getTargetType() {
        return targetType;
    }

    /** Sets target supertype to pull members up to.
     * @param targetType Target supertype.
     */
    public void setTargetType(ElementHandle targetType) {
        this.targetType = targetType;
    }

    /** Returns descriptors of the members to pull up.
     * @return Member descriptors.
     */
    public MemberInfo<ElementHandle>[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to pull up.
     * @param members Descriptors of members to be pulled up.
     */
    public void setMembers(MemberInfo<ElementHandle>[] members) {
        this.members = members;
    }
}
