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

package org.netbeans.performance.j2ee.dialogs;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author  lmartinek@netbeans.org
 */
public class InvokeEJBAction extends PerformanceTestCase {
    
    private static EditorOperator editor;
    
    private String popupMenu = null;
    private String dialogTitle = null;
    
    /**
     * Creates a new instance of InvokeEJBAction 
     */
    public InvokeEJBAction(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    /**
     * Creates a new instance of InvokeEJBAction 
     */
    public InvokeEJBAction(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    public void testAddBusinessMethodDialog(){
        popupMenu = "EJB Methods|Add Business Method";
        dialogTitle = "Add Business Method";
        doMeasurement();
    }

    public void testCallEJBDialog(){
        popupMenu = "Enterprise Resources|" + 
                org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.Bundle", "LBL_CallEjbAction");
        dialogTitle = "Call Enterprise Bean";
        doMeasurement();
    }
    
    public void initialize() {
        
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-EJBModule"),"Enterprise Beans|TestSessionSB");
        new OpenAction().performAPI(openFile);
        editor = new EditorWindowOperator().getEditor("TestSessionBean.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        editor.select(11);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK); 
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null,popupMenu).perform(editor);
        return new NbDialogOperator(dialogTitle);
    }

    public void shutdown(){
        editor.closeDiscard();
    }
    
}
