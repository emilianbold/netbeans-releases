/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.cvsclient;

import java.io.*;
import junit.framework.*;


/**
 * Test of CVSRoot class.
 *
 * @author  Martin Entlicher
 */
public class CVSRootTest extends TestCase {

    /** Creates a new instance of CVSRootTest */
    public CVSRootTest(String name) {
        super(name);
    }

    private void compareRoot(CVSRoot root, String method, String user,
                             String password, String host, int port, String repository) {
        if (!(method == root.getMethod())) {
            fail("Bad connection method is parsed: '"+root.getMethod()+"', expected was '"+method+"'");
        }
        if (user != null && !user.equals(root.getUserName())) {
            fail("Bad user name is parsed: '"+root.getUserName()+"', expected was '"+user+"'");
        }
        if (password != null && !password.equals(root.getPassword())) {
            fail("Bad password is parsed: '"+root.getPassword()+"', expected was '"+password+"'");
        }
        if (host != null && !host.equals(root.getHostName())) {
            fail("Bad host name is parsed: '"+root.getHostName()+"', expected was '"+host+"'");
        }
        if (port != root.getPort()) {
            fail("Bad port is parsed: '"+root.getPort()+"', expected was '"+port+"'");
        }
        if (!repository.equals(root.getRepository())) {
            fail("Bad repository is parsed: '"+root.getRepository()+"', expected was '"+repository+"'");
        }
    }
    
    /**
     * Test of CVSRoot.parse() method.
     * It should parse the CVSROOT String of the form
     * [:method:][[user][:password]@][hostname:[port]]/path/to/repository
     *
     * For remote repositories the colon between hostname and path to repository
     * is optional when port is not specified:
     * [:method:][[user][:password]@]hostname[:[port]]/path/to/repository
     */
    public void testParseCorrectURLS() throws Exception {
        CVSRoot root = CVSRoot.parse("/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_LOCAL, null, null, null, 0, "/path/to/repository");
        root = CVSRoot.parse(":local:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_LOCAL, null, null, null, 0, "/path/to/repository");
        root = CVSRoot.parse(":local:user@hostname:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_LOCAL, null, null, null, 0, "user@hostname:/path/to/repository");
        root = CVSRoot.parse("hostname:/path/to/repository");
        compareRoot(root, "ext", null, null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse("hostname:/path:/to/repository");
        compareRoot(root, "ext", null, null, "hostname", 0, "/path:/to/repository");
        root = CVSRoot.parse(":server:hostname:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_SERVER, null, null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:hostname:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, null, null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:user@hostname:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:user:password@hostname:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", "password", "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:hostname:2403/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, null, null, "hostname", 2403, "/path/to/repository");
        root = CVSRoot.parse(":pserver:user:password@hostname:2403/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", "password", "hostname", 2403, "/path/to/repository");

        // #67504 
        root = CVSRoot.parse("c:\\CVSROOT");
        compareRoot(root, CVSRoot.METHOD_LOCAL, null, null, null, 0, "c:\\CVSROOT");
        
        // No last colon:
        root = CVSRoot.parse("hostname/path/to/repository");
        compareRoot(root, "server", null, null, "hostname", 0, "/path/to/repository");  //??? ext or server
        root = CVSRoot.parse(":server:hostname/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_SERVER, null, null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:hostname/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, null, null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:user@hostname/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", null, "hostname", 0, "/path/to/repository");
        root = CVSRoot.parse(":pserver:user:password@hostname/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", "password", "hostname", 0, "/path/to/repository");
        
        // WinCVS, CVS 1.12 has method options
        root = CVSRoot.parse(":pserver;hostname=host;username=user:/path/to/repository");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "user", null, "host", 0, "/path/to/repository");
        
        root = CVSRoot.parse(":pserver;username=SCR_Roland;hostname=mainsrv:/Software");
        compareRoot(root, CVSRoot.METHOD_PSERVER, "SCR_Roland", null, "mainsrv", 0, "/Software");
        
        // CVSNT
        root = CVSRoot.parse(":ssh;ver=2:username@cvs.sf.net:/cvsroot/xoops");
        compareRoot(root, CVSRoot.METHOD_EXT, "username", null, "cvs.sf.net", 0, "/cvsroot/xoops");
        
        root = CVSRoot.parse(":pserver:mike@javadev.zappmobile.ro:2401:/home/cvsroot");  // #71032
        compareRoot(root, CVSRoot.METHOD_PSERVER, "mike", null, "javadev.zappmobile.ro", 2401, "/home/cvsroot");
        
    }
    
    /**
     * Test of CVSRoot.parse() method.
     * It should not parse the CVSROOT String if not of the form
     * [:method:][[user][:password]@][hostname:[port]]/path/to/repository
     */
    public void testParseBadURLS() {
        CVSRoot root;
        boolean isBad = false;
        try {
            root = CVSRoot.parse(":pserver:/path/to/repository");
        } catch (IllegalArgumentException iaex) {
            isBad = true;
        }
        if (!isBad) fail("CVSROOT ':pserver:/path/to/repository' is not considered as bad.");
        isBad = false;
        try {
            root = CVSRoot.parse(":somethig that does not end with a colon");
        } catch (IllegalArgumentException iaex) {
            isBad = true;
        }
        if (!isBad) fail("CVSROOT ':somethig that does not end with a colon' is not considered as bad.");
        isBad = false;
        try {
            root = CVSRoot.parse("somethig that does not have neither slash, nor a colon");
        } catch (IllegalArgumentException iaex) {
            isBad = true;
        }
        if (!isBad) fail("CVSROOT 'somethig that does not have neither slash, nor a colon' is not considered as bad.");
    }
    

}
