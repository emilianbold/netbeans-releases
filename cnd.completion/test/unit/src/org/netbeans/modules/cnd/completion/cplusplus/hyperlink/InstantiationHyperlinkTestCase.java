/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import junit.framework.AssertionFailedError;

/**
 *
 * @author eu155513
 */
public class InstantiationHyperlinkTestCase extends HyperlinkBaseTestCase {
    public InstantiationHyperlinkTestCase(String testName) {
        super(testName);
    }

    public void test154777() throws Exception {
        // IZ154777: Unresolved inner type of specialization
        performTest("iz154777.cpp", 16, 19, "iz154777.cpp", 10, 9); // DD in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 16, 24, "iz154777.cpp", 11, 13); // dType in CC<int>::DD::dType j;
        performTest("iz154777.cpp", 17, 15, "iz154777.cpp", 3, 9); // method in j.method();
    }

    public void testClassForward() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("classForward.h", 21, 12, "classForward.h", 16, 5);
    }

    public void testCyclicTypedef() throws Exception {
        // IZ148453 : Highlighting thread hangs on boost
        try {
            performTest("cyclic_typedef.cc", 25, 66, "cyclic_typedef.cc", 25, 66);
        } catch (AssertionFailedError e) {
            // it's ok: it won't find: it just shouldn't hang
        }
    }
    
    public void testGccVector() throws Exception {
        // IZ144869 : fixed instantiation of class forward declaration
        performTest("iz146697.cc", 41, 20, "iz146697.cc", 34, 5);
    }

    public void test153986() throws Exception {
        // MYSTL case of IZ#153986: code completion of iterators and of the [] operator
        performTest("iz153986.cc", 18, 15, "iz153986.cc", 9, 9);
        performTest("iz153986.cc", 18, 30, "iz153986.cc", 4, 9);
    }
}
