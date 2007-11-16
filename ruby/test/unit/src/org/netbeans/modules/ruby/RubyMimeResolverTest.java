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

import junit.framework.TestCase;

/**
 * @author Tor Norbye
 */
public class RubyMimeResolverTest extends TestCase {
    public RubyMimeResolverTest(String testName) {
        super(testName);
    }

    private boolean checkValidHeader(String header) {
        String truncated = header.length() > RubyMimeResolver.HEADER_LENGTH ?
            header.substring(0, RubyMimeResolver.HEADER_LENGTH) : header;
        byte[] h = truncated.getBytes();

        return RubyMimeResolver.isRubyHeader(h);
    }

    public void testRubyHeader() {
        assertTrue(checkValidHeader("#!/usr/bin/ruby"));
        assertTrue(checkValidHeader("#!/usr/bin/ruby1.8"));
        assertTrue(checkValidHeader("#!/usr/bin/jruby"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby\n"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby.exe"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby.exe "));
        assertTrue(checkValidHeader("#!C:\\programs\\ruby.exe"));
        assertTrue(checkValidHeader("#!C:\\programs\\ruby.exe"));
        assertTrue(checkValidHeader("#!/Users/tor/dev/ruby/install/ruby-1.8.5/bin/ruby\n"));
        assertTrue(checkValidHeader("#!/space/ruby/ruby-1.8.6-p110/bin/ruby1.8.6-p110"));
        assertTrue(checkValidHeader("#!/usr/bin/env jruby -J-Xmx512M"));
        assertTrue(checkValidHeader("#!/usr/bin/env ruby"));
        assertTrue(checkValidHeader("#!/usr/bin/env jruby"));
        assertTrue(checkValidHeader("#!/usr/bin/env.exe jruby"));

        assertFalse(checkValidHeader("# !C:\\programs\\ruby.exe"));
        assertFalse(checkValidHeader("#!/bin/sh"));
        assertFalse(checkValidHeader("#!/bin/rubystuff/bin/ksh"));
        assertFalse(checkValidHeader("#!/usr/bin/rub"));
        assertFalse(checkValidHeader("#!/usr/bin/"));
        assertFalse(checkValidHeader("#! /usr/bin/rub\ny"));
        assertFalse(checkValidHeader("#! /usr/b\nin/ruby"));
        assertFalse(checkValidHeader("#/usr/bin/ruby"));
        assertFalse(checkValidHeader("#!/usr/bin/env.exe jrub"));
    }    
}
