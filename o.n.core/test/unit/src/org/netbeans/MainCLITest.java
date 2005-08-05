/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.util.*;
import java.util.jar.JarOutputStream;
import junit.framework.AssertionFailedError;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class MainCLITest extends NbTestCase {
    public MainCLITest (String name) {
        super(name);
    }
    
    public void testHandlersCanBeInUserDir () throws Exception {
        clearWorkDir ();
        
        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                String[] arr = args.getArguments ();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == "--userdir") {
                        System.setProperty ("netbeans.user", arr[i + 1]);
                        return 0;
                    }
                }
                fail ("One of the arguments should be --userdir: " + Arrays.asList (arr));
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        
        File dir = super.getWorkDir ();
        File lib = new File (dir, "core"); 
        lib.mkdirs ();
        File jar = new File (lib, "sample.jar");
        JarOutputStream os = new JarOutputStream (new FileOutputStream (jar));
        os.putNextEntry (new java.util.zip.ZipEntry ("META-INF/services/org.netbeans.CLIHandler"));
        os.write (TestHandler.class.getName ().getBytes ());
        String res = "/" + TestHandler.class.getName ().replace ('.', '/') + ".class";
        os.putNextEntry (new java.util.zip.ZipEntry (res));
        org.openide.filesystems.FileUtil.copy (getClass().getResourceAsStream (res), os);
        os.close ();
        
        TestHandler.called = false;

        String[] args = new String[] { "--userdir", dir.toString () };
        assertFalse ("User dir is not correct. Will be set by org.netbeans.core.CLIOptions", dir.toString ().equals (System.getProperty ("netbeans.user")));
        int result = Main.execute (args, null, null, null);
        Main.finishInitialization ();
        assertEquals ("User set", dir.toString (), System.getProperty ("netbeans.user"));
        assertTrue ("CLI Handler from user dir was called", TestHandler.called);
    }

    /** Sample handler
     */
    public static final class TestHandler extends CLIHandler {
        public static boolean called;
        
        public TestHandler () {
            super (CLIHandler.WHEN_INIT);
        }
        
        protected int cli (org.netbeans.CLIHandler.Args args) {
            called = true;
            return 0;
        }
        
        protected void usage (PrintWriter w) {
        }
        
    }
}
