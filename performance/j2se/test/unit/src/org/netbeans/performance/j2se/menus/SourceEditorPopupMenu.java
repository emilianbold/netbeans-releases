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

package org.netbeans.performance.j2se.menus;

import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import java.awt.event.KeyEvent;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on Source Editor pane.
 *
 * @author  mmirilovic@netbeans.org
 */
public class SourceEditorPopupMenu extends PerformanceTestCase {
    
    private static String stringToInvokePopup;
    private static boolean setCaretPositionAfterString;
    private static EditorOperator editor;
    private static String fileName;
    
    /** Creates a new instance of SourceEditorPopupMenu */
    public SourceEditorPopupMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Creates a new instance of SourceEditorPopupMenu */
    public SourceEditorPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SourceEditorPopupMenu("testPopupInTxt"));
        suite.addTest(new SourceEditorPopupMenu("testPopupInXml"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName"));
        return suite;
    }
    
    public void testPopupInTxt(){
        fileName = "textfile.txt";
        stringToInvokePopup = "***********";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupInXml(){
        fileName = "xmlfile.xml";
        stringToInvokePopup = "<root";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupOnMethod(){
        fileName = "Main.java";
        stringToInvokePopup = "javax.swing.JPa";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupOnClassName(){
        fileName = "Main.java";
        stringToInvokePopup = "class Mai";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    
    public void initialize(){
        Node fileNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        
        if (fileName.endsWith("xml")) {
            new EditAction().performAPI(fileNode);
        }
        else {
            new OpenAction().performAPI(fileNode);
        }
        editor = new EditorOperator(fileName);
        waitNoEvent(2000);  // annotations, folds, toolbars, ...
    }
    
    public void prepare(){
        editor.setCaretPosition(stringToInvokePopup,setCaretPositionAfterString);
    }
    
    public ComponentOperator open(){
        editor.pushKey(KeyEvent.VK_F10, KeyEvent.SHIFT_MASK);
        return new JPopupMenuOperator();
    }
    
    public void shutdown(){
        editor.closeDiscardAll();
    }
    
}
