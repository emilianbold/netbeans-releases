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

package org.netbeans.performance.visualweb.dialogs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.performance.visualweb.windows.PaletteComponentOperator;
import org.netbeans.performance.visualweb.windows.WebFormDesignerOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PageFragmentBoxDialog extends org.netbeans.modules.performance.utilities.PerformanceTestCase  {
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    private TopComponentOperator navigator;
    
    private static String dlgName, menuCmd;
    public static final String suiteName="UI Responsiveness VisualWeb Dialogs suite";
    /** Creates a new instance of PageFragmentBoxDialog 
     * 
     * @param testName 
     * 
     */
    public PageFragmentBoxDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=5000;
    }
    /** Creates a new instance of PageFragmentBoxDialog 
     * 
     * @param testName 
     * @param performanceDataName
     * 
     */    
    public PageFragmentBoxDialog(String testName,String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=5000;
    }
    
    public void testPageFragmentBoxDialog() {
        doMeasurement();
    }
    
    public void prepare() {
        log("::prepare");
        //surface.clickOnSurface(10,10);
    }
    
    public ComponentOperator open() {
        log("::menu cmd = "+menuCmd);
        
        Node openNode = selectFragmentNodeInNavigatorTree();
        JPopupMenuOperator popup =  openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for pagefragment node ");
        }
        try {
            popup.pushMenu(menuCmd);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item "+menuCmd+" on pagefragment node ");
        }        
        
        return new NbDialogOperator(dlgName);
    }
    private Node selectFragmentNodeInNavigatorTree() {
        navigator = new TopComponentOperator("Navigator"); // NOI18N  
        
        JTreeOperator tree =  new JTreeOperator(navigator);
        
        return new Node(tree,"Page1|page1|html1|body1|form1|div|directive.include");
    }
            
    @Override
    protected void initialize() {
        log("::initialize");
        
        dlgName = Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitle"); //Select Page Fragment
        menuCmd = Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitleEllipse"); // Select Page Fragment...
        new ActionNoBlock("Window|Navigating|Navigator",null).perform(); //NOI18N
        PaletteComponentOperator.invoke();

        addPFBComponent();
        prepareCloseBoxDialog();
    }
    
    private void prepareCloseBoxDialog() {
        NbDialogOperator boxDialog = new NbDialogOperator(dlgName);
        waitNoEvent(1000);
        boxDialog.close();
    }
    
    private void addPFBComponent() throws Error {
        surface = org.netbeans.performance.visualweb.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator("Layout").selectItem("Page Fragment Box"); //  NOI18N
        
        surface.clickOnSurface(50,50);
        waitNoEvent(5000);
    }
    
    @Override
    protected void shutdown() {
        log("::shutdown");
        surface.closeDiscard();
        try {
            //new TopComponentOperator(Bundle.getString("org.netbeans.modules.visualweb.ravehelp.dynamichelp.Bundle", "MSG_DynamicHelpTab_name")).close();            
            new PropertySheetOperator("Page1").close();   
        } catch (TimeoutExpiredException timeoutExpiredException) {
            //do nothing...can be not opened properties and help tabs
        }        
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new PageFragmentBoxDialog("measureTime","Add Page Fragment Box Dialog open time"));
    }
}
