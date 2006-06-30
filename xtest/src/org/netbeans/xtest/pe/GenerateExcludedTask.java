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

package org.netbeans.xtest.pe;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.xtest.harness.MTestConfig;
import org.netbeans.xtest.harness.Testbag;
import org.netbeans.xtest.harness.Testbag.InExclude;
import org.netbeans.xtest.harness.Testbag.Patternset;
import org.netbeans.xtest.harness.Testbag.Testset;
import org.netbeans.xtest.xmlserializer.XMLSerializer;
import org.netbeans.xtest.util.SerializeDOM;

/** Generate config with excluded tests from given config file. New config
 * is written to output directory and named cfg-testtype-excluded.xml. If only
 * input directory is given, it processes all found config files.
 */
public class GenerateExcludedTask extends Task{
    
    /** Suffix added to original config file name. */
    private static final String EXCLUDED_CONFIG_SUFFIX = "-excluded";
    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("GenerateFailedTask."+message);
    }

    /** Directory where to store generated config. */
    private File outputDir;
    
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    private File inputDir;
    
    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }
    
    private File configFile;
    
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
    
    public void execute() throws BuildException {
        try {
            log("Generating excluded tests config");
            if(configFile == null || !configFile.isFile()) {
                // scan whole input directory
                File[] list = inputDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith("cfg-");
                    }
                });
                for(int i=0;i<list.length;i++) {
                    generateExcludedConfig(list[i], outputDir);
                }
            } else {
                // use config given in parameter
                generateExcludedConfig(configFile, outputDir);
            }
        } catch (Exception e) {
            log("Exception in GenerateFailedTask:"+e);
            e.printStackTrace(System.err);
        }
    }
    
    /** Regenerate given config file that new config contains only excluded
     * test cases from original config. New config file is named by pattern
     * <original_file_name>-excluded.xml.
     */
    private void generateExcludedConfig(File configFile, File outputDir) throws Exception {
        log("Processing configFile "+configFile);
        // parse config
        MTestConfig mconfig = MTestConfig.loadConfig(configFile);
        Testbag testbags[] = mconfig.getTestbags();
        if (testbags == null) {
            log("No test bag found in config!");
            return;
        }
        // revert excluded to included
        ArrayList newTestbags = new ArrayList();
        for (int i=0; i<testbags.length;i++) {
            Testset[] testsets = testbags[i].getTestsets();
            ArrayList newTestsets = new ArrayList();
            for(int j=0;j<testsets.length;j++) {
                Patternset[] patternsets = testsets[j].getAllPatternset();
                ArrayList newPatternsets = new ArrayList();
                for(int k=0;k<patternsets.length;k++) {
                    InExclude[] excludes = patternsets[k].getExcludes();
                    if(excludes != null) {
                        // revert excludes to includes
                        patternsets[k].setIncludes(excludes);
                        patternsets[k].setExcludes(null);
                        // add this patternset to new config
                        newPatternsets.add(patternsets[k]);
                    }
                }
                if(!newPatternsets.isEmpty()) {
                    testsets[j].setPatternsets((Patternset[])newPatternsets.toArray(new Patternset[newPatternsets.size()]));
                    newTestsets.add(testsets[j]);
                }
            }
            if(!newTestsets.isEmpty()) {
                testbags[i].setTestsets((Testset[])newTestsets.toArray(new Testset[newTestsets.size()]));
                newTestbags.add(testbags[i]);
            }
        }
        if(!newTestbags.isEmpty()) {
            mconfig.setTestbags((Testbag[])newTestbags.toArray(new Testbag[newTestbags.size()]));
            // new filename = original-excluded.xml
            String excludedConfigFilename = new StringBuffer(configFile.getName()).insert(configFile.getName().lastIndexOf('.'), EXCLUDED_CONFIG_SUFFIX).toString();
            File outputFile = new File(outputDir, excludedConfigFilename);
            // save new config file
            SerializeDOM.serializeToFile(XMLSerializer.toDOMDocument(mconfig), outputFile);
            log("Output config file: "+outputFile);
        } else {
            log("No excluded test cases found.");
            return;
        }
    }
}


