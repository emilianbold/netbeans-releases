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

package org.netbeans.modules.ruby.railsprojects;

import java.util.regex.Matcher;
import junit.framework.TestCase;

/**
 * Tests for the pattern that recognizes files when creating a rails project.
 *
 * @author Tor Norbye, Erno Mononen
 */
public class RailsProjectGeneratorPatternTest extends TestCase {

    public void testWindows103139() {
        // \r\n's seems to trip it up
        String s = "      create  app/helpers/application_helper.rb\r";
        Matcher matcher = matcher(s);
        assertTrue(matcher.matches());

        assertEquals("app/helpers/application_helper.rb", matcher.group(2));

        s = "      create  public/javascripts/prototype.js\r\n";
        matcher = matcher(s);
        assertTrue(matcher.matches());
        assertEquals("public/javascripts/prototype.js", matcher.group(2));
    }

    public void testCreate() {
        String s = "      create  app/helpers/application_helper.rb";
        Matcher matcher = matcher(s);
        assertTrue(matcher.matches());
        assertEquals("app/helpers/application_helper.rb", matcher.group(2));
    }

    public void testIdentical() {
        // \r\n's seems to trip it up
        String s = "   identical  app/controllers/foo_controller.rb";
        Matcher matcher = matcher(s);
        assertTrue(matcher.matches());
        assertEquals("app/controllers/foo_controller.rb", matcher.group(2));
    }

    public void testForce() {
        String s = "       force  app/controllers/foo_controller.rb";
        Matcher matcher = matcher(s);
        assertTrue(matcher.matches());
        assertEquals("app/controllers/foo_controller.rb", matcher.group(2));
    }
    public void testSkip() {
        String s = "        skip  app/controllers/foo_controller.rb";
        Matcher matcher = matcher(s);
        assertTrue(matcher.matches());
        assertEquals("app/controllers/foo_controller.rb", matcher.group(2));
    }

    public void testFiletypes() {
        String[] extensions = new String[] { "html.erb", "rb", "mab", "rjs", "rxml", "rake", "erb", "builder", "rhtml", "yml", "js", "erb", "html", "cgi", "fcgi", "txt", "png", "gif", "css"};
        for (String ext : extensions) {
            String s = "      create  app/helpers/application_helper." + ext;
            assertTrue(matcher(s).matches());
        }
    }

    public void testErrors() {
        // Make sure it doesn't recognize stuff which -shouldn't- be recognized
        assertFalse(matcher("foo").matches());
        assertFalse(matcher("create").matches());
        assertFalse(matcher("create foo.rb").matches());
        assertFalse(matcher("\"       force  app/controllers/foo_controller.rb\"").matches());
        assertFalse(matcher("       Force  app/controllers/foo_controller.rb").matches());
        assertFalse(matcher("").matches());
        assertFalse(matcher("foo.rb").matches());
        assertFalse(matcher("      *******************************************************************").matches());
        assertFalse(matcher("      * config.breakpoint_server has been deprecated and has no effect. *").matches());
    }

    private Matcher matcher(String line) {
        return RailsProjectGenerator.RAILS_GENERATOR_PATTERN.matcher(line);
    }

}
