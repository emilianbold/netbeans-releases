/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author Tor Norbye
 */
public class RubyMimeResolverTest extends TestCase {
    public RubyMimeResolverTest(String testName) {
        super(testName);
    }

    private boolean checkValidHeader(String header) {
        byte[] h = header.getBytes();

        return RubyMimeResolver.isRubyHeader(h);
    }

    public void testRubyHeader() {
        assertTrue(checkValidHeader("#!/usr/bin/ruby"));
        assertTrue(checkValidHeader("#!/usr/bin/jruby"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby\n"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby.exe"));
        assertTrue(checkValidHeader("#! /usr/bin/ruby.exe "));
        assertTrue(checkValidHeader("#!C:\\programs\\ruby.exe"));
        assertTrue(checkValidHeader("#!C:\\programs\\ruby.exe"));
        assertTrue(!checkValidHeader("# !C:\\programs\\ruby.exe"));
        assertTrue(!checkValidHeader("#!/usr/bin/jjruby"));
        assertTrue(!checkValidHeader("#!/usr/bin/rubyy"));
        assertTrue(!checkValidHeader("#!/usr/bin/rub"));
        assertTrue(!checkValidHeader("#!/usr/bin/"));
        assertTrue(!checkValidHeader("#! /usr/bin/rub\ny"));
        assertTrue(!checkValidHeader("#! /usr/b\nin/ruby"));
        assertTrue(!checkValidHeader("#/usr/bin/ruby"));
        assertTrue(!checkValidHeader("# !C:\\programs\\ruby.foo"));

        assertTrue(checkValidHeader("#!/usr/bin/env ruby"));
        assertTrue(checkValidHeader("#!/usr/bin/env jruby"));
        assertTrue(checkValidHeader("#!/usr/bin/env.exe jruby"));
        assertTrue(!checkValidHeader("#!/usr/bin/env.exe jrub"));
        assertTrue(!checkValidHeader("#!/usr/bin/env.exe rubyy"));
        assertTrue(!checkValidHeader("#!/usr/bin/env.txt ruby"));
        assertTrue(!checkValidHeader("#/usr/bin/env.exe jruby"));
        assertTrue(!checkValidHeader("#!/usr/bin/env.exejruby"));

        // TODO - what about case? Is "Env" legal on Windows?
    }    
}
