/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.common.project.ui;

import java.util.*;
import org.netbeans.spi.project.support.ant.EditableProperties;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class J2EEProjectProperties {

    public static final String JAVA_EE_5 = "1.5"; // NOI18N
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    public static final String J2EE_1_3 = "1.3"; // NOI18N

    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    
    /**
     * Remove obsolete properties from private properties.
     * @param privateProps private properties
     */
    public static void removeObsoleteLibraryLocations(EditableProperties privateProps) {
        // remove special properties from private.properties:
        Iterator<String> propKeys = privateProps.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                propKeys.remove();
            }
        }
    }
            
    

    /**
     * Returns <code>true</code> if the server library is used for j2ee instead
     * of the classpath pointing to the server installation.
     *
     * @param projectProperties project properties
     * @param j2eePlatformClasspathProperty name of the classpath property
     * @return <code>true</code> if the server library is used for j2ee instead
     *             of the classpath pointing to the server installation
     */
    public static boolean isUsingServerLibrary(EditableProperties projectProperties, String j2eePlatformClasspathProperty) {
        String value = projectProperties.getProperty(j2eePlatformClasspathProperty);
        return (value != null && !"".equals(value.trim()));
    }
}
