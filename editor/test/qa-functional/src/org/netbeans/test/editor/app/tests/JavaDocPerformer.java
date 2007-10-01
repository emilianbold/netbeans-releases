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
