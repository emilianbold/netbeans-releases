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
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down button if 10 files opened
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenu extends PerformanceTestCase {

    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    
    public void testEditorDownButtonPopupMenu(){
        doMeasurement();
    }
    
    @Override
    public void initialize(){
        CommonUtilities.openFiles("jEdit", getTenSelectedFiles());
        waitNoEvent(20000);
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        EditorWindowOperator.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(EditorWindowOperator.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
  }
    
    @Override
    public void shutdown(){
        EditorWindowOperator.closeDiscard();
    }
    
    private static String[][] getTenSelectedFiles(){
        String[][] files_path = {
            {"bsh","Interpreter.java"},
            {"bsh","JThis.java"},
            {"bsh","Name.java"},
            {"bsh","Parser.java"},
            {"bsh","Primitive.java"},
            {"com.microstar.xml","XmlParser.java"},
            {"org.gjt.sp.jedit","BeanShell.java"},
            {"org.gjt.sp.jedit","Buffer.java"},
            {"org.gjt.sp.jedit","EditPane.java"},
            {"org.gjt.sp.jedit","EditPlugin.java"},
            {"org.gjt.sp.jedit","EditServer.java"}
        };
        return files_path;
    }
    
}
