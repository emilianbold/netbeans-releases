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
package org.netbeans.modules.j2ee.weblogic9.optional;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.weblogic9.ui.wizard.WLInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;


/**
 * An entry point to the plugin's optional functionality, such as server
 * start/stop, incremental deployment, custom wizard for instance addition and
 * the ability to locate the servlet for a jsp page.
 *
 * @author Kirill Sorokin
 */
public class WLOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    /**
     * Returns an object responsible for starting a particular server instance.
     * The information about the instance is fetched from the supplied 
     * deployment manager.
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for starting/stopping the server
     */
    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return new WLStartServer(dm);
    }

    /**
     * Returns an object responsible for performing incremental deployment on
     * a particular server instance. The instance information should be fetched 
     * from the supplied deployment manager.
     * We do not support that, thus return null
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for performing the incremental deployment, i.e. null
     */
    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    /**
     * Returns an object responsible for finding a corresponsing servlet for a 
     * given jsp deployed on a particular server instance. Instance data should 
     * be fetched from the supplied deployment manager.
     * We do not support that, thus return null
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for finding the servlet, i.e. null
     */
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    /**
     * Returns an instance of the custom wizard for adding a server instance.
     * 
     * @return a custom wizard
     */
    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new WLInstantiatingIterator();
    }
    
}
