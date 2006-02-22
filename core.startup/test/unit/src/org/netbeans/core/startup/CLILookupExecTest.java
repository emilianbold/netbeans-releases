/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Permission;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbTestCase;


/** Make sure the CLIHandler can be in modules and really work.
 * @author Jaroslav Tulach
 */
public class CLILookupExecTest extends NbTestCase {
    File home, cluster2, user;
    
    public CLILookupExecTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        
        home = new File(getWorkDir(), "cluster1");
        cluster2 = new File(getWorkDir(), "cluster2");
        user = new File(getWorkDir(), "testuserdir");
        
        home.mkdirs();
        cluster2.mkdirs();
        user.mkdirs();
        
        System.setProperty("netbeans.home", home.toString());
        System.setProperty("netbeans.dirs", cluster2.toString());
    }
    

    protected void tearDown() throws Exception {
    }
    
    public void testModuleInAClusterCanBeFound() throws Exception {
        createJAR(home, "test-module-one", One.class);
        createJAR(cluster2, "test-module-two", Two.class);
        createJAR(user, "test-module-user", User.class);

        org.netbeans.Main.main(new String[] { "--userdir", user.toString(), "--nosplash", "--one", "--two", "--three"});
        org.netbeans.Main.finishInitialization();
        
        assertEquals("Usage one", 0, One.usageCnt); assertEquals("CLI one", 1, One.cliCnt);
        assertEquals("Usage two", 0, Two.usageCnt); assertEquals("CLI two ", 1, Two.cliCnt);
        assertEquals("Usage user", 0, User.usageCnt); assertEquals("CLI user", 1, User.cliCnt);
    }

    private static void createJAR(File cluster, String moduleName, Class metaInfHandler) 
    throws IOException {
        CLILookupHelpTest.createJAR(cluster, moduleName, metaInfHandler);
    }
    
    private static void assertArg(String[] arr, String expected) {
        for (int i = 0; i < arr.length; i++) {
            if (expected.equals(arr[i])) {
                arr[i] = null;
                return;
            }
        }
        
        fail("There should be: " + expected + " but was only: " + java.util.Arrays.asList(arr));
    }

    public static final class One extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public One() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--one");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class Two extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public Two() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--two");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class User extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public User() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--three");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
}
