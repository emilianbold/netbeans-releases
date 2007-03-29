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
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 */
public final class ChangeParametersRefactoring extends AbstractRefactoring {
    
    private Object selectedObject;
    // table of all the changes - it contains all the new parameters and also
    // changes in order
    private ParameterInfo[] paramTable;
    // new modifier
    private int modifier;
    
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersRefactoring(TreePathHandle method) {
        super(Lookups.singleton(method));
    }
    
    /**
     * Getter for new parameters
     * @return array of new parameters
     */
    public ParameterInfo[] getParameterInfo() {
        return paramTable;
    }
    
    /**
     * Getter for new modifiers
     * @return modifiers
     */
    public int getModifiers() {
        return modifier;
    }
    
    /**
     * Sets new parameters for a method
     * @param paramTable new parameters
     */
    public void setParameterInfo(ParameterInfo[] paramTable) {
        this.paramTable = paramTable;
    }

    /**
     * Sets modifiers for method
     * @param modifier new modifiers
     */
    public void setModifiers(int modifier) {
        this.modifier = modifier;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Represents one item for setParameters(List params) list parameter.
     * Item contains information about changes in method parameters.
     * Parameter can be added, changed or moved to another position.
     */
    public static final class ParameterInfo {
        int origIndex;
        String name;
        TypeMirrorHandle type;
        String defaultVal;

        /**
         * Creates a new instanceof of ParameterInfo. This constructor can be
         * used for newly added parameters or changed original parameters.
         * When you call method with -1 origIndex, you have to provide not
         * null values in all other pamarameters, otherwise it throws an
         * IllegalArgumentException.
         *
         * @param  origIndex  for newly added parameters, use -1, otherwise
         *                    use index in original parameters list
         * @param  name       parameter name 
         * @param  type       parameter type
         * @param  defaultVal should be provided for the all new parameters.
         *                    For changed parameters, it is ignored.
         */
        public ParameterInfo(int origIndex, String name, TypeMirrorHandle type, String defaultVal) {
            // new parameter
            // if (origIndex == -1 && (name == null || defaultVal == null || type == null || name.length() == 0 || defaultVal.length() == 0)) {
            //    throw new IllegalArgumentException(NbBundle.getMessage(ChangeParameters.class, "ERR_NoValues"));
            // }
            this.origIndex = origIndex;
            this.name = name;
            this.type = type;
            // do not set default value for existing parameters
            this.defaultVal = origIndex == -1 ? defaultVal : null;
        }
        
        /**
         * Creates a new instance of ParameterInfo. This constructor is used
         * for existing non-changed parameters. All the values except original
         * position in parameters list is set to null.
         *
         * @param  origIndex  position index in original parameters list
         */
        public ParameterInfo(int origIndex) {
            this(origIndex, null, null, null);
        }
        
        /**
         * Returns value of original parameter index.
         *
         * @return  original index of parameter in parameters list
         */
        public int getOriginalIndex() { return origIndex; }
        
        /**
         * Returns value of the name of parameter. If the name was not
         * changed, returns null.
         *
         * @return  new name for parameter or null in case that it was not changed.
         */
        public String getName() { return name; }

        /**
         * Returns value of the type of parameter. If the name was not
         * changed, returns null.
         *
         * @return new type for parameter or null if it was not changed.
         */
        public TypeMirrorHandle getType() { return type; }

        /**
         * Returns value of the default value in case of the new parameter.
         * Otherwise, it returns null.
         *
         * @return default value for new parameter, otherwise null.
         */
        public String getDefaultValue() { return defaultVal; }
    }
    
    ////////////////////////////////////////////////////////////////////////////
}
