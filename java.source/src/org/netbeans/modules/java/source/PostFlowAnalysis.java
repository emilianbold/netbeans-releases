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

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
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

    private PostFlowAnalysis(Context ctx) {
        log = Log.instance(ctx);
        types = Types.instance(ctx);
        enter = Enter.instance(ctx);
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
    public void visitMethodDef(JCMethodDecl tree) {
        super.visitMethodDef(tree);
        if (tree.sym == null || tree.type == null)
            return;
        Type type = types.erasure(tree.type);
        for (Scope.Entry e = tree.sym.owner.members().lookup(tree.name);
             e.sym != null;
             e = e.next()) {
            if (e.sym != tree.sym &&
                types.isSameType(types.erasure(e.sym.type), type)) {
                log.error(tree.pos(),
                          "name.clash.same.erasure", tree.sym, //NOI18N
                          e.sym);
                return;
            }
        }
    }
}
