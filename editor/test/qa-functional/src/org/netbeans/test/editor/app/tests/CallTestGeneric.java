/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.tests;

//import experimental.Utils;
import java.io.*;
//import javasoft.sqe.javatest.*;
import org.openide.TopManager;
import org.netbeans.test.editor.app.util.WriterOutputStream;
import org.openide.cookies.ExecCookie;
import org.openide.loaders.DataObject;

import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.AllPermission;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import org.openide.execution.NbClassLoader;

public class CallTestGeneric {//implements Test {
    
    private static final boolean debug = true;
    
    private PrintWriter log = null;
    
    protected void log(String what) {
	if (log != null) {
	    log.println(what);
	    log.flush();
	} else {
	    System.err.println(what);
	    System.err.flush();
	}
    }
    
    public void runTest(String[] args, final PrintWriter log, final PrintWriter ref) throws Exception {
	//	log("log!");
	//	ref.println("ref!");
	//	log.flush();
	//	ref.flush();
	PrintStream oout = System.out;
	PrintStream oerr = System.err;
	
	try {
	if (debug)
	    System.err.println("Testing internal execution!");
	if (TopManager.getDefault() == null) {
	    throw new IllegalStateException("TopManager.getDefault() == null, probably not internal execution.");
	}
	
	if (debug)
	    System.err.println("Redirecting System.err and System.out.");
	System.setErr(new PrintStream(new WriterOutputStream(log)));
	System.setOut(new PrintStream(new WriterOutputStream(ref)));
	
/*	if (debug)
	    log("Mounting editortest.jar:");
	if (debug)
	    log("Unmounting old editortest.jar.");
	Utils.unmountJar("editortest");
	
	if (debug)
	    log("Mounting new editortest.jar.");
	log.flush();
	if (!Utils.mountJar("experimental", "editortest_new", "jar")) {
	    log("Unable to mount editortest.jar!");
	    return Status.failed("Unable to mount editortest.jar!");
	};
	
	if (debug)
	    log.print("Mounting editortest.jar done.");*/
	System.err.println("Before trying to execute:");
	    NbClassLoader cl = new NbClassLoader();

            PermissionCollection pcoll = new Permissions();
            pcoll.add(new AllPermission());
            cl.setDefaultPermissions(pcoll);

            Class callTest = cl.loadClass("org.netbeans.test.editor.app.gui.Main");
            Method method = callTest.getMethod("main", new Class[] {args.getClass()});
            
            System.err.println(method.getReturnType());
            
            Object obj = method.invoke(null, new Object[] {args});
	} catch (Exception e) {
	    e.printStackTrace(log);
            throw e;
	} finally {
/*	    if (debug)
		log ("Unmounting editortest.jar.");
	    Utils.unmountJar ("editortest_new");
	    if (debug)
		log ("done.");*/
	    System.err.flush ();
	    System.out.flush ();
	    System.setOut (oout);
	    System.setErr (oerr);
	}
    }
    
    public static final void main(String[] args) throws Exception {
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc_test.xml",
            "Javadoc_writting.Common_Java_settings",
            "/org/netbeans/test/editor/app/tests/javadoc_test.xml",
            "Javadoc_writting.JavaDoc_inside",
        };
        PrintWriter log = new PrintWriter(System.err);
        PrintWriter ref = new PrintWriter(System.out);
        
        new CallTestGeneric().runTest(arguments, log, ref);
        
        log.close();
        ref.close();
    }
    
}
