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

package org.netbeans.performance.j2se.actions;

import javax.swing.KeyStroke;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * 
 */
public class SwitchViewTest  extends PerformanceTestCase {
    
        private String operator;
        private int ke;

    /** Creates a new instance of SwitchToFile */
    public SwitchViewTest(String testName) {
        super(testName);
        ProjectsTabOperator.invoke();
        RuntimeTabOperator.invoke();
        FilesTabOperator.invoke();
        FavoritesOperator.invoke();
        expectedTime = 100;
        WAIT_AFTER_OPEN=200;
    }
    
    /** Creates a new instance of SwitchView */
    public SwitchViewTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        ProjectsTabOperator.invoke();
        RuntimeTabOperator.invoke();
        FilesTabOperator.invoke();
        FavoritesOperator.invoke();
        expectedTime = 100;
        WAIT_AFTER_OPEN=200;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(SwitchViewTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testSwitchToServices(){
        ke=java.awt.event.KeyEvent.VK_5;
        operator="Services";
        doMeasurement();
    }

    public void testSwitchToProjects(){
        ke=java.awt.event.KeyEvent.VK_1;
        operator="Projects";
        doMeasurement();
    }

    public void testSwitchToFiles(){
        ke=java.awt.event.KeyEvent.VK_2;
        operator="Files";
        doMeasurement();
    }

    public void testSwitchToFavorites(){
        ke=java.awt.event.KeyEvent.VK_3;
        operator="Favorites";
        doMeasurement();
    }
    
    @Override
    protected void initialize() {
    }
        
    public void prepare() {
        if (!operator.equals("Projects")) new TopComponentOperator("Projects").makeComponentVisible();
           else new TopComponentOperator("Services").makeComponentVisible();
    }
    
    public ComponentOperator open() {
        new Action(null, null, KeyStroke.getKeyStroke(ke, java.awt.event.KeyEvent.CTRL_MASK)).performShortcut();
        return null;
    }
    
    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
    }

}
