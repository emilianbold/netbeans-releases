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

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class VirtualFormsDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private WebFormDesignerOperator surface;
    
    /** Creates a new instance of VirtualFormsDialog */
    public VirtualFormsDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=5000;
    }
    public VirtualFormsDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 3000;
        WAIT_AFTER_OPEN=5000;
    }
    
    public void initialize() {
        log("::initialize");
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
    }
    
    public void prepare() {
        log("::prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        
        //Invoking popup menu on component
        JPopupMenuOperator popup = surface.clickPopup(70,70);
        if(popup == null) { log("popup operator is null");}
        
        popup.pushMenu("Virtual Forms..."); // NO I18N
        
        return new DialogOperator("Virtual Forms"); // NO I18N
    }

    protected void shutdown() {
        log(":: shutdown");
        surface.close();
    }
}
