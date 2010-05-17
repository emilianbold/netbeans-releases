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

package org.netbeans.test.j2ee.serverplugins.generic;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.test.j2ee.serverplugins.api.ConstantsProvider;
import org.netbeans.test.j2ee.serverplugins.api.ServerProvider;
import org.openide.util.NbBundle;

/**
 * Generic instantiation tests
 *
 * @author Michal Mocnak
 */
public class GenericInstanceTest extends NbTestCase {
    
    private ConstantsProvider cProvider;
    private ServerProvider sProvider;
    
    /**
     * Creates a new instance of GenericInstanceTest
     *
     * @param name name of the test method which has to be performed
     * @param cProvider instance of ConstantsProvider
     * @param sProvider instance of ServerProvider
     */
    public GenericInstanceTest(String name, ConstantsProvider cProvider, ServerProvider sProvider) {
        super(name);
        
        this.cProvider = cProvider;
        this.sProvider = sProvider;
    }
    
    /**
     * Add application server's instance test
     */
    public void addInstanceTest() {
        try {
            // Add a server instance
            InstanceProperties ip = InstanceProperties.createInstanceProperties(
                    cProvider.getServerURI(), cProvider.getUsername(),
                    cProvider.getPassword(), cProvider.getDisplayName());
            
            // Set server specific properties
            sProvider.setServerSpecificProperties(ip);
            
            // Test if the instance is well added
            ServerRegistry.getInstance().checkInstanceExists(cProvider.getServerURI());
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Remove server instance test
     */
    public void removeInstanceTest() {
        try {
            // Get instance from ServerRegistry
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(cProvider.getServerURI());
            
            // Remove instance
            if (!si.isRemoveForbidden())
                si.remove();
            else
                return;
            
            // Check if the instance is removed
            if (null != ServerRegistry.getInstance().getServerInstance(cProvider.getServerURI()))
                throw new Exception(NbBundle.getMessage(GenericInstanceTest.class, "MSG_Remove_Failed", cProvider.getServerURI()));
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
}
