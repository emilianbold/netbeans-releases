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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * See DOM section of WebKit Remote Debugging Protocol for more details.
 *
 * @author Jan Stola
 */
public class DOM {
    private TransportHelper transport;
    private ResponseCallback callback;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Node documentNode;
    private Map<Integer,Node> nodes = new HashMap<Integer,Node>();

    private static final Logger LOG = Logger.getLogger(DOM.class.getName());
    
    public DOM(TransportHelper transport) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    public synchronized Node getDocument() {
        return getDocument(false);
    }
    
    public synchronized Node getDocument(boolean ignoreCache) {
        if (documentNode == null || ignoreCache) {
            Response response = transport.sendBlockingCommand(new Command("DOM.getDocument")); // NOI18N
            if (response != null) {
                JSONObject result = response.getResult();
                if (result != null) {
                    JSONObject node = (JSONObject)result.get("root"); // NOI18N
                    documentNode = new Node(node);
                    updateNodesMap(documentNode);
                }
            }
        }
        return documentNode;
    }

    private void updateNodesMap(Node node) {
        nodes.put(node.getNodeId(), node);
        synchronized (node) {
            List<Node> subNodes = node.getChildren();
            if (subNodes != null) {
                for (Node subNode : subNodes) {
                    updateNodesMap(subNode);
                }
            }
        }
    }

    public void requestChildNodes(int nodeId) {
        JSONObject params = new JSONObject();
        params.put("nodeId", nodeId); // NOI18N
        transport.sendCommand(new Command("DOM.requestChildNodes", params)); // NOI18N
    }

    public void highlightRect(Rectangle rect, Color fill, Color outline) {
        JSONObject params = new JSONObject();
        params.put("x", rect.x); // NOI18N
        params.put("y", rect.y); // NOI18N
        params.put("width", rect.width); // NOI18N
        params.put("height", rect.height); // NOI18N
        if (fill != null) {
            params.put("color", HighlightConfig.colorToJSONObject(fill)); // NOI18N
        }
        if (outline != null) {
            params.put("outlineColor", HighlightConfig.colorToJSONObject(outline)); // NOI18N
        }
        transport.sendCommand(new Command("DOM.highlightRect", params)); // NOI18N
    }

    public void highlightNode(Node node, HighlightConfig highlight) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("highlightConfig", highlight.toJSONObject()); // NOI18N
        transport.sendCommand(new Command("DOM.highlightNode", params)); // NOI18N
    }

    public void hideHighlight() {
        transport.sendCommand(new Command("DOM.hideHighlight")); // NOI18N
    }

    public String getNodeHTML(Node node) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.getOuterHTML", params)); // NOI18N
        return (String)((JSONObject)response.getResponse().get("result")).get("outerHTML");
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyChildNodesSet(Node parent) {
        for (Listener listener : listeners) {
            listener.childNodesSet(parent);
        }
    }

    private void notifyChildNodeRemoved(Node parent, Node child) {
        for (Listener listener : listeners) {
            listener.childNodeRemoved(parent, child);
        }
    }

    private void notifyChildNodeInserted(Node parent, Node child) {
        for (Listener listener : listeners) {
            listener.childNodeInserted(parent, child);
        }
    }

    private void notifyDocumentUpdated() {
        for (Listener listener : listeners) {
            listener.documentUpdated();
        }
    }

    private void notifyAttributeModified(Node node, String attrName) {
        for (Listener listener : listeners) {
            listener.attributeModified(node, attrName);
        }
    }

    private void notifyAttributeRemoved(Node node, String attrName) {
        for (Listener listener : listeners) {
            listener.attributeRemoved(node, attrName);
        }
    }

    void handleSetChildNodes(JSONObject params) {
        int parentId = ((Number)params.get("parentId")).intValue(); // NOI18N
        Node parent = nodes.get(parentId);
        JSONArray children = (JSONArray)params.get("nodes"); // NOI18N
        for (Object child : children) {
            parent.addChild(new Node((JSONObject)child));
        }
        updateNodesMap(parent);
        notifyChildNodesSet(parent);
    }

    void handleChildNodeInserted(JSONObject params) {
        int parentId = ((Number)params.get("parentNodeId")).intValue(); // NOI18N
        Node parent = nodes.get(parentId);
        int previousNodeId = ((Number)params.get("previousNodeId")).intValue(); // NOI18N
        Node previousNode = nodes.get(previousNodeId);
        JSONObject childData = (JSONObject)params.get("node"); // NOI18N
        Node child = new Node(childData);
        updateNodesMap(child);
        parent.insertChild(child, previousNode);
        notifyChildNodeInserted(parent, child);
    }

    void handleChildNodeRemoved(JSONObject params) {
        int parentId = ((Number)params.get("parentNodeId")).intValue(); // NOI18N
        Node parent = nodes.get(parentId);
        int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
        Node child = nodes.get(nodeId);
        parent.removeChild(child);
        nodes.remove(nodeId);
        notifyChildNodeRemoved(parent, child);
    }

    void handleDocumentUpdated() {
        synchronized (this) {
            nodes.clear();
            documentNode = null;
        }
        notifyDocumentUpdated();
    }

    void handleAttributeModified(JSONObject params) {
        int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
        Node node = nodes.get(nodeId);
        if (node == null) {
            return;
        }
        String name = (String)params.get("name"); // NOI18N
        String value = (String)params.get("value"); // NOI18N
        node.setAttribute(name, value);
        notifyAttributeModified(node, name);
    }

    void handleAttributeRemoved(JSONObject params) {
        int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
        Node node = nodes.get(nodeId);
        String name = (String)params.get("name"); // NOI18N
        node.removeAttribute(name);
        notifyAttributeRemoved(node, name);
    }

    /**
     * DOM listener.
     */
    public static interface Listener {
        void documentUpdated();
        void childNodesSet(Node parent);
        void childNodeRemoved(Node parent, Node child);
        void childNodeInserted(Node parent, Node child);
        void attributeModified(Node node, String attrName);
        void attributeRemoved(Node node, String attrName);
    }

    class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            JSONObject params = response.getParams();
            if ("DOM.setChildNodes".equals(method)) { // NOI18N
                handleSetChildNodes(params);
            } else if ("DOM.childNodeInserted".equals(method)) { // NOI18N
                handleChildNodeInserted(params);
            } else if ("DOM.childNodeRemoved".equals(method)) { // NOI18N
                handleChildNodeRemoved(params);
            } else if ("DOM.documentUpdated".equals(method)) { // NOI18N
                handleDocumentUpdated();
            } else if ("DOM.attributeModified".equals(method)) { // NOI18N
                handleAttributeModified(params);
            } else if ("DOM.attributeRemoved".equals(method)) { // NOI18N
                handleAttributeRemoved(params);
            }
        }

    }

}
