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
 * Write out all default abbreviations. */

public class AbbreviationsPerformer extends NbTestCase {
    public AbbreviationsPerformer(String name) {
        super(name);
    }
    public void tearDown() throws Exception {
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
    }
    
    /**
     * Call Action: InvokeAll
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Write out all default abbreviations.
     */
    
    public void testInvokeAll() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/abbrevs.xml",
            "BasicTests.InvokeAll"
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
     * Call Action: IndentAbbrevs
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Write out all default abbreviations.
     */
    
    public void testIndentAbbrevs() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/abbrevs.xml",
            "BasicTests.IndentAbbrevs"
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
     * Call Action: Cursor
     * Sub Test: BasicTests
     * Author:   ehucka
     * Version:  1.1
     * Comment:  Write out all default abbreviations.
     */
    
    public void testCursor() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/abbrevs.xml",
            "BasicTests.Cursor"
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
