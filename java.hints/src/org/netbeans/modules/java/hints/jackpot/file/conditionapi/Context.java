/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.file.conditionapi;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.hints.jackpot.spi.Hacks;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;

/**
 *
 * @author lahvac
 */
public class Context {

    private final HintContext ctx;
    private final AtomicInteger auxiliaryVariableCounter = new AtomicInteger();

    //XXX: should not be public:
    public Context(HintContext ctx) {
        this.ctx = ctx;
    }

    public @NonNull SourceVersion sourceVersion() {
        String sourceLevel = SourceLevelQuery.getSourceLevel(ctx.getInfo().getFileObject());

        if (sourceLevel == null) {
            return SourceVersion.latest(); //TODO
        }

        String[] splited = sourceLevel.split("\\.");
        String   spec    = splited[1];

        return SourceVersion.valueOf("RELEASE_"+  spec);//!!!
    }

    public @NonNull Set<Modifier> modifiers(@NonNull Variable variable) {
        final Element e = ctx.getInfo().getTrees().getElement(ctx.getVariables().get(variable.variableName));

        if (e == null) {
            return Collections.unmodifiableSet(EnumSet.noneOf(Modifier.class));
        }

        return Collections.unmodifiableSet(e.getModifiers());
    }

    public @CheckForNull ElementKind elementKind(@NonNull Variable variable) {
        final Element e = ctx.getInfo().getTrees().getElement(getSingleVariable(ctx, variable));

        if (e == null) {
            return null;
        }

        return e.getKind();
    }

    public @CheckForNull TypeKind typeKind(@NonNull Variable variable) {
        final TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(getSingleVariable(ctx, variable));

        if (tm == null) {
            return null;
        }

        return tm.getKind();
    }

    public @CheckForNull String name(@NonNull Variable variable) {
        final Element e = ctx.getInfo().getTrees().getElement(getSingleVariable(ctx, variable));

        if (e == null) {
            return null;
        }

        return e.getSimpleName().toString();
    }

    public @CheckForNull Variable parent(@NonNull Variable variable) {
        TreePath tp = ctx.getVariables().get(variable.variableName);

        if (tp.getParentPath() == null) {
            return null;
        }
        
        String output = "*" + auxiliaryVariableCounter.getAndIncrement();

        ctx.getVariables().put(output, tp.getParentPath());

        return new Variable(output);
    }

    public @NonNull Variable variableForName(@NonNull String variableName) {
        if (!ctx.getVariables().containsKey(variableName)) {
            throw new IllegalStateException("Unknown variable");
        }
        
        return new Variable(variableName);
    }

    public void createRenamed(@NonNull Variable from, @NonNull Variable to, @NonNull String newName) {
        //TODO: check (the variable should not exist)
        ctx.getVariableNames().put(to.variableName, newName);
        TreePath origVariablePath = ctx.getVariables().get(from.variableName);
        TreePath newVariablePath = new TreePath(origVariablePath.getParentPath(), Hacks.createRenameTree(origVariablePath.getLeaf(), newName));
        ctx.getVariables().put(to.variableName, newVariablePath);
    }

    public boolean isNullLiteral(@NonNull Variable var) {
        TreePath varPath = ctx.getVariables().get(var.variableName);

        return varPath.getLeaf().getKind() == Kind.NULL_LITERAL;
    }

    public @NonNull Iterable<? extends Variable> getIndexedVariables(@NonNull Variable multiVariable) {
        Collection<? extends TreePath> paths = ctx.getMultiVariables().get(multiVariable.variableName);

        if (paths == null) {
            throw new IllegalArgumentException("TODO: explanation");
        }

        Collection<Variable> result = new LinkedList<Variable>();
        int index = 0;

        for (TreePath tp : paths) {
            result.add(new Variable(multiVariable.variableName, index++));
        }

        return result;
    }

    static Iterable<? extends TreePath> getVariable(HintContext ctx, Variable v) {
        if (isMultistatementWildcard(v.variableName)) {
            return ctx.getMultiVariables().get(v.variableName);
        } else {
            return Collections.singletonList(ctx.getVariables().get(v.variableName));
        }
    }

    //XXX: copied from jackpot30.impl.Utilities:
    private static boolean isMultistatementWildcard(/*@NonNull */CharSequence name) {
        return name.charAt(name.length() - 1) == '$';
    }

    //TODO: check if correct variable is provided:
    private static TreePath getSingleVariable(HintContext ctx, Variable v) {
        if (v.index == (-1)) {
            return ctx.getVariables().get(v.variableName);
        } else {
            return new ArrayList<TreePath>(ctx.getMultiVariables().get(v.variableName)).get(v.index);
        }
    }
}
