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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class RubyServerTest extends TestCase {

    public RubyServerTest(String testName) {
        super(testName);
    }

    public void testMongrelStartup() {

        String mongrel = "** Mongrel available at 127.0.0.1:3000 **";
        String mongrel_with_version_nro = "** Mongrel 1.1.3 available at 127.0.0.1:3000 **";
        String mongrel_with_version_nro2 = "** Mongrel 1.1.2 available at 0.0.0.0:3000";
        String mongrel_dos_line_end = "** Mongrel 1.1.2 available at 0.0.0.0:3000\r\n";
        String mongrel_unix_line_end = "** Mongrel 1.1.2 available at 0.0.0.0:3000\n";

        Mongrel mongrelInstance = new Mongrel(null);
        
        assertTrue(mongrelInstance.isStartupMsg(mongrel));
        assertTrue(mongrelInstance.isStartupMsg(mongrel_with_version_nro));
        assertTrue(mongrelInstance.isStartupMsg(mongrel_with_version_nro2));
        assertTrue(mongrelInstance.isStartupMsg(mongrel_dos_line_end));
        assertTrue(mongrelInstance.isStartupMsg(mongrel_unix_line_end));

    }

    public void testWebrickStartup() {

        String webBrick = "=> Rails application started on http://0.0.0.0:3000";
        String webBrick2 = "=> Rails application started on http://localhost:3000";
        String webBrick_dos_line_end = "=> Rails application started on http://localhost:3000 \r\n";
        String webBrick_unix_line_end = "=> Rails application started on http://localhost:3000\n";
        
        WEBrick webrickInstance = new WEBrick(null);

        assertTrue(webrickInstance.isStartupMsg(webBrick));
        assertTrue(webrickInstance.isStartupMsg(webBrick2));
        assertTrue(webrickInstance.isStartupMsg(webBrick_dos_line_end));
        assertTrue(webrickInstance.isStartupMsg(webBrick_unix_line_end));

    }

    public void testIsAddressInUseMsg(){
        assertTrue(RailsServerManager.isAddressInUseMsg("/usr/local/lib/ruby/1.8/webrick/utils.rb:62:in `initialize': Address already in use - bind(2) (Errno::EADDRINUSE)"));
        assertTrue(RailsServerManager.isAddressInUseMsg("/usr/lib/ruby/gems/1.8/gems/mongrel-0.3.13.4/lib/mongrel/tcphack.rb:12:in `initialize_without_backlog': Address already in use - bind(2) (Errno::EADDRINUSE)"));
        assertFalse(RailsServerManager.isAddressInUseMsg("Address not in use"));
    }
}
