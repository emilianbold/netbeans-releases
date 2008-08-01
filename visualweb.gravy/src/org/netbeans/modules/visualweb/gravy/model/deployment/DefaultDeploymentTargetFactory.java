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

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import org.netbeans.jemmy.JemmyException;

/**
 * Default factory for creation of deployment targets.
 */

public class DefaultDeploymentTargetFactory extends DeploymentTargetFactory {

    /**
     * Default deployment target factory.
     */
    private static DefaultDeploymentTargetFactory defaultDTFactory;

    /**
     * Create new DefaultDeploymentTargetFactory.
     */
    public DefaultDeploymentTargetFactory() {
    }

    /**
     * Create instance of DefaultDeploymentTargetFactory or return it, if it is already exist.
     * Singleton.
     * @return DefaultDeploymentTargetFactory.
     */
    public static DefaultDeploymentTargetFactory getFactory() {
      if (defaultDTFactory != null) return defaultDTFactory;
      else return (defaultDTFactory = new DefaultDeploymentTargetFactory());
    }

    /**
     * Create new deployment target.
     * Return server's type in accordance with PROP_NAME_SERVER_TYPE variable.
     * @param DTDescriptor Descriptor of deployment target.
     * @return created deployment target.
     */
    protected DeploymentTarget createInstance(DeploymentTargetDescriptor DTDescriptor) {
        try {
            ServerNavigatorOperator.addDeploymentTarget(DTDescriptor);
        }
        catch(Exception e) {
            throw new JemmyException("Deployment Target can't be added!", e);
        }
        String serverType = DTDescriptor.getProperty(DTDescriptor.SERVER_TYPE_KEY);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_WEBLOGIC)) return new WebLogic9(DTDescriptor);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_JBOSS)) return new JBoss4(DTDescriptor);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_GLASSFISH_V1) ||
            serverType.equals(ServerNavigatorOperator.STR_NAME_GLASSFISH_V2) ||
            serverType.equals(ServerNavigatorOperator.STR_NAME_APPSERVER)) return new SunApplicationServer(DTDescriptor);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_TOMCAT50)) return new Tomcat50(DTDescriptor);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_TOMCAT55)) return new Tomcat55(DTDescriptor);
        if (serverType.equals(ServerNavigatorOperator.STR_NAME_TOMCAT60)) return new Tomcat60(DTDescriptor);
        return null;
    }
}
