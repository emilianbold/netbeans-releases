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
 * EXT.java
 *
 * Tests registered extensions of specified DataLoader.
 *
 * Created on May 30, 2001, 4:57 PM
 */

package DataLoaderTests.LoaderPoolTest;

import org.openide.loaders.UniFileLoader;

public class EXT{

    /** Creates new EXT */
    public EXT() {
    }

    private static java.io.PrintWriter log = null;
    private static java.io.PrintWriter ref = null;
    static boolean successful = true;
    LoaderPoolTest LPT = null;


    /**This methods write an output to log stream*/
    public static void writeLog(String text) {
        log.println(text);
        System.out.println(text);
        if (text.equals(FAILED)) successful = false;
    }
    
    /**This methods write an output to reference stream*/
    public static void writeRef(String text) {
        ref.println(text);
        System.out.println(text);
        if (text.equals(FAILED)) successful = false;
    }
    
    /**If enabled, prints exception to the output and to the ref stream*/
    static void printException(Exception e) {
        if(PRINT_EXCEPTIONS){
            e.printStackTrace();
            e.printStackTrace(ref);
        }
    }
    
    /**
     *Performs initializing before own tests starts
     */
    void prepare() {
        LPT = new LoaderPoolTest("x");
        LPT.prepare();
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
    static void dummyWait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            printException(ex);
        }
    }

    public static void main (String args[]) {
        log = new java.io.PrintWriter(System.err);
        ref = new java.io.PrintWriter(System.out);

        EXT ext = new EXT();
        
        ext.prepare();
        
        //LoaderPoolTest.printExtensions( (UniFileLoader) LoaderPoolTest.getDataLoader(LOADER));
        ext.successful = ext.LPT.refPrintExtensions(ext.LPT.getDataLoader(LOADER));
        
        ext.clean();
        
        //test if all test passed, if yes, then return passed, if any of all test failed, return failed.
        //if(successful) return Status.passed(""); else return Status.failed("");
    }    

    //if you want print exceptions into log file, put here true.
    public static final boolean PRINT_EXCEPTIONS = true;
    
    public static final String PASSED = "passed.\n";
    public static final String FAILED = "failed.\n";     
    
    protected static String LOADER = null;
}
