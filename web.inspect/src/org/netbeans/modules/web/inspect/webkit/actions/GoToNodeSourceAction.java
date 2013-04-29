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
package org.netbeans.modules.web.inspect.webkit.actions;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Go to source action for DOM nodes.
 *
 * @author Jan Stola
 */
public class GoToNodeSourceAction extends NodeAction  {
    /** {@code RequestProcessor} for this class. */
    private static final RequestProcessor RP = new RequestProcessor(GoToNodeSourceAction.class);

    @Override
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        org.openide.nodes.Node selection = activatedNodes[0];
        org.netbeans.modules.web.webkit.debugging.api.dom.Node node = selection
                .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
        Resource resource = getNodeOrigin(selection);
        FileObject fob = resource.toFileObject();
        try {
            Source source = Source.create(fob);
            ParserManager.parse(Collections.singleton(source), new GoToNodeTask(node, fob));
        } catch (ParseException ex) {
            Logger.getLogger(GoToNodeSourceAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            org.openide.nodes.Node selection = activatedNodes[0];
            org.netbeans.modules.web.webkit.debugging.api.dom.Node node = selection
                    .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            if (node == null) {
                return false;
            }
            final Resource resource = getNodeOrigin(selection);
            // Avoid invocation of Resource.toFileObject() under Children.MUTEX
            RP.post(new Runnable() {
                @Override
                public void run() {
                    if (resource.toFileObject() == null) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setEnabled(false);
                            }
                        });
                    }
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        // The default popup presenter does not observe property changes.
        // We need a presenter that observes the changes because of our
        // lazy disabling of the action. The default menu presenter observes
        // the changes.
        return super.getMenuPresenter();
    }

    /**
     * Returns the origin of the specified node.
     * 
     * @param node node whose origin should be returned.
     * @return origin of the specified node.
     */
    private Resource getNodeOrigin(org.openide.nodes.Node node) {
        Resource resource = node.getLookup().lookup(Resource.class);
        if (resource == null) {
            org.openide.nodes.Node parent = node.getParentNode();
            if (parent != null) {
                resource = getNodeOrigin(parent);
            }
        }
        return resource;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToNodeSourceAction.class, "GoToNodeSourceAction.displayName"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Task that jumps to the source of the specified node in the document
     * in the given file.
     */
    static class GoToNodeTask extends UserTask {
        /** Node to jump to. */
        private final org.netbeans.modules.web.webkit.debugging.api.dom.Node node;
        /** File to jump into. */
        private final FileObject fob;

        /**
         * Creates a new {@code GoToNodeTask} for the specified file and node.
         *
         * @param node node to jump to.
         * @param fob file to jump into.
         */
        GoToNodeTask(org.netbeans.modules.web.webkit.debugging.api.dom.Node node, FileObject fob) {
            this.node = node;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ResultIterator htmlResultIterator = WebUtils.getResultIterator(resultIterator, "text/html"); // NOI18N
            final int offsetToShow;
            if (htmlResultIterator == null) {
                offsetToShow = 0;
            } else {
                HtmlParsingResult result = (HtmlParsingResult)htmlResultIterator.getParserResult();
                Node nodeToShow = findNode(result, node);
                Snapshot snapshot = htmlResultIterator.getSnapshot();
                int snapshotOffset = nodeToShow.from();
                offsetToShow = snapshot.getOriginalOffset(snapshotOffset);
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CSSUtils.openAtOffset(fob, offsetToShow);
                }
            });
        }

        /**
         * Finds a source node that corresponds to the specified DOM node.
         * 
         * @param result parsing result of the source file.
         * @param node DOM node whose counterpart should be found.
         * @return the best source approximation of the specified DOM node.
         */
        private Node findNode(HtmlParsingResult result,
                org.netbeans.modules.web.webkit.debugging.api.dom.Node node) {
            org.netbeans.modules.web.webkit.debugging.api.dom.Node domParent = node;
            Node root = result.root();
            Node nearestNode = root;

            // Find a root of our search. The possible roots
            // are either the HTML tag or nodes with ID.
            while (domParent != null) {
                String tagName = domParent.getNodeName();
                org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute attribute
                        = domParent.getAttribute("id"); // NOI18N
                String id = (attribute == null) ? null : attribute.getValue();
                Node sourceParent;
                if (id != null) {
                    sourceParent = findElementByID(root, id);
                    if (sourceParent != null) {
                        nearestNode = findNode(node, domParent, sourceParent);
                    }
                    break;
                }
                if ("html".equalsIgnoreCase(tagName)) { // NOI18N
                    sourceParent = findElementByTagName(root, "html"); // NOI18N
                    nearestNode = findNode(node, domParent, sourceParent);
                    break;
                }
                domParent = domParent.getParent();
            }
            return nearestNode;
        }

        /**
         * Returns the source node with the specified ID.
         * 
         * @param root root of the source tree to search.
         * @param id ID of the desired source node.
         * @return source node with the specified ID or {@code null}
         * if such a node doesn't exist.
         */
        private Node findElementByID(Node root, String id) {
            Node result = null;
            if (root instanceof OpenTag) {
                OpenTag tag = (OpenTag)root;
                Attribute attr = tag.getAttribute("id"); // NOI18N
                CharSequence seq = (attr == null) ? null : attr.unquotedValue();
                String nodeId = (seq == null) ? null : seq.toString();
                if (id.equals(nodeId)) {
                    result = root;
                } else {
                    for (Element element : root.children(ElementType.OPEN_TAG)) {
                        result = findElementByID((Node)element, id);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Returns some source node with the specified tag name.
         * 
         * @param root root of the source tree to search.
         * @param tagName tag name of the desired source node.
         * @return source node with the specified tag name or {@code null}
         * if such node doesn't exist.
         */
        private Node findElementByTagName(Node root, String tagName) {
            Node result = null;
            if (root instanceof OpenTag) {
                OpenTag tag = ((OpenTag)root);
                String name = tag.name().toString();
                if (tagName.equalsIgnoreCase(name)) {
                    result = root;
                } else {
                    for (Element element : root.children(ElementType.OPEN_TAG)) {
                        result = findElementByTagName((Node)element, tagName);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Returns a source node that corresponds to the specified DOM node.
         * 
         * @param node DOM node whose counterpart should be found.
         * @param domParent node from the parent-chain of the DOM node.
         * @param sourceParent source node that corresponds to {@code domParent}.
         * @return the best source approximation of the specified DOM node.
         */
        private Node findNode(org.netbeans.modules.web.webkit.debugging.api.dom.Node node,
                org.netbeans.modules.web.webkit.debugging.api.dom.Node domParent,
                Node sourceParent) {
            List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> parentChain
                    = new LinkedList<org.netbeans.modules.web.webkit.debugging.api.dom.Node>();
            org.netbeans.modules.web.webkit.debugging.api.dom.Node parent = node;
            while (parent != domParent) {
                parentChain.add(0, parent);
                parent = parent.getParent();
            }
            parentChain.add(0, parent);
            return findNode(parentChain, sourceParent);
        }

        /**
         * Returns a source node that corresponds to the last element of
         * the DOM parent chain collection.
         * 
         * @param parentChain DOM parent chain of the node whose counterpart
         * should be found (the node itself is the last element of this collection).
         * @param sourceParent source node that corresponds to the first
         * element of the DOM parent chain collection.
         * @return the best source approximation of the specified DOM node.
         */
        private Node findNode(List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> parentChain,
                Node sourceParent) {
            if (parentChain.size() == 1) {
                return sourceParent;
            }
            Node nextParent = null;
            parentChain.remove(0);
            org.netbeans.modules.web.webkit.debugging.api.dom.Node domChild = parentChain.get(0);
            int domIndex = elementIndexInParent(domChild);
            List<Element> children = new ArrayList<Element>(sourceParent.children(ElementType.OPEN_TAG));
            // Try the candidates according to their distance from the original
            // position of the corresponding DOM node.
            for (int i=0; i<=Math.max(domIndex, children.size()-1); i++) {
                int index = domIndex+i;
                if (index < children.size()) {
                    Node candidate = (Node)children.get(index);
                    if (match(domChild, candidate)) {
                        nextParent = candidate;
                        break;
                    }
                }
                if (i != 0) {
                    index = domIndex-i;
                    if ((0 <= index) && (index < children.size())) {
                        Node candidate = (Node)children.get(index);
                        if (match(domChild, candidate)) {
                            nextParent = candidate;
                        }
                    }
                }
            }
            return (nextParent == null) ? sourceParent : findNode(parentChain, nextParent);
        }

        /**
         * Determines whether the specified DOM node matches the given
         * source node. The comparison is based on these nodes only
         * (i.e. the surrounding nodes are not considered).
         * 
         * @param domNode DOM node to compare.
         * @param sourceNode source node to compare.
         * @return {@code true} is these given nodes matches,
         * returns {@code false} otherwise.
         */
        private boolean match(org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode, Node sourceNode) {
            if (sourceNode instanceof OpenTag) {
                OpenTag tag = (OpenTag)sourceNode;

                // Check the tag names
                String sourceTagName = tag.name().toString();
                if (!domNode.getNodeName().equalsIgnoreCase(sourceTagName)) {
                    return false;
                }
                // Some tags are unique - no need to check anything besides their name.
                if ("html".equalsIgnoreCase(sourceTagName) // NOI18N
                        || "body".equalsIgnoreCase(sourceTagName) // NOI18N
                        || "head".equalsIgnoreCase(sourceTagName) // NOI18N
                        || "title".equalsIgnoreCase(sourceTagName)) { // NOI18N
                    return true;
                }

                // Check the ID
                org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute domAttr = domNode.getAttribute("id"); // NOI18N
                String domID = (domAttr == null) ? null : domAttr.getValue();
                Attribute sourceAttr = tag.getAttribute("id"); // NOI18N
                CharSequence seq = (sourceAttr == null) ? null : sourceAttr.unquotedValue(); 
                String sourceID = (seq == null) ? null : seq.toString();
                if ((domID == null) != (sourceID == null)) {
                    return false;
                }
                if ((domID != null) && domID.equals(sourceID)) {
                    return true;
                }
                
                // Check if attributes are the same
                Map<String,String> attributes = new HashMap<String,String>();
                for (org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute attribute : domNode.getAttributes()) {
                    attributes.put(attribute.getName().toUpperCase(), attribute.getValue());
                }
                for (Attribute attribute : tag.attributes()) {
                    String name = attribute.name().toString().toUpperCase();
                    String domValue = attributes.get(name);
                    CharSequence sourceSeq = attribute.unquotedValue();
                    String sourceValue = (sourceSeq == null) ? "" : sourceSeq.toString(); // NOI18N
                    if (domValue == null || !domValue.equals(sourceValue)) {
                        return false;
                    }
                    attributes.remove(name);
                }
                for (String attribute : attributes.keySet()) {
                    String name = attribute.toUpperCase();
                    // Ignore STYLE and CLASS attributes - they are often modified
                    // dynamically during JavaScript-based styling.
                    if (!("STYLE".equals(name) || "CLASS".equals(name))) { // NOI18N
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the index of the specified element in its parent
         * among other elements under this parent.
         * 
         * @param element element whose index should be returned.
         * @return index of the specified element.
         */
        private int elementIndexInParent(org.netbeans.modules.web.webkit.debugging.api.dom.Node element) {
            int index = 0;
            org.netbeans.modules.web.webkit.debugging.api.dom.Node parent = element.getParent();
            for (org.netbeans.modules.web.webkit.debugging.api.dom.Node child : parent.getChildren()) {
                if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    if (child == element) {
                        return index;
                    }
                    index++;
                }
            }
            return -1;
        }
        
    }

}
