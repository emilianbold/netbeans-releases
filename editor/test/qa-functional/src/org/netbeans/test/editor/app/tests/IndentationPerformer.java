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
 * @author ehucka, jlahoda
 * @version 1.1
 *  */

public class IndentationPerformer extends NbTestCase {
public IndentationPerformer(String name) {
super(name);
}
public void tearDown() throws Exception {
    assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
}

/**
 * Call Action: Round
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testRound() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.Round"
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
 * Call Action: RoundAddNewLine
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testRoundAddNewLine() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.RoundAddNewLine"
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
 * Call Action: RoundAddSpace
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testRoundAddSpace() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.RoundAddSpace"
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
 * Call Action: Compound
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testCompound() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.Compound"
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
 * Call Action: CompoundNewLine
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testCompoundNewLine() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.CompoundNewLine"
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
 * Call Action: ClosingSameLine
 * Sub Test: Reformatting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testClosingSameLine() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Reformatting.ClosingSameLine"
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
 * Call Action: SpecialAddNewLine
 * Sub Test: Writting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testSpecialAddNewLine() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Writting.SpecialAddNewLine"
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
 * Call Action: TwoBrackets
 * Sub Test: Writting
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testTwoBrackets() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Writting.TwoBrackets"
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
 * Call Action: DoWhile
 * Sub Test: Issues
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testDoWhile() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Issues.DoWhile"
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
