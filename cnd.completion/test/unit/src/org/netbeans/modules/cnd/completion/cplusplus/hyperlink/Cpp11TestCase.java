/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 * @author Nikolay Krasilnikov
 */
public class Cpp11TestCase extends HyperlinkBaseTestCase {

    public Cpp11TestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAuto() throws Exception {
        performTest("auto.cpp", 14, 14, "auto.cpp", 2, 5);
    }

    public void testStronglyTypedEnumerations() throws Exception {
        performTest("enum.cpp", 8, 34, "enum.cpp", 2, 5);
    }

    public void testRangeBasedForLoop() throws Exception {
        performTest("rangefor.cpp", 4, 9, "rangefor.cpp", 3, 9);
    }

    public void testBug210019() throws Exception {
        // Bug 210019 - Unresolved variadic template parameter
        performTest("bug210019.cpp", 2, 50, "bug210019.cpp", 2, 10);
        performTest("bug210019.cpp", 2, 54, "bug210019.cpp", 2, 18);
    }

    public void testBug210191() throws Exception {
        // Bug 210191 - Unresolved class members in lambdas
        performTest("bug210191.cpp", 7, 28, "bug210191.cpp", 3, 5);
    }
    
    public void testBug210192() throws Exception {
        // Bug 210192 - Unresolved template functions
        performTest("bug210192.cpp", 5, 36, "bug210192.cpp", 1, 1);
    }    
    
    public void testBug210194() throws Exception {
        // Bug 210194 - Unresolved instantiations with function pointers
        performTest("bug210194.cpp", 19, 77, "bug210194.cpp", 6, 3);
        performTest("bug210194.cpp", 20, 82, "bug210194.cpp", 6, 3);
        performTest("bug210194.cpp", 21, 88, "bug210194.cpp", 6, 3);
    }    
    
    public void testBug210257() throws Exception {
        // Bug 210257 - Ellipsis breaks hyperlink
        performTest("bug210257.cpp", 9, 52, "bug210257.cpp", 6, 3);
    }    
    
    public void testBug210291() throws Exception {
        // Bug 210291 - Unresolved ids in instantiations
        performTest("bug210291.cpp", 13, 59, "bug210291.cpp", 8, 5);
    }    
    
//    public void testBug210303() throws Exception {
//        // Bug 210303 - Unresolved instantiation
//        performTest("bug210303.cpp", 18, 11, "bug210303.cpp", 11, 9);
//    }
    
}
