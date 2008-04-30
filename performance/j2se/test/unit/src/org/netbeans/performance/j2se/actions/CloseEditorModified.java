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

package org.netbeans.performance.j2se.actions;

import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Closing Editor tabs.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseEditorModified extends PerformanceTestCase {
    
    /** Editor with opened file */
    public static EditorOperator editorOperator;
    
    /** Dialog with asking for Save */
    private static NbDialogOperator dialog;
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     */
    public CloseEditorModified(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=1500;
    }
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditorModified(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=1500;
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        new OpenAction().performPopup(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = new EditorOperator("Main.java");
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        editorOperator.txtEditorPane().typeText("XXX");
    }
    
    public ComponentOperator open(){
        //TODO issue 44593 new CloseViewAction().performPopup(editorOperator); 
        new CloseViewAction().performMenu(editorOperator); 

        dialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.text.Bundle", "LBL_SaveFile_Title"));
        return dialog;
    }
    
    public void close(){
        dialog.cancel();
    }
    
}
