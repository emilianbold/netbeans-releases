/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.doc;

import com.oracle.nashorn.ir.AccessNode;
import com.oracle.nashorn.ir.BinaryNode;
import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.IdentNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.ir.ReferenceNode;
import com.oracle.nashorn.ir.VarNode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsElement.Kind;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.PathNodeVisitor;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationCompleter {

    private static final RequestProcessor RP = new RequestProcessor("JavaScript Documentation Completer"); //NOI18N

    public static void generateCompleteComment(BaseDocument doc, int caretOffset, int indent) {
        Runnable documentationGenerator = new DocumentationGenerator(doc, caretOffset, indent);
        RP.post(documentationGenerator);
    }

    private static class DocumentationGenerator implements Runnable {

        private final BaseDocument doc;
        private final int offset;
        private final int indent;

        public DocumentationGenerator(BaseDocument doc, int offset, int indent) {
            this.doc = doc;
            this.offset = offset;
            this.indent = indent;
        }

        @Override
        public void run() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ParserResult parserResult = (ParserResult) resultIterator.getParserResult();
                        if (parserResult != null && parserResult instanceof JsParserResult) {
                            final JsParserResult jsParserResult = (JsParserResult) parserResult;
                            if (jsParserResult.getRoot() == null) {
                                // broken source
                                return;
                            }
                            Node nearestNode = getNearestNode(jsParserResult, offset);
                            int examinedOffset = nearestNode instanceof VarNode ? nearestNode.getStart() : nearestNode.getFinish();
                            JsObject jsObject = findJsObjectFunctionVariable(jsParserResult.getModel().getGlobalObject(), examinedOffset);
                            assert jsObject != null;
                            if (jsObject.getJSKind() == Kind.FILE) {
                                // was not a global variable, function, object
                                if (nearestNode instanceof PropertyNode) {
                                    // is defined property
                                    Node value = ((PropertyNode) nearestNode).getValue();
                                    if (value instanceof ReferenceNode || value instanceof LiteralNode) {
                                        PathToNodeVisitor ptnv = new PathToNodeVisitor(nearestNode);
                                        FunctionNode root = jsParserResult.getRoot();
                                        root.accept(ptnv);
                                        // TODO - getting fqn has to be rewrite to visitor usage
                                        String lhs = "";
                                        for (Node node : ptnv.getFinalPath()) {
                                            if (node instanceof AccessNode) {
                                                lhs = ((AccessNode) node).toString();
                                            } else if (node instanceof BinaryNode) {
                                                lhs = ((BinaryNode) node).lhs().toString();
                                            } else if (node instanceof VarNode) {
                                                lhs = ((VarNode)node).getName().getName();
                                            }
                                        }
                                        jsObject = ModelUtils.findJsObjectByName(jsParserResult.getModel(), lhs + "." + ((PropertyNode) nearestNode).getKeyName());
                                    }
                                } else {
                                    // not a global object or function - it's defined by its fqn
                                    String nearestNodeFqn = getNearestNodeFqn(jsParserResult, offset);
                                    jsObject = ModelUtils.findJsObjectByName(jsParserResult.getModel(), nearestNodeFqn);
                                }
                            }
                            if (isField(jsObject)) {
                                generateFieldComment(doc, offset, indent, jsParserResult, jsObject);
                            } else if (isFunction(jsObject)) {
                                generateFunctionComment(doc, offset, indent, jsParserResult, jsObject);
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void generateFieldComment(BaseDocument doc, int offset, int indent, JsParserResult jsParserResult, JsObject jsObject) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        // TODO - rewrite @type according to doc tool
        generateDocEntry(doc, toAdd, "@type", indent, null, null);

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static void generateFunctionComment(BaseDocument doc, int offset, int indent, JsParserResult jsParserResult, JsObject jsObject) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        // TODO - rewrite @param, @return according to doc tool
        JsFunction function = ((JsFunction) jsObject);
        addParameters(doc, toAdd, "@param", indent, function.getParameters());
        if (!function.getReturnTypes().isEmpty()) {
            addReturns(doc, toAdd, "@return", indent, function.getReturnTypes());
        }

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static void addParameters(BaseDocument doc, StringBuilder toAdd, String tag, int indent, Collection<? extends JsObject> params) {
        for (JsObject jsObject : params) {
            generateDocEntry(doc, toAdd, tag, indent, jsObject.getName(), null);
        }
    }

    private static void addReturns(BaseDocument doc, StringBuilder toAdd, String tag, int indent, Collection<? extends TypeUsage> returns) {
        StringBuilder sb = new StringBuilder();
        for (TypeUsage typeUsage : returns) {
            // TODO - doc tool related delimiter
            sb.append("|").append(typeUsage.getType());
        }
        String returnString = returns.isEmpty() ? "" : sb.toString().substring(1);
        generateDocEntry(doc, toAdd, tag, indent, null, returnString);
    }

    private static void generateDocEntry(BaseDocument doc, StringBuilder toAdd, String text, int indent, String name, String type) {
        toAdd.append("\n");
        toAdd.append(IndentUtils.createIndentString(doc, indent));

        toAdd.append("* ");
        toAdd.append(text);
        if (type != null) {
            if (type != null) {
                toAdd.append(" ");
                toAdd.append(type);
            }
        } else {
            toAdd.append(" ");
            // TODO - rewrite to doc tool related syntax
            toAdd.append("{type}");
        }
        if (name != null) {
            toAdd.append(" ");
            toAdd.append(name);
        }
    }

    private static boolean isField(JsObject jsObject) {
        Kind kind = jsObject.getJSKind();
        return kind == Kind.FIELD || kind == Kind.VARIABLE || kind == Kind.PROPERTY;
    }

    private static boolean isFunction(JsObject jsObject) {
        return jsObject.getJSKind().isFunction();
    }

    /**
     * Gets the nearest next node for given offset.
     *
     * @param parserResult parser result of the JS file
     * @param offset offset where to start searching
     * @return {@code Node} which is the closest one
     */
    private static Node getNearestNode(JsParserResult parserResult, int offset) {
        FunctionNode root = parserResult.getRoot();
        NearestNodeVisitor offsetVisitor = new NearestNodeVisitor(offset);
        root.accept(offsetVisitor);
        return offsetVisitor.getNearestNode();
    }

    private static String getNearestNodeFqn(JsParserResult parserResult, int offset) {
        FunctionNode root = parserResult.getRoot();
        NearestNodeVisitor offsetVisitor = new NearestNodeVisitor(offset);
        root.accept(offsetVisitor);
        return offsetVisitor.getNearestNodeFqn();
    }

    private static JsObject findJsObjectFunctionVariable(JsObject object, int offset) {
        JsObjectImpl jsObject = (JsObjectImpl) object;
        JsObject result = null;
        JsObject tmpObject = null;
        if (jsObject.getOffsetRange(null).containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT
                        || kind == JsElement.Kind.FUNCTION || kind == JsElement.Kind.METHOD || kind == JsElement.Kind.CONSTRUCTOR
                        || kind == JsElement.Kind.VARIABLE) {
                    tmpObject = findJsObjectFunctionVariable(property, offset);
                }
                if (tmpObject != null) {
                    result = tmpObject;
                    break;
                }
            }
        }
        return result;
    }

    private static class NearestNodeVisitor extends PathNodeVisitor {

        private final int offset;
        private Node nearestNode = null;

        public NearestNodeVisitor(int offset) {
            this.offset = offset;
        }

        private void processNode(Node node, boolean onset) {
            if (onset) {
                if (offset < node.getStart() && (nearestNode == null || node.getStart() < nearestNode.getStart())) {
                    nearestNode = node;
                }
            }
        }

        public String getNearestNodeFqn() {
            if (nearestNode instanceof AccessNode || nearestNode instanceof BinaryNode) {
                FarestIdentNodeVisitor farestNV = new FarestIdentNodeVisitor();
                nearestNode.accept(farestNV);
                return farestNV.getFarestFqn();
            }
            return nearestNode.toString();
        }

        public Node getNearestNode() {
            if (nearestNode instanceof AccessNode) {
                FarestIdentNodeVisitor farestNV = new FarestIdentNodeVisitor();
                nearestNode.accept(farestNV);
                return farestNV.getFarestNode();
            }
            return nearestNode;
        }

        @Override
        public Node visit(AccessNode accessNode, boolean onset) {
            processNode(accessNode, onset);
            return super.visit(accessNode, onset);
        }

        @Override
        public Node visit(FunctionNode functionNode, boolean onset) {
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                processNode(functionNode, onset);
            }
            return super.visit(functionNode, onset);
        }

        @Override
        public Node visit(PropertyNode propertyNode, boolean onset) {
            processNode(propertyNode, onset);
            return super.visit(propertyNode, onset);
        }

        @Override
        public Node visit(VarNode varNode, boolean onset) {
            processNode(varNode, onset);
            return super.visit(varNode, onset);
        }

        @Override
        public Node visit(BinaryNode binaryNode, boolean onset) {
            processNode(binaryNode, onset);
            return super.visit(binaryNode, onset);
        }
    }

    private static class FarestIdentNodeVisitor extends PathNodeVisitor {

        private Node farestNode;
        private final StringBuilder farestPath = new StringBuilder();

        @Override
        public Node visit(IdentNode identNode, boolean onset) {
            farestNode = identNode;
            if (onset) {
                farestPath.append(".").append(identNode.getName());
            }
            return super.visit(identNode, onset);
        }

        public Node getFarestNode() {
            return farestNode;
        }

        public String getFarestFqn() {
            return farestPath.toString().substring(1);
        }
    }

    private static class PathToNodeVisitor extends PathNodeVisitor {

        private final Node finalNode;
        private List<? extends Node> finalPath;

        public PathToNodeVisitor(Node finalNode) {
            this.finalNode = finalNode;
        }

        @Override
        public void addToPath(Node node) {
            super.addToPath(node);
            if (node.equals(finalNode)) {
                finalPath = new LinkedList<Node>(getPath());
            }
        }

        public List<? extends Node> getFinalPath() {
            return finalPath;
        }

    }

//    private static class IdentsToNodeVisitor extends NodeVisitor {
//
//        private final StringBuilder finalPathName = new StringBuilder();
//
//        public String getFinalPathName() {
//            return finalPathName.toString().substring(1);
//        }
//
//        @Override
//        public Node visit(IdentNode identNode, boolean onset) {
//            if (onset) {
//                finalPathName.append(".").append(identNode.getName());
//            }
//            return super.visit(identNode, onset);
//        }
//    }
}
