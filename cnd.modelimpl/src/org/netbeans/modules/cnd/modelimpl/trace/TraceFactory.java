/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

import java.util.LinkedList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.parser.CppParserActionImpl;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Alexander Simon
 */
public class TraceFactory {

    public static TraceWriter getTraceWriter(Object parser) {
        if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
            return new SimpleTraceWriter(parser);
        } else {
            return new OutputTraceWriter(parser);
        }
    }

    public interface TraceWriter {
        void printIn(String message, Token... token);
        void printOut(String message, Token... token);
        void print(String message, Token... token);
    }

    private static class SimpleTraceWriter implements TraceWriter {

        private final LinkedList<String> stack = new LinkedList<String>();
        private int level = 0;
        private final CppParserActionImpl parser;
        private SimpleTraceWriter(Object parser) {
            if (parser instanceof CppParserActionImpl) {
                this.parser = (CppParserActionImpl) parser;
            } else {
                this.parser = null;
            }
        }
        
        protected void println(String s) {
            System.out.println(s);
        }
        
        @Override
        public void printIn(String message, Token... token) {
            stack.addLast(message);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append('>'); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                }
                for (int j = 0; j < token.length; j++) {
                    buf.append(' '); //NOI18N
                    buf.append(token[j].toString());
                }
            }
            println(buf.toString());
            level++;
        }

        @Override
        public void printOut(String message, Token... token) {
            String top = stack.removeLast();
            if (!message.equals(top)) {
                println("UNBALANCED exit. Actual " + message + " Expected " + top);//NOI18N
            }
            level--;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append('<'); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                    buf.append(' '); //NOI18N
                }
                buf.append(token[0].toString());
            }
            println(buf.toString());
        }

        @Override
        public void print(String message, Token... token) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append(' '); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                    buf.append(' '); //NOI18N
                }
                buf.append(token[0].toString());
            }
            println(buf.toString());
        }
    }
    
    private static final class OutputTraceWriter extends SimpleTraceWriter {

        private final InputOutput io;
        private final OutputWriter out;
        
        private OutputTraceWriter(Object parser) {
            super(parser);
            io = IOProvider.getDefault().getIO("Trace actions", false); // NOI18N
            io.select();
            out = io.getOut();
            //out.close();
        }

        @Override
        protected void println(String s) {
            out.println(s);
        }
    }
}
