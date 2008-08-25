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

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner.Configuration;
import org.netbeans.modules.gsf.api.TranslatedSource;

/**
 * Analyzer for JSON
 * 
 * @author Tor Norbye
 */
public class JsonAnalyzer extends JsAnalyzer {
    @Override
    public List<? extends StructureItem> scan(CompilationInfo info) {
        JsParseResult result = AstUtilities.getParseResult(info);
        AnalysisResult ar = result.getStructure();

        List<?extends AstElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        if (JsUtils.isJsonFile(info.getFileObject()) && result.getRootNode() != null) {
            Node topObjLit = result.getRootNode().getFirstChild();
            if (topObjLit != null && topObjLit.getType() == Token.OBJECTLIT) {
                assert result.getTranslatedSource() == null; // No embedding for JSON
                addJsonItems(false, topObjLit, itemList, info);
                return itemList;
            }
        }

        return super.scan(info);
    }

    // Special handling for JSON files
    private void addJsonItems(boolean skipObjLit, Node node, List<StructureItem> items, CompilationInfo info) {
        if (skipObjLit && (node.getType() == Token.OBJECTLIT || node.getType() == Token.ARRAYLIT)) {
            return;
        }

        if (node.getType() == Token.OBJLITNAME) {
            String name = node.getString();
            Node labelled = AstUtilities.getLabelledNode(node);
            boolean isObjLitLabel = false;
            if (labelled != null && (labelled.getType() == Token.OBJECTLIT || labelled.getType() == Token.ARRAYLIT)) {
                isObjLitLabel = true;
            }
            JsFakeStructureItem item = new JsFakeStructureItem(name,
                    isObjLitLabel ? ElementKind.METHOD : ElementKind.PROPERTY,
                        null, info);
            items.add(item);
            item.begin = node.getSourceStart();
            item.end = node.getSourceEnd();

            if (isObjLitLabel) {
                items = item.children = new ArrayList<StructureItem>();
                addJsonItems(false, labelled, item.children, info);
            }
        }

        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addJsonItems(true, child, items, info);
            }
        }
    }

    // Special handling for JSON files
    private void addJsonFolds(boolean isRoot, Node node, List<OffsetRange> codeBlocks) {
        if (node.getType() == Token.OBJECTLIT) {
            if (isRoot) {
                // Don't add a fold for the outermost {} in JSON
                isRoot = false;
            } else {
                OffsetRange range = AstUtilities.getRange(node);
                // No AST offset translation necessary - JSON contents aren't embedded
                codeBlocks.add(range);
            }
        }

        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addJsonFolds(isRoot, child, codeBlocks);
            }
        }
    }

    @Override
    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        JsParseResult result = AstUtilities.getParseResult(info);
        TranslatedSource source = result.getTranslatedSource();
        assert source == null; // No embedding for JSON files

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codeblocks = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codeblocks); // NOI18N

        BaseDocument doc = (BaseDocument)info.getDocument();
        if (doc == null) {
            return Collections.emptyMap();
        }

        Node root = AstUtilities.getRoot(result);
        if (root != null) {
            addJsonFolds(true, root, codeblocks);
        }

        return folds;
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false);
    }
}
