//This file was automaticaly generated. Do not modify.
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
import java.io.PrintWriter;
import org.openide.filesystems.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import java.io.File;
import org.netbeans.test.editor.LineDiff;


/**
 *
 * @author ehucka
 * @version 1.1
 * Test of invoking CC. */

public class CompletionPerformer extends NbTestCase {
    public CompletionPerformer(String name) {
        super(name);
    }
    public void tearDown() throws Exception {
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
    }
    
    /**
     * Call Action: fooClass
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Test of invoking CC.
     */
    
    public void testfooClass() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "BasicTests.fooClass"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: objectMethods
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Test of invoking CC.
     */
    
    public void testobjectMethods() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "BasicTests.objectMethods"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: retypeClass
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Test of invoking CC.
     */
    
    public void testretypeClass() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "BasicTests.retypeClass"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: stayInvoked
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Test of invoking CC.
     */
    
    public void teststayInvoked() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "BasicTests.stayInvoked"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: CaseInsensitive
     * Sub Test: Settings
     * Author:   ehucka
     * Version:  1.1
     * Comment:  This test will fails depend on issue 29406.
     */
    
    public void testCaseInsensitive() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "Settings.CaseInsensitive"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: InstantSubstitution
     * Sub Test: Settings
     * Author:   ehucka
     * Version:  1.1
     * Comment:  This test will fails depend on issue 29406.
     */
    
    public void testInstantSubstitution() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "Settings.InstantSubstitution"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: CCAfterMethods
     * Sub Test: Issues
     * Author:   ehucka
     * Version:  1.1
     * Comment:  It tests issues 28055.
     */
    
    public void testCCAfterMethods() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "Issues.CCAfterMethods"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: Repository
     * Sub Test: Issues
     * Author:   ehucka
     * Version:  1.1
     * Comment:  It tests issues 28055.
     */
    
    public void testRepository() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "Issues.Repository"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: BasicHTML
     * Sub Test: HTMLCompletion
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Basic test of HTML CC.
 
     */
    
    public void testBasicHTML() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/completion.xml",
            "HTMLCompletion.BasicHTML"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
}
