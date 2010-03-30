/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.html.editor.api.gsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParserResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.HtmlVersion;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 * HTML parser result
 *
 * @author mfukala@netbeans.org
 */
public class HtmlParserResult extends ParserResult {

    /** 
     * Used as a key of a swing document to find a default fallback dtd.
     */
    public static final String FALLBACK_DTD_PROPERTY_NAME = "fallbackDTD";

    private SyntaxParserResult result;
    private List<Error> errors;
    private boolean isValid = true;

    private HtmlParserResult(Snapshot snapshot, SyntaxParserResult result) {
        super(snapshot);
        this.result = result;
    }

    /** The parser result may be invalidated by the parsing infrastructure.
     * In such case the method returns false.
     * @return true for valid result, false otherwise.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Returns an html version for the specified parser result input.
     * The return value depends on:
     * 1) doctype declaration content
     * 2) if not present, xhtml file extension
     * 3) if not xhtml extension, present of default XHTML namespace declaration
     * 
     */
    public HtmlVersion getHtmlVersion() {
        String publicId = result.getPublicID();
        if(publicId == null) {
            FileObject fo = getSnapshot().getSource().getFileObject();
            if(fo != null) {
                if("text/xhtml".equals(fo.getMIMEType())) { //NOI18N
                    //TODO resolve the xhtml version once we support more than 1.0
                    return HtmlVersion.XHTML10;
                } else {
                    //is there an xhtml namespace declared in the file?
                    if(result.getDeclaredNamespaces().containsKey(HtmlVersion.XHTML10.getDefaultNamespace())) {
                        return HtmlVersion.XHTML10;
                    }
                }
            }
            return HtmlVersion.UNKNOWN;
        } else {
            return HtmlVersion.findHtmlVersion(publicId);
        }

    }

    /** @return an instance of DTD for the parser input. */
    public DTD dtd() {
        return result.getDTD();
    }

    /** @return a root node of the hierarchical parse tree of the document.
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since
     * the postprocessing takes some time and is done lazily.
     */
    public AstNode root() {
        return root(getHtmlVersion().getDefaultNamespace());
    }

    /** returns a parse tree for non-html content */
    public AstNode root(String namespace) {
        DTD dtd = null;
        if(namespace == null || namespace != null && namespace.equals(getHtmlVersion().getDefaultNamespace())) {
            //html content, use fallback dtd
            dtd = getFallbackDTD(getHtmlVersion());
        }

        return result.getASTRoot(namespace, dtd);
    }

    private DTD getFallbackDTD(HtmlVersion version) {
        String fallbackPublicID = version == HtmlVersion.UNKNOWN ?
            HtmlVersion.HTML41.getFallbackPublicId() :
            version.getFallbackPublicId();
        
        return org.netbeans.editor.ext.html.dtd.Registry.getDTD(fallbackPublicID, null);
    }

    /** returns a map of all namespaces to astnode roots.*/
    public Map<String, AstNode> roots() {
        Map<String, AstNode> roots = new HashMap<String, AstNode>();
        for(String uri : getNamespaces().keySet()) {
            roots.put(uri, root(uri));
        }
        
        //non xhtml workaround, add the default namespaces if missing
        if(!roots.containsValue(root())) {
            roots.put(null, root());
        }

        return roots;

    }

    /**declared uri to prefix map */
    public Map<String, String> getNamespaces() {
        return result.getDeclaredNamespaces();
    }

    /** Returns a leaf most AstNode from the parse tree to which range the given
     * offset belongs. Uses logical node ranges.
     *
     * @param offset of the searched node
     */
    public AstNode findLeaf(int offset) {
        return findLeaf(offset, false);
    }

    public AstNode findLeaf(int offset, boolean exclusiveStartOffset) {
        //first try to find the leaf in html content
        AstNode mostLeaf = AstNodeUtils.findDescendant(root(), offset, exclusiveStartOffset);
        //now search the non html trees
        for(String uri : getNamespaces().keySet()) {
            AstNode root = root(uri);
            AstNode leaf = AstNodeUtils.findDescendant(root, offset, exclusiveStartOffset);
            if(mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if(leaf.logicalStartOffset() > mostLeaf.logicalStartOffset() ) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }

    /** Returns a leaf most AstNode from the parse tree to which range the given
     * offset belongs.
     *
     * @param offset of the searched node
     */
    public AstNode findLeafTag(int offset, boolean useLogicalRanges, boolean forward) {
        //first try to find the leaf in html content
        AstNode mostLeaf = AstNodeUtils.findDescendantTag(root(), offset, useLogicalRanges, forward);
        //now search the non html trees
        for(String uri : getNamespaces().keySet()) {
            AstNode root = root(uri);
            AstNode leaf = AstNodeUtils.findDescendantTag(root, offset, useLogicalRanges, forward);
            if(leaf == null) {
                continue;
            }
            if(mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if(leaf.logicalStartOffset() > mostLeaf.logicalStartOffset() ) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }

    /** @return a list of SyntaxElement-s representing parse elements of the html source. */
    public List<SyntaxElement> elementsList() {
        return result.getElements();
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
        this.isValid = false;
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
                                DefaultError error =
                                        new DefaultError(desc.getKey(), //NOI18N
                                        desc.getText(),
                                        desc.getText(),
                                        getSnapshot().getSource().getFileObject(),
                                        desc.getFrom(),
                                        desc.getTo(),
                                        false /* not line error */,
                                        desc.getType() == Description.WARNING ? Severity.WARNING : Severity.ERROR); //NOI18N

                                error.setParameters(new Object[]{node});

                                _errors.add(error);

                            }
                        }
                    }
                });

        return _errors;

    }

    public static AstNode getBoundAstNode(Error e) {
        if(e instanceof DefaultError) {
            if(e.getParameters() != null && e.getParameters().length > 0 && e.getParameters()[0] instanceof AstNode) {
                return (AstNode)e.getParameters()[0];
            }
        }
        
        return null;
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
