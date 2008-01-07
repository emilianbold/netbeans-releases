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

package org.netbeans.modules.visualweb.gravy.model.deployment;

import org.netbeans.modules.visualweb.gravy.model.Descriptor;
import org.netbeans.modules.visualweb.gravy.RaveTestCase;

import java.util.Hashtable;

/**
 * Descriptor for deployment targets.
 */

public class DeploymentTargetDescriptor implements Descriptor{

    /**
     * Key for getting deployment target's name from properties.
     */
    public static final String NAME_KEY = "deployment.target.name.";

    /**
     * Key for getting deployment target's type from properties.
     */
    public static final String SERVER_TYPE_KEY = "deployment.target.type";

    /**
     * Key for getting deployment target's web request prefix from properties.
     */
    public static final String REQUEST_PREFIX_KEY = "deployment.target.prefix.";
    
    /**
     * Key for getting path to deployment target from properties.
     */
    public static final String PATH_KEY = "deployment.target.path.";
    
    /**
     * Key for getting deployment target's domain from properties.
     */
    public static final String DOMAIN_KEY = "deployment.target.domain.";
    
    /**
     * Key for getting login for deployment target from properties.
     */
    public static final String LOGIN_KEY = "deployment.target.login.";
    
    /**
     * Key for getting password for deployment target from properties.
     */
    public static final String PASSWORD_KEY = "deployment.target.password.";
    
    /**
     * Deployment target's properties.
     */
    private Hashtable properties = new Hashtable();

    /**
     * Return deployment target's properties.
     * @return Hashtable of properties.
     */
    public Hashtable getProperties() {
        return properties;
    }

    /**
     * Get value of specified deployment target's property.
     * @param propertyName Name of necessary property.
     * @return String value of property.
     */
    public String getProperty(String propertyName) {
        return properties.get(propertyName).toString();
    }
    
    /**
     * Set value for specified deployment target's property.
     * @param propertyName Name of necessary property.
     * @param propertyValue Value for necessary property.
     */
    public void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    /**
     * Load deployment target's properties.
     */
    public void load() {
        properties = RaveTestCase.getDeploymentTargetProperties();
    }

    /**
     * Load deployment target's properties from specified file.
     * @param path_to_file Path to file with properties.
     */
    public void load(String path_to_file) {
        properties = RaveTestCase.getDeploymentTargetProperties(path_to_file);
    }

    /**
     * Load default deployment target's properties.
     */
    public void loadDefault() {
        properties = RaveTestCase.getDefaultDeploymentTargetProperties();
    }
}
