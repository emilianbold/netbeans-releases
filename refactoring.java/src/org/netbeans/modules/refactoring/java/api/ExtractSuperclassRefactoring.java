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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.api;

import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** 
 * Extract Super Class Refactoring.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Pokorsky
 */
public final class ExtractSuperclassRefactoring extends AbstractRefactoring {
    private static final MemberInfo[] EMPTY_MEMBERS = new MemberInfo[0];
    // parameters of the refactoring
    private String scName;
    private MemberInfo[] members;
    
    /**
     * Creates a new instance of ExtractSuperclassRefactoring 
     * 
     * @param sourceType Type the members of which should be pulled up.
     */
    public ExtractSuperclassRefactoring(TreePathHandle sourceType) {
        super(Lookups.fixed(sourceType));
    }
    
    /** Returns the type the members of which should be pulled up
     * by this refactoring.
     * @return Source of the members to be pulled up.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns the name for the super class to be created.
     * @return Super class name.
     */
    public String getSuperClassName() {
        return scName;
    }

    /** Sets name for the super class to be created.
     * @param scName Super class name.
     */
    public void setSuperClassName(String scName) {
        this.scName = scName;
    }

    /** Returns descriptors of the members to extract into the new super class.
     * @return Member descriptors.
     */
    public MemberInfo<ElementHandle<? extends Element>>[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to extract into the new super class.
     * @param members Descriptors of members to be extracted into the new super class.
     */
    public void setMembers(MemberInfo<ElementHandle<? extends Element>>[] members) {
        this.members = members;
    }
}
