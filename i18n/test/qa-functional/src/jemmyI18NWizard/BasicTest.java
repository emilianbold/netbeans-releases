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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package jemmyI18NWizard;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.test.oo.gui.jelly.JellyProperties;
import org.netbeans.test.oo.gui.jam.JamUtilities;

import jemmyI18NWizard.wizardSupport.*;
import org.netbeans.jemmy.operators.JListOperator;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.oo.gui.jam.Jemmy;


/**
 * <B>What it tests:</B>
 * Go through Internationalization Wizard. It is the ideal shortest path.
 * Then it checks new generated items
 * in resource bundles, internationalized sources and log file.
 */
public class BasicTest extends NbTestCase {
    
    
    public BasicTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() {
        // redirect error and trace messages from Jemmy
        JellyProperties.setJemmyOutput(new PrintWriter(getLog(), true), new PrintWriter(getLog(), true));
        JellyProperties.setDefaults();
    }
    
    public void test() {
        try {
            log("###### Opening wizard.");
            Page0 page0 = new Page0();
            log("###### Wizard opened.");
            log("###### Removing selected sources.");
            for(int i=0; i<page0.getItemCount(); i++) {
                page0.selectItem(i);
                page0.removeSource();
            }
            assertEquals("Selected sources list must be empty.", page0.getItemCount(), 0);
            log("###### Clicking 'Add Source(s)' button.");
            page0.addSource();

            log("###### Opening 'Select Sources' dialog.");
            SelectSourcesDialog selSour = new SelectSourcesDialog("Select Sources");
            log("###### 'Select Sources' dialog opened.");
            
            log("###### Selecting testing filesystem.");
            assertTrue("Filesystem '" + Utilities.getFilesystemPath() +"' not selected.", selSour.selectFilesystem(Utilities.getFilesystemPath()));
            
            log("###### Selecting source files in tree.");
            selSour.selectPath(new String[] {"jemmyI18NWizard", "data", "SimpleMainClass"});
            
            log("###### Clicking 'OK' button.");
            selSour.ok();
            
            JamUtilities.waitEventQueueEmpty(500);
            assertEquals("Source file SimpleMainClass.java not present in list", page0.getItemCount(), 1);
            page0.selectItem(0);
            //String selected = page0.getSelectedItem();
            // use jelly2 here because jelly1 is not reliable
            Object selected = new JListOperator(Jemmy.getOp(page0), 1).getSelectedValue();
            assertNotNull("getSelectedValue() returned null.", selected);
            assertTrue("Source file SimpleMainClass.java not selected", selected.toString().endsWith("SimpleMainClass.java]"));
            
            log("###### Clicking 'Next >' button.");
            page0.next();
            
            log("###### Opening second window of wizard.");
            Page1 page1 = new Page1();
            log("###### Window succesfully opened.");
            
            log("###### Bundle should be selected - continuing to next window.");
            log("###### Clicking 'Next >' button.");
            page1.next();
            
            log("###### Opening third window.");
            Page2 page2 = new Page2();
            log("###### Window succesfully opened.");
            
            log("###### Finishing.");
            log("###### Clicking 'Finish' button.");
            page2.finish();
            
            log("###### Test finished.");
            log("###### Saving source file.");
            try {
                Utilities.saveFile("jemmyI18NWizard/data/SimpleMainClass.java");
            } catch(Exception e) {
                log("###### Error when saving source files.");
                fail("Error saving source file.");
            }
            log("###### Source file saved succesfully.");
            
            log("###### Saving bundle.");
            try {
                Utilities.saveFile("jemmyI18NWizard/data/Bundle.properties");
            } catch(Exception e) {
                log("###### Error when saving bundle.");
                fail("###### Error saving bundle.");
            }
            log("###### Bundle saved succesfully.");
            
            log("###### Comparing sources.");
            String sep = File.separator;
            String dataPath = Utilities.getFilesystemPath()+sep+"jemmyI18NWizard"+sep+"data";
            assertFile("Generated changes in SimpleMainClass differ.", dataPath+sep+"SimpleMainClass.java", dataPath+sep+"goldenfiles"+sep+"BasicTest"+sep+"SimpleMainClass.pass", getWorkDirPath()+sep+"SimpleMainClass.diff");
            
            log("###### Comparing bundles.");
            
            if(!Utilities.compareBundles(Utilities.getFilesystemPath()+"/jemmyI18NWizard/data/Bundle.properties"
            , Utilities.getFilesystemPath()+"/jemmyI18NWizard/data/goldenfiles/BasicTest/Bundle.properties"))  {
                log("###### Bundles compared succesfully.");
            } else {
                log("###### Error when comparing bundles.");
                fail("Error comparing bundles.");
            }
            
        } catch (Exception e) {
            // save screenshot
            try {
                PNGEncoder.captureScreen(getWorkDirPath()+File.separator+"screen.png");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            // print and log exception
            e.printStackTrace();
            e.printStackTrace(getLog());
            fail(e.getMessage());
        }
    }
}
