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

public class Modify extends NbTestCase {

    public Modify(java.lang.String testName) {
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
    
    public void testAWTFormModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.AWTFormObject.AWTFormObject_modify("testDOModify").testDOModify();
    }    
    public void testClassModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.ClassObject.ClassObject_modify("testDOModify").testDOModify();
    }
    public void testHTMLModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.HTMLObject.HTMLObject_modify("testDOModify").testDOModify();
    }
    public void testImageModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.ImageObject.ImageObject_modify("testDOModify").testDOModify();
    }
    public void testJSPModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.JSPObject.JSPObject_modify("testDOModify").testDOModify();
    }
    public void testJavaFormModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.JavaSourceObject.JavaSourceObject_modify("testDOModify").testDOModify();
    }
    /*
     * test removed because refactoring shows some dialogs
     *
    public void testPackageModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.Package.Package_modify("testDOModify").testDOModify();
    }*/
    public void testSecurityJAppletModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.SecurityJApplet.SecurityJApplet_modify("testDOModify").testDOModify();
    }
    public void testSwingFormModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.SwingFormObject.SwingFormObject_modify("testDOModify").testDOModify();
    }
    public void testTextualModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.TextualObject.TextualObject_modify("testDOModify").testDOModify();
    }
    public void testURLModify() throws Exception {
        new DataLoaderTests.DataObjectTest.modify.URLObject.URLObject_modify("testDOModify").testDOModify();
    }

    public static Test suite()   {
        NbTestSuite suite = new NbTestSuite(Modify.class);
        return suite;
    }
  
}
