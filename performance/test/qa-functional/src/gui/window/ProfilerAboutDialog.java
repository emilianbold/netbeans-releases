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

import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ProfilerAboutDialog  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private JDialogOperator about;
    /**
     * 
     * @param testName 
     */
    public ProfilerAboutDialog(String testName) {
        super(testName);
        expectedTime =  WINDOW_OPEN;
    }
    /**
     * 
     * @param testName 
     * @param performanceDataName
     */    
    public ProfilerAboutDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime =  WINDOW_OPEN;        
    }
    /**
     *  initialize 
     */
    public void initialize(){
        log("::initialize::");
    }
    
    public void prepare() {
        log("::prepare");
    }    
    public ComponentOperator open() {
        new ActionNoBlock("Profile|Help|"+org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle", "LBL_AboutProfilerAction"),null).performMenu(); // NOI18N
        about = new JDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.actions.Bundle", "AboutProfilerAction_AboutDialogCaption"));
        return null;
    }
    
    public void close() {
        about.close();
  
    }
    protected void shutdown() {
        log(":: shutdown");
    }    

}
