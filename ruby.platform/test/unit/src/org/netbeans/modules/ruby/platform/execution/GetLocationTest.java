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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby.platform.execution;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 * Tests for the {@link ExecutionUtils#getLocation(java.lang.String) } method.
 *
 * @author Tor Norbye
 */
public class GetLocationTest extends NbTestCase {

    public GetLocationTest(String testName) {
        super(testName);
    }

    private void assertRecognized(String toRecognize, String file, int line) {
        ExecutionUtils.FileLocation location = ExecutionUtils.getLocation(toRecognize);

        assertNotNull(location);
        assertEquals(file, location.file);
        assertEquals(line, location.line);
    }

    private void assertNotRecognized(String toRecognize) {
        assertNull(ExecutionUtils.getLocation(toRecognize));
    }

    private String touch(String pathInWorkDir) throws IOException {
        File f = new File(getWorkDir(), pathInWorkDir);
        f.createNewFile();
        return f.getAbsolutePath();
    }

    public void testStackUnix() {
        assertRecognized(":[-1,-1]:[0,0]: main.rb:7: unterminated string meets end of file (SyntaxError)",
                "main.rb", 7);
        assertRecognized("main.rb:6 warning: parenthesize argument(s) for future version",
                "main.rb", 6);
        assertRecognized("/Users/tor/codehaus/jruby/trunk/trunk/jruby/src/builtin/socket.rb:240:in `initialize': Errno::EADDRINUSE (Errno::EADDRINUSE)",
                "/Users/tor/codehaus/jruby/trunk/trunk/jruby/src/builtin/socket.rb", 240);
        assertRecognized("        from /Users/tor/semplice/modules/scripting/build/cluster/jruby-0.9.1/lib/ruby/1.8/webrick/utils.rb:73:in `new'",
                "/Users/tor/semplice/modules/scripting/build/cluster/jruby-0.9.1/lib/ruby/1.8/webrick/utils.rb",
                73);
        assertRecognized("        from /Users/tor/semplice/modules/scripting/build/cluster/jruby-0.9.1/lib/ruby/gems/1.8/gems/rails-1.1.6/lib/commands/servers/webrick.rb:59",
                "/Users/tor/semplice/modules/scripting/build/cluster/jruby-0.9.1/lib/ruby/gems/1.8/gems/rails-1.1.6/lib/commands/servers/webrick.rb",
                59);
    }

    public void testNotRecognized() {
        assertNotRecognized("        Hello World");
        assertNotRecognized("        Hello:World");
        assertNotRecognized("        ");
        assertNotRecognized("To set a breakpoint, enter 'b test.rb:0'");
    }

    public void testStackWindows() {
        // No spaces
        assertRecognized("        from C:\\DocumentsAndSettings\\pb97924\\MyDocuments\\NetBeansProjects\\RubyApplication2\\lib\\main.rb:10:in `say_hi'",
                "C:\\DocumentsAndSettings\\pb97924\\MyDocuments\\NetBeansProjects\\RubyApplication2\\lib\\main.rb",
                10);
        // Spaces
        assertRecognized("        from C:\\Documents and Settings\\pb97924\\My Documents\\NetBeansProjects\\RubyApplication2\\lib\\main.rb:10:in `say_hi'",
                "C:\\Documents and Settings\\pb97924\\My Documents\\NetBeansProjects\\RubyApplication2\\lib\\main.rb",
                10);
    }

    public void testInstantRails() {
        assertRecognized("  C:/instantrails/ruby/lib/ruby/gems/1.8/gems/active-record-1.15.3/lib/active_record/connection_adapters/mysql_adapter.rb:389:in `real_connect'",
                "C:/instantrails/ruby/lib/ruby/gems/1.8/gems/active-record-1.15.3/lib/active_record/connection_adapters/mysql_adapter.rb",
                389);
        // Make sure we can handle a \r at the end too
        assertRecognized("  C:/instantrails/ruby/lib/ruby/gems/1.8/gems/active-record-1.15.3/lib/active_record/connection_adapters/mysql_adapter.rb:389:in `real_connect'\r",
                "C:/instantrails/ruby/lib/ruby/gems/1.8/gems/active-record-1.15.3/lib/active_record/connection_adapters/mysql_adapter.rb",
                389);
    }

    // Make sure hyperlinks work for RAILS_ROOT stack traces - see #108080
    public void testRailsRoot() {
        assertRecognized("  #{RAILS_ROOT}/app/models/tree_diff_summary.rb:101:in `calculate'",
                "app/models/tree_diff_summary.rb", 101);
        assertRecognized("test_execute_requests_with_empty_queue(RestPhone::PhoneCallTest) [#{RAILS_ROOT}/test/unit/fake/fake_for_testing_test.rb:9]:",
                "test/unit/fake/fake_for_testing_test.rb", 9);
        assertRecognized("test_execute_requests_with_empty_queue(RestPhone::PhoneCallTest) [   #{RAILS_ROOT}/test/unit/fake/fake_for_testing_test.rb:9]:",
                "test/unit/fake/fake_for_testing_test.rb", 9);
    }

    public void testTestHyperlink() {
        assertRecognized("test_to_xml(RightsDTOTest)[/Users/stephenmolitor/mercy/mercyresources/test/unit/rights_dto_test.rb:23]: <\"resource\"> ",
                "/Users/stephenmolitor/mercy/mercyresources/test/unit/rights_dto_test.rb", 23);
    }

    public void testCheck109721() {
        assertRecognized(" \"C:/InstantRails/ruby/lib/ruby/site_ruby/1.8/rubygems/custom_require.rb:27:in `require'\",",
                "C:/InstantRails/ruby/lib/ruby/site_ruby/1.8/rubygems/custom_require.rb", 27);
    }

    public void testCheck109724() {
        assertRecognized("./test/unit/http_phone/asterisk_response_test.rb:3",
                "test/unit/http_phone/asterisk_response_test.rb", 3);
    }

    public void testCheck109724b() {
        assertRecognized(" \"./test/unit/http_phone/asterisk_response_test.rb:3:in `require'\"",
                "test/unit/http_phone/asterisk_response_test.rb", 3);
    }

    public void testCheck109278() {
        assertRecognized("C:/InstantRails/rails_apps/ss/app/models/s2_test_case.rb:105: syntax error, unexpected $end, expecting tSTRING_CONTENT or tSTRING_DBEG or tSTRING_DVAR or tSTRING_END    sql = <<EOS\r",
                "C:/InstantRails/rails_apps/ss/app/models/s2_test_case.rb", 105);
        assertRecognized("SyntaxError: C:/InstantRails/rails_apps/ss/app/models/s2_test_case.rb:175: can't find string \"EOS\" anywhere before EOF",
                "C:/InstantRails/rails_apps/ss/app/models/s2_test_case.rb", 175);
    }

    public void testCheck110633() throws IOException {
        String sqlupPath = touch("sqlup");
        String rdebugPath = touch("rdebug-ide");

        assertRecognized("C:/InstantRails/ruby/lib/ruby/1.8/pathname.rb:205:in `dup': can't dup NilClass (TypeError)",
                "C:/InstantRails/ruby/lib/ruby/1.8/pathname.rb", 205);
        assertRecognized("from C:/InstantRails/ruby/lib/ruby/1.8/pathname.rb:205:in `initialize'",
                "C:/InstantRails/ruby/lib/ruby/1.8/pathname.rb", 205);
        assertRecognized("from " + sqlupPath + ":64:in `new'", sqlupPath, 64);
        assertRecognized("from " + sqlupPath + ":64", sqlupPath, 64);
        assertRecognized("from C:/InstantRails/ruby/lib/ruby/gems/1.8/gems/ruby-debug-ide-0.1.8/lib/ruby-debug.rb:79:in `debug_load'",
                "C:/InstantRails/ruby/lib/ruby/gems/1.8/gems/ruby-debug-ide-0.1.8/lib/ruby-debug.rb", 79);
        assertRecognized("from C:/InstantRails/ruby/lib/ruby/gems/1.8/gems/ruby-debug-ide-0.1.8/lib/ruby-debug.rb:79:in `main'",
                "C:/InstantRails/ruby/lib/ruby/gems/1.8/gems/ruby-debug-ide-0.1.8/lib/ruby-debug.rb", 79);
        assertRecognized("from " + rdebugPath + ":74", rdebugPath, 74);
        assertRecognized("from " + rdebugPath + ":16:in `load'", rdebugPath, 16);
        assertRecognized("from " + rdebugPath + ":16", rdebugPath, 16);

        // the same with extensions
        assertRecognized("        from C:\\InstantRails\\rails_apps\\sq\\bin\\sqlup.rb:64:in `new'",
                "C:\\InstantRails\\rails_apps\\sq\\bin\\sqlup.rb", 64);
    }
    
    public void testCheck112254() {
        assertRecognized("test_snark(RestPhoneTest::PhoneRequestRecordTest) [./test/unit/http_phone/asterisk_cmd_test.rb:76]:",
                "test/unit/http_phone/asterisk_cmd_test.rb", 76);
        assertRecognized("test_execute_requests_with_empty_queue(RestPhone::PhoneCallTest) [C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_call_test.rb:21]:",
                "C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_call_test.rb", 21);
        assertRecognized("test_snark(RestPhoneTest::PhoneRequestRecordTest) [./test/unit/http_phone/asterisk_cmd_test.rb:76]:\r",
                "test/unit/http_phone/asterisk_cmd_test.rb", 76);
        assertRecognized("test_execute_requests_with_empty_queue(RestPhone::PhoneCallTest) [C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_call_test.rb:21]:\r",
                "C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_call_test.rb", 21);
    }

    public void testCheck107236() {
        assertRecognized("LOG>   Stacktrace: /applications/ruby/tester/lib/test.rb:1",
                "/applications/ruby/tester/lib/test.rb", 1);
    }

    public void testCheck98799() {
        assertRecognized("To set a breakpoint, enter 'b test.rb:4'", "test.rb", 4);
    }
}
