/*
 * RubyMimeResolverTest.java
 * JUnit based test
 *
 * Created on November 6, 2006, 12:24 PM
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
