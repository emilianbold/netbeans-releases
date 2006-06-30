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
 * Test of invoking CC. */

public class CompletionPerformer extends NbTestCase {
public CompletionPerformer(String name) {
super(name);
}
public void tearDown() throws Exception {
    assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
}

/**
 * Call Action: FooClass
 * Sub Test: BasicTests
 * Author:   ehucka
 * Version:  1.1
 * Comment:  Test of invoking CC.
 */

public void testFooClass() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/completion.xml",
"BasicTests.FooClass"
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
 * Call Action: ObjectMethods
 * Sub Test: BasicTests
 * Author:   ehucka
 * Version:  1.1
 * Comment:  Test of invoking CC.
 */

public void testObjectMethods() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/completion.xml",
"BasicTests.ObjectMethods"
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
 * Call Action: RetypeClass
 * Sub Test: BasicTests
 * Author:   ehucka
 * Version:  1.1
 * Comment:  Test of invoking CC.
 */

public void testRetypeClass() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/completion.xml",
"BasicTests.RetypeClass"
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
 * Call Action: StayInvoked
 * Sub Test: BasicTests
 * Author:   ehucka
 * Version:  1.1
 * Comment:  Test of invoking CC.
 */

public void testStayInvoked() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/completion.xml",
"BasicTests.StayInvoked"
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
 * Call Action: PackageCC
 * Sub Test: Issues
 * Author:   ehucka
 * Version:  1.1
 * Comment:  It tests issues 28055.
 */

public void testPackageCC() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/completion.xml",
"Issues.PackageCC"
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
