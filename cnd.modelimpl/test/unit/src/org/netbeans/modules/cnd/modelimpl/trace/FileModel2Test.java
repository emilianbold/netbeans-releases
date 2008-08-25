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

package org.netbeans.modules.cnd.modelimpl.trace;

/**
 * Just a continuation of the FileModelTest
 * (which became too large)
 * @author Vladimir Kvashin
 */
public class FileModel2Test extends TraceModelTestBase {

    public FileModel2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        super.setUp();
    }
    
    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }
    
    public void testIZ143977_0() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_0.cc");
    }
    
    public void testIZ143977_1() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_1.cc");
    }
    
    public void testIZ143977_2() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_2.cc");
    }
    
    public void testIZ143977_3() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_3.cc");
    }

    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc");
    }
}
