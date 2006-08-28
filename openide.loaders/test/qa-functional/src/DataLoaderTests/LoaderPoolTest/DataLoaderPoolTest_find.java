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
 * DataLoaderPoolTest_find.java
 *
 * Tests access to DataObject - recognizing, finding etc.
 *
 * Created on June 22, 2001, 12:25 PM
 */

package DataLoaderTests.LoaderPoolTest;

import junit.framework.*;
import org.netbeans.junit.*;

public class DataLoaderPoolTest_find extends NbTestCase {

    /** Creates new DataLoaderPoolTest_find */
    public DataLoaderPoolTest_find(java.lang.String testName) {
        super(testName);
    }

    /**Allows this test to be executed inside ide*/
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**This suite*/
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(DataLoaderPoolTest_find.class);
        return suite;
    }
    
    static boolean successful = true;
    static org.openide.loaders.DataLoader textdl = null;
    LoaderPoolTest LPT = null;
            
    /**
     *Performs initializing before own tests starts
     */
    void prepare() {
        try {
            //when not in XTest harness -> woring directory will be under actual userdir
            if (Manager.getWorkDirPath()==null) System.setProperty("nbjunit.workdir",System.getProperty("netbeans.user"));
            //clearWorkDir();
            LPT = new LoaderPoolTest(getName());
            //now setting workdir - this class will write nothing into the logs, only the utility class should,
            //however into location for this class
            LPT.work = ""+
                Manager.getWorkDirPath()+
                java.io.File.separator + this.getClass().getName().replace('.',java.io.File.separatorChar)+
                java.io.File.separator + getName();
            LPT.prepare();
            textdl = LPT.getDataLoader("Textual");
            LPT.textdl = textdl;
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
        LPT.clean();
    }
    
    /**
     *Performs waiting of current thread for time in millis
     *@param millist integer number - time in millis to wait
     */
    void dummyWait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            LPT.printException(ex);
        }
    }
    
    /**Own test:
     * Asks for DataObjects, asks for its type, removes DataLoader
     * Asks again for the same and new DataObject, adds DataLoader back, asks again
     */
    public void testDLPFind() {
        
        String file1 = new LoaderPoolTest("x").getClass().getPackage().getName().replace('.','/') + "/textfile3.txt";
        String file2 = new LoaderPoolTest("x").getClass().getPackage().getName().replace('.','/') + "/textfile4.txt";
        System.out.println("file1:" + file1);
        System.out.println("file2:" + file2);
        try{
            prepare();
            LPT.writeRef(new LoaderPoolTest("x").getClass().getName());
            LPT.existDataObject(file1);
            LPT.recognizedAs(file1,textdl.getDisplayName());
            LPT.removeDataLoader(textdl);
            dummyWait(5000);
            if ( LPT.noOfChanges != 1 ) {
                LPT.writeRef("Not registered change event over DataLoaderPool!",FAILED);
                //LPT.writeRef(FAILED);
            }
            LPT.existDataObject(file1);
            //test the bug with dataloaderpoolrefresh
            LPT.notRecognizedAs(file1,textdl.getDisplayName());
            //should pass (not recognized as text)
            LPT.existDataObject(file2);
            LPT.notRecognizedAs(file2,textdl.getDisplayName());
            LPT.addDataLoader("org.netbeans.modules.text");
            dummyWait(5000);
            if ( LPT.noOfChanges != 2 ) {
                LPT.writeRef("Not registered change event over DataLoaderPool!",FAILED);
                //LPT.writeRef(FAILED);
            }
            LPT.existDataObject(file1);
            LPT.recognizedAs(file1,textdl.getDisplayName());
            LPT.recognizedAs(file2,textdl.getDisplayName());
            clean();
            //do not forget to merge the results!
            successful = successful && LPT.successful;
            System.out.println("\n" + successful );
        }catch(Throwable ee){
            ee.printStackTrace(getRef());
            ee.printStackTrace();
            LPT.writeRef(FAILED);
        }
        
        assertTrue("Find test failed!",successful);

    }
    
    //if you want print exceptions into log file, put here true.
    public static final boolean PRINT_EXCEPTIONS = true;
    
    public static final String PASSED = "passed.\n";
    public static final String FAILED = "failed.\n";
}
