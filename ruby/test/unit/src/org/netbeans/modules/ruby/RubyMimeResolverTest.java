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
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tor Norbye, Jiri Skrivanek
 */
public class RubyMimeResolverTest extends NbTestCase {

    public RubyMimeResolverTest(String testName) {
        super(testName);
    }

    public void testRubyResolver() throws IOException {
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("ruby");
        String[] validHeaders = {
            "#!/usr/bin/ruby",
            "#!/usr/bin/ruby1.8",
            "#!/usr/bin/jruby",
            "#! /usr/bin/ruby",
            "#! /usr/bin/ruby\n",
            "#! /usr/bin/ruby.exe",
            "#! /usr/bin/ruby.exe ",
            "#!C:\\programs\\ruby.exe",
            "#!C:\\programs\\ruby.exe",
            "#!/Users/tor/dev/ruby/install/ruby-1.8.5/bin/ruby\n",
            "#!/space/ruby/ruby-1.8.6-p110/bin/ruby1.8.6-p110",
            "#!/usr/bin/env jruby -J-Xmx512M",
            "#!/usr/bin/env ruby",
            "#!/usr/bin/env jruby",
            "#!/usr/bin/env.exe jruby",
            "#!D:/Development/Ruby/ruby-1.8.6-dist/bin/ruby"
        };
        String[] invalidHeaders = {
            "# !C:\\programs\\ruby.exe",
            "#!/bin/sh",
            //"#!/bin/rubystuff/bin/ksh", // mistakenly resolved as ruby file
            "#!/usr/bin/rub",
            "#!/usr/bin/",
            "#! /usr/bin/rub\ny",
            //"#! /usr/b\nin/ruby", // mistakenly resolved as ruby file
            "#/usr/bin/ruby",
            "#!/usr/bin/env.exe jrub"
        };
        for (String header : validHeaders) {
            assertHeader(fo, header, "text/x-ruby");
        }
        for (String header : invalidHeaders) {
            assertHeader(fo, header, "content/unknown");
        }
        fo = FileUtil.createMemoryFileSystem().getRoot().createData("rakefile");
        assertEquals("rakefile should be resolved.", "text/x-ruby", fo.getMIMEType());
        fo = FileUtil.createMemoryFileSystem().getRoot().createData("Rakefile");
        assertEquals("rakefile should be resolved.", "text/x-ruby", fo.getMIMEType());
        fo = FileUtil.createMemoryFileSystem().getRoot().createData("a.rb");
        assertEquals("All .rb should be resolved.", "text/x-ruby", fo.getMIMEType());
    }

    private void assertHeader(FileObject fo, String header, String expectedMimeType) throws IOException {
        OutputStream os = fo.getOutputStream();
        os.write(header.getBytes());
        os.close();
        assertEquals("Header " + header + " wrongly resolved.", expectedMimeType, fo.getMIMEType());
    }
}
