/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PageFragmentBoxDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase  {
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    private TopComponentOperator navigator;
    
    private static String dlgName, menuCmd;
    
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
        JComboBoxOperator modeCombo = new JComboBoxOperator(navigator);
        modeCombo.selectItem("Outline");        
        
        JTreeOperator tree =  new JTreeOperator(navigator);
        
        return new Node(tree,"Page1|page1|html1|body1|form1|div|directive.include");
    }
            
    protected void initialize() {
        log("::initialize");
        
        dlgName = Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitle"); //Select Page Fragment
        menuCmd = Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitleEllipse"); // Select Page Fragment...
        new ActionNoBlock("Window|Navigating|Navigator",null).perform(); //NOI18N
        PaletteOperator.invoke();

        addPFBComponent();
        prepareCloseBoxDialog();
    }
    
    private void prepareCloseBoxDialog() {
        NbDialogOperator boxDialog = new NbDialogOperator(dlgName);
        waitNoEvent(1000);
        boxDialog.close();
    }
    
    private void addPFBComponent() throws Error {
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator("Layout").selectItem("Page Fragment Box"); //  NOI18N
        
        surface.clickOnSurface(50,50);
        waitNoEvent(5000);
    }
    
    protected void shutdown() {
        log("::shutdown");
        surface.closeDiscard();
        try {
            new TopComponentOperator(Bundle.getString("org.netbeans.modules.visualweb.ravehelp.dynamichelp.Bundle", "MSG_DynamicHelpTab_name")).close();            
            new PropertySheetOperator("Page1").close();   
        } catch (TimeoutExpiredException timeoutExpiredException) {
            //do nothing...can be not opened properties and help tabs
        }        
    }
    
    public void close() {
        super.close();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new PageFragmentBoxDialog("measureTime","Add Page Fragment Box Dialog open time"));
    }
}
