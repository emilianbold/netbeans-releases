//This file was automaticaly generated. Do not modify.
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
    assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
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
