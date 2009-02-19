/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.testrunner.ui;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class RubyTestMethodNodeTest extends TestCase {

    public void testFindLocationLine() {
        String[] stacktrace = {
            "<\"this \n that \n\"> expected but was <\"that \n this \n\">.",
            "/home/uuno/nb/testproject/test/unit/some_test.rb:40:in `test_that'",
            "/home/uuno/nb/testproject/test/unit/some_test.rb:45:in `nothing'",
            "/usr/lib/ruby/gems/1.8/gems/activesupport-2.1.1/lib/active_support/testing/setup_and_teardown.rb:33:in `__send__'",
            "/usr/lib/ruby/gems/1.8/gems/activesupport-2.1.1/lib/active_support/testing/setup_and_teardown.rb:33:in `run'"
        };

        String testName = "test_that";
        String fileName = "some_test.rb";

        String line = RubyTestMethodNode.findLocationLine(stacktrace, testName, fileName);
        assertEquals("/home/uuno/nb/testproject/test/unit/some_test.rb:40:in `test_that'", line);

    }

    public void testFindLocationLineWithClassNameOnly() {
        String[] stacktrace = {
            "NameError: undefined local variable or method `a_var' for #<ShouldaTest:0x7f6f569d83f8>",
            "/usr/lib/ruby/gems/1.8/gems/actionpack-2.1.1/lib/action_controller/test_process.rb:467:in `method_missing'",
            "/home/uuno/nb/nb_shoulda/test/shoulda_test.rb:10:in `__bind_1234867269_1480'",
            "/home/uuno/nb/nb_shoulda/vendor/plugins/shoulda/lib/shoulda/context.rb:254:in `call'",
            "/home/uuno/nb/nb_shoulda/vendor/plugins/shoulda/lib/shoulda/context.rb:254:in `test: should test this and that. '",
            "/usr/lib/ruby/gems/1.8/gems/activesupport-2.1.1/lib/active_support/testing/setup_and_teardown.rb:33:in `__send__'",
            "/usr/lib/ruby/gems/1.8/gems/activesupport-2.1.1/lib/active_support/testing/setup_and_teardown.rb:33:in `run'",
            "/usr/lib/ruby/1.8/test/unit/testsuite.rb:34:in `run'",
            "/usr/lib/ruby/1.8/test/unit/testsuite.rb:33:in `each'",
            "/usr/lib/ruby/1.8/test/unit/testsuite.rb:33:in `run'",
            "/usr/lib/ruby/1.8/test/unit/ui/testrunnermediator.rb:46:in `run_suite'"};

        String testName = "test_this_and_that";
        String fileName = "shoulda_test.rb";

        String line = RubyTestMethodNode.findLocationLine(stacktrace, testName, fileName);
        assertEquals("/home/uuno/nb/nb_shoulda/test/shoulda_test.rb:10:in `__bind_1234867269_1480'", line);

    }
}