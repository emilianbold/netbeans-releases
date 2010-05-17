/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
