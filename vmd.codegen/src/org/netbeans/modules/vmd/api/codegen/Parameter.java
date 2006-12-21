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
package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * Represents a parameter of a setter.
 *
 * @author David Kaspar
 */
public interface Parameter {

    public static final String PARAM_INDEX = "#INDEX#"; // NOI18N

    /**
     * Returns a name of the parameter.
     * @return the parameter name
     */
    String getParameterName ();

    /**
     * Returns a priority of the parameter while a duplicate parameters are found.
     * The highest priority parameter overrides other parameters with the same name.
     * @return the parameter priority; 0 is normal
     */
    int getParameterPriority ();

    /**
     * Generates a parameter code for the parameter.
     * @param component the related component
     * @param section where the code has to be generated
     * @param index index of the array which is used in related setter; -1 when there is no array in related setter used
     */
    void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index);

    /**
     * Returns whether the parameter is required to be set.
     * @param component the related component
     * @return true, if required to be set
     */
    boolean isRequiredToBeSet (DesignComponent component);

    /**
     * Returns how many times the setter has to be used to set all values
     * @param component the related component
     * @return the usage count; -1 if non-array setter
     */
    int getCount (DesignComponent component);

    /**
     * Returns whether the parameter required to be set for a particular index.
     * @param component the related component
     * @param index the setter index
     * @return true, if required to be set
     */
    boolean isRequiredToBeSet (DesignComponent component, int index);

    static final Parameter INDEX = new Parameter() {

        public String getParameterName () {
            return PARAM_INDEX;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            section.getWriter ().write (Integer.toString (index));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return false;
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return false;
        }

    };

}
