/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.html.navigator;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.navigator.actions.HighlightInBrowserAction;
import org.netbeans.modules.html.navigator.actions.OpenAction;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class HtmlElementNode extends AbstractNode {

    private static final Image ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element.png"); //NOI18N
    private HtmlNavigatorPanelUI ui;
    private FileObject fileObject;
    //actions
    private Action openAction;
    private Action highlightInBrowserAction;
    //static description (of the source element)
    private HtmlElementDescription element;
    //dynamic description (of the webkit DOM element)
    private WebKitNodeDescription webKitNodeDescription;
    //an openide Node representing the counterpart in the browsers DOM tree.
    //note: we need to hold the openide node itself, not just the webkit node
    //since most of the operations on PageModel requires to pass the Node instances
    //obtained from the same API.
    private Node webKitNode;

    public HtmlElementNode(HtmlElementDescription element, HtmlNavigatorPanelUI ui, FileObject fileObject) {
//        super(element.isLeaf()
//                ? Children.LEAF
//                : new ElementChildren(ui, fileObject));

        //TODO put back the Children.LEAF type if empty children.
        //creates initialy empty children
        super(new ElementChildren(ui, fileObject));

        this.element = element;
        this.ui = ui;
        this.fileObject = fileObject;

        openAction = new OpenAction(element);
        highlightInBrowserAction = new HighlightInBrowserAction(element, ui);

        //set static keys to the Children object. The static keys represents the html elements
        //from the source code
        getElementChildren().setStaticKeys(element.getChildren());

        //set all the data related to the webkit "connection" of this source element node
        refreshWebkitCounterpartState(false);
      
        //finally set the children keys to the node
        getElementChildren().resetKeys();
    }
    
    private PageModel getPageModel() {
        return ui.getPageModel();
    }

    /**
     * Return true if this source node is "connected" to the webkit DOM node.
     */
    private boolean isConnected() {
        return webKitNode != null;
    }

    private Node findWebKitNode() {
        //check if the inspected fileobject matches our fileobject
        FileObject inspectedFile = ui.getInspectedFileObject();
        if(inspectedFile == null) {
            return null;
        }
        if(!inspectedFile.equals(fileObject)) {
            //foreign fileobject, someone likely switched the inspector do different file
            return null;
        }
        
        PageModel pageModel = getPageModel();
        if(pageModel == null) {
            return null;
        }
        Node domDocumentNode = pageModel.getDocumentNode();
        if(domDocumentNode == null) {
            return null;
        }
        return Utils.findNode(domDocumentNode, element);
    }
    
    /**
     * Refreshes all the data related to the webkit DOM node corresponding to
     * this source element node
     */
    private synchronized void refreshWebkitCounterpartState(boolean forceChildrenKeysRefresh) {
        ElementChildren children = getElementChildren();
        WebKitNodeDescription currentWebKitNodeDescription = webKitNodeDescription;
        Node freshWebKitNode = findWebKitNode();
        if (isConnected()) {
            //the source node has already assigned a webkit counterpart
            if (freshWebKitNode == null) {
                //"disconnected"
                webKitNode = null;
                webKitNodeDescription = null;

                webkitNodeDescriptionChanged(currentWebKitNodeDescription, webKitNodeDescription);
                children.setDynamicKeys(Collections.<Description>emptyList(), forceChildrenKeysRefresh);
            } else {
                //still "connected" - refresh
                if (freshWebKitNode != webKitNode) { //instances comparison
                    webKitNode = freshWebKitNode;
                    webKitNodeDescription = WebKitNodeDescription.forNode(webKitNode);

                    webkitNodeDescriptionChanged(currentWebKitNodeDescription, webKitNodeDescription);
                    children.setDynamicKeys(webKitNodeDescription.getChildren(), forceChildrenKeysRefresh);
                }
            }
        } else {
            //the source node has no assigned webkit node counterpart
            if (freshWebKitNode == null) {
                //still "disconnected", no change
            } else {
                //now "connected" - initialize
                webKitNode = freshWebKitNode;
                webKitNodeDescription = WebKitNodeDescription.forNode(webKitNode);

                webkitNodeDescriptionChanged(currentWebKitNodeDescription, webKitNodeDescription);
                children.setDynamicKeys(webKitNodeDescription.getChildren(), forceChildrenKeysRefresh);
            }

        }
    }
    
    //recursively refreshes the DOM counterpart status
    void refreshDOMStatus() {
        refreshWebkitCounterpartState(true);
        
        for (Node child : getChildren().getNodes(true)) {
            if (child instanceof HtmlElementNode) {
                ((HtmlElementNode) child).refreshDOMStatus();
            }
        }
    }

    private void webkitNodeDescriptionChanged(WebKitNodeDescription oldDescription, WebKitNodeDescription newDescription) {
        //update the "connected" status 
        fireDisplayNameChange(getHtmlDisplayName(oldDescription != null), 
                getHtmlDisplayName(newDescription != null));
        
        //refresh the "connection" sensitive actions state
        highlightInBrowserAction.setEnabled(newDescription != null);
    }

    private ElementChildren getElementChildren() {
        Children ch = getChildren();
        return ch == Children.LEAF ? null : (ElementChildren) getChildren();
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (getElement().getElementType() == ElementType.OPEN_TAG) {
            final AtomicReference<HtmlElementProperties.PropertiesPropertySet> pset_ref = new AtomicReference<HtmlElementProperties.PropertiesPropertySet>();
            try {
                getElement().runTask(new HtmlElementDescription.Task() {
                    @Override
                    public void run(HtmlParserResult result) {
                        OpenTag openTag = (OpenTag) getElement().resolve(result);
                        HtmlElementProperties.PropertiesPropertySet pset = new HtmlElementProperties.PropertiesPropertySet(result, openTag);
                        pset_ref.set(pset);
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            HtmlElementProperties.PropertiesPropertySet pset = pset_ref.get();
            if (pset == null) {
                return super.getPropertySets();
            } else {
                return new PropertySet[]{pset};
            }

        } else {
            return super.getPropertySets();
        }
    }

    @Override
    public String getDisplayName() {
        return element.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        return getHtmlDisplayName(isConnected());
    }

    private String getHtmlDisplayName(boolean connected) {
        StringBuilder b = new StringBuilder();
        if (connected) {
            b.append("<b>");
        }
        b.append(getDisplayName());
        if (connected) {
            b.append("</b>");
        }
        if (getElement().getIdAttr() != null) {
            b.append("&nbsp;");
            b.append("<font color=\"#777777\">");
            b.append("id=");
            b.append(getElement().getIdAttr());
            b.append("</font>");
        }
        if (getElement().getClassAttr() != null) {
            b.append("&nbsp;");
            b.append("<font color=\"#777777\">");
            b.append("class=");
            b.append(getElement().getClassAttr());
            b.append("</font>");
        }

        return b.toString();
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Action[] getActions(boolean context) {

        if (context || getElement().getName() == null) {
            return ui.getActions();
        } else {
            Action panelActions[] = ui.getActions();

            Action actions[] = new Action[4 + panelActions.length];
            actions[0] = openAction;
            actions[1] = null;
            actions[2] = highlightInBrowserAction;
            actions[3] = null;
            for (int i = 0; i < panelActions.length; i++) {
                actions[4 + i] = panelActions[i];
            }
            return actions;
        }
    }

    @Override
    public Action getPreferredAction() {
        return openAction;
    }

//    /**
//     * Refreshes the Node recursively. Only initiates the refresh; the refresh
//     * itself may happen asynchronously.
//     */
//    public void refreshRecursively() {
//        List<Node> toExpand = new ArrayList<Node>();
//        refreshRecursively(Collections.singleton(this), toExpand);
//        ui.performExpansion(toExpand, Collections.<Node>emptyList());
//    }
//
//    private void refreshRecursively(Collection<HtmlNode> toDo, final Collection<Node> toExpand) {
//        for (HtmlNode elnod : toDo) {
//            final Children ch = elnod.getChildren();
//            if (ch instanceof ElementChildren) {
//                ((ElementChildren) ch).resetKeys(elnod.element.getChildren());
//
//                Collection<HtmlNode> children = (Collection<HtmlNode>) (List) Arrays.asList(ch.getNodes());
//                toExpand.addAll(children);
//                refreshRecursively(children, toExpand);
//            }
//        }
//    }
    public HtmlElementNode getNodeForOffset(int offset) {
        if (getElement().getFrom() > offset) {
            return null;
        }

        // Inefficient linear search because the children may not be
        // ordered according to the source
        Children ch = getChildren();
        if (ch instanceof ElementChildren) {
            Node[] children = ch.getNodes();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof HtmlElementNode) {
                    HtmlElementNode c = (HtmlElementNode) children[i];
                    long start = c.getElement().getFrom();
                    if (start <= offset) {
                        long end = c.getElement().getTo();
                        if (end >= offset) {
                            return c.getNodeForOffset(offset);
                        }
                    }
                }
            }
        }

        return this;
    }

    public void updateRecursively(HtmlElementDescription newElement) {
        List<Node> nodesToExpand = new LinkedList<Node>();
        List<Node> nodesToExpandRec = new LinkedList<Node>();
        updateRecursively(newElement, nodesToExpand, nodesToExpandRec);
        ui.performExpansion(nodesToExpand, nodesToExpandRec);
    }

    private void updateRecursively(HtmlElementDescription newDescription, List<Node> nodesToExpand, List<Node> nodesToExpandRec) {
        ElementChildren ch = getElementChildren();

//        //If a node that was a LEAF now has children the child type has to be changed from Children.LEAF
//        //to ElementChildren to be able to hold the new child data
//        if (!(ch instanceof ElementChildren) && !newDescription.isLeaf()) {
//            ch = new ElementChildren(ui, fileObject);
//            setChildren(ch);
//        }


        Set<HtmlElementDescription> oldSubs = new HashSet<HtmlElementDescription>(element.getChildren());

        // Create a hashtable which maps StructureItem to node.
        // We will then identify the nodes by the description. The trick is 
        // that the new and old description are equal and have the same hashcode
        Node[] nodes = ch.getNodes(true);
        HashMap<HtmlElementDescription, HtmlElementNode> oldD2node = new HashMap<HtmlElementDescription, HtmlElementNode>();
        for (Node node : nodes) {
            if (node instanceof HtmlElementNode) {
                oldD2node.put(((HtmlElementNode) node).element, (HtmlElementNode) node);
            }
        }

        List<HtmlElementDescription> newDescriptionChildren = newDescription.getChildren();
        // Now refresh keys
        ch.setStaticKeys(newDescription.getChildren());
        ch.resetKeys();

        // Reread nodes
        nodes = ch.getNodes(true);

        for (HtmlElementDescription newSub : newDescriptionChildren) {
            HtmlElementNode node = oldD2node.get(newSub);
            if (node != null) {
                //the node already existed in the old node children
                if (!oldSubs.contains(newSub)) {
                    nodesToExpand.add(node);
                }
                // update the node recursively
                node.updateRecursively(newSub, nodesToExpand, nodesToExpandRec);
            } else {
                // a new node
                for (Node newNode : nodes) {
                    if (newNode instanceof HtmlElementNode && ((HtmlElementNode) newNode).getElement() == newSub) {
                        //recursively expand the new nodes
                        nodesToExpandRec.add(newNode);
                        break;
                    }
                }
            }
        }



        String oldHtml = element.getName();
        String descHtml = newDescription.getName();

        if (oldHtml != null && !oldHtml.equals(descHtml)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldHtml, descHtml);
        }

        element = newDescription;
    }

    public HtmlElementDescription getElement() {
        return element;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    private static class ElementChildren extends Children.Keys<Description> {

        private HtmlNavigatorPanelUI ui;
        private FileObject fileObject;
        private Collection<? extends Description> staticKeys = new HashSet<Description>();
        private Collection<? extends Description> dynamicKeys = new HashSet<Description>();

        public ElementChildren(HtmlNavigatorPanelUI ui, FileObject fileObject) {
            this.ui = ui;
            this.fileObject = fileObject;
        }

        @Override
        protected Node[] createNodes(Description key) {
            switch (key.getType()) {
                case Description.STATIC_NODE:
                    return new Node[]{new HtmlElementNode((HtmlElementDescription) key, ui, fileObject)};
                case Description.DYNAMIC_NODE:
                    return new Node[]{new WebKitNode((WebKitNodeDescription) key)};
                default:
                    return null;
            }
        }

        void setStaticKeys(Collection<? extends Description> staticDescriptions) {
            staticKeys = staticDescriptions;
        }

        void setDynamicKeys(Collection<? extends Description> dynamicDescriptions, boolean forceKeysReset) {
            dynamicKeys = dynamicDescriptions;
            if(forceKeysReset) {
                resetKeys();
            }
        }

        void resetKeys() {
            Set<Description> set = new HashSet<Description>();
            //static keys have precendence
            set.addAll(staticKeys);
            set.addAll(dynamicKeys);

            setKeys(set);
        }
    }
}
