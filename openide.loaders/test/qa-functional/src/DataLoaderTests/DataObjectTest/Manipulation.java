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

public class Manipulation extends NbTestCase {

    public Manipulation(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private DataObjectTest T = null;

    protected void setUp(){
        System.err.println("xxxxx");
        T = new DataObjectTest("Dummy");
        //now setting workdir - this class will write nothing into the logs, only the utility class should,
        //however into location for this class    
        log(Manager.getWorkDirPath());
        T.work = Manager.getWorkDirPath()+
        java.io.File.separator + this.getClass().getName().replace('.',java.io.File.separatorChar)+
        java.io.File.separator + getName();
    }
    /*
    public void testAWTFormManipulation() throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.AWTFormObject.AWTFormObject_manipulation("testDOManipulation").testDOManipulation();
    }*/
    /*
    public void testClassManipulation() throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.ClassObject.ClassObject_manipulation("testDOManipulation").testDOManipulation();
    }*/
    public void testHTMLManipulation()throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.HTMLObject.HTMLObject_manipulation("testDOManipulation").testDOManipulation();
    }
    public void testImageManipulation()throws Exception{
        new DataLoaderTests.DataObjectTest.manipulation.ImageObject.ImageObject_manipulation("testDOManipulation").testDOManipulation();
    }
    public void testJSPManipulation() throws Exception{
        new DataLoaderTests.DataObjectTest.manipulation.JSPObject.JSPObject_manipulation("testDOManipulation").testDOManipulation();
    }
    
    public void testJavaFormManipulation()throws Exception{
        new DataLoaderTests.DataObjectTest.manipulation.JavaSourceObject.JavaSourceObject_manipulation("testDOManipulation").testDOManipulation();
    }
    /**
     * removed from suite because refactoring show dialgs 
     *
    public void testPackageManipulation()throws Exception{
        new DataLoaderTests.DataObjectTest.manipulation.Package.Package_manipulation("testDOManipulation").testDOManipulation();
    }
     **/
    public void testSecurityJAppletManipulation()throws Exception{
        new DataLoaderTests.DataObjectTest.manipulation.SecurityJApplet.SecurityJApplet_manipulation("testDOManipulation").testDOManipulation();
    }
    
    public void testSwingFormManipulation() throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.SwingFormObject.SwingFormObject_manipulation("testDOManipulation").testDOManipulation();
    }
    public void testTextualManipulation() throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.TextualObject.TextualObject_manipulation("testDOManipulation").testDOManipulation();
    }
    public void testURLManipulation() throws Exception {
        new DataLoaderTests.DataObjectTest.manipulation.URLObject.URLObject_manipulation("testDOManipulation").testDOManipulation();
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(Manipulation.class);
        return suite;
    }
    
}
