//This file was automaticaly generated. Do not modify.
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

/**
 * Call Action: Ternary
 * Sub Test: Issues
 * Author:   ehucka, jlahoda
 * Version:  1.1
 * Comment:  
 */

public void testTernary() throws Exception {
PrintWriter ref = null;
PrintWriter log = null;
String[] arguments = new String[] {
"/org/netbeans/test/editor/app/tests/indentation.xml",
"Issues.Ternary"
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
