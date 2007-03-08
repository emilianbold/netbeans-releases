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

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class JSFComponentOptionsDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected PaletteComponentOperator palette;
    protected WebFormDesignerOperator surface;
    protected String categoryName;
    protected String componentName;
    protected java.awt.Point addPoint;
    
    /** Creates a new instance of JSFComponentOptionsDialog */
    public JSFComponentOptionsDialog(String testName) {
        super(testName);
    }
    
    /**
     *
     * @param testName
     * @param performanceDataName
     */
    public JSFComponentOptionsDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public void initialize() {
        log("::initialize");
        
        PaletteComponentOperator.invoke();
        openPageAndAddComponent();
    }
    
    private void openPageAndAddComponent() throws Error {
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        palette = new PaletteComponentOperator();
        
        //Select component in palette
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
        
        //Click on design surface to add selected component on page
        surface.clickOnSurface(new Double(addPoint.getX()).intValue(),new Double(addPoint.getY()).intValue());
        
        long click1 = System.currentTimeMillis();
        log(":: click on surface");
        //Click some other surface point to make added component deselected
        
        new QueueTool().waitEmpty();
        long click2 = System.currentTimeMillis();
        surface.clickOnSurface(10,10);
        log(":: click on surface");
        log(":: Delta = " +(click2-click1));
        waitNoEvent(5000);
    }
    
    public void prepare() {
        log("::prepare");
    }
    
    public ComponentOperator open(){
        log("::open");
        return null;
    }
    
    protected void shutdown() {
        log(":: shutdown");
        surface.closeDiscard();
    }
    
}
