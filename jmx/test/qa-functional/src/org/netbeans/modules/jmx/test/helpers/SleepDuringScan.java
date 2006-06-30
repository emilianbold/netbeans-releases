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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

/**
 *
 * @author an156382
 */
public class SleepDuringScan extends JellyTestCase {

    /** Creates a new instance of CloseAllDocuments */
    public SleepDuringScan(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SleepDuringScan("createSampleProject"));
        suite.addTest(new SleepDuringScan("waitScan"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        
    }
    
    public void tearDown() {
        
    }
    public void createSampleProject() {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Samples|Management");
        npwo.selectProject("Anagram Game Managed with JMX");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        
        npnlso.txtProjectName().setText("JMXTESTBootstrapMDRScan");
        npwo.finish();
        
    }
    
    public void waitScan() {
        int time2wait = 60000;
        int loop = 2;
        while(loop > 0) {
            System.out.println("Need to sleep " + loop + " minutes");
            try {
                Thread.sleep(time2wait);
            }catch(java.lang.Exception e) {}
            System.out.println("Slept 1 minute, back to sleep");
            loop--;
        }
    }
}
