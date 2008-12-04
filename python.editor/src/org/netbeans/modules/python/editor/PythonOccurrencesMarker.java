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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;

/**
 * Occurrences finder for Python - highlights regions under the caret
 * as well as all other occurrences of the same symbol in the current file
 *
 * @todo Highlight if/elif/else keyword pairs
 *
 * @author Tor Norbye
 */
public class PythonOccurrencesMarker implements OccurrencesFinder {
    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;
    /** For testsuite */
    static Throwable error;

    public PythonOccurrencesMarker() {
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    public void run(CompilationInfo info) {
        resume();

        if (isCancelled()) {
            return;
        }

        PythonParserResult ppr = PythonAstUtils.getParseResult(info);
        if (ppr == null) {
            return;
        }
        PythonTree root = PythonAstUtils.getRoot(ppr);
        if (root == null) {
            return;
        }

        int astOffset = PythonAstUtils.getAstOffset(info, caretPosition);
        if (astOffset == -1) {
            return;
        }

        //PythonTree closest = PythonPositionManager.findClosest(root, astOffset);
        AstPath path = AstPath.get(root, astOffset);
        if (path == null) {
            return;
        }
        PythonTree closest = path.leaf();
        OffsetRange blankRange = ppr.getSanitizedRange();

        if (blankRange.containsInclusive(astOffset)) {
            closest = null;
        }

        if (closest == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights = null;
        Set<OffsetRange> offsets = null;

        boolean isNameNode = PythonAstUtils.isNameNode(closest);
        //if (isNameNode && !(path.leafParent() instanceof Call)) {
        if (isNameNode) {
            // TODO - how do I get the name?
            //String name = closest.getString();
            //addNodes(scopeNode != null ? scopeNode : root, name, highlights);
            //closest = null;
            String name = ((Name)closest).id;
            offsets = findNames(ppr, path, name, info, offsets);
        } else if (closest instanceof Attribute) {
            Attribute attr = (Attribute)closest;
            offsets = findSameAttributes(info, root, attr);
        } else if (closest instanceof Import || closest instanceof ImportFrom) {
            // Try to find occurrences of an imported symbol
            offsets = findNameFromImport(caretPosition, ppr, path, info, offsets);
        } else if ((closest instanceof FunctionDef || closest instanceof ClassDef) &&
                PythonAstUtils.getNameRange(null, closest).containsInclusive(astOffset)) {
            String name;
            if (closest instanceof FunctionDef) {
                name = ((FunctionDef)closest).name;
            } else {
                assert closest instanceof ClassDef;
                name = ((ClassDef)closest).name;
            }
            offsets = findNames(ppr, path, name, info, offsets);

            if (offsets == null || offsets.size() == 0) {
                if (closest instanceof FunctionDef) {
                    FunctionDef def = (FunctionDef)closest;
                    // Call: highlight calls and definitions
                    CallVisitor visitor = new CallVisitor(info, def, null);
                    PythonTree scope = PythonAstUtils.getClassScope(path);
                    try {
                        visitor.visit(scope);
                        Set<OffsetRange> original = offsets;
                        offsets = visitor.getRanges();
                        offsets.addAll(original);
                    } catch (Exception ex) {
                        error = ex;
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

        } else {
            Call call = null;
            FunctionDef def = null;
            if (!isNameNode) {
                PythonTree nearest = null;
                Iterator<PythonTree> it = path.leafToRoot();
                while (it.hasNext()) {
                    PythonTree node = it.next();
                    if (node instanceof Call || node instanceof FunctionDef) {
                        nearest = node;
                        break;
                    } else if (node instanceof ClassDef) {
                        break;
                    }
                }
                if (nearest != null) {
                    OffsetRange range = PythonAstUtils.getNameRange(info, nearest);
                    if (!range.containsInclusive(astOffset)) {
                        nearest = null;
                    }
                }
                if (nearest instanceof Call) {
                    call = (Call)nearest;
                } else if (nearest instanceof FunctionDef) {
                    def = (FunctionDef)nearest;
                }
            } else {
                call = (Call)path.leafParent();
            }
            if (call != null || def != null) {
                // Call: highlight calls and definitions
                CallVisitor visitor = new CallVisitor(info, def, call);
                PythonTree scope = PythonAstUtils.getClassScope(path);
                try {
                    visitor.visit(scope);
                    offsets = visitor.getRanges();
                } catch (Exception ex) {
                    error = ex;
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        if (offsets != null) {
            Map<OffsetRange, ColoringAttributes> h =
                    new HashMap<OffsetRange, ColoringAttributes>(100);

            for (OffsetRange lexRange : offsets) {
                h.put(lexRange, ColoringAttributes.MARK_OCCURRENCES);
            }

            highlights = h;
        }

        // TODO - traverse looking for the same nodes
        // Decide what scope we have, whether we're looking for a function def/call
        // or a local parameter or var, etc.

        if (highlights != null && highlights.size() > 0) {
            this.occurrences = highlights;
        } else {
            this.occurrences = null;
        }
    }

    private static class CallVisitor extends Visitor {
        private final Call call;
        private final FunctionDef def;
        private final String name;
        private final Set<OffsetRange> ranges = new HashSet<OffsetRange>();
        private final CompilationInfo info;

        CallVisitor(CompilationInfo info, FunctionDef def, Call call) {
            this.info = info;
            this.def = def;
            this.call = call;

            if (call != null) {
                this.name = PythonAstUtils.getCallName(call);
            } else if (def != null) {
                this.name = def.name;
            } else {
                throw new IllegalArgumentException(); // call or def must be nonnull
            }
        }

        @Override
        public Object visitCall(Call node) throws Exception {
            if (node == call) {
                ranges.add(PythonAstUtils.getNameRange(info, node));
            } else {
                if (name != null && name.equals(PythonAstUtils.getCallName(node))) {
                    ranges.add(PythonAstUtils.getNameRange(info, node));
                }
            }

            return super.visitCall(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            if (node == def || node.name.equals(name)) {
                ranges.add(PythonAstUtils.getNameRange(info, node));
            }

            return super.visitFunctionDef(node);
        }

        private Set<OffsetRange> getRanges() {
            return ranges;
        }
    }

    private Set<OffsetRange> findNameFromImport(int lexOffset, PythonParserResult ppr, AstPath path, CompilationInfo info, Set<OffsetRange> offsets) {
        BaseDocument doc = (BaseDocument)info.getDocument();
        try {
            String identifier = Utilities.getIdentifier(doc, lexOffset);
            if (identifier == null) {
                return null;
            }
            if ("*".equals(identifier)) {
                // TODO - something more complicated...
                return null;
            }
            // TODO - determine if you're hovering over a whole module name instead of an imported
            // symbol, and if so, work a bit harder...
            if (identifier.length() > 0) {
                return findNames(ppr, path, identifier, info, offsets);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private Set<OffsetRange> findNames(PythonParserResult ppr, AstPath path, String name, CompilationInfo info, Set<OffsetRange> offsets) {
        //offsets = PythonAstUtils.getLocalVarOffsets(info, scope, name);
        SymbolTable symbolTable = ppr.getSymbolTable();
        PythonTree scope = PythonAstUtils.getLocalScope(path);
        List<PythonTree> nodes = symbolTable.getOccurrences(scope, name, false);
        offsets = new HashSet<OffsetRange>();
        for (PythonTree node : nodes) {
            OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
            OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
            if (node instanceof Import || node instanceof ImportFrom) {
                // Try to find the exact spot
                lexRange = PythonLexerUtils.getImportNameOffset((BaseDocument)info.getDocument(), lexRange, node, name);
            }
            if (lexRange != OffsetRange.NONE) {
                offsets.add(lexRange);
            }
        }
        return offsets;
    }

    private Set<OffsetRange> findSameAttributes(CompilationInfo info, PythonTree root, Attribute attr) {
        List<PythonTree> result = new ArrayList<PythonTree>();
        PythonAstUtils.addNodesByType(root, new Class[] { Attribute.class }, result);

        Set<OffsetRange> offsets = new HashSet<OffsetRange>();

        String attrName = attr.attr;
        String name = PythonAstUtils.getName(attr.value);

        for (PythonTree node : result) {
            Attribute a = (Attribute)node;
            if (attrName.equals(a.attr) && (name == null || name.equals(PythonAstUtils.getName(a.value)))) {
                OffsetRange astRange = PythonAstUtils.getRange(node);
                // Adjust to be the -value- part
                int start = a.value.getCharStopIndex()+1;
                if (start < astRange.getEnd()) {
                    astRange = new OffsetRange(start, astRange.getEnd());
                }

                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                if (node instanceof Import || node instanceof ImportFrom) {
                    // Try to find the exact spot
                    lexRange = PythonLexerUtils.getImportNameOffset((BaseDocument)info.getDocument(), lexRange, node, name);
                }
                if (lexRange != OffsetRange.NONE) {
                    offsets.add(lexRange);
                }
            }
        }

        return offsets;
    }
}
