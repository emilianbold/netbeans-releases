/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation covering the basic CSS3 features.
 *
 * @author marekfukala
 */
@ServiceProvider(service = CssModule.class)
public class DefaultCssModule extends CssModule {

    @Override
    public List<CompletionProposal> complete(CompletionContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case elementName:
                    case elementSubsequent:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.METHOD_SET);
                        break;
                    case property:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CUSTOM1_SET);
                        break;
                    case namespaceName:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CUSTOM3_SET);
                        break;

                    //add vendor specific coloring
                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {

        final Snapshot snapshot = context.getSnapshot();

        int astCaretOffset = snapshot.getEmbeddedOffset(context.getCaretOffset());
        if (astCaretOffset == -1) {
            return null;
        }


        Node current = NodeUtil.findDescendant(context.getParseTreeRoot(), astCaretOffset);
        if (current == null) {
            return null;
        }
        
        //we must always get a token node
        assert current.type() == NodeType.leaf;
        
        //get the rule node (its parent)
        current = current.parent();
        
        final NodeType nodeType = current.type();

        //process only some interesting nodes
        switch (nodeType) {
            case elementName:
            case cssClass:
            case cssId:
                break;
            default:
                return null;
        }

        final CharSequence currentNodeImage = current.image();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                if (nodeType == node.type()) {
                    boolean ignoreCase = nodeType == NodeType.hexColor;
                    if (LexerUtils.equals(currentNodeImage, node.image(), ignoreCase, false)) {
                        
                        int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                        int docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0]);

                        //virtual class or id handling - the class and id elements inside
                        //html tag's CLASS or ID attribute has the dot or hash prefix just virtual
                        //so if we want to highlight such occurances we need to increment the
                        //start offset by one
                        if (docFrom == -1 && (node.type() == NodeType.cssClass || node.type() == NodeType.cssId)) {
                            docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0] + 1); //lets try +1 offset
                        }

                        int docTo = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                        if (docFrom != -1 && docTo != -1) {
                            getResult().add(new OffsetRange(docFrom, docTo));
                        }                        
                    }
                }

                return false;


            }
        };



    }
}
