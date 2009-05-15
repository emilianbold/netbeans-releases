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

package org.netbeans.modules.java.source;

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

/**
 *
 * @author Dusan Balek
 */
public class PostFlowAnalysis extends TreeScanner {
    
    private Log log;
    private Types types;
    private Enter enter;
    private Names names;

    private List<Pair<TypeSymbol, Symbol>> outerThisStack;
    private TypeSymbol currentClass;

    private PostFlowAnalysis(Context ctx) {
        log = Log.instance(ctx);
        types = Types.instance(ctx);
        enter = Enter.instance(ctx);
        names = Names.instance(ctx);
        outerThisStack = List.nil();
    }
    
    public static void analyze(Iterable<? extends Element> elems, Context ctx) {
        assert elems != null;
        PostFlowAnalysis postFlowAnalysis = new PostFlowAnalysis(ctx);
        for (Element e : elems) {
            if (e instanceof TypeSymbol) {
                Env<AttrContext> env = postFlowAnalysis.enter.getClassEnv((TypeSymbol)e);
                if (env != null) {
                    JavaFileObject prev = postFlowAnalysis.log.useSource(env.enclClass.sym.sourcefile != null
                            ? env.enclClass.sym.sourcefile : env.toplevel.sourcefile);
                    try {
                        postFlowAnalysis.scan(env.toplevel);
                    } finally {
                        postFlowAnalysis.log.useSource(prev);
                    }
                }
            }
        }
    }
    
    private void analyze(Element e) {
        if (e instanceof TypeSymbol) {
            Env<AttrContext> env = enter.getClassEnv((TypeSymbol)e);
            if (env != null)
                this.scan(env.toplevel);
        }
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        TypeSymbol currentClassPrev = currentClass;
        currentClass = tree.sym;
        List<Pair<TypeSymbol, Symbol>> prevOuterThisStack = outerThisStack;
        try {
            if (currentClass.hasOuterInstance())
                outerThisDef(currentClass);
            super.visitClassDef(tree);
        } finally {
            outerThisStack = prevOuterThisStack;
            currentClass = currentClassPrev;
        }
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
        if (tree.name == names.init &&
            (currentClass.isInner() ||
             (currentClass.owner.kind & (Kinds.VAR | Kinds.MTH)) != 0)) {
            List<Pair<TypeSymbol, Symbol>> prevOuterThisStack = outerThisStack;
            try {
                if (currentClass.hasOuterInstance())
                    outerThisDef(tree.sym);
                super.visitMethodDef(tree);
            } finally {
                outerThisStack = prevOuterThisStack;
            }
        } else {
            super.visitMethodDef(tree);
        }
        if (tree.sym == null || tree.type == null)
            return;
        Type type = types.erasure(tree.type);
        for (Scope.Entry e = tree.sym.owner.members().lookup(tree.name);
             e.sym != null;
             e = e.next()) {
            if (e.sym != tree.sym &&
                types.isSameType(types.erasure(e.sym.type), type)) {
                log.error(tree.pos(), "name.clash.same.erasure", tree.sym, e.sym); //NOI18N
                return;
            }
        }
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
        super.visitNewClass(tree);
        Symbol c = tree.constructor.owner;
        if (c.hasOuterInstance()) {
            if (tree.encl == null && (c.owner.kind & (Kinds.MTH | Kinds.VAR)) != 0) {
                checkThis(tree.pos(), c.type.getEnclosingType().tsym);
            }
        }
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        super.visitApply(tree);
        Symbol meth = TreeInfo.symbol(tree.meth);
        Name methName = TreeInfo.name(tree.meth);
        if (meth.name==names.init) {
            Symbol c = meth.owner;
            if (c.hasOuterInstance()) {
                if (tree.meth.getTag() != JCTree.SELECT && ((c.owner.kind & (Kinds.MTH | Kinds.VAR)) != 0 || methName == names._this)) {
                    checkThis(tree.meth.pos(), c.type.getEnclosingType().tsym);
                }
            }
        }
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {
        super.visitSelect(tree);
        if (tree.name == names._this || tree.name == names._super)
            checkThis(tree.pos(), tree.selected.type.tsym);
    }

    private void checkThis(DiagnosticPosition pos, TypeSymbol c) {
        if (currentClass != c) {
            List<Pair<TypeSymbol, Symbol>> ots = outerThisStack;
            if (ots.isEmpty()) {
                log.error(pos, "no.encl.instance.of.type.in.scope", c); //NOI18N
                return;
            }
            Pair<TypeSymbol, Symbol> ot = ots.head;
            TypeSymbol otc = ot.fst;
            while (otc != c) {
                do {
                    ots = ots.tail;
                    if (ots.isEmpty()) {
                        log.error(pos, "no.encl.instance.of.type.in.scope", c); //NOI18N
                        return;
                    }
                    ot = ots.head;
                } while (ot.snd != otc);
                if (otc.owner.kind != Kinds.PCK && !otc.hasOuterInstance()) {
                    log.error(pos, "cant.ref.before.ctor.called", c); //NOI18N
                    return;
                }
                otc = ot.fst;
            }
        }
    }

    private void outerThisDef(Symbol owner) {
        Type target = types.erasure(owner.enclClass().type.getEnclosingType());
        Pair<TypeSymbol, Symbol> outerThis = Pair.of(target.tsym, owner);
        outerThisStack = outerThisStack.prepend(outerThis);
    }
}
