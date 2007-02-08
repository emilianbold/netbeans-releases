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

import java.lang.reflect.Modifier;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;

/** Extract Interface Refactoring implementation class.
 *
 * @author Martin Matula, Jan Becicka
 */
public final class ExtractInterfaceRefactoring extends AbstractRefactoring {
    private static final TreePathHandle[] EMPTY_MEMBERS = new TreePathHandle[0];

    // source type
    private final TreePathHandle sourceType;
    // name of the new class to be created
    private String ifcName;
    private TreePathHandle[] members;
    
    /** Creates a new instance of ExtractInterfaceRefactoring 
     * @param sourceType Type the members of which should be extracted into an interface.
     */
    public ExtractInterfaceRefactoring(TreePathHandle sourceType) {
        // check if the sourceType is a ParameterizedType - if so, unwrap it
        this.sourceType = sourceType;
    }
    
    /** Returns the type the members of which should be extracted into an interface
     * by this refactoring.
     * @return Source of the members to be extracted.
     */
    public TreePathHandle getSourceType() {
        return sourceType;
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns name of the interface to be created.
     * @return Name of the new interface or null if it is not set.
     */
    public String getInterfaceName() {
        return ifcName;
    }

    /** Sets the name of the interface to be created.
     * @param ifcName Name of the new interface.
     */
    public void setInterfaceName(String ifcName) {
        this.ifcName = ifcName;
    }

    /** Returns members to extract.
     * @return Array of members.
     */
    public TreePathHandle[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members to extract.
     * @param members Array of members to extract.
     */
    public void setMembers(TreePathHandle[] members) {
        this.members = members;
    }
    
    // --- HELPER METHODS ------------------------------------------------------
    
//    public boolean acceptFeature(Feature feature) {
//        int modifiers = feature.getModifiers();
//        if (Modifier.isPublic(modifiers)) {
//            if (feature instanceof JavaClass) {
//                if (Modifier.isStatic(modifiers)) {
//                    return true;
//                }
//            } else if (feature instanceof Field) {
//                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && ((Field) feature).getInitialValueText() != null) {
//                    return true;
//                }
//            } else if (feature instanceof Method) {
//                if (!Modifier.isStatic(modifiers)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}
