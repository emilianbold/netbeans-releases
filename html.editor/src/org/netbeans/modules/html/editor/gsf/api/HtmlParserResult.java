/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.html.editor.gsf.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.netbeans.editor.ext.html.parser.SyntaxParserResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.gsf.HtmlGSFParser;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * HTML parser result
 *
 * @author mfukala@netbeans.org
 */
public class HtmlParserResult extends ParserResult {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    private SyntaxParserResult result;
    private List<Error> errors;

    private HtmlParserResult(Snapshot snapshot, SyntaxParserResult result) {
        super(snapshot);
        this.result = result;
    }

    /** @return a root node of the hierarchical parse tree of the document.
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since
     * the postprocessing takes some time and is done lazily.
     */
    public AstNode root() {
        return result.getASTRoot();
    }

    /** @return a list of SyntaxElement-s representing parse elements of the html source. */
    public List<SyntaxElement> elementsList() {
        return result.getElements();
    }

    /** @return an instance of DTD bound to the html document. */
    public DTD dtd() {
        return result.getDTD();
    }

    /** @return a set of html document element's ids. */
    public Set<TagAttribute> elementsIds() {
        HashSet ids = new HashSet(elementsList().size() / 10);
        for (SyntaxElement element : elementsList()) {
            if (element.type() == SyntaxElement.TYPE_TAG) {
                TagAttribute attr = ((SyntaxElement.Tag) element).getAttribute(ID_ATTR_NAME);
                if (attr != null) {
                    ids.add(attr);
                }
            }
        }
        return ids;
    }

    @Override
    public synchronized List<? extends Error> getDiagnostics() {
        if (errors == null) {
            errors = new ArrayList<Error>(findErrors());
        }
        return errors;
    }

    @Override
    protected void invalidate() {
        //todo
    }

    private List<Error> findErrors() {
        final List<Error> _errors = new ArrayList<Error>();
        AstNodeUtils.visitChildren(root(),
                new AstNodeVisitor() {

                    public void visit(AstNode node) {
                        if (node.type() == AstNode.NodeType.OPEN_TAG ||
                                node.type() == AstNode.NodeType.ENDTAG ||
                                node.type() == AstNode.NodeType.UNKNOWN_TAG) {

                            for (Description desc : node.getDescriptions()) {
                                if (desc.getType() < Description.WARNING) {
                                    continue;
                                }
                                //some error in the node, report
                                Error error =
                                        DefaultError.createDefaultError("tag_error", //NOI18N
                                        desc.getText(),
                                        desc.getText(),
                                        getSnapshot().getSource().getFileObject(),
                                        getSnapshot().getOriginalOffset(desc.getFrom()),
                                        getSnapshot().getOriginalOffset(desc.getTo()),
                                        false /* not line error */,
                                        desc.getType() == Description.WARNING ? Severity.WARNING : Severity.ERROR); //NOI18N
                                _errors.add(error);

                            }
                        }
                    }
                });

        return _errors;

    }

    static {
        HtmlParserResultAccessor.set(new Accessor());
    }

    private static class Accessor extends HtmlParserResultAccessor {

        public HtmlParserResult createInstance(Snapshot snapshot, SyntaxParserResult result) {
            return new HtmlParserResult(snapshot, result);
        }
    }
}
