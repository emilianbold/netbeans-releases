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
