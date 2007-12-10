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

import junit.framework.TestCase;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FileLocation;
import org.netbeans.modules.ruby.platform.execution.RegexpOutputRecognizer;

/**
 * @author Tor Norbye
 */
public class RailsProjectGeneratorTest extends TestCase {
    
    public RailsProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testWindows103139() {
        // \r\n's seems to trip it up
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "      create  app/helpers/application_helper.rb\r";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/helpers/application_helper.rb", location.file);
        s = "      create  public/javascripts/prototype.js\r\n";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("public/javascripts/prototype.js", location.file);
    }    

    public void testCreate() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "      create  app/helpers/application_helper.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/helpers/application_helper.rb", location.file);
    }    

    public void testIdentical() {
        // \r\n's seems to trip it up
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "   identical  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    

    public void testForce() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "       force  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    
    public void testSkip() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "        skip  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    

    // Exists is used only for dirs, which we don't hyperlink -- XXX should we? And select/open in projects view?
//    public void testExists() {
//        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
//        FileLocation location;
//
//        String s = "      exists  app/views/foo.rb";
//        location = recognizer.processLine(s);
//        assertNotNull(location);
//        assertEquals("app/views/foo.rb", location.file);
//    }    
    
    public void testFiletypes() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String[] extensions = new String[] { "html.erb", "rb", "mab", "rjs", "rxml", "rake", "erb", "builder", "rhtml", "yml", "js", "erb", "html", "cgi", "fcgi", "txt", "png", "gif", "css"};
        for (String ext : extensions) {
            String s = "      create  app/helpers/application_helper." + ext;
            location = recognizer.processLine(s);
            assertNotNull("Failed to recognize " + s, location);
            assertEquals("app/helpers/application_helper." + ext, location.file);
        }
    }    

    public void testErrors() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        // Make sure it doesn't recognize stuff which -shouldn't- be recognized
        assertNull(recognizer.processLine("foo"));
        assertNull(recognizer.processLine("create"));
        assertNull(recognizer.processLine("create foo.rb"));
        assertNull(recognizer.processLine("\"       force  app/controllers/foo_controller.rb\""));
        assertNull(recognizer.processLine("       Force  app/controllers/foo_controller.rb"));
        assertNull(recognizer.processLine(""));
        assertNull(recognizer.processLine("foo.rb"));        
        assertNull(recognizer.processLine("      *******************************************************************"));
        assertNull(recognizer.processLine("      * config.breakpoint_server has been deprecated and has no effect. *"));
    }

}
