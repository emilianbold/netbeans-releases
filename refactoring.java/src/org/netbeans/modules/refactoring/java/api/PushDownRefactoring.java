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

import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;

/**
 * Push Down Refactoring implementation class.
 *
 * @author Jan Becicka
 * @author Pavel Flaska
 */
public class PushDownRefactoring extends AbstractRefactoring {
    
    private static final MemberInfo<ElementHandle<? extends Element>>[] EMPTY_MEMBERS = new MemberInfo[0];
    private MemberInfo<ElementHandle<? extends Element>>[] members;
    
    /** Creates a new instance of PushDownRefactoring 
     * @param sourceType 
     */
    public PushDownRefactoring(Lookup sourceType) {
        super(sourceType);
    }
    
    
    /** Returns descriptors of the members to pull up.
     * @return Member descriptors.
     */
    public MemberInfo<ElementHandle<? extends Element>>[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to pull up.
     * @param members 
     * @members Descriptors of members to be pulled up.
     */
    public void setMembers(MemberInfo<ElementHandle<? extends Element>>[] members) {
        this.members = members;
    }
    
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }
}
