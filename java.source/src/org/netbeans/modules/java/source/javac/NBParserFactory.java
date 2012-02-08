/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.javac;

import com.sun.tools.javac.parser.EndPosParser;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.util.Map;

/**
 *
 * @author lahvac
 */
public class NBParserFactory extends ParserFactory {

    public static void preRegister(Context context) {
        context.put(parserFactoryKey, new Context.Factory<ParserFactory>() {
            public ParserFactory make(Context c) {
                return new NBParserFactory(c);
            }
        });
    }

    private final ScannerFactory scannerFactory;
    private final CancelService cancelService;

    protected NBParserFactory(Context context) {
        super(context);
        this.scannerFactory = ScannerFactory.instance(context);
        this.cancelService = CancelService.instance(context);
    }

    @Override
    public Parser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap) {
        return newParser (input, keepDocComments, keepEndPos, keepLineMap, false);
    }

    @Override
    public Parser newParser(CharSequence input, int startPos, Map<JCTree,Integer> endPos) {
        Lexer lexer = scannerFactory.newScanner(input, true);
        ((Scanner)lexer).seek(startPos);
        JavacParser p = new NBEndPosParser(this, lexer, true, false, endPos, cancelService);
        return p;
    }

    @Override
    public Parser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap, boolean partial) {
        Lexer lexer = scannerFactory.newScanner(input, keepDocComments);
        JavacParser p;
        if (keepEndPos) {
            p = new NBEndPosParser(this, lexer, keepDocComments, keepLineMap, cancelService);
        } else {
            p = new NBJavacParser(this, lexer, keepDocComments, keepLineMap, cancelService);
        }
        return p;
    }

    protected static class NBJavacParser extends JavacParser {

        private final CancelService cancelService;

        public NBJavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, CancelService cancelService) {
            super(fac, S, keepDocComments, keepLineMap);
            this.cancelService = cancelService;
        }

        @Override
        protected JCClassDecl classDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.classDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl interfaceDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.interfaceDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.enumDeclaration(mods, dc);
        }

        @Override
        protected JCTree methodDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name, List<JCTypeParameter> typarams, boolean isInterface, boolean isAnno, boolean isVoid, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.methodDeclaratorRest(pos, mods, type, name, typarams, isInterface, isAnno, isVoid, dc);
        }

    }

    public static class NBEndPosParser extends EndPosParser {

        private final CancelService cancelService;

        public NBEndPosParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, CancelService cancelService) {
            super(fac, S, keepDocComments, keepLineMap);
            this.cancelService = cancelService;
        }

        public NBEndPosParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, Map<JCTree, Integer> endPositions, CancelService cancelService) {
            super(fac, S, keepDocComments, keepLineMap, endPositions);
            this.cancelService = cancelService;
        }

        @Override
        protected JCClassDecl classDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.classDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl interfaceDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.interfaceDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.enumDeclaration(mods, dc);
        }

        @Override
        protected JCTree methodDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name, List<JCTypeParameter> typarams, boolean isInterface, boolean isAnno, boolean isVoid, String dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.methodDeclaratorRest(pos, mods, type, name, typarams, isInterface, isAnno, isVoid, dc);
        }

    }


}
