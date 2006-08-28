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
import org.openide.loaders.DataObject;

public class DataObjectTest_delegate extends NbTestCase {

    /** Creates new DataObjectTest_manipulation */
    public DataObjectTest_delegate(java.lang.String testName) {
        super(testName);
    }

    /**Allows this test to be executed inside ide*/
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
     /**This suite*/
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(DataObjectTest_delegate.class);
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
            if (Manager.getWorkDirPath()==null) System.setProperty("nbjunit.workdir",System.getProperty("netbeans.user"));
            //clearWorkDir();
            String newname = NAME.substring(NAME.lastIndexOf('/')+1,((NAME.lastIndexOf('.')==-1)?NAME.length():NAME.lastIndexOf('.')));
            DOT = new DataObjectTest(getName());
            System.out.println("Name: " + DOT.getName());
  //          successful = true;
            //next condition removes the last dot from folder
            if (NAME.endsWith(".")) NAME = NAME.substring(0,NAME.length()-1);
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
    
    /**
     *Own test
     */
    public void testDODelegate() {        
        
        try{
            prepare();
       //     org.openide.filesystems.FileObject fo = DOT.rep.findResource(new DataObjectTest("x").getClass().getPackage().getName().replace('.','/')
       //                                                                           + NAME);
            DataObject testedDO = DataObjectTest.findResource(NAME);
//            org.openide.loaders.DataObject testedDO = org.openide.loaders.DataObject.find(fo);
            DOT.testDelegate(testedDO);
            clean();
            //do not forget to merge the results!
//            successful = successful && DOT.successful;
            System.out.println("\n" + DOT.successful );
        }catch(Throwable ee){
            ee.printStackTrace(getRef());
            ee.printStackTrace();
            DOT.writeRef("Delegate test failed!",FAILED);
        }
        assertTrue("Delegate test failed!",DOT.successful);        
    }
    
    //if you want print exceptions into log file, put here true.
    public static final boolean PRINT_EXCEPTIONS = true;
    
    public static final String PASSED = "passed.\n";
    public static final String FAILED = "failed.\n";
    
    protected  String NAME = "/DataObjects/JavaSourceObject.java";//null;
}
