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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PageFragmentBoxDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase  {
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    
    private static final String dlgName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitle"); //Select Page Fragment
    
    /** Creates a new instance of PageFragmentBoxDialog */
    public PageFragmentBoxDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=5000;          
    }
    
    public PageFragmentBoxDialog(String testName,String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=5000;          
    }
    
    public void prepare() {
        log("::prepare");
        surface.clickOnSurface(10,10);
    }
    
    public ComponentOperator open() {
        String menuCmd = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.xhtml.Bundle", "fragmentCustTitleEllipse"); // Select Page Fragment...
        log("::menu cmd = "+menuCmd);
        surface.pushPopupMenu(menuCmd, 60, 60);
                
        return new NbDialogOperator(dlgName);
    }
    
    protected void initialize() {
        log("::initialize");
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
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator("Layout").selectItem("Page Fragment Box"); //  NOI18N
        
        surface.clickOnSurface(50,50);
        waitNoEvent(5000);
    }
    
    protected void shutdown() {
        log("::shutdown");
        surface.closeDiscard(); 
    }    
    
    public void close() {
        super.close();        
    }
    
    public static void main(String[] args) {
       junit.textui.TestRunner.run(new PageFragmentBoxDialog("measureTime","Add Page Fragment Box Dialog open time")); 
    }
    
}
