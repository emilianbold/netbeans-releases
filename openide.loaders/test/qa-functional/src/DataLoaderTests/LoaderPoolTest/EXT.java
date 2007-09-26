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
