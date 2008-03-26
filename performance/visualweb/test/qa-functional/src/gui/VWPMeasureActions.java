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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mkhramov@netbeans.org
 */
public class VWPMeasureActions  {
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
	
//TODO do Open project through UI	suite.addTest(new OpenWebPackProject("measureTime","Open Small Web Project"));
//TODO do Open project through UI        suite.addTest(new OpenHugeWebPackProject("testOpenWebPackProject","Open Huge Web Project"));
        
        suite.addTest(new OpenProjectFirstPage("testOpenSmallProjectFirstPage","Open Small Project First Page"));
        suite.addTest(new OpenProjectFirstPage("testOpenLargeProjectFirstPage","Open Large Project First Page"));

// manual results differ from automated        
//        suite.addTest(new TypingInCSSEditor("measureTime","Type a character in CSS Editor"));

        suite.addTest(new CSSRuleAddTest("measureTime","Measure time to add and modify CSS rule"));
        suite.addTest(new PasteCSSText("measureTime","Measure time to Paste text into CSS editor"));
        
        suite.addTest(new OpenBeanFiles("testApplicationBean","Open Application Bean"));
        suite.addTest(new OpenBeanFiles("testRequestBean","Open Request  Bean"));
        suite.addTest(new OpenBeanFiles("testSessionBean","Open Session Bean"));
        suite.addTest(new OpenNavigationPage("measureTime","Open Navigation Page"));
        

        suite.addTest(new CreateWebPackFiles("testCreateCSSTable","Create CSS table"));	
	suite.addTest(new CreateWebPackFiles("testCreateJSPFragment","Create JSP fragment for VWP project"));
        suite.addTest(new CreateWebPackFiles("testCreateJSPPage","Create JSP page for VWP project"));

        suite.addTest(new gui.window.DatabaseTableDrop("measureTime","Database table drop on Table time")); 
        //TODO Disabled because throws exception. See bugid #99202      
        suite.addTest(new ComponentAddTest("testAddTableComponent","Adding Table Component"));
	suite.addTest(new ComponentAddTest("testAddButtonComponent","Adding Button Component"));
	suite.addTest(new ComponentAddTest("testAddListboxComponent","Adding Listbox Component"));
        
        //suite.addTest(new CreateWebPackProjectSBS("testCreateWebPackProject","Create Visual Web Project SBS"));
        
//        suite.addTest(new WebProjectDeployment("testDeploySmallProject","Deployment Small Project"));     
//        suite.addTest(new WebProjectDeployment("testDeployLargeProject","Deployment Huge Project"));
//        
//        suite.addTest(new CleanAndBuildProject("testCleanAndBuildSingleOpenedPageProject","CnB project with single opened page"));
//        suite.addTest(new CleanAndBuildProject("testCleanAndBuildMultipleOpenedPagesProject","CnB project with two opened pages"));
        
        suite.addTest(new PageSwitchTest("testPageSwitch"));
        suite.addTest(new ViewSwitchTest("doMeasurement","Test view switch time"));
        suite.addTest(new CreateWebPackProject("testCreateWebPackProject","Create Visual Web Project"));        
        
        return suite;
    }
}

