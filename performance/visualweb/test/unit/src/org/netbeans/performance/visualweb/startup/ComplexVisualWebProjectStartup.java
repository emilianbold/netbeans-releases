/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.performance.visualweb.startup;

//import java.awt.Component;
//import java.awt.Container;
import java.io.File;
import java.io.IOException;
//import java.util.Set;
//import org.openide.windows.TopComponent;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog.
 * Number of starts with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 * Run measurement defined number times, but forget first measured value,
 * it's a attempt to have still the same testing conditions with
 * loaded and cached files.
 *
 * @author mmirilovic@netbeans.org, mkhramov@netbeags.org
 */
public class ComplexVisualWebProjectStartup extends org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase {    

    private static final String TCN = "org.netbeans.modules.visualweb.designer.DesignerPane";
    private long MAX_TIMEOUT = 1000000;
    private long SLEEP_TIME = 50;
    
    private long timeoutTime = 0;    
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public ComplexVisualWebProjectStartup(String testName) {
        super(testName);
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithOpenedVWProject() throws java.io.IOException {
        measureComplexStartupTime("Startup Time with opened Visual Web project");
    }
    @Override
    protected long runIDEandMeasureStartup(String performanceDataName, File measureFile, File userdir, long timeout) throws IOException {
        long startupTimeNoDocLoaded = super.runIDEandMeasureStartup(performanceDataName, measureFile, userdir, timeout);
        long docLoadTime = waitDocumentLoaded();
        reportPerformance(performanceDataName+ " | Page load", docLoadTime, "ms", 1);
        return startupTimeNoDocLoaded+docLoadTime;
        
    }
    private long waitDocumentLoaded() {
//        long startTime = System.currentTimeMillis();
//        try {
//            waitDocumentLoadedViaAPI();
//        } catch(InterruptedException ie) {
//            fail("Document loading failed because of "+ie.toString());
//        }
//        long stopTime = System.currentTimeMillis();
//        long delta = stopTime-startTime;
//        if(delta <= 0) {
//            fail("Measured value ["+delta+"] is not > 0 !");
//        }
        return 0; //delta;
    }
    /*
    private void waitDocumentLoadedViaAPI() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        
        // Wait for TopComponent
        TopComponent tc;
        while((tc = findTopComponent("Page1")) == null) {
            Thread.currentThread().sleep(SLEEP_TIME);
            if(timeoutExceed(startTime)) {
               fail("waitDocumentLoadedViaAPI:findTopComponent wait exceeds "+MAX_TIMEOUT);
            }
        }
        
        startTime = System.currentTimeMillis();
        //Wait for Designer Surface loaded into TopComponent
        while(findNestedComponent(tc, TCN) == false) {
            Thread.currentThread().sleep(SLEEP_TIME);
            if(timeoutExceed(startTime)) {
               fail("waitDocumentLoadedViaAPI:findNestedComponent wait exceeds "+MAX_TIMEOUT);
            }            
        }
    }
    private boolean timeoutExceed(long startTime) {
        long timeout = System.currentTimeMillis() - startTime;
        if(timeout >= MAX_TIMEOUT) {
            return true;
        }
        return false;
    }
    private TopComponent findTopComponent(String componentName) {
        log("finding TopComponent...");
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        log("taken a list of TCs");
        
        for (TopComponent tc : tcs) {          
          if(tc.getName().equals(componentName) && (tc.isShowing()))  {
              log("function findTopComponent passed with success");
              return tc;
          }
        }
        log("function findTopComponent passed with no result");
        return null;
    }
    private boolean compareClass(Component x) {
        return x.getClass().getName().equals(TCN);
    }    
    private boolean findNestedComponent(Container x, String componentClassName) {
        log("finding nested components");
        Component[] child = x.getComponents();
        
        if(child.length == 0) { // No nested components
            log("no nested components found in container. returning");
            return false;
        }
        log("enumeration nested components");
        // Passed component has nested components
        for(Component c : child) {
            if(compareClass(c) && c.isShowing()) { 
                log("expected component found in container. returning");
                return true; 
            }
            log("try to find expected component in current");
            if(findNestedComponent((Container)c,componentClassName)) { return true; }            
        }
        log("expected component not found neither in nested components nor in current container");
        return false;
    }
*/
}
