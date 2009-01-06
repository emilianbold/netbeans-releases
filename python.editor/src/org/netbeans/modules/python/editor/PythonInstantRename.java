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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;

/**
 *
 * @author Tor Norbye
 */
public class PythonInstantRename implements InstantRenamer {
    public boolean isRenameAllowed(CompilationInfo info, int caretOffset, String[] explanationRetValue) {
        PythonTree root = PythonAstUtils.getRoot(info);
        if (root == null) {
            return false;
        }

        AstPath path = AstPath.get(root, caretOffset);
        PythonTree leaf = path.leaf();

        if (PythonAstUtils.isNameNode(leaf)) {
            return true;
        }

        if ((leaf instanceof FunctionDef || leaf instanceof Call || leaf instanceof ClassDef) &&
                PythonAstUtils.getNameRange(null, leaf).containsInclusive(caretOffset)) {
            return true;
        }

        return false;
    }

    public Set<OffsetRange> getRenameRegions(CompilationInfo info, int caretOffset) {
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        PythonTree root = parseResult.getRoot();
        if (root != null) {
            SymbolTable symbolTable = parseResult.getSymbolTable();
            AstPath path = AstPath.get(root, caretOffset);
            PythonTree leaf = path.leaf();
            String name = null;
            if (leaf instanceof Name) {
                name = ((Name)leaf).getInternalId();
                PythonTree scope = PythonAstUtils.getLocalScope(path);
                List<PythonTree> nodes = symbolTable.getOccurrences(scope, name, true);
                if (nodes == null) {
                    // Aborted - we've encountered free variables and full renaming is required
                    return Collections.emptySet();
                }
                Set<OffsetRange> offsets = new HashSet<OffsetRange>();
                for (PythonTree node : nodes) {

                    if (node instanceof Import || node instanceof ImportFrom ||
                            node instanceof FunctionDef || node instanceof ClassDef) {
                        return Collections.emptySet();
                    }
                    OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                    if (lexRange != OffsetRange.NONE) {
                        offsets.add(lexRange);
                    }
                }

                return offsets;
            }
        }

        return Collections.emptySet();
    }
}
