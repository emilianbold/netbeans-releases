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
package org.netbeans.modules.refactoring.java.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsRefactoring extends AbstractRefactoring {
    
    private Collection<EncapsulateFieldInfo> refactorFields = Collections.emptyList();
    private Set<Modifier> methodModifiers = Collections.emptySet();
    private Set<Modifier> fieldModifiers = Collections.emptySet();;
    private boolean alwaysUseAccessors;

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObject field to encapsulate, whatever tree of class
     *          containing trees to encapsulate
     */
    public EncapsulateFieldsRefactoring(TreePathHandle selectedObject) {
        super(Lookups.fixed(selectedObject));
    }
    
    /**
     * Getter for property refactorFields
     * @return Value of refactorFields
     */
    public Collection<EncapsulateFieldInfo> getRefactorFields() {
        return refactorFields;
    }

    /**
     * Getter for property methodModifier
     * @return Value of methodModifier
     */
    public Set<Modifier> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifier
     * @return Value of fieldModifier
     */
    public Set<Modifier> getFieldModifiers() {
        return fieldModifiers;
    }

    /**
     * Getter for property alwaysUseAccessors
     * @return Value of alwaysUseAccessors
     */
    public boolean isAlwaysUseAccessors() {
        return alwaysUseAccessors;
    }
    
    /**
     * Setter for property refactorFields
     * @param refactorFields New value of property refactorFields
     */
    public void setRefactorFields(Collection<EncapsulateFieldInfo> refactorFields) {
        this.refactorFields = Collections.unmodifiableCollection(
                new ArrayList<EncapsulateFieldInfo>(refactorFields));
    }

    /**
     * Setter for property methodModifier
     * @param methodModifier New value of property methodModifier
     */
    public void setMethodModifiers(Set<Modifier> methodModifier) {
        this.methodModifiers = methodModifier;
    }

    /**
     * Setter for property fieldModifier
     * @param fieldModifier New value of property fieldModifier
     */
    public void setFieldModifiers(Set<Modifier> fieldModifier) {
        this.fieldModifiers = fieldModifier;
    }

    /**
     * Setter for property alwaysUseAccessors
     * @param alwaysUseAccessors New value of property alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }

    public TreePathHandle getSelectedObject() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }
    

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Represents data from the panel.
     */
    public static final class EncapsulateFieldInfo {
        TreePathHandle field;
        String getterName;
        String setterName;
        
        /**
         * Creates an instance of Encapsulate Field Info
         * @param field 
         * @param getterName 
         * @param setterName 
         */
        public EncapsulateFieldInfo(TreePathHandle field, String getterName, String setterName) {
            this.field = field;
            this.getterName = getterName;
            this.setterName = setterName;
        }
        
        /**
         * Getter for property getterName.
         *
         * @return Value of property getterName.
         */
        public String getGetterName() { return getterName; }
        
        /**
         * Getter for property setterName.
         * 
         * @return Value of property setterName.
         */
        public String getSetterName() { return setterName; }
        
        /**
         * Getter for property field.
         *
         * @return Value of property field.
         */
        public TreePathHandle getField() { return field; }
        
    }
}
