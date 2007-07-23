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
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing encapsulate field.
 * It creates getter and setter for given field and replaces all references.
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoring extends AbstractRefactoring {
    private String getterName,setterName;
    private Set<Modifier> methodModifiers;
    private Set<Modifier> fieldModifiers;
    private boolean alwaysUseAccessors;
    
    /**
     * Creates a new instance of EncapsulateFieldRefactoring
     * @param field field to refactor
     */
    public EncapsulateFieldRefactoring(TreePathHandle field) {
        super(Lookups.fixed(field));
    }
    
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    /**
     * Getter for property getterName
     * @return Value of property getterName
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * Getter for property setterName
     * @return Value of property setterName
     */
    public String getSetterName() {
        return setterName;
    }

    /**
     * Getter for property methodModifiers
     * @return Value of property methodModifiers
     */
    public Set<Modifier> getMethodModifiers() {
        return methodModifiers;
    }

    /**
     * Getter for property fieldModifiers
     * @return Value of property fieldModifiers
     */
    public Set<Modifier> getFieldModifiers() {
        return fieldModifiers;
    }

    /**
     * Getter for boolean property alwaysUseAccessors
     * @return Value of property alwaysUseAccessors
     */
    public boolean isAlwaysUseAccessors() {
        return alwaysUseAccessors;
    }
    
    /**
     * Setter for getterName property
     * @param getterName New value of getterName
     */
    public void setGetterName(String getterName) {
        this.getterName = getterName;
    }

    /**
     * Setter for setterName property
     * @param setterName New value of setterName
     */
    public void setSetterName(String setterName) {
        this.setterName = setterName;
    }

    /**
     * Setter for methodModifiers property
     * @param methodModifiers New value of methodModifiers
     */
    public void setMethodModifiers(Set<Modifier> methodModifiers) {
        this.methodModifiers = Collections.unmodifiableSet(
                new HashSet<Modifier>(methodModifiers));
    }

    /**
     * Setter for fieldModifiers property
     * @param fieldModifiers New value of fieldModifiers
     */
    public void setFieldModifiers(Set<Modifier> fieldModifiers) {
        this.fieldModifiers = Collections.unmodifiableSet(
                new HashSet<Modifier>(fieldModifiers));
    }

    /**
     * Setter for alwaysUseAccessors property
     * @param alwaysUseAccessors New value of alwaysUseAccessors
     */
    public void setAlwaysUseAccessors(boolean alwaysUseAccessors) {
        this.alwaysUseAccessors = alwaysUseAccessors;
    }
}
