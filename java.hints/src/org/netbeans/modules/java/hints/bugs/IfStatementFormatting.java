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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IfTree;
import com.sun.source.util.TreePath;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;

/**
 * When 'if' statement does not have braces, and does not have the else-statement,
 * issues a warning if the statement indentation (or indentation of following statements) does not suggest conditionality.
 * 
 * @author sdedic
 */
@Hint(
        displayName="Correct 'if' statement formatting",
        description = "Checks that if statement without curly braces is properly indented",
        category = "suggestions",
        enabled = true,
        severity = Severity.HINT
)
public class IfStatementFormatting {
    
    private static Preferences javaPreferences;
    
    private static Preferences getJavaPreferences() {
        return MimeLookup.getLookup("text/x-java").lookup(Preferences.class);
    }
    
    // trigger on an if with >= 1 following statement without brace
    @TriggerPattern("if ($expr) $stmt1; $stmt2;")
    public static ErrorDescription run(HintContext ctx) {
        final Map<String, TreePath> vars = ctx.getVariables();
        TreePath ifPath = ctx.getPath();

        IfTree tree = (IfTree)ifPath.getLeaf();
        
        CompilationUnitTree cut = ctx.getInfo().getCompilationUnit();
        long ifPos = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(cut, tree);
        
        CharSequence text = ctx.getInfo().getSnapshot().getText();
        
        return null;
    }
    
    
}
