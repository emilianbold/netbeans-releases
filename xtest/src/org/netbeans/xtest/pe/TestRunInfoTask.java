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
        TestRun tr;
        try {
            Document doc = SerializeDOM.parseFile(this.outfile);
            tr = (TestRun)XMLBean.getXMLBean(doc);
            //System.out.println("TestRun already created");
            // testrun already created - return
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
            System.err.println("TestRunInfoTask - cannot save testrun");
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println("TestRunInfoTask - XMLBean exception ???");
            e.printStackTrace(System.err);           
        }
    }
}
