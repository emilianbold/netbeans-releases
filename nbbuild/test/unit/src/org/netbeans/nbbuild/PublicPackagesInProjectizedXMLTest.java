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

package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;

/** Check the behaviour of <public-packages> in project.xml modules.
 *
 * @author Jaroslav Tulach
 */
public class PublicPackagesInProjectizedXMLTest extends NbTestCase {
    public PublicPackagesInProjectizedXMLTest (String name) {
        super (name);
    }
    
    public void testPackageCannotContainComma () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <package>org,org.apache.bsf</package>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains comma");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testPackageCannotContainStar () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <package>org.**</package>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains *");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testPublicPackagesCannotContainGarbageSubelements () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <pkgs>org.hello</pkgs>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains *");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }
    
    public void testItIsPossibleToDefineSubpackages () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <module-dependencies/>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "   </data></configuration>" +
            "</project>"
        );
        execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f, "-Dexpected.public.packages=org.hello.**" });
    }
    
    /* DISABLED because of fix for #52135:
    public void testSubpackagesDoNotWorkForJavadocNow () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f, "withjavadoc" });
            fail ("We do not support <subpackage> when javadoc packages are requested, so the execution should fail");
        } catch (ExecutionError ex) {
            // ok
        }
    }
     */

    public void testSubpackagesDoNotWorkForJavadocNowButThisWorksWhenSpecifiedByHand () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <module-dependencies/>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "   </data></configuration>" +
            "</project>"
        );
        execute ("GarbageUnderPackages.xml", new String[] { "-Djavadoc.pac=some",  "-Dproject.file=" + f, "withjavadoc" });
    }
    
    final static String readFile (java.io.File f) throws java.io.IOException {
        int s = (int)f.length ();
        byte[] data = new byte[s];
        assertEquals ("Read all data", s, new java.io.FileInputStream (f).read (data));
        
        return new String (data);
    }
    
    final static File extractString (String res) throws Exception {
        File f = File.createTempFile("res", ".xml");
        f.deleteOnExit ();
        
        FileOutputStream os = new FileOutputStream(f);
        InputStream is = new ByteArrayInputStream(res.getBytes("UTF-8"));
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close ();
            
        return f;
    }
    
    final static File extractResource(String res) throws Exception {
        URL u = PublicPackagesInProjectizedXMLTest.class.getResource(res);
        assertNotNull ("Resource should be found " + res, u);
        
        File f = File.createTempFile("res", ".xml");
        f.deleteOnExit ();
        
        FileOutputStream os = new FileOutputStream(f);
        InputStream is = u.openStream();
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close ();
            
        return f;
    }
    
    final static void execute (String res, String[] args) throws Exception {
        execute (extractResource (res), args);
    }
    
    private static ByteArrayOutputStream out;
    private static ByteArrayOutputStream err;
    
    final static String getStdOut() {
        return out.toString();
    }
    final static String getStdErr() {
        return err.toString();
    }
    
    final static void execute(File f, String[] args) throws Exception {
        // we need security manager to prevent System.exit
        if (! (System.getSecurityManager () instanceof MySecMan)) {
            out = new java.io.ByteArrayOutputStream ();
            err = new java.io.ByteArrayOutputStream ();
            System.setOut (new java.io.PrintStream (out));
            System.setErr (new java.io.PrintStream (err));
            
            System.setSecurityManager (new MySecMan ());
        }
        
        MySecMan sec = (MySecMan)System.getSecurityManager();
        
        // Jesse claims that this is not the right way how the execution
        // of an ant script should be invoked:
        //
        // better IMHO to just run the task directly
        // (setProject() and similar, configure its bean properties, and call
        // execute()), or just make a new Project and initialize it.
        // ant.Main.main is not intended for embedded use. Then you could get rid
        // of the SecurityManager stuff, would be cleaner I think.
        //
        // If I had to write this once again, I would try to follow the
        // "just make a new Project and initialize it", but as this works
        // for me now, I leave it for the time when somebody really 
        // needs that...
        
        List<String> arr = new ArrayList<String>();
        arr.add ("-f");
        arr.add (f.toString ());
        arr.addAll(Arrays.asList(args));
        
        
        out.reset ();
        err.reset ();
        
        try {
            sec.setActive(true);
            org.apache.tools.ant.Main.main(arr.toArray(new String[0]));
        } catch (MySecExc ex) {
            assertNotNull ("The only one to throw security exception is MySecMan and should set exitCode", sec.exitCode);
            ExecutionError.assertExitCode (
                "Execution has to finish without problems", 
                sec.exitCode.intValue ()
            );
        } finally {
            sec.setActive(false);
        }
    }
    
    static class ExecutionError extends AssertionFailedError {
        public final int exitCode;
        
        public ExecutionError (String msg, int e) {
            super (msg);
            this.exitCode = e;
        }
        
        public static void assertExitCode (String msg, int e) {
            if (e != 0) {
                throw new ExecutionError (
                    msg + " was: " + e + "\nOutput: " + out.toString () +
                    "\nError: " + err.toString (),  
                    e
                );
            }
        }
    }
    
    private static class MySecExc extends SecurityException {
        @Override
        public void printStackTrace() {
        }
        @Override
        public void printStackTrace(PrintStream ps) {
        }
        @Override
        public void printStackTrace(PrintWriter ps) {
        }
    }
    
    @SuppressWarnings("deprecation")
    private static class MySecMan extends SecurityManager {
        public Integer exitCode;
        
        private boolean active;
        
        @Override
        public void checkExit (int status) {
            if (active) {
                exitCode = new Integer (status);
                throw new MySecExc ();
            }
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkPermission(Permission perm) {
        /*
            if (perm instanceof RuntimePermission) {
                if (perm.getName ().equals ("setIO")) {
                    throw new MySecExc ();
                }
            }
         */
        }

        @Override
        public void checkMulticast(InetAddress maddr) {
        }

        @Override
        public void checkAccess (ThreadGroup g) {
        }

        @Override
        public void checkWrite (String file) {
        }

        @Override
        public void checkLink (String lib) {
        }

        @Override
        public void checkExec (String cmd) {
        }

        @Override
        public void checkDelete (String file) {
        }

        @Override
        public void checkPackageAccess (String pkg) {
        }

        @Override
        public void checkPackageDefinition (String pkg) {
        }

        @Override
        public void checkPropertyAccess (String key) {
        }

        @Override
        public void checkRead (String file) {
        }

        @Override
        public void checkSecurityAccess (String target) {
        }

        @Override
        public void checkWrite(FileDescriptor fd) {
        }

        @Override
        public void checkListen (int port) {
        }

        @Override
        public void checkRead(FileDescriptor fd) {
        }

        @Override
        public void checkMulticast(InetAddress maddr, byte ttl) {
        }

        @Override
        public void checkAccess (Thread t) {
        }

        @Override
        public void checkConnect (String host, int port, Object context) {
        }

        @Override
        public void checkRead (String file, Object context) {
        }

        @Override
        public void checkConnect (String host, int port) {
        }

        @Override
        public void checkAccept (String host, int port) {
        }

        @Override
        public void checkMemberAccess (Class clazz, int which) {
        }

        @Override
        public void checkSystemClipboardAccess () {
        }

        @Override
        public void checkSetFactory () {
        }

        @Override
        public void checkCreateClassLoader () {
        }

        @Override
        public void checkAwtEventQueueAccess () {
        }

        @Override
        public void checkPrintJobAccess () {
        }

        @Override
        public void checkPropertiesAccess () {
        }

        void setActive(boolean b) {
            active = b;
        }
    } // end of MySecMan
}
