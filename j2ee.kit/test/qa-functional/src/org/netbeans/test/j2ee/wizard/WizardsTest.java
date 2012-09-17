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
package org.netbeans.test.j2ee.wizard;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.j2ee.wizard.NewProjectWizardsTest.NewProjectWizardsTest4;

/** Tests new file wizard for Java EE 1.4 projects.
 *
 * @author jungi, Jiri Skrivanek
 */
public class WizardsTest extends J2eeTestCase {

    public WizardsTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, NewProjectWizardsTest4.class, "testEJBModWizard");
        if (isRegistered(Server.GLASSFISH)) {
            conf = conf.addTest(Suite.class);
        }
        return conf.suite();
    }

    public static class Suite extends NbTestSuite {

        public Suite() {
            super();
            // EJB project
            addTest(new NewFileWizardsTest("testLocalSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testRemoteSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalRemoteSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalStatefulSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testRemoteStatefulSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalRemoteStatefulSessionBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalEntityBean", "1.4"));
            addTest(new NewFileWizardsTest("testRemoteEntityBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalRemoteEntityBean", "1.4"));
            addTest(new NewFileWizardsTest("testQueueMdbBean", "1.4"));
            addTest(new NewFileWizardsTest("testTopicMdbBean", "1.4"));
            addTest(new NewFileWizardsTest("testServiceLocatorInEjb", "1.4"));
            addTest(new NewFileWizardsTest("testCachingServiceLocatorInEjb", "1.4"));
            addTest(new NewFileWizardsTest("testBuildDefaultNewEJBMod", "1.4"));
            // new files are uncompilable
            addTest(new NewFileWizardsTest("testLocalBeanEntityBean", "1.4"));
            addTest(new NewFileWizardsTest("testRemoteBeanEntityBean", "1.4"));
            addTest(new NewFileWizardsTest("testLocalRemoteBeanEntityBean", "1.4"));
            // web project
            addTest(new NewProjectWizardsTest("testWebModWizard", "1.4"));
            addTest(new NewFileWizardsTest("testServiceLocatorInWeb", "1.4"));
            addTest(new NewFileWizardsTest("testCachingServiceLocatorInWeb", "1.4"));
            addTest(new NewFileWizardsTest("testBuildDefaultNewWebMod", "1.4"));
            // EAR project
            addTest(new NewProjectWizardsTest("testEnterpriseAppWizard", "1.4"));
            addTest(new NewProjectWizardsTest("closeProjects", "1.4"));
        }
    }
}
