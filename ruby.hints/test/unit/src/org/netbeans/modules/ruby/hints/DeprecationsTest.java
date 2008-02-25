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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.ruby.hints.HintTestBase;

/**
 *
 * @author Tor Norbye
 */
public class DeprecationsTest extends HintTestBase {
    
    public DeprecationsTest(String testName) {
        super(testName);
    }            

    public void testHint1() throws Exception {
        findHints(this, new Deprecations(), "testfiles/require_gem.rb", null);
    }

    public void testHint2() throws Exception {
        findHints(this, new Deprecations(), "testfiles/deprecations.rb", null);
    }

    public void testFix1() throws Exception {
        applyHint(this, new Deprecations(), "testfiles/require_gem.rb", 
                "req^uire_gem", "Replace");
    }

    public void testFix2() throws Exception {
        applyHint(this, new Deprecations(), "testfiles/deprecations.rb", 
                "asse^rt_raises", "Replace");
    }

    public void testFix3() throws Exception {
        applyHint(this, new Deprecations(), "testfiles/require_gem.rb", 
                "require_g^em 'rails', '2.0.1'", "Replace");
    }

    public void testFix4() throws Exception {
        applyHint(this, new Deprecations(), "testfiles/require_gem.rb", 
                "require^_gem \"rails\" #2", "Replace");
    }

    public void testFix5() throws Exception {
        applyHint(this, new Deprecations(), "testfiles/require_gem.rb", 
                "require_g^em File.dirname", "Replace");
    }

    public void testNoPositives() throws Exception {
        try {
            parseErrorsOk = true;
            Set<String> exceptions = new HashSet<String>();
            
            // Env.rb and parsearg.rb include deprecated require calls (optparse and printenv)
            // Everything else is related to the assert_raises call.
            
            // Known exceptions
            exceptions.add("assert.rb");
            exceptions.add("invocation_test.rb");
            exceptions.add("test_package_task.rb");
            exceptions.add("Env.rb");
            exceptions.add("parsearg.rb");
            exceptions.add("dispatcher_action_controller_soap_test.rb");
            exceptions.add("routing_test.rb");
            exceptions.add("associations_test.rb");
            exceptions.add("url_helper_test.rb");
            exceptions.add("callbacks_test.rb");
            exceptions.add("deprecated_finder_test.rb");
            exceptions.add("finder_test.rb");
            exceptions.add("test_tasks.rb");
            exceptions.add("url_rewriter_test.rb");
            exceptions.add("client_xmlrpc_test.rb");
            exceptions.add("base_test.rb");
            exceptions.add("locking_test.rb");
            exceptions.add("inheritance_test.rb");
            exceptions.add("resources_test.rb");
            exceptions.add("casting_test.rb");
            exceptions.add("redirect_test.rb");
            exceptions.add("selector_test.rb");
            exceptions.add("test_fileutils.rb");
            exceptions.add("test_application.rb");
            exceptions.add("test_definitions.rb");
            exceptions.add("deprecated_associations_test.rb");
            exceptions.add("api_test.rb");
            exceptions.add("fixtures_test.rb");
            exceptions.add("new_render_test.rb");
            exceptions.add("format.rb");
            exceptions.add("calculations_test.rb");
            exceptions.add("render_test.rb");
            exceptions.add("action_pack_assertions_test.rb");
            exceptions.add("join_model_test.rb");
            exceptions.add("validations_test.rb");
            exceptions.add("filters_test.rb");
            exceptions.add("test_rules.rb");
            exceptions.add("eager_test.rb");
            exceptions.add("assert_select_test.rb");
            exceptions.add("container_test.rb");
            exceptions.add("aggregations_test.rb");
            exceptions.add("migration_test.rb");
            exceptions.add("simple.rb");
            
            // Hits for require ftools.rb
            exceptions.add("rake.rb");
            exceptions.add("ri_generator.rb");
            exceptions.add("install.rb");
            exceptions.add("command-processor.rb");
            exceptions.add("rdoc.rb");
            exceptions.add("xml_generator.rb");
            exceptions.add("html_generator.rb");
            exceptions.add("filecreation.rb");
            exceptions.add("sys.rb");

            // New in Rails 2.0.2:
            
            exceptions.add("authorization_test.rb");
            exceptions.add("session_fixation_test.rb");
            exceptions.add("request_forgery_protection_test.rb");
            exceptions.add("test_helper_test.rb");
            exceptions.add("test_test.rb");
            exceptions.add("attribute_methods_test.rb");
            exceptions.add("mail_render_test.rb");
            exceptions.add("mime_responds_test.rb");
            exceptions.add("request_test.rb");
            
            assertNoJRubyMatches(new Deprecations(), exceptions);
            
        } finally {
            parseErrorsOk = false;
        }
    }

}
