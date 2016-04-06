/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package jdk.jshell;

import com.sun.source.tree.Tree;
import java.lang.reflect.Field;
import java.util.List;
import jdk.jshell.Snippet.Status;
import jdk.jshell.TaskFactory.ParseTask;
import static jdk.jshell.Util.REPL_DOESNOTMATTER_CLASS_NAME;
/**
 *
 * @author sdedic
 */
public class JShellAccessor {
    /**
     * Adds a classpath to the compilation. Does not require or start a target VM,
     * unlike {@link JShell#addToClasspath}. Note that the classpath added to the compiler
     * <b>must<b/> be announced to the VM in some other way; the intended use is 
     * an initial configuration for the project's VM.
     * 
     * @param instance JShell instance
     * @param classpath the classpath (file path or URL) to add
     */
    public static void addCompileClasspath(JShell instance, String classpath) {
        instance.taskFactory.addToClasspath(classpath);
    }
    
    public static void resetCompileClasspath(JShell instance, String classpath) {
        try {
            Field f = TaskFactory.class.getDeclaredField("classpath");
            f.setAccessible(true);
            f.set(instance.taskFactory, "");
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        addCompileClasspath(instance, classpath);
    }
    
    /**
     * Retrieves key of a snippet
     * @param snip
     * @return 
     */
    public static Object snippetKey(Snippet snip) {
        return snip.key();
    }
    
    public static String snippetClass(Snippet snip) {
        return snip.className();
    }
    
    public static int getWrappedPosition(Snippet snip, int snippetPos) {
        OuterWrap wrap = snip.outerWrap();
        return wrap == null ? -1 : snip.outerWrap().snippetIndexToWrapIndex(snippetPos);
    }
    
    /**
     * Attempts to detect the snippet kind for wrapping.
     * @param state
     * @param source
     * @return 
     */
    public Tree.Kind snippetKind(JShell state, String source) {
        String compileSource = Util.trimEnd(new MaskCommentsAndModifiers(source, false).cleared());
        ParseTask pt = state.taskFactory.new ParseTask(compileSource);
        if (pt.getDiagnostics().hasOtherThanNotStatementErrors()) {
            return Tree.Kind.ERRONEOUS;
        }

        List<? extends Tree> units = pt.units();
        if (units.isEmpty()) {
            return null;
        }
        Tree unitTree = units.get(0);
        return unitTree.getKind();
    }
    
    /**
     * Returns text corresponding to a snippet
     * @param state
     * @param s
     * @return 
     */
    public static SnippetWrapping snippetWrap(JShell state, Snippet s) {
        OuterWrap outer = s.outerWrap();
        if (outer != null) {
            return new StdWrapper(s);
        }
        String src = s.source();
        Wrap w = Wrap.methodWrap(src);
        String imports = state.maps.packageAndImportsExcept(null, null);
        OuterWrap ow = OuterWrap.wrapInClass(state.maps.packageName(), 
                REPL_DOESNOTMATTER_CLASS_NAME, imports, src, w);
        return new ErrWrapper(s, ow, s.kind());
    }

    public static SnippetWrapping wrapInput(JShell state, String input) {
        //XXX: modifiers/comments!
        String compileSource = new MaskCommentsAndModifiers(input, true).cleared();
        ParseTask pt = state.taskFactory.new ParseTask(compileSource/*, "-XDallowStringFolding=false"*/);
        List<? extends Tree> units = pt.units();
        Tree.Kind kind = Tree.Kind.EXPRESSION_STATEMENT;
        if (!units.isEmpty()) {
            kind = units.get(0).getKind();
        }
        String imports = state.maps.packageAndImportsExcept(null, null);
        Wrap w;
        Snippet.Kind snipKind = null;
                
        switch (kind) {
            case IMPORT:
                w = Wrap.importWrap(compileSource);
                snipKind = Snippet.Kind.IMPORT;
                break;
            case CLASS:
            case ENUM:
            case ANNOTATION_TYPE:
            case INTERFACE:
                snipKind = Snippet.Kind.TYPE_DECL;
                w = Wrap.classMemberWrap(compileSource);
                break;
            case METHOD:
                snipKind = Snippet.Kind.METHOD;
                w = Wrap.classMemberWrap(compileSource);
                break;
            case VARIABLE:
                snipKind = Snippet.Kind.VAR;
                w = Wrap.classMemberWrap(compileSource);
                break;
            case EXPRESSION_STATEMENT:
                snipKind = Snippet.Kind.EXPRESSION;
                w = Wrap.methodWrap(compileSource);
                break;
            default:
                snipKind = Snippet.Kind.STATEMENT;
                w = Wrap.methodWrap(compileSource);
                break;
        }
        OuterWrap outer;
        if (kind == Tree.Kind.IMPORT) {
            outer = OuterWrap.wrapImport(input, w);
        } else {
            outer = OuterWrap.wrapInClass(state.maps.packageName(), REPL_DOESNOTMATTER_CLASS_NAME, imports, input, w);
        }
        return new ErrWrapper(null, outer, snipKind);
    }
    
    private static class ErrWrapper implements SnippetWrapping {
        private final Snippet snippet;
        private final OuterWrap ow;
        private final Snippet.Kind kind;
        
        public ErrWrapper(Snippet snippet, OuterWrap ow, Snippet.Kind kind) {
            this.snippet = snippet;
            this.ow = ow;
            this.kind = kind;
        }

        @Override
        public Snippet.Kind getSnippetKind() {
            return kind;
        }

        @Override
        public Status getStatus() {
            return snippet != null ? snippet.status() : Status.NONEXISTENT;
        }
        
        public String getSource() {
            return ow.getUserSource();
        }

        @Override
        public Snippet getSnippet() {
            return snippet;
        }

        @Override
        public String getCode() {
            return ow.wrapped();
        }

        @Override
        public int getWrappedPosition(int pos) {
            return ow.snippetIndexToWrapIndex(pos);
        }
    }

    private static class StdWrapper implements SnippetWrapping {
        private final Snippet snippet;
        public StdWrapper(Snippet snippet) {
            this.snippet = snippet;
        }

        @Override
        public Snippet.Kind getSnippetKind() {
            return snippet.kind();
        }

        @Override
        public String getSource() {
            return snippet.source();
        }

        @Override
        public Status getStatus() {
            return snippet.status();
        }

        @Override
        public Snippet getSnippet() {
            return snippet;
        }

        @Override
        public String getCode() {
            return snippet.outerWrap().wrapped();
        }

        @Override
        public int getWrappedPosition(int pos) {
            return snippet.outerWrap().snippetIndexToWrapIndex(pos);
        }
    }
    
    public static List<Snippet> getDependents(JShell state, Snippet snip) {
        return state.maps.getDependents(snip);
    }
    
    public static NbExecutionControl getNbExecControl(JShell state) {
        return (NbExecutionControl)state.executionControl();
    }
}
