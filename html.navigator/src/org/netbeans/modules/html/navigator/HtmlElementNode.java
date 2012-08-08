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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.navigator.actions.OpenAction;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class HtmlElementNode extends AbstractNode {
    
    private enum State {
        SOURCE, SOURCE_AND_DOM, DOM, NON;
    }

    private static final Logger LOGGER = Logger.getLogger(HtmlElementNode.class.getSimpleName());
    
    private static final Image SOURCE_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element_bw.png"); //NOI18N
    private static final Image SOURCE_AND_DOM_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element.png"); //NOI18N
    private static final Image DOM_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/extbrowser.gif"); //NOI18N
    
    private HtmlNavigatorPanelUI ui;
    private FileObject fileObject;
    
    //actions
    private Action openAction;
    private Action highlightInBrowserAction;
    
    //static description (of the source element)
    private SourceDescription source;
    //dynamic description (of the webkit DOM element)
    private Description dom;
    
    //an openide Node representing the counterpart in the browsers DOM tree.
    //note: we need to hold the openide node itself, not just the webkit node
    //since most of the operations on PageModel requires to pass the Node instances
    //obtained from the same API.
//    private Node webKitNode;

    public HtmlElementNode(SourceDescription sourceDescription, HtmlNavigatorPanelUI ui, FileObject fileObject) {
        this(ui, fileObject);
        this.source = sourceDescription;
        getElementChildren().setStaticKeys(sourceDescription.getChildren(), true);
        
        openAction = new OpenAction((HtmlElementDescription)sourceDescription);
//        highlightInBrowserAction = new HighlightInBrowserAction(element, ui);
    }
    
    public HtmlElementNode(Description domDescription, HtmlNavigatorPanelUI ui, FileObject fileObject) {
        this(ui, fileObject);
        this.dom = domDescription;
        getElementChildren().setDynamicKeys(domDescription.getChildren(), true);
    }
    
    public HtmlElementNode(HtmlNavigatorPanelUI ui, FileObject fileObject) {
        super(new ElementChildren(ui, fileObject));

        this.ui = ui;
        this.fileObject = fileObject;


    }
    
    private State getState() {
        Description s = getSourceDescription();
        Description d = getDOMDescription();
        
        if(s != null && s != Description.EMPTY_SOURCE_DESCRIPTION) {
            if(d != null && d != Description.EMPTY_DOM_DESCRIPTION) {
                return State.SOURCE_AND_DOM;
            } else {
                return State.SOURCE;
            }
        } else {
            if(d != null && d != Description.EMPTY_DOM_DESCRIPTION) {
                return State.DOM;
            }
        }
        return State.NON;
    }
    
    /**
     * The update entry point for changes in the source code or dom tree.
     * 
     * Called by the HtmlSourceTask and PageModel listener.
     * 
     * Should be called only on the root node.
     * 
     */
    public void setDescription(Description description) {
        List<Node> nodesToExpand = new LinkedList<Node>();
        List<Node> nodesToExpandRec = new LinkedList<Node>();
        
        updateRecursively(description, nodesToExpand, nodesToExpandRec);
        
        ui.performExpansion(nodesToExpand, nodesToExpandRec);
        
    }
    
    private SourceDescription getSourceDescription() {
        return source;
    }
    
    private Description getDOMDescription() {
        return dom;
    }
    
    public Description getDescription() {
        switch(getState()) {
            case DOM:
                return getDOMDescription();
            case SOURCE:
            case SOURCE_AND_DOM:
                return getSourceDescription();
                
            default:
                return null; 
        }
    }
     
    public Description getDescription(int type) {
        switch(type) {
            case Description.DOM:
                return getDOMDescription();
            case Description.SOURCE:
                return getSourceDescription();
            default: 
                return null; //will not happen
        }
    }

//    private PageModel getPageModel() {
//        return ui.getPageModel();
//    }

//    private Node findWebKitNode() {
//        //check if the inspected fileobject matches our fileobject
//        FileObject inspectedFile = ui.getInspectedFileObject();
//        if (inspectedFile == null) {
//            return null;
//        }
//        if (!inspectedFile.equals(fileObject)) {
//            //foreign fileobject, someone likely switched the inspector do different file
//            return null;
//        }
//
//        PageModel pageModel = getPageModel();
//        if (pageModel == null) {
//            return null;
//        }
//        Node domDocumentNode = pageModel.getDocumentNode();
//        if (domDocumentNode == null) {
//            return null;
//        }
//        return Utils.findNode(domDocumentNode, source);
//    }

//    /**
//     * Refreshes all the data related to the webkit DOM node corresponding to
//     * this source element node
//     */
//    private synchronized void refreshWebkitCounterpartState(boolean forceChildrenKeysRefresh) {
//        ElementChildren children = getElementChildren();
//        WebKitNodeDescription currentWebKitNodeDescription = dom;
//        Node freshWebKitNode = findWebKitNode();
//        if (isConnected()) {
//            //the source node has already assigned a webkit counterpart
//            if (freshWebKitNode == null) {
//                //"disconnected"
//                webKitNode = null;
//                dom = null;
//
//                webkitNodeDescriptionChanged(currentWebKitNodeDescription, dom);
//                children.setDynamicKeys(Collections.<Description>emptyList(), forceChildrenKeysRefresh);
//            } else {
//                //still "connected" - refresh
//                if (freshWebKitNode != webKitNode) { //instances comparison
//                    webKitNode = freshWebKitNode;
//                    dom = WebKitNodeDescription.forNode(webKitNode);
//
//                    webkitNodeDescriptionChanged(currentWebKitNodeDescription, dom);
//                    children.setDynamicKeys(dom.getChildren(), forceChildrenKeysRefresh);
//                }
//            }
//        } else {
//            //the source node has no assigned webkit node counterpart
//            if (freshWebKitNode == null) {
//                //still "disconnected", no change
//            } else {
//                //now "connected" - initialize
//                webKitNode = freshWebKitNode;
//                dom = WebKitNodeDescription.forNode(webKitNode);
//
//                webkitNodeDescriptionChanged(currentWebKitNodeDescription, dom);
//                children.setDynamicKeys(dom.getChildren(), forceChildrenKeysRefresh);
//            }
//
//        }
//
//        LOGGER.log(Level.INFO, "{0}: refreshWebkitCounterpartState() called.", getDisplayName());
//    }
//
//    //recursively refreshes the DOM counterpart status
//    void refreshDOMStatus() {
//        refreshWebkitCounterpartState(true);
//
//        for (Node child : getChildren().getNodes(true)) {
//            ((HtmlElementNode) child).refreshDOMStatus();
//        }
//    }
//
//    private void webkitNodeDescriptionChanged(WebKitNodeDescription oldDescription, WebKitNodeDescription newDescription) {
//        //update the "connected" status 
//        fireDisplayNameChange(getHtmlDisplayName(oldDescription != null),
//                getHtmlDisplayName(newDescription != null));
//
//        //refresh the "connection" sensitive actions state
//        highlightInBrowserAction.setEnabled(newDescription != null);
//    }

    private ElementChildren getElementChildren() {
        return (ElementChildren) getChildren();
    }

    @Override
    public PropertySet[] getPropertySets() {
        switch(getState()) {
            case SOURCE:
            case SOURCE_AND_DOM:
                return getSourcePropertySets();
                
            //TODO DOM property sets
                
            default:
                return super.getPropertySets();
        }

    }
    
    private PropertySet[] getSourcePropertySets() {
        final HtmlElementDescription htmlD = (HtmlElementDescription)getSourceDescription();
        
        if (htmlD.getElementType() == ElementType.OPEN_TAG) {
            final AtomicReference<HtmlElementProperties.PropertiesPropertySet> pset_ref = new AtomicReference<HtmlElementProperties.PropertiesPropertySet>();
            try {
                htmlD.runTask(new HtmlElementDescription.Task() {
                    @Override
                    public void run(HtmlParserResult result) {
                        OpenTag openTag = (OpenTag) htmlD.resolve(result);
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
        switch(getState()) {
            case SOURCE:
            case SOURCE_AND_DOM:
                return source.getName();
            case DOM:
                return dom.getName();
            case NON:
                return "NON"; 
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String getHtmlDisplayName() {
        Description dd = getDOMDescription();
        Description sd = getSourceDescription();
        
        StringBuilder b = new StringBuilder();
        b.append(getDisplayName());
        
        Description d = sd != null ? sd : dd; //prefer source in favour of dom
        if(d != null) {
            String idVal = d.getAttributeValue("id");
            if (idVal != null) {
                b.append("&nbsp;");
                b.append("<font color=\"#777777\">");
                b.append("id=");
                b.append(idVal);
                b.append("</font>");
            }
            String classVal = d.getAttributeValue("class");
            if (classVal != null) {
                b.append("&nbsp;");
                b.append("<font color=\"#777777\">");
                b.append("class=");
                b.append(classVal);
                b.append("</font>");
            }
        }
        
        boolean debug = false;
        if(debug) {
            b.append(" [");
            if(getSourceDescription() != null) {
                b.append("SOURCE:");
                b.append("idx:");
                b.append(Diff.getIndexInParent(getSourceDescription()));
                b.append("hc:");
                b.append(Diff.hashCode(getSourceDescription()));
            }
            b.append(' ');
            if(getDOMDescription() != null) {
                b.append("DOM:");
                b.append("idx:");
                b.append(Diff.getIndexInParent(getDOMDescription()));
                b.append("hc:");
                b.append(Diff.hashCode(getDOMDescription()));
            }
            
            b.append("]");
        }

        return b.toString();
    }

    @Override
    public Image getIcon(int type) {
        switch(getState()) {
            case SOURCE:
                return SOURCE_ICON;
            case SOURCE_AND_DOM:
                return SOURCE_AND_DOM_ICON;
            case DOM:
                return DOM_ICON;
            default:
                return null;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context || getDescription().getName() == null) {
            return ui.getActions();
        } else {
            Action panelActions[] = ui.getActions();

            Action actions[] = new Action[4 + panelActions.length];
            actions[0] = openAction;
//            actions[1] = null;
//            actions[2] = highlightInBrowserAction;
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
    
    /**
     * Finds source Node spanning over the the given offset.
     * 
     * The returned node is always a descendant of this node.
     * 
     * @param offset - must be within the range of this node
     * @return found node or this node is no such descendant found.
     */
    public HtmlElementNode getNodeForOffset(int offset) {
        if (getSourceDescription().getFrom() > offset) {
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
                    SourceDescription sd = c.getSourceDescription();
                    if(sd == null) {
                        continue; //no source
                    }
                    long start = sd.getFrom();
                    if (start <= offset) {
                        long end = sd.getTo();
                        if (end >= offset) {
                            return c.getNodeForOffset(offset);
                        }
                    }
                }
            }
        }

        return this;
    }

    private void updateRecursively(Description newDescription, List<Node> nodesToExpand, List<Node> nodesToExpandRec) {
        LOGGER.log(Level.INFO, "{0}: entering updateRecursively()", getDisplayName());

        State currentState = getState();
        int descriptionType = newDescription.getType();
        
        //current children
        ElementChildren ch = getElementChildren();
        
        Description originalDescription;
        //current children descriptions
        switch(descriptionType) {
            case Description.SOURCE:
                originalDescription = getSourceDescription();
                this.source = (SourceDescription)newDescription;
                break;
                
            case Description.DOM:
                originalDescription = getDOMDescription();
                this.dom = newDescription;
                break;
                
            default:
                originalDescription = null; 
        }

        Collection<? extends Description> originalChildrenDescriptions = originalDescription != null ? originalDescription.getChildren() : Collections.<Description>emptyList();

        Node[] nodes = ch.getNodes(true);
        
        //map of primary description (source or dom) to the peer node
        HashMap<DescriptionSetWrapper, HtmlElementNode> oldD2node = new HashMap<DescriptionSetWrapper, HtmlElementNode>();
        for (Node node : nodes) {
            HtmlElementNode htmlElementNode = (HtmlElementNode)node;
            oldD2node.put(new DescriptionSetWrapper(htmlElementNode.getDescription()), htmlElementNode);
        }

        //get children from the new description
        Collection<? extends Description> newChildrenDescriptions = newDescription.getChildren();
        
        // Now refresh keys
        switch(newDescription.getType()) {
            case Description.SOURCE:
                Collection newSourceKeys = Diff.mergeOldAndNew(ch.staticKeys, newChildrenDescriptions, this);
                ch.setStaticKeys(newSourceKeys, false); //will set re-set the keys later
                break;
            case Description.DOM:
                Collection newDOMKeys = Diff.mergeOldAndNew(ch.dynamicKeys, newChildrenDescriptions, this);
                ch.setDynamicKeys(newDOMKeys, false); //will set re-set the keys later
                break;
        }

        //merge the source and dom keys
        Collection<? extends Description> newKeys = Diff.mergeSourceAndDOM(ch.staticKeys, ch.dynamicKeys, this);
        ch.resetKeys(newKeys);

        //update text & icon
        boolean stateChanged = currentState != getState();
        boolean descriptionChanged = originalDescription == null && newDescription != null || originalDescription.hashCode() != newDescription.hashCode();
        if(stateChanged) {
            //state (type) changed
            fireIconChange();
        }
        if(stateChanged || descriptionChanged) {
            fireDisplayNameChange(null, getDisplayName());
        }
        
        //follows the recursive update...
        
        // Reread nodes
        nodes = ch.getNodes(true);
        
        Collection<? extends Description> removedKeys = new HashSet<Description>(originalChildrenDescriptions);
        removedKeys.removeAll(newChildrenDescriptions);
        
        Collection<? extends Description> addedKeys = new HashSet<Description>(newChildrenDescriptions);
        addedKeys.removeAll(originalChildrenDescriptions);
        
        Collection<Description> changedKeys = new HashSet<Description>(originalChildrenDescriptions);
        changedKeys.retainAll(newChildrenDescriptions);
        
        addedKeys.removeAll(removedKeys);
        removedKeys.removeAll(addedKeys);
        
        for(Description addedKey : addedKeys) {
            System.out.println("+ added key   : " + addedKey);
        }
        
        //refresh nodes for removed keys
        for(Description removedKey : removedKeys) {
            System.out.println("- removed key : " + removedKey);
            DescriptionSetWrapper wrapper = new DescriptionSetWrapper(removedKey);
            Node n = oldD2node.get(wrapper);
            if(n != null) {
                ((HtmlElementNode)n).updateRecursively(Description.empty(descriptionType), nodesToExpand, nodesToExpandRec);
            }
        }
        
        for(Description changedKey : changedKeys) {
            System.out.println("* changed key : " + changedKey);
        }
        
        
        
        //refresh new keys' nodes
        for (Description descriptionChild : newChildrenDescriptions) {
            DescriptionSetWrapper wrapper = new DescriptionSetWrapper(descriptionChild);
            HtmlElementNode node = oldD2node.get(wrapper);
            if (node != null) {
                //the node already existed in the old node children
                if (!originalChildrenDescriptions.contains(descriptionChild)) {
                    nodesToExpand.add(node);
                }
                // update the node recursively
                node.updateRecursively(descriptionChild, nodesToExpand, nodesToExpandRec);
            } else {
                // a new node
                for (Node newNode : nodes) {
                    if (((HtmlElementNode) newNode).getDescription(descriptionType) == descriptionChild) {
                        //recursively expand the new nodes
                        nodesToExpandRec.add(newNode);
                        break;
                    }
                }
            }
        }
        
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    private static class ElementChildren extends Children.Keys<Description> {

        private HtmlNavigatorPanelUI ui;
        private FileObject fileObject;
        private Collection<? extends Description> staticKeys = Collections.emptyList();
        private Collection<? extends Description> dynamicKeys = Collections.emptyList();
        
        public ElementChildren(HtmlNavigatorPanelUI ui, FileObject fileObject) {
            this.ui = ui;
            this.fileObject = fileObject;
        }

        @Override
        protected Node[] createNodes(Description key) {
            HtmlElementNode newNode;
            switch (key.getType()) {
                case Description.SOURCE:
                    newNode = new HtmlElementNode((SourceDescription)key, ui, fileObject);
                    break;
                case Description.DOM:
                    newNode = new HtmlElementNode(key, ui, fileObject);
                    break;
                default:
                    return null;
            }
            return new Node[]{newNode};
        }

        void setStaticKeys(Collection<? extends Description> staticDescriptions, boolean resetKeys) {
            staticKeys = Collections.<Description>unmodifiableCollection(staticDescriptions);
            if(resetKeys) {
                resetKeys(staticDescriptions);
            }
        }

        void setDynamicKeys(Collection<? extends Description> dynamicDescriptions, boolean resetKeys) {
            dynamicKeys = Collections.<Description>unmodifiableCollection(dynamicDescriptions);
            if(resetKeys) {
                resetKeys(dynamicDescriptions);
            }
        }

        void resetKeys(Collection<? extends Description> keys) {
            setKeys(keys);
        }
    }
}
