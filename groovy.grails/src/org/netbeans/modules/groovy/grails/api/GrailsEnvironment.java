/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.api;

import org.openide.util.Parameters;

/**
 * Represents the environment of the Grails.
 *
 * @author Petr Hejl
 */
public enum GrailsEnvironment {

    /** Development environment. */
    DEVELOPMENT("Development"), // NOI18N

    /** Production environment. */
    PRODUCTION("Production"), // NOI18N

    /** Test environment. */
    TEST("Test"); // NOI18N

    private final String value;

    private GrailsEnvironment(String value) {
        this.value = value;
    }

    /**
     * Find the enum value for the given string representation of the grails
     * environment.
     *
     * @param value the grails environment
     * @return enum representing the value
     * @throws IllegalArgumentException if the value is not the known value of
     *             grails environment
     */
    public static GrailsEnvironment forString(String value) {
        Parameters.notNull("value", value);

        if ("Development".equals(value)) { // NOI18N
            return DEVELOPMENT;
        } else if ("Production".equals(value)) { // NOI18N
            return PRODUCTION;
        } else if ("Test".equals(value)) { // NOI18N
            return TEST;
        } else {
            throw new IllegalArgumentException("Unknown environment type"); // NOI18N
        }
    }

    /**
     * Returns the string representation usable in grails.
     *
     * @return the string representation usable in grails
     */
    @Override
    public String toString() {
        return value;
    }
}
