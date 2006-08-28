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

public class Validity extends NbTestCase {

    public Validity(java.lang.String testName) {
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
  /*  
    public void testAWTFormValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.AWTFormObject.AWTFormObject_validity("testDOValidity").testDOValidity();
    }    
    public void testClassValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.ClassObject.ClassObject_validity("testDOValidity").testDOValidity();
    }
   */
    public void testHTMLValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.HTMLObject.HTMLObject_validity("testDOValidity").testDOValidity();
    }
    public void testImageValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.ImageObject.ImageObject_validity("testDOValidity").testDOValidity();
    }
    public void testJSPValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.JSPObject.JSPObject_validity("testDOValidity").testDOValidity();
    }
    /*
    public void testJavaFormValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.JavaSourceObject.JavaSourceObject_validity("testDOValidity").testDOValidity();
    }*/
    /*
     * removed test. The Refarctoring shows dialog :(
     *
    public void testPackageValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.Package.Package_validity("testDOValidity").testDOValidity();
    }
     
    public void testSecurityJAppletValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.SecurityJApplet.SecurityJApplet_validity("testDOValidity").testDOValidity();
    }
    public void testSwingFormValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.SwingFormObject.SwingFormObject_validity("testDOValidity").testDOValidity();
    }
     */
    public void testTextualValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.TextualObject.TextualObject_validity("testDOValidity").testDOValidity();
    }
    public void testURLValidity() throws Exception {
        new DataLoaderTests.DataObjectTest.validity.URLObject.URLObject_validity("testDOValidity").testDOValidity();
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(Validity.class);
        return suite;
    }
  
}
