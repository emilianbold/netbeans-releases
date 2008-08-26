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

package org.netbeans.performance.languages.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenRubyProject extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    private static String projectName; 
    private JButtonOperator openButton;
    private String workdir;    
    
    public OpenRubyProject(String testName)
    {
        super(testName);
        //projectName = "TestRubyApplication"; //NO18N
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
        workdir = System.getProperty("nbjunit.workdir");
        try {
            workdir = new File(workdir + "/../../../../../../../nbextra/data/").getCanonicalPath();
        } catch (IOException ex) {
            System.err.println("Exception: "+ex);
        }        
    }
    public OpenRubyProject(String testName, String performanceDataName)
    {
        super(testName, performanceDataName);
        //projectName = "TestRubyApplication"; //NO18N
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000; 
        workdir = System.getProperty("nbjunit.workdir");
        try {
            workdir = new File(workdir + "/../../../../../../../nbextra/data/").getCanonicalPath();
        } catch (IOException ex) {
            System.err.println("Exception: "+ex);
        }        
    }
    
    @Override
    public void initialize()
    {
        log("::initialize::");        
    }
    
    @Override
    public void prepare() {
        new ActionNoBlock("File|Open Project...",null).perform(); //NOI18N
        WizardOperator opd = new WizardOperator("Open Project"); //NOI18N
        JTextComponentOperator path = new JTextComponentOperator(opd,1);
        openButton = new JButtonOperator(opd,"Open Project"); //NOI18N
        String paths= workdir + java.io.File.separator + projectName;
        path.setText(paths);        
    }

    @Override
    public ComponentOperator open() {
        openButton.pushNoBlock();
        return null;
    }
    
    @Override
    public void close()
    {
        log("::close");
        ProjectSupport.closeProject(projectName);
    }
    
    
    public void testOpenRubyProject()
    {
        projectName = "RubyPerfTest"; //NO18N
        doMeasurement();
    }
    public void testOpenRailsProject()
    {
        projectName = "RailsPerfTest";
        doMeasurement();
    }
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenRubyProject("testOpenRubyProject"));
    }    

}
