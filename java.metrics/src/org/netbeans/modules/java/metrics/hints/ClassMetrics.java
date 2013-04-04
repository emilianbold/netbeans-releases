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
package org.netbeans.modules.java.metrics.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.metrics.hints.Bundle.*;

/**
 * Provides basic class metrics
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - class name",
    "# {1} - complexity",
    "TEXT_ClassTooComplex=Class {0} is too complex. Cyclomatic complexity = {1}",
    "# {0} - complexity",
    "TEXT_ClassAnonymousTooComplex=Anonymous class is too complex. Cyclomatic complexity = {0}",
    "# {0} - class name",
    "# {1} - number of referencies",
    "TEXT_ClassTooCoupled=Class {0} is too coupled. References {1} other types",
})
public class ClassMetrics {
    static final int DEFAULT_ANONYMOUS_COMPLEXITY_LIMIT = 5;
    static final int DEFAULT_COMPLEXITY_LIMIT = 80;
    static final int DEFAULT_COUPLING_LIMIT = 25;
    static final boolean DEFAULT_COUPLING_IGNORE_JAVA = true;
    
    @IntegerOption(
        displayName = "#OPTNAME_ClassAnonymousComplexityLimit",
        tooltip = "#OPTDESC_ClassAnonymousComplexityLimit",
        maxValue = 1000,
        step = 1,
        defaultValue = DEFAULT_ANONYMOUS_COMPLEXITY_LIMIT
    )
    public static final String OPTION_ANONYMOUS_COMPLEXITY_LIMIT = "metrics.class.anonymous.complexity.limit"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_ClassComplexityLimit",
        tooltip = "#OPTDESC_ClassComplexityLimit",
        maxValue = 1000,
        step = 1,
        defaultValue = DEFAULT_COMPLEXITY_LIMIT
    )
    public static final String OPTION_COMPLEXITY_LIMIT = "metrics.class.complexity.limit"; // NOI18N
    
    @IntegerOption(
        displayName = "#OPTNAME_ClassCouplingLimit",
        tooltip = "#OPTDESC_ClassCouplingLimit",
        maxValue = 1000,
        step = 1,
        defaultValue = DEFAULT_COUPLING_LIMIT
    )
    public static final String OPTION_COUPLING_LIMIT = "metrics.class.coupling.limit"; // NOI18N
    
    @BooleanOption(
        displayName = "#OPTNAME_ClassCouplingIgnoreJava",
        tooltip = "#OPTDESC_ClassCouplingIgnoreJava",
        defaultValue = DEFAULT_COUPLING_IGNORE_JAVA
    )
    public static final String OPTION_COUPLING_IGNORE_JAVA = "metrics.class.coupling.nojava"; // NOI18N
    
    
    @Hint(
        displayName = "#DN_ClassAnonymousTooComplex",
        description = "#DESC_ClassAnonymousTooComplex",
        category = "metrics",
        options = { Hint.Options.HEAVY, Hint.Options.QUERY }
    )
    @UseOptions(OPTION_ANONYMOUS_COMPLEXITY_LIMIT)
    @TriggerPatterns({
        @TriggerPattern("new $classname<$tparams$>($params$) { $members$; }"),
        @TriggerPattern("$expr.new $classname<$tparams$>($params$) { $members$; }"),
        @TriggerPattern("new $classname($params$) { $members$; }"),
        @TriggerPattern("$expr.new $classname($params$) { $members$; }"),
    })
    public static ErrorDescription tooComplexAnonymousClass(HintContext ctx) {
        CyclomaticComplexityVisitor v = new CyclomaticComplexityVisitor();
        v.scan(ctx.getPath(), null);
        
        int complexity = v.getComplexity();
        int limit = ctx.getPreferences().getInt(OPTION_ANONYMOUS_COMPLEXITY_LIMIT, 
                DEFAULT_ANONYMOUS_COMPLEXITY_LIMIT);
        if (complexity > limit) {
            return ErrorDescriptionFactory.forName(ctx, 
                    ctx.getPath(), 
                    TEXT_ClassAnonymousTooComplex(complexity));
        } else {
            return null;
        }
    }
    
    
    @Hint(
        displayName = "#DN_ClassTooComplex",
        description = "#DESC_ClassTooComplex",
        category = "metrics",
        options = { Hint.Options.HEAVY, Hint.Options.QUERY }
    )
    @UseOptions(OPTION_COMPLEXITY_LIMIT)
    @TriggerTreeKind(Tree.Kind.CLASS)
    public static ErrorDescription tooComplexClass(HintContext ctx) {
        ClassTree clazz = (ClassTree)ctx.getPath().getLeaf();
        TypeElement e = (TypeElement)ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (e.getNestingKind() == NestingKind.ANONYMOUS) {
            return null;
        }
        CyclomaticComplexityVisitor v = new CyclomaticComplexityVisitor();
        v.scan(ctx.getPath(), null);
        
        int complexity = v.getComplexity();
        int limit = ctx.getPreferences().getInt(OPTION_COMPLEXITY_LIMIT, DEFAULT_COMPLEXITY_LIMIT);
        if (complexity > limit) {
            return ErrorDescriptionFactory.forName(ctx, 
                    ctx.getPath(), 
                    TEXT_ClassTooComplex(clazz.getSimpleName().toString(), complexity));
        } else {
            return null;
        }
    }
    
    @Hint(
        displayName = "#DN_ClassTooCoupled",
        description = "#DESC_ClassTooCoupled",
        category = "metrics",
        options = { Hint.Options.HEAVY, Hint.Options.QUERY }
    )
    @UseOptions({ OPTION_COUPLING_LIMIT, OPTION_COUPLING_IGNORE_JAVA })
    @TriggerTreeKind(Tree.Kind.CLASS)
    public static ErrorDescription tooCoupledClass(HintContext ctx) {
        ClassTree clazz = (ClassTree)ctx.getPath().getLeaf();
        DependencyCollector col = new DependencyCollector(ctx.getInfo());
        boolean ignoreJava = ctx.getPreferences().getBoolean(OPTION_COUPLING_IGNORE_JAVA, DEFAULT_COUPLING_IGNORE_JAVA);
        col.setIgnoreJavaLibraries(ignoreJava);
        col.scan(ctx.getPath(), null);
        
        int coupling = col.getSeenQNames().size();
        int limit = ctx.getPreferences().getInt(OPTION_COUPLING_LIMIT, DEFAULT_COUPLING_LIMIT);
        if (coupling > limit) {
            return ErrorDescriptionFactory.forName(ctx, 
                    ctx.getPath(), 
                    TEXT_ClassTooCoupled(clazz.getSimpleName().toString(), coupling));
        } else {
            return null;
        }
    }
}
