/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestRunInfoTask.java
 *
 * Created on November 13, 2001, 6:56 PM
 */

package org.netbeans.xtest.pe;


import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;
import org.w3c.dom.*;
import org.netbeans.xtest.util.SerializeDOM;

/**
 *
 * @author  mb115822
 * @version 
 */
public class TestRunInfoTask extends Task{

    /** Creates new TestRunInfoTask */
    public TestRunInfoTask() {
    }
    
    private File outfile;
    private String config;
    private String name;
    
    
    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    public void setConfig(String config) {
        this.config = config;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public TestRun getTestRunInfo() {
        TestRun tr = new TestRun();
        tr.xmlat_config = config;
        tr.xmlat_name = name;
        tr.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        return tr;
    }

    public void execute () throws BuildException {
        log("Generating test run info xml");
        TestRun tr;
        try {
            Document doc = SerializeDOM.parseFile(this.outfile);
            tr = (TestRun)XMLBean.getXMLBean(doc);
            //System.out.println("TestRun already created");
            // testrun already created - return
            log("Test run info already exists - skipping");
            return;
        } catch (Exception e) {
            //System.out.println("TestRun not found, have to crete a new one");
            tr = getTestRunInfo();
        }
        //System.err.println("TR:"+tr);
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(tr.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save testrun:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:+e");
            e.printStackTrace(System.err);           
        }
    }
}
