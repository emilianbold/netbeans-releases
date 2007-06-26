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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** 
 * Extract Interface Refactoring.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class ExtractInterfaceRefactoring extends AbstractRefactoring {
    // name of the new class to be created
    private String ifcName;
    private List<ElementHandle<ExecutableElement>> methods;
    private List<ElementHandle<VariableElement>> fields;
    private List<TypeMirrorHandle<TypeMirror>> implementz;
    
    /** Creates a new instance of ExtractInterfaceRefactoring 
     * @param sourceType Type the members of which should be extracted into an interface.
     */
    public ExtractInterfaceRefactoring(TreePathHandle sourceType) {
        super(Lookups.fixed(sourceType));
    }
    
    /** Returns the type the members of which should be extracted into an interface
     * by this refactoring.
     * @return Source of the members to be extracted.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
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

    /**
     * Gets methods to extract.
     * @return list of methods
     */
    public List<ElementHandle<ExecutableElement>> getMethods() {
        return methods != null? methods: Collections.<ElementHandle<ExecutableElement>>emptyList();
    }

    /**
     * Sets public methods to extract.
     * @param methods list of methods
     */
    public void setMethods(List<ElementHandle<ExecutableElement>> methods) {
        if (methods == null) {
            throw new NullPointerException();
        }
        List<ElementHandle<ExecutableElement>> l = new ArrayList<ElementHandle<ExecutableElement>>(methods);
        this.methods = Collections.<ElementHandle<ExecutableElement>>unmodifiableList(l);
    }

    /**
     * Gets fields to extract.
     * @return list of fields
     */
    public List<ElementHandle<VariableElement>> getFields() {
        return fields != null? fields: Collections.<ElementHandle<VariableElement>>emptyList();
    }

    /**
     * Sets public static final fields with default value to extract.
     * @param fields list of fields
     */
    public void setFields(List<ElementHandle<VariableElement>> fields) {
        if (fields == null) {
            throw new NullPointerException();
        }
        List<ElementHandle<VariableElement>> l = new ArrayList<ElementHandle<VariableElement>>(fields);
        this.fields = Collections.<ElementHandle<VariableElement>>unmodifiableList(l);
    }

    /**
     * Gets interfaces to extract.
     * @return list of interfaces
     */
    public List<TypeMirrorHandle<TypeMirror>> getImplements() {
        return implementz != null? implementz: Collections.<TypeMirrorHandle<TypeMirror>>emptyList();
    }

    /**
     * Sets interfaces to extract.
     * @param implementz list of interfaces
     */
    public void setImplements(List<TypeMirrorHandle<TypeMirror>> implementz) {
        if (implementz == null) {
            throw new NullPointerException();
        }
        List<TypeMirrorHandle<TypeMirror>> l = new ArrayList<TypeMirrorHandle<TypeMirror>>(implementz);
        this.implementz = Collections.<TypeMirrorHandle<TypeMirror>>unmodifiableList(l);
    }
}
