/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.api.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntry;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 *
 * @author ak119685
 */
public class DefaultStackParserImplTest {

    private final DefaultStackParserImpl parser;

    public DefaultStackParserImplTest() {
        parser = new DefaultStackParserImpl();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parseEntry method, of class DefaultStackParserImpl.
     */
    @Test
    public void testParseEntry() {
        parse("libstdc++.so.6.0.3`_ZdaPv+0x1b", new CallStackEntryImpl(
                "libstdc++.so.6.0.3`_ZdaPv+0x1b",
                "libstdc++.so.6.0.3",
                -1,
                "_ZdaPv",
                27,
                null));

        parse("libstdc++.so.6.0.3+0xf`_ZdaPv+0x1b", new CallStackEntryImpl(
                "libstdc++.so.6.0.3+0xf`_ZdaPv+0x1b",
                "libstdc++.so.6.0.3",
                15,
                "_ZdaPv",
                27,
                null));

        parse("libstdc++.so.6.0.3+15`_ZdaPv+0x1b", new CallStackEntryImpl(
                "libstdc++.so.6.0.3+15`_ZdaPv+0x1b",
                "libstdc++.so.6.0.3",
                15,
                "_ZdaPv",
                27,
                null));

        parse("/tmp/test/example+0x11f58`_start+0xd8", new CallStackEntryImpl(
                "/tmp/test/example+0x11f58`_start+0xd8",
                "/tmp/test/example",
                73560,
                "_start",
                216, null));

        parse("/tmp/test/example+0x11f58`main+0x32:/tmp/test/test.c:48", new CallStackEntryImpl(
                "/tmp/test/example+0x11f58`main+0x32:/tmp/test/test.c:48",
                "/tmp/test/example",
                73560,
                "main",
                50,
                new SourceFileInfo("/tmp/test/test.c", 48, -1)));
    }

    private void parse(String entry, CallStackEntryImpl expected) {
        System.out.print("Parsing '" + entry + "' ... ");
        
        CallStackEntry result = parser.parseEntry(entry);
        assertNotNull(result);
        assertEquals(result.getOriginalEntry(), expected.getOriginalEntry());
        assertEquals(result.getModulePath(), expected.getModulePath());
        assertEquals(result.getOffsetInModule(), expected.getOffsetInModule());
        assertEquals(result.getFunctionName(), expected.getFunctionName());
        assertEquals(result.getOffsetInFunction(), expected.getOffsetInFunction());

        SourceFileInfo r = result.getSourceFileInfo();
        SourceFileInfo e = result.getSourceFileInfo();

        if (r == null) {
            assertNull(e);
        } else if (e == null) {
            assertNull(r);
        } else {
            assertEquals(r.getFileName(), e.getFileName());
            assertEquals(r.getLine(), e.getLine());
            assertEquals(r.getColumn(), e.getColumn());
        }
        System.out.println("OK");
    }

    private static final class CallStackEntryImpl implements CallStackEntry {

        private final CharSequence orig;
        private final CharSequence module;
        private final long moduleOffset;
        private final CharSequence function;
        private final long functionOffset;
        private final SourceFileInfo srcInfo;

        public CallStackEntryImpl(CharSequence orig, CharSequence module, long moduleOffset, CharSequence function, long functionOffset, SourceFileInfo srcInfo) {
            this.orig = orig;
            this.module = module;
            this.moduleOffset = moduleOffset;
            this.function = function;
            this.functionOffset = functionOffset;
            this.srcInfo = srcInfo;
        }

        @Override
        public CharSequence getOriginalEntry() {
            return orig;
        }

        @Override
        public CharSequence getModulePath() {
            return module;
        }

        @Override
        public long getOffsetInModule() {
            return moduleOffset;
        }

        @Override
        public CharSequence getFunctionName() {
            return function;
        }

        @Override
        public long getOffsetInFunction() {
            return functionOffset;
        }

        @Override
        public SourceFileInfo getSourceFileInfo() {
            return srcInfo;
        }
    }
}