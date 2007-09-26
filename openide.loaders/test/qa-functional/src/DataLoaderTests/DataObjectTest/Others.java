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
