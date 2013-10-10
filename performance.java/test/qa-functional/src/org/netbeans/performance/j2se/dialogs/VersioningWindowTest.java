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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test opening Versioning Tab.
 *
 * @author  mmirilovic@netbeans.org
 */
public class VersioningWindowTest extends PerformanceTestCase {

    private String MENU, NAME;
    

    /** Creates a new instance of VersioningWindow */
    public VersioningWindowTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of VersioningWindow */
    public VersioningWindowTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(VersioningWindowTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void prepare() {
    }
    
     public void testCVSVersioningWindow() {
        NAME=Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.status.Bundle", "BK0001"); // Window | Versioning | CVS
        MENU = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window") + '|' +
                Bundle.getStringTrimmed("org.netbeans.modules.versioning.Bundle","Menu/Window/Versioning") + '|' + NAME;
        doMeasurement();
    }

     public void testMercurialVersioningWindow() {
        NAME=Bundle.getStringTrimmed("org.netbeans.modules.mercurial.Bundle", "CTL_Mercurial_DisplayName"); // Window | Versioning | Mercurial
        MENU = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window") + '|' +
               Bundle.getStringTrimmed("org.netbeans.modules.versioning.Bundle","Menu/Window/Versioning") + '|' + NAME;
        doMeasurement();
    }

    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(MENU,"|");
        return new TopComponentOperator(NAME);
    }
    
    @Override
    public void close() {
        new TopComponentOperator(NAME).close();
    }
 
}
