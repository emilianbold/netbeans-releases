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
import org.openide.util.lookup.Lookups;

/** 
 * Convert Inner to Top-Level refactoring implementation class. This refactoring
 * is capable of converting an inner class into a top-level class.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public final class InnerToOuterRefactoring extends AbstractRefactoring {

    // parameters of the refactoring
    private String className;
    private String referenceName;
    
    /**
     * Creates a new instance of InnerToOuterRefactoring.
     * 
     * @param sourceType An inner class that should be converted to a top-level class.
     */
    public InnerToOuterRefactoring(TreePathHandle sourceType) {
        super(Lookups.singleton(sourceType));
    }
    
    /** Returns the type the members of which should be pulled up
     * by this refactoring.
     * @return Source of the members to be pulled up.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns the name for the top-level class to be created.
     * @return Class name.
     */
    public String getClassName() {
        return className;
    }

    /** Sets name for the top-level class to be created.
     * @param className Class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /** Returns name of the field that should be generated as a reference to the original
     * outer class. If null, no field will be generated.
     * @return Name of the field to be generated or null if no field will be generated.
     */
    public String getReferenceName() {
        return referenceName;
    }
    
    /** Sets name of the field that should be generated as a reference to the original
     * outer class. Can be set to null which indicates that no field should be generated.
     * @param referenceName Name of the field or null if no field should be generated.
     */ 
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }
}
