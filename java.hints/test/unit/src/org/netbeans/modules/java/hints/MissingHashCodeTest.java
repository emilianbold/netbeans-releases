/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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
package org.netbeans.modules.java.hints;

import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;

/**
 *
 * @author Jaroslav Tulach
 */
public class MissingHashCodeTest extends TestBase {

    public MissingHashCodeTest(String testName) {
        super(testName, MissingHashCode.class);
    }

    public void testMissingHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean ";
        String after = " equals(Object snd) {" + "  return snd != null && getClass().equals(snd.getClass());" + " }" + "}";

        performAnalysisTest("test/Test.java", before + after, "0:65-0:71:verifier:Generate missing hashCode()");
    }

    public void testMissingEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int ";
        String after = " hashCode() {" + "  return 1;" + " }" + "}";

        performAnalysisTest("test/Test.java", before + after, "0:61-0:69:verifier:Generate missing equals(Object)");
    }

    public void testWhenNoFieldsGenerateHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean equa";
        String after = "ls(Object snd) { return snd == this; } }";

        String res = performFixTest("test/Test.java", before + after, "0:64-0:70:verifier:Generate missing hashCode()", "FixImpl", null);

        if (!res.matches(".*equals.*hashCode.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }

    public void testWhenNoFieldsGenerateEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int hash";
        String after = "Code() { return 1; } }";

        String res = performFixTest("test/Test.java", before + after, "0:60-0:68:verifier:Generate missing equals(Object)", "FixImpl", null);

        if (!res.matches(".*hashCode.*equals.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }

}
