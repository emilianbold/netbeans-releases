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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Auto Comment Window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class AutoCommentWindow extends PerformanceTestCase {

    private static EditorOperator editor;
    private String MENU, TITLE;

    public static final String suiteName="UI Responsiveness J2SE Dialogs";    
    
    /** Creates a new instance of AutoCommentWindow */
    public AutoCommentWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of AutoCommentWindow */
    public AutoCommentWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public void testAutoCommentWindow() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.javadoc.comments.Bundle","CTL_AUTOCOMMENT_MenuItem");
        TITLE = Bundle.getStringTrimmed("org.netbeans.modules.javadoc.comments.Bundle","CTL_AUTOCOMMENT_WindowTitle");
    
        // open a java file in the editor
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance", "Main20kB.java", true);
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open() {
        // invoke Tools / Auto Comment from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new TopComponentOperator(TITLE);
    }
    
    @Override
    public void close() {
        // close the tab
        new TopComponentOperator(TITLE).close();
    }
    
    @Override
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AutoCommentWindow("measureTime"));
    }
    
}
