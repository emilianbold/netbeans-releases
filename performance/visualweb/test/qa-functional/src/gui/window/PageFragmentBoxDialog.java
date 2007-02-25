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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.DialogOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PageFragmentBoxDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase  {
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    
    private static final String dlgName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitle");
    
    /** Creates a new instance of PageFragmentBoxDialog */
    public PageFragmentBoxDialog(String testName) {
        super(testName);
        expectedTime = 2000;
        WAIT_AFTER_OPEN=2000;          
    }
    
    public PageFragmentBoxDialog(String testName,String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 2000;
        WAIT_AFTER_OPEN=2000;          
        
    }
    
    public void prepare() {
        log("::prepare");
        surface.clickOnSurface(10,10);
    }
    
    public ComponentOperator open() {
        JPopupMenuOperator popup = surface.clickPopup(60,60);
        if(popup == null) { log("popup operator is null");}
        
        String menuCmd = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitleEllipse");
        log("::menu cmd = "+menuCmd);
        popup.pushMenu(menuCmd);
                
        return new DialogOperator(dlgName);
    }
    
    protected void initialize() {
        log("::initialize");
        new Action("Window|Palette",null).perform(); //  NOI18N
        addPFBComponent();
        prepareCloseBoxDialog();        
    }

    private void prepareCloseBoxDialog() {
        NbDialogOperator boxDialog = new NbDialogOperator(dlgName);
        waitNoEvent(1000);
        boxDialog.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        
//        ButtonOperator CloseBtn = null;
//        try {
//            CloseBtn = new ButtonOperator(BoxDialog,"Close");
//        } catch (TimeoutExpiredException ex) {
//           fail("Cannot find Close button");
//        }
//        CloseBtn.pushNoBlock();        
    }
    
    private void addPFBComponent() throws Error {
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        palette = new PaletteComponentOperator();
        
        palette.getCategoryListOperator("Layout").selectItem("Page Fragment Box"); //  NOI18N
        surface.clickOnSurface(50,50);
        waitNoEvent(5000);
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        log(":: finalize");
        surface.closeDiscard();        
    }
    
    protected void shutdown() {
        log("::shutdown");
        surface.closeDiscard(); 
    }    
    
    public void close() {
        super.close();        
    }
}
