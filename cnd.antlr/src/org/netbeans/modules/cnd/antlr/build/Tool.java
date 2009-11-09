package org.netbeans.modules.cnd.antlr.build;

import org.netbeans.modules.cnd.antlr.Utils;

import java.io.*;
import java.lang.reflect.*;

/** An application-specific build tool for ANTLR.  Old build
 *  was a shell script that of course is not portable.  All it
 *  did though was to compile; this Java program is a portable
 *  compile script. :)
 *
 *  This class knows how to portable invoke a system command with
 *  wildcard expansion.  It also knows to how dynamically load a project
 *  description based upon the name.  Actions are just method names
 *  within that object.  The ANTLR object defines build and jar methods
 *  that use Tool to portably compile and jar--it is the replacement
 *  the script.
 *
 *  I almost used Python for this, but I hate forcing users to download
 *  yet another tool just to compile this one.  (Another reason not
 *  to use Ant).
 *
 *  I am using a class to describe the build (e.g., ANTLR) rather than
 *  a file format at the moment since I am not sure how all this will
 *  progress.
 *
 *  Properties org.netbeans.modules.cnd.antlr.build.compiler and org.netbeans.modules.cnd.antlr.build.root may be set
 *  according to your needs.  They default to "javac" and ".".  The
 *  root is the dir containing the org.netbeans.modules.cnd.antlr directory associated with
 *  the org.netbeans.modules.cnd.antlr Java package.
 *
 *  To build this builder, run "javac *.java" in this dir.
 *
 *  To build ANTLR, just run
 *
 *    java org.netbeans.modules.cnd.antlr.build.Tool build"
 *
 *  from the
 *  directory containing package org.netbeans.modules.cnd.antlr's "org.netbeans.modules.cnd.antlr" dir.  Or, use
 *  something like:
 *
 *  java -Dorg.netbeans.modules.cnd.antlr.build.root=/usr/local/org.netbeans.modules.cnd.antlr-2.7.2 org.netbeans.modules.cnd.antlr.build.Tool build
 *
 *  To make the org.netbeans.modules.cnd.antlr.jar file, use
 *
 *  java org.netbeans.modules.cnd.antlr.build.Tool jar
 */
public class Tool {
    public String os = null;

    public Tool() {
        os = System.getProperty("os.name");
    }

    public static void main(String[] args) {
        if ( args.length!=1 ) {
            System.err.println("usage: java org.netbeans.modules.cnd.antlr.build.Tool action");
            return;
        }
        String app = "org.netbeans.modules.cnd.antlr.build.ANTLR"; // hardcode to build ANTLR app
        String action = args[0];
        Tool t = new Tool();
        t.perform(app, action);
    }

    /** Find a class named after the app and then find a method within
     *  that is named after the action.
     */
    public void perform(String app, String action) {
        if ( app==null || action==null ) {
            error("missing app or action");
            return;
        }
        Class c = null;
        Method m = null;
        Object appObj = null;
        try {
            appObj = Utils.createInstanceOf(app);
        }
        catch (Exception e) {
            // try again with org.netbeans.modules.cnd.antlr.build.app
            try {
                if ( !app.startsWith("org.netbeans.modules.cnd.antlr.build.") ) {
                    c = Utils.loadClass("org.netbeans.modules.cnd.antlr.build."+app);
                }
                error("no such application "+app, e);
            }
            catch (Exception e2) {
                error("no such application "+app, e2);
            }
        }
        if ( c==null || appObj==null ) {
            return;
        }
        try {
            m = c.getMethod(action, new Class[] {org.netbeans.modules.cnd.antlr.build.Tool.class});
            //log("begin "+action+" on "+app);
            m.invoke(appObj, new Object[] {this});
            //log("end "+action+" on "+app);
        }
        catch (Exception e) {
            error("no such action for application "+app, e);
        }
    }

    /** A portable system command execution routine that logs stdout/stderr
     *  and handles cmd-line wildcards properly.
     */ 
    public void system(String cmd) {
        Runtime rt = Runtime.getRuntime();
        try {
            log(cmd);
            Process proc = null;
            if ( !os.startsWith("Windows") ) {
                // assume unixen if not windoze (windoze tool expands *)
                proc = rt.exec(new String[] {"sh","-c",cmd});
            }
            else {
                proc = rt.exec(cmd);
            }
            StreamScarfer stderrScarfer =
                    new StreamScarfer(proc.getErrorStream(), "stderr", this);
            StreamScarfer stdoutScarfer =
                    new StreamScarfer(proc.getInputStream(), "stdout", this);
            stderrScarfer.start();
            stdoutScarfer.start();
            int exitVal = proc.waitFor();
        }
        catch (Exception e) {
            error("cannot exec "+cmd, e);
        }
    }

    /** Run ANTLR on a grammar file, leaving the output in the directory of the
     *  grammar file.
     */
    public void antlr(String fullyQualifiedFilename) {
        String path = null;
        try {
            path = new File(fullyQualifiedFilename).getParent();
            if ( path!=null ) {
                path = new File(path).getCanonicalPath();
            }
        }
        catch (IOException ioe) {
            error("Invalid grammar file: "+fullyQualifiedFilename);
        }
        if ( path!=null ) {
            log("java org.netbeans.modules.cnd.antlr.Tool -o "+path+" "+fullyQualifiedFilename);
            org.netbeans.modules.cnd.antlr.Tool theTool = new org.netbeans.modules.cnd.antlr.Tool();
            theTool.doEverything(new String[] {"-o", path, fullyQualifiedFilename});
        }
    }

    /** Stdout from executing sub tools */
    public void stdout(String s) {
        System.out.println(s);
    }

    /** Stderr from executing sub tools */
    public void stderr(String s) {
        System.err.println(s);
    }

    public void error(String msg) {
        System.err.println("org.netbeans.modules.cnd.antlr.build.Tool: "+msg);
    }

    public void log(String cmd) {
        System.out.println("executing: "+cmd);
    }

    public void error(String msg, Exception e) {
        System.err.println("org.netbeans.modules.cnd.antlr.build.Tool: "+msg);
        e.printStackTrace(System.err);
    }
}
