/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.execute;

import java.io.StringWriter;
import junit.framework.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public class OutputHandlerTest extends TestCase {

    public OutputHandlerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(OutputHandlerTest.class);

        return suite;
    }

    public void testSequence() {
        HashMap procs = new HashMap();
        HashSet set = new HashSet();
        TestProcessor proc = new TestProcessor();
        set.add(proc);
        procs.put("mojo-execute#test:test", set);
        JavaOutputHandler handler = new JavaOutputHandler();
        handler.setup(procs, new NullOutputWriter(), new NullOutputWriter());
        assertFalse(proc.processing);
        handler.startEvent("mojo-execute", "test:xxx", 0);
        assertFalse(proc.processing);
        handler.startEvent("mojo-execute", "test:test", 0);
        assertTrue(proc.processing);
        handler.error("xxx");
        handler.endEvent("mojo-execute", "test:test", 0);
        assertFalse(proc.processing);
        handler.error("xxx");
//       fail();
    }

    private class TestProcessor implements OutputProcessor {

        boolean processing = false;

        public String[] getRegisteredOutputSequences() {
            return new String[]{
                        "mojo-execute#test:test"
                    };
        }

        public void processLine(String line, OutputVisitor visitor) {
            if (!processing) {
                fail();
            }
        }

        public void sequenceStart(String sequenceId, OutputVisitor visitor) {
            processing = true;
        }

        public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
            processing = false;
        }

        public void sequenceFail(String sequenceId, OutputVisitor visitor) {
            processing = false;
        }
    }

    private class NullOutputWriter extends OutputWriter {

        NullOutputWriter() {
            super(new StringWriter());
        }

        public void println(String string, OutputListener outputListener) throws IOException {
        }

        public void reset() throws IOException {
        }

        @Override
        public void print(Object obj) {
        }

        @Override
        public void println(Object x) {
        }

        @Override
        public void println(boolean x) {
        }

        @Override
        public void print(boolean b) {
        }

        @Override
        public void print(double d) {
        }

        @Override
        public void println(double x) {
        }

        @Override
        public void println(char x) {
        }

        @Override
        public void print(char c) {
        }

        @Override
        public void write(char[] buf, int off, int len) {
        }

        @Override
        public void print(float f) {
        }

        @Override
        public void println(float x) {
        }

        @Override
        public void print(String s) {
        }

        @Override
        public void println(String x) {
        }

        @Override
        public void write(String s) {
        }

        @Override
        public void print(int i) {
        }

        @Override
        public void println(int x) {
        }

        @Override
        public void write(int c) {
        }

        @Override
        public void println(String s, OutputListener l, boolean important) throws IOException {
        }

        @Override
        public void print(long l) {
        }

        @Override
        public void println(long x) {
        }

        @Override
        public void println(char[] x) {
        }

        @Override
        public void print(char[] s) {
        }

        @Override
        public void write(char[] buf) {
        }

        @Override
        public void write(String s, int off, int len) {
        }

        @Override
        public void println() {
        }

        @Override
        protected void setError() {
        }
    }
}
