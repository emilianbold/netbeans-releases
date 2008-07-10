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

package org.netbeans.modules.visualweb.gravy.model.project;

import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.model.Descriptor;

import java.util.Hashtable;

/**
 * Descriptor for projects.
 */

public class ProjectDescriptor implements Descriptor{

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.Bundle";
    
    public final static String J2EE13 = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "CustomizerBundle"),
                                                  Bundle.getStringTrimmed(bundle, "J2EE13"));
    
    public final static String J2EE14 = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "CustomizerBundle"),
                                                  Bundle.getStringTrimmed(bundle, "J2EE14"));
    
    public final static String JavaEE5 = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "CustomizerBundle"),
                                                  Bundle.getStringTrimmed(bundle, "JavaEE5"));
    
    /**
     * Key for getting project's name property from properties.
     */
    public static final String NAME_KEY = "projectName";

    /**
     * Key for getting project's location property from properties.
     */
    public static final String LOCATION_KEY = "projectLocation";

    /**
     * Key for getting project's J2EE version property from properties.
     */
    public static final String J2EEVERSION_KEY = "J2EEVersion";
    
    /**
     * Key for getting project's target server property from properties.
     */
    public static final String SERVER_KEY = "targetServer";
    
    /**
     * Project's properties.
     */
    private Hashtable properties = new Hashtable();

    /**
     * Create descriptor of the project with default J2EE version (1.4).
     * @param projectName Name of the project.
     * @param projectLocation Location of the project.
     */
    public ProjectDescriptor(String projectName, String projectLocation) {
        this(projectName, projectLocation, J2EE14);
    }

    /**
     * Create descriptor of the project.
     * @param projectName Name of the project.
     * @param projectLocation Location of the project.
     * @param J2EEVersion Version of J2EE (can be "J2EE 1.3", "J2EE 1.4", "Java EE 5").
     */
    public ProjectDescriptor(String projectName, String projectLocation, String J2EEVersion) {
        this(projectName, projectLocation, J2EEVersion, null);
    }
    
    /**
     * Create descriptor of the project.
     * @param projectName Name of the project.
     * @param projectLocation Location of the project.
     * @param J2EEVersion Version of J2EE (can be "J2EE 1.3", "J2EE 1.4", "Java EE 5").
     * @param targetServer Target Server for deploying of application.
     */
    public ProjectDescriptor(String projectName, String projectLocation, String J2EEVersion, String targetServer) {
        properties.put(NAME_KEY, projectName);
        properties.put(LOCATION_KEY, projectLocation);
        properties.put(J2EEVERSION_KEY, J2EEVersion);
        properties.put(SERVER_KEY, targetServer);
    }

    /**
     * Get project's properties.
     * @return Hashtable of properties.
     */
    public Hashtable getProperties() {
        return properties;
    }

    /**
     * Get value of specified project's property.
     * @param propertyName Name of necessary property.
     * @return String value of property.
     */
    public String getProperty(String propertyName) {
        return properties.get(propertyName).toString();
    }
}
