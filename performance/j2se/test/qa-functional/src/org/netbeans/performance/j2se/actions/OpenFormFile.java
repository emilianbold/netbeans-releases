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

package org.netbeans.performance.j2se.actions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;


/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFormFile extends OpenFilesNoCloneableEditor {

   public static final String suiteName="UI Responsiveness J2SE Actions";
   
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenFormFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFormFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    
    public void testOpening20kBFormFile(){
        WAIT_AFTER_OPEN = 15000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        // don't measure paint events from StatusLine
        repaintManager().addRegionFilter(STATUSLINE_FILTER);
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        // reset filter
        repaintManager().resetRegionFilters();
    }
    
    @Override
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            new java.lang.Error("Cannot get context menu for node [" + openNode.getPath() + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error ("Cannot push menu item "+this.menuItem+" of node [" + openNode.getPath() + "]");
        }
        log("------------------------- after open ------------");
        return new FormDesignerOperator("JFrame20kB");
    }

    @Override
    public void close() {
//        ((FormDesignerOperator)testedComponentOperator).close();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFormFile("testOpening20kBFormFile"));
    }
    
    private static final RegionFilter STATUSLINE_FILTER =
            new RegionFilter() {

                public boolean accept(javax.swing.JComponent c) {
                    return !c.getClass().getName().equals("org.netbeans.core.windows.view.ui.StatusLine");
                }

                public String getFilterName() {
                    return "Don't accept paints from org.netbeans.core.windows.view.ui.StatusLine";
                }
            };
    
}
