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

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class PageStyleSheetDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private WebFormDesignerOperator surface;
    private PropertySheetOperator pto;
    private Property property;
    private JDialogOperator styleDialog;
    
    /**
     * 
     * @param testName 
     */
    public PageStyleSheetDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;             
    }
    /**
     * 
     * @param testName 
     * @param performanceDataName
     */    
    public PageStyleSheetDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;             
    }
    protected void initialize() {
        log(":: initialize");
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");        
    }
    public void prepare() {
        log(":: prepare");
        pto =  new PropertySheetOperator("Page1").invoke();
        property = new Property(pto,org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.insync.live.Bundle", "LBL_StyleSheet"));
        
    }

    public ComponentOperator open() {
        log(":: open");
        property.openEditor();
        styleDialog = new JDialogOperator("Page1");
        return null;
    }
    public void close() {
        log(":: close");
        styleDialog.close();
        super.close();
    }
    protected void shutdown() {
        log(":: shutdown");
        pto.close();
        try {
            new TopComponentOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.ravehelp.dynamichelp.Bundle", "MSG_DynamicHelpTab_name")).close();
        } catch (TimeoutExpiredException timeoutExpiredException) {
            // do nothing 
        }
        super.shutdown();
    }

}
