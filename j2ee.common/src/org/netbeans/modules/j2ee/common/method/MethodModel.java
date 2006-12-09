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
import org.openide.util.Parameters;

/**
 * Immutable model of method.
 * Check {@link MethodModelSupport} for additional support functionality related to this class
 * 
 * @author Martin Adamek
 */
public final class MethodModel {
    
    private final String name;
    private final String returnType;
    private final String body;
    private final List<Variable> parameters; // unmodifiable list
    private final List<String> exceptions; // unmodifiable list
    private final Set<Modifier> modifiers; // unmodifiable set
    
    private MethodModel(String name, String returnType, String body, List<Variable> parameters, List<String> exceptions, Set<Modifier> modifiers) {
        this.name = name;
        this.returnType = returnType;
        this.body = body;
        this.parameters = Collections.unmodifiableList(parameters);
        this.exceptions = Collections.unmodifiableList(exceptions);
        this.modifiers = Collections.unmodifiableSet(modifiers);
    }
    
    /**
     * Creates new instance of method model. None of the parameters can be null.
     * 
     * @param name name of the method, must be valid Java identifier
     * @param returnType name of return type as written in source code,
     * for non-primitive types fully-qualfied name must be used,
     * must contain at least one non-whitespace character
     * @param body string representation of body, can be empty string
     * @param parameters list of method parameters, can be empty
     * @param exceptions list of exceptions represented by fully-qualified names of exceptions, can be empty
     * @param modifiers list of modifiers of method, can be empty
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @throws IllegalArgumentException if the paramter returnType does not contain at least one non-whitespace character
     * or the parameter name is not a valid Java identifier
     * @return immutable model of method
     */
    public static MethodModel create(String name, String returnType, String body, List<Variable> parameters, List<String> exceptions, Set<Modifier> modifiers) {
        Parameters.javaIdentifier("name", name);
        Parameters.notWhitespace("returnType", returnType);
        Parameters.notNull("body", body);
        Parameters.notNull("parameters", parameters);
        Parameters.notNull("exceptions", exceptions);
        Parameters.notNull("modifiers", modifiers);
        return new MethodModel(name, returnType, body, parameters, exceptions, modifiers);
    }
    
    /**
     * Immutable type representing class field or method parameter
     */
    public static final class Variable {
        
        private final String type;
        private final String name;
        
        private Variable(String type, String name) {
            this.type = type;
            this.name = name;
        }
        
        /**
         * Creates new instance of model of class variable or method parameter
         * 
         * @param type name of type as written in source code
         * for non-primitive types fully-qualfied name must be used,
         * must contain at least one non-whitespace character
         * @param name name of the paramter or variable, must be valid Java identifier
         * @throws NullPointerException if any of the parameters is <code>null</code>.
         * @throws IllegalArgumentException if the paramter type does not contain at least one non-whitespace character
         * or the parameter name is not a valid Java identifier
         * @return immutable model of variable or method parameter
         */
        public static Variable create(String type, String name) {
            Parameters.notWhitespace("type", type);
            Parameters.javaIdentifier("name", name);
            return new MethodModel.Variable(type, name);
        }
        
        // <editor-fold defaultstate="collapsed" desc="Variable's getters">

        /**
         * Variable or method paramter type, for non-primitive types fully-qualified name is returned
         * 
         * @return non-null value
         */
        public String getType() {
            return type;
        }
        
        /**
         * Variable or method paramter name
         * 
         * @return non-null value
         */
        public String getName() {
            return name;
        }
        
    // </editor-fold>

    }
    
    // <editor-fold defaultstate="collapsed" desc="MethodModel's getters">
    
    /**
     * Method name
     * 
     * @return non-null value
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return type, for non-primitive types fully-qualified name is returned
     * 
     * @return non-null value
     */
    public String getReturnType() {
        return returnType;
    }
    
    /**
     * String representation of method body
     * 
     * @return non-null value
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Unmodifiable list of method parameters. Attempts to modify the returned list, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public List<Variable> getParameters() {
        return parameters;
    }
    
    /**
     * Unmodifiable list of method exceptions. Attempts to modify the returned list, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public List<String> getExceptions() {
        return exceptions;
    }
    
    /**
     * Unmodifiable set of method modifiers. Attempts to modify the returned set, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    // </editor-fold>
    
    @Override
    public String toString() {
        return "MethodModel<" + modifiers + "," + returnType + "," + name + "," + parameters + "," + exceptions + ",{" + body + "}>";
    }
    
}
