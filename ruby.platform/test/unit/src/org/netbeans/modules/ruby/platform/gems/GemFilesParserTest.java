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

package org.netbeans.modules.ruby.platform.gems;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class GemFilesParserTest extends TestCase {
    
    public GemFilesParserTest(String testName) {
        super(testName);
    }            

    public void testParseInfo() {
        
        String gem1 = "mongrel-1.1.3-i386-mswin32";
        assertEquals("mongrel", GemFilesParser.parseInfo(gem1).getName());
        assertEquals("1.1.3", GemFilesParser.parseInfo(gem1).getVersion());
        
        String gem2 = "activeresource-2.0.2";
        assertEquals("activeresource", GemFilesParser.parseInfo(gem2).getName());
        assertEquals("2.0.2", GemFilesParser.parseInfo(gem2).getVersion());
        
        String gem3 = "win32-sapi-0.1.4";
        assertEquals("win32-sapi", GemFilesParser.parseInfo(gem3).getName());
        assertEquals("0.1.4", GemFilesParser.parseInfo(gem3).getVersion());
        
        String gem4 = "cgi_multipart_eof_fix-2.5.0";
        assertEquals("cgi_multipart_eof_fix", GemFilesParser.parseInfo(gem4).getName());
        assertEquals("2.5.0", GemFilesParser.parseInfo(gem4).getVersion());
        
        String gem5 = "win32-api-1.0.5-x86-mswin32-60";
        assertEquals("win32-api", GemFilesParser.parseInfo(gem5).getName());
        assertEquals("1.0.5", GemFilesParser.parseInfo(gem5).getVersion());
        
    }

}
