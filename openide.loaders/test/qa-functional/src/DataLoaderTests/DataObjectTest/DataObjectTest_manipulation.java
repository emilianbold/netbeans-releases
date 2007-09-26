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
 * DataObjectTest_manipulation.java
 *
 * Tests ...
 *
 * Created on June 26, 2001, 3:39 PM
 */

package DataLoaderTests.DataObjectTest;

import junit.framework.*;
import org.netbeans.junit.*;

public class DataObjectTest_manipulation extends NbTestCase {

    /** Creates new DataObjectTest_manipulation */
    public DataObjectTest_manipulation(java.lang.String testName) {
        super(testName);
    }

    /**Allows this test to be executed inside ide*/
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
     /**This suite*/
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(DataObjectTest_manipulation.class);
        return suite;
    }    
    
//     boolean successful = true;
     DataObjectTest DOT = null;
    

    /**
     *Performs initializing before own tests starts
     */
     void prepare() {
        try {
            //when not in XTest harness -> woring directory will be under actual userdir
//            if (Manager.getWorkDirPath()==null) System.setProperty("nbjunit.workdir",System.getProperty("netbeans.user"));
            //clearWorkDir();
//            String newname = NAME.substring(NAME.lastIndexOf('/')+1,((NAME.lastIndexOf('.')==-1)?NAME.length():NAME.lastIndexOf('.')));
            DOT = new DataObjectTest(getName());
            System.out.println("Name: " + DOT.getName());
  //          successful = true;
            //next condition removes the last dot from folder
            if (NAME.endsWith(".")) {
                NAME = NAME.substring(0,NAME.length()-1);
            }
            DOT.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(getRef());
            assertTrue("Initialization of test failed! ->" + e,false);
        }
    }
    
    /**
     *Performs clean up
     */
     void clean() {
        DOT.clean();
    }
    
    /**
     *Performs waiting of current thread for time in millis
     *@param millist integer number - time in millis to wait
     */
     void dummyWait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            DOT.printException(ex);
        }
    }
    
    /**Own test:
     *
     */
    public void testDOManipulation() throws Exception {

            prepare();
            log("dataDir " + getDataDir());
//            org.openide.filesystems.FileObject fo = DOT.rep.findResource(new DataObjectTest("x").getClass().getPackage().getName().replace('.','/')
//                                                                                  + NAME);
  //          org.openide.loaders.DataObject testedDO = org.openide.loaders.DataObject.find(fo);
            log("resource:" + NAME);
            DOT.checkManipulationOperations(DataObjectTest.findResource(NAME));
            clean();
            //do not forget to merge the results!
//            successful = successful && DOT.successful;
//            System.out.println("\n" + successful );
   
        assertTrue("Manipulation test failed!",DOT.successful);                
    }
    
    //if you want print exceptions into log file, put here true.
    public static final boolean PRINT_EXCEPTIONS = true;
    
    public static final String PASSED = "passed.\n";
    public static final String FAILED = "failed.\n";
    
    protected  String NAME = "/DataObjects/JavaSourceObject.java";//null;
}
