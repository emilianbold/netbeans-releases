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
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ProfilerCalibrationDialog   extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private NbDialogOperator jPlatformDialog;
    private NbDialogOperator calibrationInfoDialog;
    /**
     * 
     * @param testName 
     */
    public ProfilerCalibrationDialog(String testName) {
        super(testName);
        expectedTime =  WINDOW_OPEN;
    }
    /**
     * 
     * @param testName 
     * @param performanceDataName
     */    
    public ProfilerCalibrationDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime =  WINDOW_OPEN;        
    }
    public void initialize() {
        log(":: initialize");
    }
    public void prepare() {
        log("::prepare");
        String calibrateCmd = "Profile"+"|"+"Advanced Commands"+"|"+"Run Profiler Calibration";
        new ActionNoBlock(calibrateCmd,null).performMenu();
        jPlatformDialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.actions.Bundle", "JavaPlatformSelector_SelectPlatformCalibrateDialogCaption"));
        
    } 

    public ComponentOperator open() {
        jPlatformDialog.ok();
        calibrationInfoDialog = new NbDialogOperator("Information"); 
        return null;
    }
    public void close() {
        calibrationInfoDialog.ok();
        
    }
    public void shutdown() {
        log(":: shutdown");
    }
}
