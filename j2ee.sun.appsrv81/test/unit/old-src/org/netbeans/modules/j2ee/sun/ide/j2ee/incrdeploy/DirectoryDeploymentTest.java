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
/*
 * DirectoryDeploymentTest.java
 * JUnit based test
 *
 * Created on May 27, 2004, 2:26 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy;

import junit.framework.*;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment;

/*import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.io.File;
import junit.framework.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Constants;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Status;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SunWebApp;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ProgressEventSupport;
 **/

/**
 *
 * @author vkraemer
 */
public class DirectoryDeploymentTest extends TestCase {
    
    public void testCoverage() {
        DirectoryDeployment foo = new DirectoryDeployment();
        try {
            foo.setDeploymentManager(null);
            fail("null is an IllegalArgument");
        } catch (IllegalArgumentException iae) {
        }
        try {
            foo = new DirectoryDeployment(null);
            fail("null is an IllegalArgument");
        } catch (IllegalArgumentException iae) {
        }
        boolean ret = foo.canFileDeploy(null,null);
        //assertFalse("the null target cannot file deploy a null module", ret);
        assertTrue("the null target cannot file deploy a null module", !ret);
        foo.getDeploymentPlanFileNames(ModuleType.EAR);
        foo.getDeploymentPlanFileNames(ModuleType.WAR);
        foo.getDeploymentPlanFileNames(ModuleType.EJB);
        foo.getDeploymentPlanFileNames(ModuleType.CAR);
        foo.getDeploymentPlanFileNames(ModuleType.RAR);
        /*java.io.File f = foo.getDirectoryForModule(null);
        assertNull("a null TMID deploys to the null directory",f);
        f = foo.getDirectoryForNewApplication(null, null,null);
        assertNull(f);
        f = foo.getDirectoryForNewModule(null, null, null, null);
        assertNull(f);
        String s = foo.getModuleUrl(null);
        assertNull(s);
         **/
        try {
            foo.readDeploymentPlanFiles(null, null, null);
            fail("I shouldn't get here: readDeploymentPlanFiles(null,null,null)");
        } catch (ConfigurationException ce) {
        }
        try {
            foo.writeDeploymentPlanFiles(null, null, null);
            fail("I shouldn't get here: readDeploymentPlanFiles(null,null,null)");
        } catch (ConfigurationException ce) {
            
        }
    }
    
    public DirectoryDeploymentTest(java.lang.String testName) {
        super(testName);
    }
    
}
