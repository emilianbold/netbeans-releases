/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.pm;

import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.introduce.CopyFinder;
import org.netbeans.modules.java.hints.introduce.CopyFinder.VariableAssignments;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.spi.Hacks;

/**XXX: cancelability!
 *
 * @author Jan Lahoda
 */
public class Pattern {

    private final CompilationInfo info;
    private final String pattern;
    private final Tree patternTree;
    private final Iterable<Tree> antipatterns;

    private final Map<String, TypeMirror> constraintsHack;

    public Pattern(CompilationInfo info, String pattern, Tree patternTree, Iterable<Tree> antipatterns, Map<String, TypeMirror> constraintsHack) {
        this.info = info;
        this.pattern = pattern;
        this.patternTree = patternTree;
        this.antipatterns = antipatterns;
        this.constraintsHack = constraintsHack;
    }

    public static Pattern compile(CompilationInfo info, String pattern) {
        Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();
        pattern = parseOutTypesFromPattern(info, pattern, constraints);

        return compile(info, pattern, constraints, Collections.<String>emptyList());
    }

    public static Pattern compile(CompilationInfo info, String pattern, Map<String, TypeMirror> constraints, Iterable<? extends String> imports) {
        return compile(info, pattern, Collections.<String>emptyList(), constraints, imports);
    }

    public static Pattern compile(CompilationInfo info, String pattern, Iterable<String> antipatterns, Map<String, TypeMirror> constraints, Iterable<? extends String> imports) {
        Scope[] scope = new Scope[1];
        Tree patternTree = parseAndAttribute(info, pattern, constraints, scope, imports);

        List<Tree> antipatternsTrees = new LinkedList<Tree>();

        for (String ap : antipatterns) {
            Tree p = info.getTreeUtilities().parseExpression(ap, new SourcePositions[1]);

            info.getTreeUtilities().attributeTree(p, scope[0]);

            antipatternsTrees.add(p);
        }
        
        return new Pattern(info, pattern, patternTree, antipatternsTrees, constraints);
    }

    public Map<String, TreePath> match(TreePath toCheck) {
        VariableAssignments variables = CopyFinder.computeVariables(info, new TreePath(new TreePath(info.getCompilationUnit()), patternTree), toCheck, new AtomicBoolean(), constraintsHack);

        if (variables == null) {
            return null;
        }

        return variables.variables;
    }

    public boolean checkAntipatterns(TreePath tp) {
        for (Tree ap : antipatterns) {
            if (CopyFinder.computeVariables(info, new TreePath(new TreePath(info.getCompilationUnit()), ap), tp, new AtomicBoolean(), constraintsHack) != null) {
                return false;
            }
        }

        return true;
    }

    public Map<String, TypeMirror> getConstraints() {
        return constraintsHack;
    }

    public String getPatternCode() {
        return pattern;
    }

    public Tree getPattern() {
        return patternTree;
    }
    
    private static String parseOutTypesFromPattern(CompilationInfo info, String pattern, Map<String, TypeMirror> variablesToTypes) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\$.)(\\{([^}]*)\\})?");
        StringBuffer filtered = new StringBuffer();
        Matcher m = p.matcher(pattern);
        int i = 0;

        while (m.find()) {
            filtered.append(pattern.substring(i, m.start()));
            i = m.end();
            
            String var  = m.group(1);
            String type = m.group(3);

            filtered.append(var);
            variablesToTypes.put(var, type != null ? Hacks.parseFQNType(info, type) : null);
        }

        filtered.append(pattern.substring(i));

        return filtered.toString();
    }

    public static Tree parseAndAttribute(CompilationInfo info, String pattern, Map<String, TypeMirror> constraints, Scope[] scope, Iterable<? extends String> imports) {
        scope[0] = Utilities.constructScope(info, constraints, imports);

        if (scope[0] == null) {
            return null;
        }

        return Utilities.parseAndAttribute(info, pattern, scope[0]);
    }

}
