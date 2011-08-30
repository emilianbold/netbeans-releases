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
package org.netbeans.modules.css.editor.module.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssElement;
import org.netbeans.modules.css.editor.csl.CssPropertyElement;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author mfukala@netbeans.org
 */
public class Utilities {

    private Utilities() {
    }
    
    /**
     * Creates a generic mark occurrences node visitor for given node types. 
     * Only elements of the same type (from the given types list) and the same image are marked.
     */
    public static <T extends Set<OffsetRange>> NodeVisitor<T> createMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result, NodeType... nodeTypesToMatch) {

        final Snapshot snapshot = context.getSnapshot();

        int astCaretOffset = snapshot.getEmbeddedOffset(context.getCaretOffset());
        if (astCaretOffset == -1) {
            return null;
        }


        final Node current = NodeUtil.findNonTokenNodeAtOffset(context.getParseTreeRoot(), astCaretOffset);
        if (current == null) {
            //this may happen if the offset falls to the area outside the selectors rule node.
            //(for example when the stylesheet starts or ends with whitespaces or comment and
            //and the offset falls there).
            //In such case root node (with null parent) is returned from NodeUtil.findNodeAtOffset() 
            return null;
        }

        Set types = EnumSet.copyOf(Arrays.asList(nodeTypesToMatch));
        if(!types.contains(current.type())) {
            return null;
        }
        
        final CharSequence selectedNamespacePrefixImage = current.image();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == current.type()
                        && CharSequenceUtilities.textEquals(selectedNamespacePrefixImage, node.image())) {
                    OffsetRange documentNodeRange = Css3Utils.getDocumentOffsetRange(node, snapshot);
                    getResult().add(Css3Utils.getValidOrNONEOffsetRange(documentNodeRange));
                }
                return false;
            }
        };
    }

      public static  List<CompletionProposal> createRAWCompletionProposals(Collection<String> props, ElementKind kind, int anchor) {
          return createRAWCompletionProposals(props, kind, anchor, null);
      }
      
      public static  List<CompletionProposal> createRAWCompletionProposals(Collection<String> props, ElementKind kind, int anchor, String addPrefix) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (String value : props) {
            if(addPrefix != null) {
                value = addPrefix + value;
            }
            CssElement handle = new CssElement(value);
            CompletionProposal proposal = CssCompletionItem.createRAWCompletionItem(handle, value, kind, anchor, false);
            proposals.add(proposal);
        }
        return proposals;
    }
 
    public static List<CompletionProposal> wrapProperties(Collection<PropertyDescriptor> props, int anchor) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (PropertyDescriptor p : props) {
            //filter out non-public properties
            if (!p.getName().startsWith("-")) { //NOI18N
                CssElement handle = new CssPropertyElement(p);
                CompletionProposal proposal = CssCompletionItem.createPropertyNameCompletionItem(handle, p.getName(), anchor, false);
                proposals.add(proposal);
            }
        }
        return proposals;
    }
      
}
