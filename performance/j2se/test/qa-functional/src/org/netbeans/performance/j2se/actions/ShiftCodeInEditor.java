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

import java.awt.event.KeyEvent;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of Page Up and Page Down in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class ShiftCodeInEditor extends PerformanceTestCase {
    
    private int line=1;
    private EditorOperator editorOperator;

    public static final String suiteName="UI Responsiveness J2SE Actions";    
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public ShiftCodeInEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public ShiftCodeInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }
    
    public void testShiftCode1Line(){
        line=1;
        doMeasurement();
    }

    public void testShiftCode100Lines(){
        line=100;
        doMeasurement();
    }

    public void testShiftCode1000Lines(){
        line=1000;
        doMeasurement();
    }
    
    @Override
    public void initialize() {
//        EditorOperator.closeDiscardAll();
        
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
        
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new SourcePackagesNode("jEdit"), "com.microstar.xml|XmlParser.java"));
        editorOperator = EditorWindowOperator.getEditor("XmlParser.java");
    }
    
    public void prepare() {
        editorOperator.select(1, line);
   }
    
    public ComponentOperator open(){
        new Action("Source|Shift Right", null).perform(editorOperator);
        return null;
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        repaintManager().resetRegionFilters();
    }
    
}
