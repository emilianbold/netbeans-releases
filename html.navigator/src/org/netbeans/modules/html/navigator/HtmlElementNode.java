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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.html.editor.api.actions.DeleteElementAction;
import org.netbeans.modules.html.editor.api.actions.ModifyElementRulesAction;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.navigator.actions.HighlightInBrowserAction;
import org.netbeans.modules.html.navigator.actions.OpenAction;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Node representing a source or dom element or both.
 * 
 * @author marekfukala
 */
public class HtmlElementNode extends AbstractNode {
    
    private enum State {
        SOURCE, SOURCE_AND_DOM, DOM, NON;
    }

    private static final Logger LOGGER = Logger.getLogger(HtmlElementNode.class.getSimpleName());
    
    private static final Image SOURCE_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element_bw.png"); //NOI18N
    private static final Image SOURCE_AND_DOM_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element.png"); //NOI18N
    private static final Image DOM_ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element_live.png"); //NOI18N
    /** Lookup path with context actions. */
    private static final String DOM_ACTIONS_PATH = "Navigation/DOM/Actions"; // NOI18N
    
    private HtmlNavigatorPanelUI ui;
    private FileObject fileObject;
    
    //actions
    private OpenAction openAction;
    private HighlightInBrowserAction highlightInBrowserAction;
    private ModifyElementRulesAction editRulesAction;
    private DeleteElementAction deleteElementAction;
    
    //static description (of the source element)
    private SourceDescription source;
    //dynamic description (of the webkit DOM element)
    private Description dom;
    private final NodeLookupProvider lookupProvider;
    
    
    //an openide Node representing the counterpart in the browsers DOM tree.
    //note: we need to hold the openide node itself, not just the webkit node
    //since most of the operations on PageModel requires to pass the Node instances
    //obtained from the same API.
//    private Node webKitNode;

    public HtmlElementNode(SourceDescription sourceDescription, HtmlNavigatorPanelUI ui, FileObject fileObject) {
        this(ui, fileObject);
        this.source = sourceDescription;
        getElementChildren().setStaticKeys(sourceDescription.getChildren(), true);
        
        deleteElementAction = new DeleteElementAction(fileObject, sourceDescription.getElementPath());
        editRulesAction = new ModifyElementRulesAction(fileObject, sourceDescription.getElementPath());
    }
    
    public HtmlElementNode(Description domDescription, HtmlNavigatorPanelUI ui, FileObject fileObject) {
        this(ui, fileObject);
        this.dom = domDescription;
        updateNodeLookup(domDescription);
        
        getElementChildren().setDynamicKeys(domDescription.getChildren(), true);
        
        deleteElementAction = new DeleteElementAction(fileObject, null);
        editRulesAction = new ModifyElementRulesAction(fileObject, domDescription.getElementPath());
    }
    
    private HtmlElementNode(HtmlNavigatorPanelUI ui, FileObject fileObject) {
        this(ui, fileObject, createLookupProvider());
    }
    
    private HtmlElementNode(HtmlNavigatorPanelUI ui, FileObject fileObject, NodeLookupProvider lookupProvider) {
        super(new ElementChildren(ui, fileObject), Lookups.proxy(lookupProvider));
        this.ui = ui;
        this.fileObject = fileObject;

        this.lookupProvider = lookupProvider;
        updateNodeLookup(null);
        
        
        openAction = new OpenAction(this);
        highlightInBrowserAction = new HighlightInBrowserAction(this, ui);
    }
    
    private static NodeLookupProvider createLookupProvider() {
        return new NodeLookupProvider(Lookups.fixed());
    }
    
    private void updateNodeLookup(Description newDescription) {
        org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode = null;
        if (newDescription instanceof WebKitNodeDescription) {
            domNode = ((WebKitNodeDescription) newDescription).getOONNode().getLookup().
                        lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
        }
        if (domNode != null) {
            lookupProvider.setLookup(Lookups.fixed(this, fileObject, domNode));
        } else {
            lookupProvider.setLookup(Lookups.fixed(this, fileObject));
        }
    }
    
    public Node getDOMNode() {
        Description domDescription = getDOMDescription();
        if (domDescription instanceof WebKitNodeDescription) {
            return ((WebKitNodeDescription) domDescription).getOONNode();
        }
        return null;
    }
    
    private State getState() {
        Description s = getSourceDescription();
        Description d = getDOMDescription();
        
        if(s != null && s != Description.empty(Description.SOURCE)) {
            if(d != null && d != Description.empty(Description.DOM)) {
                return State.SOURCE_AND_DOM;
            } else {
                return State.SOURCE;
            }
        } else {
            if(d != null && d != Description.empty(Description.DOM)) {
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
    
    /**
     * Returns source element description for this node.
     */
    private SourceDescription getSourceDescription() {
        return source;
    }
    
    /**
     * Returns DOM element description for this node.
     */
    private Description getDOMDescription() {
        return dom;
    }
    
    /**
     * Gets primary description of this node.
     */
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
     
    /**
     * Gets {@link Description} of the given type.
     */
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
                        if(openTag != null) {
                            HtmlElementProperties.PropertiesPropertySet pset = new HtmlElementProperties.PropertiesPropertySet(result, openTag);
                            pset_ref.set(pset);
                        }
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
    public boolean canRename() {
        return false;
    }

    @Override
    public String getName() {
        return getDisplayName();
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
                b.append(Diff.getIndexInParent(getSourceDescription(), false));
                b.append("hc:");
                b.append(Diff.hashCode(getSourceDescription(), false));
            }
            b.append(' ');
            if(getDOMDescription() != null) {
                b.append("DOM:");
                b.append("idx:");
                b.append(Diff.getIndexInParent(getDOMDescription(), false));
                b.append("hc:");
                b.append(Diff.hashCode(getDOMDescription(), false));
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
        Collection<Action> actions = new ArrayList<Action>();
        
        if (context || getDescription().getName() == null) {
            actions.addAll(Arrays.asList(ui.getActions()));
        } else {
            
            actions.add(openAction);
            actions.add(null);
            actions.add(editRulesAction);
            actions.add(deleteElementAction);
            actions.add(null);
            actions.add(highlightInBrowserAction);
            actions.add(null);
            actions.addAll(Arrays.asList(ui.getActions()));
        }
        for (Action action : org.openide.util.Utilities.actionsForPath(DOM_ACTIONS_PATH)) {
            if (action instanceof ContextAwareAction) {
                actions.add(((ContextAwareAction) action).createContextAwareInstance(getLookup()));
            }
        }
        return actions.toArray(new Action[]{});        
    }

    @Override
    public Action getPreferredAction() {
        return openAction;
    }
    
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

    /**
     * Updates this node descriptions according to the given {@link Description}.
     * 
     * @param newDescription the new description to be set.
     * @param nodesToExpand
     * @param nodesToExpandRec 
     */
    private void updateRecursively(Description newDescription, List<Node> nodesToExpand, List<Node> nodesToExpandRec) {
        LOGGER.log(Level.FINE, "{0}: entering updateRecursively()", getDisplayName());

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
                updateNodeLookup(newDescription);
                break;
                
            default:
                originalDescription = null; 
        }

        //creates a map of primary description (source or dom) to the peer node
        Node[] nodes = ch.getNodes(true);
        HashMap<DescriptionSetWrapper, HtmlElementNode> oldD2node = new HashMap<DescriptionSetWrapper, HtmlElementNode>();
        for (Node node : nodes) {
            HtmlElementNode htmlElementNode = (HtmlElementNode)node;
            oldD2node.put(new DescriptionSetWrapper(htmlElementNode.getDescription()), htmlElementNode);
        }

        // Now set the appropriate children keys
        switch(newDescription.getType()) {
            case Description.SOURCE:
                Collection<? extends Description> newSourceKeys = Diff.mergeOldAndNew(ch.staticKeys, source.getChildren(), this);
                ch.setStaticKeys(newSourceKeys, false); //will re-set the keys later
                break;
            case Description.DOM:
                Collection<? extends Description> newDOMKeys = Diff.mergeOldAndNew(ch.dynamicKeys, dom.getChildren(), this);
                ch.setDynamicKeys(newDOMKeys, false); //will re-set the keys later
                break;
        }

        //merge the source and dom keys
        Collection<? extends Description> newKeys = Diff.mergeSourceAndDOM(ch.staticKeys, ch.dynamicKeys, this);
        
        //setting the children keys really does the merge of the old and new state
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
        
        // Reread nodes
        nodes = ch.getNodes(true);
        
        //Refresh nodes for removed keys:
        //
        //Why to refresh child nodes if their keys were removed? 
        //Since there are two keys - source and DOM.
        //If for examole the DOM key is removed, the node still exists since there's the source key, 
        //but the node needs to be properly updated to reflect such change.
        Collection<? extends Description> newChildrenDescriptions = newDescription.getChildren();
        Collection<? extends Description> originalChildrenDescriptions = originalDescription != null ? originalDescription.getChildren() : Collections.<Description>emptyList();
        Collection<? extends Description> removedKeys = new HashSet<Description>(originalChildrenDescriptions);
        removedKeys.removeAll(newChildrenDescriptions);
        for(Description removedKey : removedKeys) {
            DescriptionSetWrapper wrapper = new DescriptionSetWrapper(removedKey);
            Node n = oldD2node.get(wrapper);
            if(n != null) {
                ((HtmlElementNode)n).updateRecursively(Description.empty(descriptionType), nodesToExpand, nodesToExpandRec);
            }
        }
        
        //recursively refresh nodes from the previous generation (those which were not added by this change)
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
    
    private static class NodeLookupProvider implements Lookup.Provider {
        
        private Lookup lookup;
        
        NodeLookupProvider(Lookup lookup) {
            this.lookup = lookup;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }
        
        void setLookup(Lookup lookup) {
            this.lookup = lookup;
        }
        
    }    

    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * Children.Keys subclass which allows to reset its keys.
     */
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
    
    //some unused code, still may be reused?!?
    
    //    private PageModel getPageModel() {
//        return ui.getPageModel();
//    }
//
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
//        return Utils.findNode(domDocumentNode, getSourceDescription());
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
    
    
}
