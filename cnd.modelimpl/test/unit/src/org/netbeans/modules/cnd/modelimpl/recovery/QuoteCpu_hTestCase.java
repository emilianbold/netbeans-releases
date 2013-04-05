/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.recovery;

import java.io.File;
import org.junit.Test;
import org.netbeans.junit.Manager;

/**
 *
 * @author Alexander Simon
 */
public class QuoteCpu_hTestCase extends RecoveryTestCaseBase {

    private static final String SOURCE = "cpu.h";
    public QuoteCpu_hTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected File getTestCaseDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/recovery/cpu_h"));
    }

    @Gramma(newGramma = false)
    @Golden
    @Test
    public void A_Golden() throws Exception {
        implTest(SOURCE);
    }

    //<editor-fold defaultstate="collapsed" desc="before class">
    @Gramma(newGramma = true)
    @Diff(file=SOURCE)
    @Test
    public void testBeforeClass00n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "ID()")
    @Test
    public void testBeforeClass01() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "ID()")
    @Test
    public void testBeforeClass01n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "int")
    @Test
    public void testBeforeClass02() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "int")
    @Test
    public void testBeforeClass02n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "*")
    @Test
    public void testBeforeClass03() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "*")
    @Test
    public void testBeforeClass03n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "&")
    @Test
    public void testBeforeClass04() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "&")
    @Test
    public void testBeforeClass04n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "{")
    @Test
    public void testBeforeClass05() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "{")
    @Test
    public void testBeforeClass05n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "}")
    @Test
    public void testBeforeClass06() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "}")
    @Test
    public void testBeforeClass06n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "+")
    @Test
    public void testBeforeClass07() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 46, column = 1, length = 0, insert = "+")
    @Test
    public void testBeforeClass07n() throws Exception {
        implTest(SOURCE);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="between class members">
    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID")
    @Test
    public void testBetweenClassMembers01() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID")
    @Test
    public void testBetweenClassMembers01n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID()")
    @Test
    public void testBetweenClassMembers02() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID()")
    @Test
    public void testBetweenClassMembers02n() throws Exception {
        implTest(SOURCE);
    }
    
    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID(SIGNAL)")
    @Test
    public void testBetweenClassMembers03() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "ID(SIGNAL)")
    @Test
    public void testBetweenClassMembers03n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int")
    @Test
    public void testBetweenClassMembers04() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int")
    @Test
    public void testBetweenClassMembers04n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int a")
    @Test
    public void testBetweenClassMembers05() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int a")
    @Test
    public void testBetweenClassMembers05n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int a()")
    @Test
    public void testBetweenClassMembers06() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int a()")
    @Test
    public void testBetweenClassMembers06n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int *")
    @Test
    public void testBetweenClassMembers07() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "int *")
    @Test
    public void testBetweenClassMembers07n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "class")
    @Test
    public void testBetweenClassMembers08() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 53, column = 1, length = 0, insert = "class")
    @Test
    public void testBetweenClassMembers08n() throws Exception {
        implTest(SOURCE);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="inside class members">
//TODO: both grammars do not recover
//    @Gramma(newGramma = false)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID ")
//    @Test
//    public void testInsideMemberDeclaration01() throws Exception {
//        implTest(SOURCE);
//    }
//
//    @Gramma(newGramma = true)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID ")
//    @Test
//    public void testInsideMemberDeclaration01n() throws Exception {
//        implTest(SOURCE);
//    }
//
//    @Gramma(newGramma = false)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID() ")
//    @Test
//    public void testInsideMemberDeclaration02() throws Exception {
//        implTest(SOURCE);
//    }
//
//    @Gramma(newGramma = true)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID() ")
//    @Test
//    public void testInsideMemberDeclaration02n() throws Exception {
//        implTest(SOURCE);
//    }
//
//    @Gramma(newGramma = false)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID(E) ")
//    @Test
//    public void testInsideMemberDeclaration03() throws Exception {
//        implTest(SOURCE);
//    }
//
//    @Gramma(newGramma = true)
//    @Diff(file=SOURCE, line = 58, column = 36, length = 0, insert = " ID(E) ")
//    @Test
//    public void testInsideMemberDeclaration03n() throws Exception {
//        implTest(SOURCE);
//    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="inside class member parameters">
    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "ID(E)")
    @Test
    public void testInsideMemberParameter01() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "ID(E)")
    @Test
    public void testInsideMemberParameter01n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "class")
    @Test
    public void testInsideMemberParameter02() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "class")
    @Test
    public void testInsideMemberParameter02n() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = false)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "struct")
    @Test
    public void testInsideMemberParameter03() throws Exception {
        implTest(SOURCE);
    }

    @Gramma(newGramma = true)
    @Diff(file=SOURCE, line = 58, column = 35, length = 0, insert = "struct")
    @Test
    public void testInsideMemberParameter03n() throws Exception {
        implTest(SOURCE);
    }
    //</editor-fold>

}
