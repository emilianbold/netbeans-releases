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
package org.netbeans.modules.ruby.platform.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.ruby.platform.execution.OutputForwarder;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.RecognizedOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import static org.netbeans.modules.ruby.platform.execution.OutputForwarder.RANGE_ERROR_RE;

public class OutputForwarderTest extends TestCase {

    public OutputForwarderTest(String testName) {
        super(testName);
    }

    public void testRangeErrorRegExp() throws Exception {
        assertFalse(RANGE_ERROR_RE.matcher("whatever").matches());
        assertFalse(RANGE_ERROR_RE.matcher("#<RangeError: 0x is recycled object>").matches());
        assertTrue(RANGE_ERROR_RE.matcher("#<RangeError: 0xdb4a90ec is recycled object>").matches());
        assertTrue(RANGE_ERROR_RE.matcher("#<RangeError: 0x15a64524196c is recycled object>").matches());
        assertTrue(RANGE_ERROR_RE.matcher("#<RangeError: 0x1 is recycled object>").matches());
    }

    public void testRSpec4AnsiColors() throws  Exception {
        TestOutputRecognizer tor = new TestOutputRecognizer();
        TestOutputWriter writer = new TestOutputWriter(new PrintWriter(new ByteArrayOutputStream()));
        OutputForwarder forwarder = new OutputForwarder(null, writer, null, Collections.<OutputRecognizer>singletonList(tor), null, null);

        forwarder.processLine("\033[1;35m2 examples, 0 failures, 5 not implemented\033[0m");
        forwarder.processLine("\033[32m1 example, 1 failure\033[0m\r");
        assertEquals("two lines", 2, tor.lines.size());
        assertEquals("2 examples, 0 failures, 5 not implemented", tor.lines.get(0));
        assertEquals("1 example, 1 failure\r", tor.lines.get(1));
    }

    private static class TestOutputRecognizer extends OutputRecognizer {
        
        List<String> lines = new ArrayList<String>();

        public @Override RecognizedOutput processLine(String line) {
            lines.add(line);
            return null;
        }
        
    }
    
    private static class TestOutputWriter extends OutputWriter {
        
        TestOutputWriter(Writer w) {
            super(w);
        }

        public @Override void println(String s, OutputListener l) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public @Override void reset() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}