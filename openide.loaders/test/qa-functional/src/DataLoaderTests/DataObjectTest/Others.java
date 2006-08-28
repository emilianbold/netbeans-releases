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

package DataLoaderTests.DataObjectTest;

import junit.framework.*;
import org.netbeans.junit.*;

public class Others extends NbTestCase {

    public Others(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private DataObjectTest T = null;

    protected void setUp(){
        T = new DataObjectTest("Dummy");
        //now setting workdir - this class will write nothing into the logs, only the utility class should,
        //however into location for this class        
        T.work = Manager.getWorkDirPath()+
        java.io.File.separator + this.getClass().getName().replace('.',java.io.File.separatorChar)+
        java.io.File.separator + getName();
    }
    
    public void testAWTFormOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.AWTFormObject.AWTFormObject_others("testDOOthers").testDOOthers();
    }    
    public void testClassOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.ClassObject.ClassObject_others("testDOOthers").testDOOthers();
    }
    public void testHTMLOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.HTMLObject.HTMLObject_others("testDOOthers").testDOOthers();
    }
    public void testImageOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.ImageObject.ImageObject_others("testDOOthers").testDOOthers();
    }
    public void testJSPOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.JSPObject.JSPObject_others("testDOOthers").testDOOthers();
    }
    public void testJavaFormOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.JavaSourceObject.JavaSourceObject_others("testDOOthers").testDOOthers();
    }
    public void testPackageOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.Package.Package_others("testDOOthers").testDOOthers();
    }
    public void testSecurityJAppletOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.SecurityJApplet.SecurityJApplet_others("testDOOthers").testDOOthers();
    }
    public void testSwingFormOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.SwingFormObject.SwingFormObject_others("testDOOthers").testDOOthers();
    }
    public void testTextualOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.TextualObject.TextualObject_others("testDOOthers").testDOOthers();
    }
    public void testURLOthers() throws Exception {
        new DataLoaderTests.DataObjectTest.others.URLObject.URLObject_others("testDOOthers").testDOOthers();
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(Others.class);
        return suite;
    }
  
}
