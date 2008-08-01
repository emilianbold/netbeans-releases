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

package org.netbeans.performance.j2se.actions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of opening files, where CloneableEditor isn't super class of their editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditor extends OpenFiles {

    private String compName;
    
    public static final String suiteName="UI Responsiveness J2SE Actions";    

    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public void testOpening20kBPropertiesFile(){
        WAIT_AFTER_OPEN = 2500;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        compName = "Bundle20kB.properties";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBPictureFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        compName = "splash.gif";
        menuItem = OPEN;
        doMeasurement();
    }
        
    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
    }
    
    @Override
    protected void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    @Override
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error ("Cannot get context menu for node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error ("Cannot push menu item ["+this.menuItem+"] of node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new TopComponentOperator(compName);
    }
    
    @Override
    public void close(){
/*        if (testedComponentOperator != null) {
            ((TopComponentOperator)testedComponentOperator).close();
        }
        else {
            throw new Error ("no component to close");
        }
*/    }
}
