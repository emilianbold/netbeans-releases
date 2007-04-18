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

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.swing.Icon;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** Extract Super Class Refactoring implementation class.
 *
 * @author Martin Matula, Jan Pokorsky
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
    public MemberInfo[] getMembers() {
        // never return null
        return members == null ? EMPTY_MEMBERS : members;
    }

    /** Sets members (using their descriptors) to extract into the new super class.
     * @param members Descriptors of members to be extracted into the new super class.
     */
    public void setMembers(MemberInfo[] members) {
        this.members = members;
    }

    
    // --- HELPER METHODS ------------------------------------------------------
//    
//    public boolean acceptFeature(Feature feature) {
//        return (((feature instanceof JavaClass) || (feature instanceof Field) || (feature instanceof Method)) && (!(Modifier.isPrivate(feature.getModifiers()))));
//    }
    
    // --- HELPER CLASSES ------------------------------------------------------
    
    
    /** Class describing a member to be extracted into the new super class.
     */
    public static final class MemberInfo<H> {
        public final H handle;
        public final String htmlText;
        public final Icon icon;
        public final String name;
        public final Group group;
        public boolean makeAbstract;
        public final Set<Modifier> modifiers;
        
        public enum Group {
            IMPLEMENTS, METHOD, FIELD;
        }
        
        private MemberInfo(H handle, String htmlText, Icon icon, String name, Group group, Set<Modifier> modifiers, boolean makeAbstract) {
            this.handle = handle;
            this.htmlText = htmlText;
            this.icon = icon;
            this.name = name;
            this.group = group;
            this.modifiers = modifiers;
            this.makeAbstract = makeAbstract;
        }
        
        public static MemberInfo<ElementHandle<VariableElement>> createField(VariableElement elm, String htmlText, Icon icon) {
            return new MemberInfo<ElementHandle<VariableElement>>(
                    ElementHandle.<VariableElement>create(elm),
                    htmlText, icon, elm.getSimpleName().toString(), Group.FIELD, elm.getModifiers(), false);
        }
        
        public static MemberInfo<ElementHandle<ExecutableElement>> createMethod(ExecutableElement elm, String htmlText, Icon icon) {
            return new MemberInfo<ElementHandle<ExecutableElement>>(
                    ElementHandle.<ExecutableElement>create(elm),
                    htmlText, icon, elm.getSimpleName().toString(), Group.METHOD,
                    elm.getModifiers(), elm.getModifiers().contains(Modifier.ABSTRACT));
        }
        
        public static MemberInfo<TypeMirrorHandle> createImplements(TypeMirrorHandle handle, String htmlText, Icon icon, String name) {
            return new MemberInfo<TypeMirrorHandle>(handle, htmlText, icon, name, Group.IMPLEMENTS, Collections.<Modifier>emptySet(), false);
        }
    }
}
