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

package org.netbeans.modules.j2ee.common.method;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * Immutable model of method
 *
 * @author Martin Adamek
 */
public final class MethodModel {
    
    private final String name;
    private final String returnType;
    private final String body;
    /** null if no parent class exists yet */
    private final String className;
    /** unmodifiable list */
    private final List<VariableModel> parameters;
    /** unmodifiable list */
    private final List<String> exceptions;
    /** unmodifiable set */
    private final Set<Modifier> modifiers;
    
    // package-private for MethodModelSupport's factory methods
    MethodModel(String name, String returnType, String body, String className,
            List<VariableModel> parameters, List<String> exceptions, Set<Modifier> modifiers) {
        this.name = name;
        this.returnType = returnType;
        this.body = body;
        this.className = className;
        this.parameters = Collections.unmodifiableList(parameters);
        this.exceptions = Collections.unmodifiableList(exceptions);
        this.modifiers = Collections.unmodifiableSet(modifiers);
    }
    
    /**
     * Immutable type representing class field or method parameter
     */
    public static final class VariableModel {
        
        private final String type;
        private final String name;
        
        // package-private for MethodModelSupport's factory methods
        VariableModel(String type, String name) {
            this.type = type;
            this.name = name;
        }
        
        // <editor-fold defaultstate="collapsed" desc="VariableModel's getters">

        public String getType() {
            return type;
        }
        
        public String getName() {
            return name;
        }
        
    // </editor-fold>

    }
    
    // <editor-fold defaultstate="collapsed" desc="MethodModel's getters">
    
    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public String getBody() {
        return body;
    }
    
    public String getClassName() {
        return className;
    }
    
    public List<VariableModel> getParameters() {
        return parameters;
    }
    
    public List<String> getExceptions() {
        return exceptions;
    }
    
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    // </editor-fold>
    
    @Override
    public String toString() {
        return "<" + modifiers + "," + returnType + "," + name + "," + parameters + "," + exceptions + ",{" + body + "}>";
    }
    
}
