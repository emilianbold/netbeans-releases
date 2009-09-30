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
package org.netbeans.modules.ruby;

import java.util.HashMap;
import java.util.Map;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Parameters;

public final class ContextKnowledge {

    /** Map from variable or field(etc) name to type. */
    private Map<String, RubyType> typesForSymbols;
    private Map<Node, RubyType> typesForNodes;
    
    private final RubyIndex index;
    private final Node root;
    private final Node target;
    private final int astOffset;
    private final int lexOffset;
    private final ParserResult parserResult;

    private boolean analyzed;

    ContextKnowledge(RubyIndex index, Node root, ParserResult parserResult) {
        this(index, root, null, -1, -1, parserResult);
    }

    public ContextKnowledge(RubyIndex index, Node root, Node target, int astOffset,
            int lexOffset, ParserResult parserResult) {
        Parameters.notNull("root", root);
        Parameters.notNull("parserResult", parserResult);
        this.index = index;
        this.root = root;
        this.target = target;
        this.astOffset = astOffset;
        this.lexOffset = lexOffset;
        this.typesForSymbols = new HashMap<String, RubyType>();
        this.typesForNodes = new HashMap<Node, RubyType>();
        this.parserResult = parserResult;
    }

    RubyType getType(final String symbol) {
        RubyType type = typesForSymbols.get(symbol);
        return type == null ? RubyType.createUnknown() : type;
    }

    RubyType getType(final Node node) {
        return typesForNodes.get(node);
    }

    void setType(Node node, RubyType type) {
        typesForNodes.put(node, type);
    }

    void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    boolean wasAnalyzed() {
        return analyzed;
    }

    Map<String, RubyType> getTypesForSymbols() {
        return typesForSymbols;
    }

    void maybePutTypeForSymbol(String var, String type, boolean override) {
        maybePutTypeForSymbol(var, RubyType.create(type), override);
    }

    void maybePutTypeForSymbol(
            final String symbol,
            final RubyType newType,
            final boolean override) {
        RubyType mapType = typesForSymbols.get(symbol);
        if (mapType == null || override) {
            mapType = new RubyType();
            typesForSymbols.put(symbol, mapType);
        }
        mapType.append(newType);
    }

    static RubyType getTypesForSymbol(
            final Map<String, RubyType> typeForSymbol, final String name) {
        RubyType type = typeForSymbol.get(name);
        return type == null ? RubyType.createUnknown() : type;
    }

    int getAstOffset() {
        return astOffset;
    }

    RubyIndex getIndex() {
        return index;
    }

    int getLexOffset() {
        return lexOffset;
    }

    Node getRoot() {
        return root;
    }

    ParserResult getParserResult() {
        return parserResult;
    }

    /**
     * The target node to which we are performing analysis. Ignore everything in
     * the AST from this node further.
     */
    Node getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "ContextKnowledge[realTypes:" + typesForSymbols + ']'; // NOI18N
    }

}
