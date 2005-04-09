/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "       <javadoc/>" +
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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
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
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/1\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <path>scripting/bsf</path>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        execute ("GarbageUnderPackages.xml", new String[] { "-Djavadoc.pac=some",  "-Dproject.file=" + f, "withjavadoc" });
    }
    
    final static java.io.File extractString (String res) throws Exception {
        java.io.File f = java.io.File.createTempFile ("res", ".xml");
        f.deleteOnExit ();
        
        java.io.FileOutputStream os = new java.io.FileOutputStream (f);
        java.io.InputStream is = new java.io.ByteArrayInputStream (res.getBytes ("utf-8"));
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close ();
            
        return f;
    }
    
    final static java.io.File extractResource (String res) throws Exception {
        java.net.URL u = PublicPackagesInProjectizedXMLTest.class.getResource (res);
        assertNotNull ("Resource should be found " + res, u);
        
        java.io.File f = java.io.File.createTempFile ("res", ".xml");
        f.deleteOnExit ();
        
        java.io.FileOutputStream os = new java.io.FileOutputStream (f);
        java.io.InputStream is = u.openStream ();
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
    
    private static java.io.ByteArrayOutputStream out;
    private static java.io.ByteArrayOutputStream err;
    
    final static void execute (java.io.File f, String[] args) throws Exception {
        // we need security manager to prevent System.exit
        if (! (System.getSecurityManager () instanceof MySecMan)) {
            out = new java.io.ByteArrayOutputStream ();
            err = new java.io.ByteArrayOutputStream ();
            /*
            System.setOut (new java.io.PrintStream (out));
            System.setErr (new java.io.PrintStream (err));
             */
            
            System.setSecurityManager (new MySecMan ());
        }
        
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
        
        java.util.ArrayList arr = new java.util.ArrayList ();
        arr.add ("-f");
        arr.add (f.toString ());
        arr.addAll (java.util.Arrays.asList (args));
        
        
        out.reset ();
        err.reset ();
        
        try {
            org.apache.tools.ant.Main.main ((String[])arr.toArray (new String[0]));
        } catch (MySecExc ex) {
            assertNotNull ("The only one to throw security exception is MySecMan and should set exitCode", MySecMan.exitCode);
            ExecutionError.assertExitCode (
                "Execution has to finish without problems", 
                MySecMan.exitCode.intValue ()
            );
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
        public void printStackTrace() {
        }
        public void printStackTrace(java.io.PrintStream ps) {
        }
        public void printStackTrace(java.io.PrintWriter ps) {
        }
    }
    
    private static class MySecMan extends SecurityManager {
        public static Integer exitCode;
        
        public void checkExit (int status) {
            exitCode = new Integer (status);
            throw new MySecExc ();
        }

        public void checkPermission (java.security.Permission perm, Object context) {
        }

        public void checkPermission (java.security.Permission perm) {
        /*
            if (perm instanceof java.lang.RuntimePermission) {
                if (perm.getName ().equals ("setIO")) {
                    throw new MySecExc ();
                }
            }
         */
        }

        public void checkMulticast (java.net.InetAddress maddr) {
        }

        public void checkAccess (ThreadGroup g) {
        }

        public void checkWrite (String file) {
        }

        public void checkLink (String lib) {
        }

        public void checkExec (String cmd) {
        }

        public void checkDelete (String file) {
        }

        public void checkPackageAccess (String pkg) {
        }

        public void checkPackageDefinition (String pkg) {
        }

        public void checkPropertyAccess (String key) {
        }

        public void checkRead (String file) {
        }

        public void checkSecurityAccess (String target) {
        }

        public void checkWrite (java.io.FileDescriptor fd) {
        }

        public void checkListen (int port) {
        }

        public void checkRead (java.io.FileDescriptor fd) {
        }

        public void checkMulticast (java.net.InetAddress maddr, byte ttl) {
        }

        public void checkAccess (Thread t) {
        }

        public void checkConnect (String host, int port, Object context) {
        }

        public void checkRead (String file, Object context) {
        }

        public void checkConnect (String host, int port) {
        }

        public void checkAccept (String host, int port) {
        }

        public void checkMemberAccess (Class clazz, int which) {
        }

        public void checkSystemClipboardAccess () {
        }

        public void checkSetFactory () {
        }

        public void checkCreateClassLoader () {
        }

        public void checkAwtEventQueueAccess () {
        }

        public void checkPrintJobAccess () {
        }

        public void checkPropertiesAccess () {
        }
    } // end of MySecMan
}
