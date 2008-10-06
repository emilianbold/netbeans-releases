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

package org.netbeans.modules.languages.yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jvyamlb.Position.Range;
import org.jvyamlb.nodes.Node;
import org.jvyamlb.nodes.PositionedScalarNode;
import org.jvyamlb.nodes.PositionedSequenceNode;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;

/**
 * Semantic Analyzer for YAML
 * 
 * @author Tor Norbye
 */
public class YamlSemanticAnalyzer implements SemanticAnalyzer {
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }

        YamlParserResult ypr = (YamlParserResult) info.getEmbeddedResult(YamlTokenId.YAML_MIME_TYPE, 0);
        if (ypr == null || ypr.getObject() == null) {
            this.semanticHighlights = Collections.emptyMap();
            return;
        }

        Node root = ypr.getObject();

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        IdentityHashMap<Object,Boolean> seen = new IdentityHashMap<Object,Boolean>(100);
        addHighlights(ypr, root, highlights, seen, 0);

        this.semanticHighlights = highlights;
    }

    private void addHighlights(YamlParserResult ypr, Node node, Map<OffsetRange, Set<ColoringAttributes>> highlights, IdentityHashMap<Object,Boolean> seen, int depth) {
        if (depth > 10 || node == null) {
            // Avoid boundless recursion; some datastructures from YAML appear to be recursive
            return;
        }
        Object value = node.getValue();
        if (seen.containsKey(value)) {
            return;
        }
        seen.put(value, Boolean.TRUE);

        if (value instanceof Map) {
            Map map = (Map)value;
            Set<Map.Entry> entrySet = map.entrySet();

            for (Map.Entry entry : entrySet) {
                Object key = entry.getKey();
                if (key instanceof PositionedSequenceNode) {
                    PositionedSequenceNode psn = (PositionedSequenceNode)key;
                    Object keyValue = psn.getValue();
                    assert keyValue instanceof List;
                    List<Node> list = (List<Node>)keyValue;
                    for (Node child : list) {
                        if (child == node) {
                            // Circularity??
                            return;
                        }
                        addHighlights(ypr, child, highlights, seen, depth+1);
                    }
                    Object entryValue = entry.getValue();
                    if (entryValue instanceof PositionedSequenceNode) {
                        psn = (PositionedSequenceNode)entryValue;
                        keyValue = psn.getValue();
                        assert keyValue instanceof List;
                        list = (List<Node>)keyValue;
                        for (Node o : list) {
                            if (o == node) {
                                // Circularity??
                                return;
                            }
                            addHighlights(ypr, o, highlights, seen, depth+1);
                        }
                    }
                } else {
                    assert key instanceof PositionedScalarNode;
                    PositionedScalarNode scalar = (PositionedScalarNode)key;
                    Range r = scalar.getRange();
                    OffsetRange range = ypr.getAstRange(r);
                    highlights.put(range, ColoringAttributes.METHOD_SET);
                    Node child = (Node) entry.getValue();
                    addHighlights(ypr, child, highlights, seen, depth+1);
                }
            }
        } else if (value instanceof List) {
            List<Node> list = (List<Node>)value;
            for (Node child : list) {
                if (child == node) {
                    // Circularity??
                    return;
                }
                addHighlights(ypr, child, highlights, seen, depth+1);
            }
        }
    }
}
