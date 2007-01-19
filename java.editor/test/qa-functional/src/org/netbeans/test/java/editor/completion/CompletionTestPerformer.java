//This class is automatically generated - DO NOT MODIFY (ever)
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
package org.netbeans.test.java.editor.completion;
import java.io.BufferedReader;
import java.io.PrintWriter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.test.editor.LineDiff;
import java.io.File;
import java.io.FileReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**This class is automatically generated from <I>config.txt</I> using bash
 * script <I>create</I>. For any changes, change the code generating script
 * and re-generate.
 *
 * Althought this class is runned as a test, there is no real code. This class
 * is only wrapper between xtest and harness independet test code. Main information
 * source is <B>CompletionTest</B> class ({@link CompletionTest}).
 *
 * @see CompletionTest
 */
public class CompletionTestPerformer extends NbTestCase {
    
    
    // automatic generation of golden files
    protected boolean generateGoledFiles = false;
    
    protected PrintWriter outputWriter  = null;
    
    protected PrintWriter logWriter = null;
    
    /** Need to be defined because of JUnit */
    public CompletionTestPerformer(String name) {
        super(name);
    }
    
    protected void setUp() {
        log("CompletionTestPerformer.setUp started.");
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        log("CompletionTestPerformer.setUp finished.");
        log("Test "+getName()+  "started");
    }
    
    
    protected void tearDown() throws Exception{
        log("Test "+getName()+" finished");
        log("CompletionTestPerformer.tearDown");
        outputWriter.flush();        
        String goldenName = getJDKVersionCode() + "-" + getName() + ".pass";        
        File ref = new File(getWorkDir(), this.getName() + ".ref");
        if(generateGoledFiles) {
            BufferedReader br = null;
            FileWriter fw = null;
            try {
                String newGoldenName = "qa-functional/data/goldenfiles/"+this.getClass().getName().replace('.', '/')+ "/" + goldenName;
                File newGolden = new File(getDataDir().getParentFile().getParentFile().getParentFile(),newGoldenName);
                br = new BufferedReader(new FileReader(ref));
                fw = new FileWriter(newGolden);
                getLog().println("Creating golden file "+newGolden.getName()+" in "+newGolden.getAbsolutePath());
                String s;
                while((s=br.readLine())!=null) fw.write(s+"\n");
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            } finally {
                try {
                    if(fw!=null) fw.close();
                    if(br!=null) br.close();
                } catch (IOException ioe) {
                    fail(ioe.getMessage());
                }
            }
            fail("Generating golden files");            
        }
        File golden =  getGoldenFile(goldenName);
        File diff = new File(getWorkDir(), this.getName() + ".diff");
        logWriter.flush();
        assertFile("Output does not match golden file.", golden, ref, diff, new LineDiff(false));
        
    }
    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.4"))
            return "jdk14";
        
        if (specVersion.startsWith("1.5"))
            return "jdk15";
        
        if (specVersion.startsWith("1.6"))
            return "jdk16";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }
    
    private File resolveGoldenFile(String proposedGoldenFileName) {
        if ("@".equals(proposedGoldenFileName.trim()))
            return getGoldenFile(getJDKVersionCode() + "-" + getName() + ".pass");
        else
            return getGoldenFile(getJDKVersionCode() + "-" + proposedGoldenFileName + ".pass");
    }
       
}
