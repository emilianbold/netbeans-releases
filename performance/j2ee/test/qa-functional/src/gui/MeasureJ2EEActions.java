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

package gui;


import gui.action.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureJ2EEActions  {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
	if (System.getProperty("onlyDeployment") != null && System.getProperty("onlyDeployment").equals("true")) {
		suite.addTest(new Deploy("measureTime", "Deploy Enterprise Application"));
	} else {

	        suite.addTest(new CreateJ2EEProject("testCreateEnterpriseApplicationProject", "Create Enterprise Application projects"));
	        suite.addTest(new CreateJ2EEProject("testCreateStandaloneEnterpriseApplicationProject", "Create standalone Enterprise Application project"));
	        suite.addTest(new CreateJ2EEProject("testCreateEJBModuleProject", "Create EJB Module project"));

	        suite.addTest(new ExpandEJBNodesProjectsView("testExpandEjbProjectNode", "Expand EJB Project node"));
	        suite.addTest(new ExpandEJBNodesProjectsView("testExpandEjbNode", "Expand Enterprise Beans node"));
	
	        suite.addTest(new OpenJ2EEFiles("testOpeningJava", "Open Java file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSessionBean", "Open Session Bean file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningEntityBean", "Open Entity Bean file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningEjbJarXml", "Open ejb-jar.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSunEjbJarXml", "Open sun-ejb-jar.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningApplicationXml", "Open application.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSunApplicationXml", "Open sun-application.xml file"));
	        
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSessionBean", "Open Session Bean file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningEntityBean", "Open Entity Bean file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningEjbJarXml", "Open ejb-jar.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSunEjbJarXml", "Open sun-ejb-jar.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningApplicationXml", "Open application.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSunApplicationXml", "Open sun-application.xml file if Editor opened"));

/* Wrong tests results, disabled util further investigation	        
	        suite.addTest(new MeasureSessionBeanAction("testAddBusinessMethod", "Add business method to SB"));
	        suite.addTest(new MeasureEntityBeanAction("testAddBusinessMethod", "Add business method to EB"));
                suite.addTest(new MeasureEntityBeanAction("testAddFinderMethod", "Add finder method to EB"));
	        suite.addTest(new MeasureEntityBeanAction("testAddSelectMethod", "Add select method to EB"));
	        suite.addTest(new MeasureWebServiceAction("testAddOperation", "Add operation to WS"));
	        
                
	        suite.addTest(new MeasureCallEjbAction("measureTime", "Call EJB in session bean"));
*/	        
	        //suite.addTest(new Deploy("measureTime", "Deploy Enterprise Application"));

	        suite.addTest(new CreateNewFile("testCreateNewSessionBean", "Create new session bean"));
	        suite.addTest(new CreateNewFile("testCreateNewEntityBean", "Create new entity bean"));
            
/* Unstable	        
	        suite.addTest(new CreateNewFile("testCreateNewWebService", "Create new web service"));
*/	
        }
        
        return suite;
    }
    
}
