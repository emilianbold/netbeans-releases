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
 * @version 1.2
 *  */

public class JavaDocPerformer extends NbTestCase {
public JavaDocPerformer(String name) {
super(name);
}
public void tearDown() throws Exception {
    assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
}

/**
 * Call Action: Inside
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testInside() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.Inside"
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
 * Call Action: Start
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testStart() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.Start"
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
 * Call Action: End
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testEnd() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.End"
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
 * Call Action: InsideNoLeading
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testInsideNoLeading() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.InsideNoLeading"
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
 * Call Action: StartNoLeading
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testStartNoLeading() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.StartNoLeading"
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
 * Call Action: EndNoLeading
 * Sub Test: Writting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testEndNoLeading() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Writting.EndNoLeading"
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
 * Call Action: ReformattingLeadingStar
 * Sub Test: Reformatting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testReformattingLeadingStar() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Reformatting.ReformattingLeadingStar"
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
 * Call Action: ReformatingNoLeadingStar
 * Sub Test: Reformatting
 * Author:   ehucka
 * Version:  1.2
 * Comment:  
 */

public void testReformatingNoLeadingStar() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/javadoc.xml",
"Reformatting.ReformatingNoLeadingStar"
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
