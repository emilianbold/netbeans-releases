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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ws.qaf;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author lukas
 */
public class FullWsValidation extends NbTestCase {

    public FullWsValidation(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new WsValidation("testCreateNewWs")); //NOI18N
        suite.addTest(new WsValidation("testAddOperation")); //NOI18N
        suite.addTest(new WsValidation("testStartServer")); //NOI18N
        suite.addTest(new WsValidation("testWsHandlers")); //NOI18N
        suite.addTest(new WsValidation("testDeployWsProject")); //NOI18N
        suite.addTest(new WsValidation("testCreateWsClient")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInServlet")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInJSP")); //NOI18N
        suite.addTest(new WsValidation("testCallWsOperationInJavaClass")); //NOI18N
        suite.addTest(new WsValidation("testWsClientHandlers")); //NOI18N
        suite.addTest(new WsValidation("testRefreshClient"));  //NOI18N
        suite.addTest(new WsValidation("testDeployWsClientProject")); //NOI18N
        suite.addTest(new EjbWsValidation("testCreateNewWs")); //NOI18N
        suite.addTest(new EjbWsValidation("testAddOperation")); //NOI18N
        suite.addTest(new EjbWsValidation("testWsHandlers")); //NOI18N
        suite.addTest(new EjbWsValidation("testDeployWsProject")); //NOI18N
        suite.addTest(new EjbWsValidation("testCreateWsClient")); //NOI18N
        suite.addTest(new EjbWsValidation("testCallWsOperationInSessionEJB")); //NOI18N
        suite.addTest(new EjbWsValidation("testCallWsOperationInJavaClass")); //NOI18N
        suite.addTest(new EjbWsValidation("testWsFromEJBinClientProject")); //NOI18N
        suite.addTest(new EjbWsValidation("testWsClientHandlers")); //NOI18N
        suite.addTest(new EjbWsValidation("testRefreshClientAndReplaceWSDL"));  //NOI18N
        suite.addTest(new EjbWsValidation("testDeployWsClientProject")); //NOI18N
        suite.addTest(new AppClientWsValidation("testCreateWsClient")); //NOI18N
        suite.addTest(new AppClientWsValidation("testCallWsOperationInJavaMainClass")); //NOI18N
        suite.addTest(new AppClientWsValidation("testCallWsOperationInJavaClass")); //NOI18N
        suite.addTest(new AppClientWsValidation("testWsClientHandlers")); //NOI18N
        suite.addTest(new AppClientWsValidation("testRefreshClient"));  //NOI18N
        suite.addTest(new AppClientWsValidation("testRunWsClientProject")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testCreateWsClient")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testCallWsOperationInJavaMainClass")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testFixClientLibraries")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testWsClientHandlers")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testRefreshClientAndReplaceWSDL"));  //NOI18N
        suite.addTest(new JavaSEWsValidation("testRunWsClientProject")); //NOI18N
        suite.addTest(new WsValidation("testUndeployProjects")); //NOI18N
        suite.addTest(new EjbWsValidation("testUndeployProjects")); //NOI18N
        suite.addTest(new AppClientWsValidation("testUndeployClientProject")); //NOI18N
        suite.addTest(new WsValidation("testStopServer")); //NOI18N
        return suite;
    }
    
    public static void main(String... args) {
        TestRunner.run(suite());
    }
}
