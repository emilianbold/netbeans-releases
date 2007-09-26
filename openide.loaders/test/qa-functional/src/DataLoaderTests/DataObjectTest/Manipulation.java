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
