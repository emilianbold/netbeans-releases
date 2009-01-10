/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.List;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.VisitorBase;
import org.python.antlr.base.expr;

/**
 * Type Analyzer for Python. This class is responsible for
 * figuring out the type of variables and expressions, by analyzing the
 * python parse tree, and in some cases, consulting the persistent index.
 *
 * @author Tor Norbye
 */
public class PythonTypeAnalyzer {
    private static final Pattern TYPE_DECLARATION = Pattern.compile("\\s*#\\s*@type\\s+(\\S+)\\s+(\\S+).*"); // NOI18N
    private PythonIndex index;
    /** Map from variable or field(etc) name to type. */
    private Map<String, String> localVars;
    private final int astOffset;
    private final int lexOffset;
    private final PythonTree root;
    /** PythonTree we are looking for;  */
    private PythonTree target;
    private final FileObject fileObject;
    private final CompilationInfo info;
    private long startTime;

    /** Creates a new instance of JsTypeAnalyzer for a given position.
     * The {@link #analyze} method will do the rest. */
    public PythonTypeAnalyzer(CompilationInfo info, PythonIndex index, PythonTree root, PythonTree target, int astOffset, int lexOffset, FileObject fileObject) {
        this.info = info;
        this.index = index;
        this.root = root;
        this.target = target;
        this.astOffset = astOffset;
        this.lexOffset = lexOffset;
        this.fileObject = fileObject;
    }

//    /**
//     * Determine if the given expression depends on local variables.
//     * If it does not, we can skip tracking variables through the functon
//     * and only compute the current expression.
//     */
//    private boolean dependsOnLocals() {
//        ...
//    }
    private final class TypeVisitor extends VisitorBase<String> {
        private int targetAstOffset;
        private Map<String, String> localVars;

        TypeVisitor(Map<String, String> localVars, int targetAstOffset) {
            this.localVars = localVars;
            this.targetAstOffset = targetAstOffset;
        }

        public String visit(PythonTree node) throws Exception {
            if (node.getCharStartIndex() >= targetAstOffset) {
                return null;
            }
            String ret = node.accept(this);
            return ret;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            if (node.getCharStartIndex() >= targetAstOffset) {
                return;
            }
            node.traverse(this);
        }

        @Override
        protected String unhandled_node(PythonTree node) throws Exception {
            return null;
        }

        @Override
        public String visitStr(Str str) throws Exception {
            return "str"; // NOI18N
        }

        @Override
        public String visitNum(Num node) throws Exception {
            return "int"; // NOI18N
        }

        @Override
        public String visitTuple(Tuple node) throws Exception {
            return "tuple"; // NOI18N
        }

        @Override
        public String visitList(List node) throws Exception {
            return "list"; // NOI18N
        }

        // ListComp?
        @Override
        public String visitAssign(Assign assign) throws Exception {
            if (assign.getCharStartIndex() >= targetAstOffset) {
                return null;
            }
            String type = null;
            expr value = assign.getInternalValue();
            if (value instanceof Name) {
                Name name = (Name)value;
                type = localVars.get(name.getInternalId());
            } else if (value instanceof Call) {
                Call call = (Call)value;
                if (call.getInternalFunc() instanceof Name) {
                    String funcName = ((Name)call.getInternalFunc()).getInternalId();
                    if (Character.isUpperCase(funcName.charAt(0)) ||
                            index.isLowercaseClassName(funcName)) {
                        // If you do x = Foo(), then the type of x is Foo.
                        // Can't just do upper-case comparison here
                        // since file() will return a "file" class object
                        // (Python has, despite PEP8, many lowercase-named classes)
                        type = funcName;
                    }
                }

            }
            if (type == null) {
                type = value.accept(this);
            }
            if (type != null) {
                java.util.List<expr> targets = assign.getInternalTargets();
                if (targets != null) {
                    for (expr et : targets) {
                        if (et instanceof Name) {
                            Name name = (Name)et;
                            localVars.put(name.getInternalId(), type);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public String visitCall(Call call) throws Exception {
            if (call.getCharStartIndex() >= targetAstOffset) {
                return null;
            }
            if (call.getInternalFunc() != null) {
                return call.getInternalFunc().accept(this);
            }
            return null;
        }

        @Override
        public String visitName(Name name) throws Exception {
            if (name.getCharStartIndex() >= targetAstOffset) {
                return null;
            }
            String id = name.getInternalId();
            if (id != null && id.length() > 0 && Character.isUpperCase(id.charAt(0))) {
                return id;
            }

            return null;
        }
    }

    private void init() {
        if (localVars == null) {
            startTime = System.currentTimeMillis();
            localVars = new HashMap<String, String>();

            if (info != null && root != null) {
                // Look for type annotations
                BaseDocument doc = (BaseDocument)info.getDocument();
                if (doc != null) {
                    OffsetRange range = PythonLexerUtils.getLexerOffsets(info, PythonAstUtils.getRange(root));
                    if (range != OffsetRange.NONE) {
                        try {
                            doc.readLock();
                            int start = Math.min(doc.getLength(), range.getStart());
                            int length = Math.min(range.getLength(), doc.getLength() - start);
                            String code = doc.getText(start, length);
                            int next = 0;
                            while (true) {
                                int idx = code.indexOf("@type ", next);
                                if (idx == -1) {
                                    break;
                                }
                                int lineBegin = idx;
                                for (; lineBegin >= 0; lineBegin--) {
                                    if (code.charAt(lineBegin) == '\n') {
                                        break;
                                    }
                                }
                                lineBegin++;

                                int lineEnd = idx;
                                for (; lineEnd < code.length(); lineEnd++) {
                                    if (code.charAt(lineEnd) == '\n') {
                                        break;
                                    }
                                }

                                Matcher matcher = TYPE_DECLARATION.matcher(code.substring(lineBegin, lineEnd));
                                if (matcher.matches()) {
                                    String var = matcher.group(1);
                                    String type = matcher.group(2);
                                    localVars.put(var, type);
                                }

                                next = lineEnd + 1;
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            doc.readUnlock();
                        }
                    }
                }
            }


            TypeVisitor visitor = new TypeVisitor(localVars, astOffset);
            try {
                visitor.visit(root);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                localVars = Collections.emptyMap();
            }
        }
    }

    /** Like getType(), but doesn't strip off array type parameters etc. */
    private String getTypeInternal(String symbol) {
        String type = null;

        if (localVars != null) {
            type = localVars.get(symbol);
        }

        // TODO:
        // Look in the FunctionCache

        return type;
    }

    /** Return the type of the given symbol */
    public String getType(String symbol) {
        init();

        String type = getTypeInternal(symbol);

        // We keep track of the types contained within Arrays
        // internally (and probably hashes as well, TODO)
        // such that we can do the right thing when you operate
        // on an Array. However, clients should only see the "raw" (and real)
        // type.
        if (type != null && type.startsWith("Array<")) { // NOI18N
            return "Array"; // NOI18N
        }

        return type;
    }
}
