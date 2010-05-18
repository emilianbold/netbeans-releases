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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.VWPUtilities;
import org.netbeans.performance.visualweb.WebFormDesignerOperator;
import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */

public class OpenProjectFirstPageTest extends PerformanceTestCase  {
    
    private Node openNode;
    private String targetProject;
    private ProjectsTabOperator pto;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");

    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPageTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPageTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(OpenProjectFirstPageTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpenVWProjectFirstPage() {
        targetProject = "UltraLargeWA";
        doMeasurement();
    }
    
    
    
    @Override
    public void initialize(){
        EditorOperator.closeDiscardAll();
        pto = VWPUtilities.invokePTO();        
        //Workaround for "Update data sources" dialog
        try {
            new JDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.dataconnectivity.utils.Bundle", "MSG_Update_Datasources_Title")).close();
        } catch (TimeoutExpiredException tex) {
            // Do nothing
            log(tex.toString());
        }        
    }
    
    public void prepare(){
        try {
            openNode = new Node(pto.getProjectRootNode(targetProject), org.netbeans.performance.visualweb.VWPUtilities.WEB_PAGES + "|" + "Page1.jsp");
        } catch(TimeoutExpiredException tex) {
            throw new Error("Cannot find expected node because of Timeout");
        }
        
        if (this.openNode == null) {
            throw new Error("Cannot find expected node");
        }
        openNode.select();
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        popup.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout", 90000);
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }
        
        return WebFormDesignerOperator.findWebFormDesignerOperator("Page1");
    }
    
    @Override
    public void close(){
        if(testedComponentOperator != null) {
            ((WebFormDesignerOperator)testedComponentOperator).close();
            testedComponentOperator = null;
        }
    }
    
    @Override
    protected void shutdown() {
    }

}
