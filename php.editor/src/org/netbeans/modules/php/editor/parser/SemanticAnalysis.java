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

package org.netbeans.modules.php.editor.parser;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalysis implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    
    public SemanticAnalysis () {
        semanticHighlights = null;
    }
    
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo compilationInfo) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }
        
        PHPParseResult result = getParseResult(compilationInfo);
        Map<OffsetRange, Set<ColoringAttributes>> highlights =
            new HashMap<OffsetRange, Set<ColoringAttributes>>(100);
        
        if (result.getProgram() != null) {
            result.getProgram().accept(new SemanticHighlightVisitor(highlights));
                        
            if (highlights.size() > 0) {
                semanticHighlights = highlights;
            }
            else {
                semanticHighlights = null;
            }
        }
    }
    
    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    private PHPParseResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((PHPParseResult)result);
        }
    }
    
    private class SemanticHighlightVisitor extends DefaultVisitor {
        Map<OffsetRange, Set<ColoringAttributes>> highlights;
        
        public SemanticHighlightVisitor(Map<OffsetRange, Set<ColoringAttributes>> highlights) {
            this.highlights = highlights;
        }
        
        private OffsetRange createOffsetRange (ASTNode node) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }
        
        @Override
        public void visit(ClassDeclaration cldec) {
            if (isCancelled())
                return;
            Identifier name = cldec.getName();
            OffsetRange or = new OffsetRange(name.getStartOffset(), name.getEndOffset());
            highlights.put(or, ColoringAttributes.CLASS_SET);
            cldec.getBody().accept(this);
        }
        
        @Override
        public void visit(MethodDeclaration md) {
            Identifier name = md.getFunction().getFunctionName();
            highlights.put(createOffsetRange(name), ColoringAttributes.METHOD_SET);
        }

    }
}
