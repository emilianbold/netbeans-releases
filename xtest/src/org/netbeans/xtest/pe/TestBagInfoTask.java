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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * TestBagInfoTask.java
 *
 * Created on November 13, 2001, 6:56 PM
 */

package org.netbeans.xtest.pe;


import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.TestBag;
import java.io.*;
import org.netbeans.xtest.util.SerializeDOM;

/**
 *
 * @author  mb115822
 * @version
 */
public class TestBagInfoTask extends Task{

    public static final String UNKNOWN = "Unknown";

    /** Creates new TestBagInfoTask */
    public TestBagInfoTask() {
       tb = new TestBag();
       tb.xmlat_name = UNKNOWN;
       tb.xmlat_module = UNKNOWN;
       tb.xmlat_testType = UNKNOWN;
       tb.xmlat_executor = UNKNOWN;
    }
    
    private File outfile;
    private TestBag tb;
    
    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    public void setName(String name) {
        tb.xmlat_name = name;
    }
    
    public void setModule(String module) {
        tb.xmlat_module = module;
    }
    
    public void setTestType(String testType) {
        tb.xmlat_testType = testType;
    }
    
    public void setExecutor(String executor) {
        tb.xmlat_executor = executor;
    }
    
    public void setTestAttribs(String testAttribs) {
        tb.xmlat_testAttribs = testAttribs;
    }

    public void setUnexpectedFailure(String failure) {
        tb.xmlat_unexpectedFailure = failure;
    }
    
    public void execute () throws BuildException {
        log("Generating test bag info xml");
        tb.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        //System.err.println("TB:"+tb);
        try {            
            try {
                TestBag loadedTestBag = ResultsUtils.getTestBag(this.outfile);
                if (!tb.xmlat_name.equals(UNKNOWN)) {
                    loadedTestBag.xmlat_name=tb.xmlat_name;
                }
                if (!tb.xmlat_module.equals(UNKNOWN)|(loadedTestBag.xmlat_module==null)) {
                    loadedTestBag.xmlat_module=tb.xmlat_module;
                }
                if (!tb.xmlat_testType.equals(UNKNOWN)) {
                    loadedTestBag.xmlat_testType=tb.xmlat_testType;
                }
                if (!tb.xmlat_name.equals(UNKNOWN)) {
                    loadedTestBag.xmlat_name=tb.xmlat_name;
                }
                if (!tb.xmlat_executor.equals(UNKNOWN)) {
                    loadedTestBag.xmlat_executor=tb.xmlat_executor;
                }
                if (tb.xmlat_testAttribs!=null) {
                     loadedTestBag.xmlat_testAttribs=tb.xmlat_testAttribs;
                }
                if (tb.xmlat_unexpectedFailure!=null) {
                     loadedTestBag.xmlat_unexpectedFailure=tb.xmlat_unexpectedFailure;
                }                                
                // ok let's use the loadedTestBag instead
                tb = loadedTestBag;
            } catch (Exception e) {
                // nothing bad has happened - testbag is created for the first time
                log("creating testbag info for the first time");
            }
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(tb.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save test bag info:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:"+e);
            e.printStackTrace(System.err);           
        }
    }
}
