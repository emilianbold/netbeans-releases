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
import javax.swing.Action;
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
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class HtmlNode extends AbstractNode {

    private static final Image ICON = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/html_element.png"); //NOI18N
    
    private HtmlElementDescription element;
    private HtmlNavigatorPanelUI ui;
    private FileObject fileObject; // For the root description

    private Action openAction;
    private Action highlightInBrowserAction;
    
    private Node domCounterpart;

    public HtmlNode(HtmlElementDescription element, HtmlNavigatorPanelUI ui, FileObject fileObject) {
        super(element.isLeaf()
                ? Children.LEAF
                : new ElementChildren(element.getChildren(), ui, fileObject));
        this.element = element;
        this.ui = ui;
        this.fileObject = fileObject;
        
        openAction = new OpenAction(element);
        highlightInBrowserAction = new HighlightInBrowserAction(element, ui);
        
        if(ui.getPageModel() != null) {
            domCounterpart = Utils.findNode(ui.getPageModel().getDocumentNode(), element);
        }
        highlightInBrowserAction.setEnabled(domCounterpart != null);
    }
    
    //recursively refreshes the DOM counterpart status
    void refreshDOMStatus() {
        Node match = Utils.findNode(ui.getPageModel().getDocumentNode(), element);
        if(domCounterpart == null && match != null || domCounterpart != null && match == null) {
            String oldHtmlDisplayName = getHtmlDisplayName();
            domCounterpart = match;
            fireDisplayNameChange(oldHtmlDisplayName, getHtmlDisplayName());
            highlightInBrowserAction.setEnabled(domCounterpart != null);
        }
        
        for(Node child : getChildren().getNodes()) {
            ((HtmlNode)child).refreshDOMStatus();
        }
    }
    
    @Override
    public PropertySet[] getPropertySets() {
        if(getElement().getType() == ElementType.OPEN_TAG) {
            final AtomicReference<HtmlElementProperties.PropertiesPropertySet> pset_ref = new AtomicReference<HtmlElementProperties.PropertiesPropertySet>();
            try {
                getElement().runTask(new HtmlElementDescription.Task() {

                    @Override
                    public void run(HtmlParserResult result) {
                        OpenTag openTag = (OpenTag)getElement().resolve(result);
                        HtmlElementProperties.PropertiesPropertySet pset = new HtmlElementProperties.PropertiesPropertySet(result, openTag);
                        pset_ref.set(pset);
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            HtmlElementProperties.PropertiesPropertySet pset = pset_ref.get();
            if(pset == null) {
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
        StringBuilder b = new StringBuilder();
        if(domCounterpart != null) {
            b.append("<b>");
        }
        b.append(getDisplayName());
        if(domCounterpart != null) {
            b.append("</b>");
        }
        if(getElement().getIdAttr() != null) {
            b.append("&nbsp;");
            b.append("<font color=\"#777777\">");
            b.append("id=");
            b.append(getElement().getIdAttr());
            b.append("</font>");
        }
        if(getElement().getClassAttr() != null) {
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

    /**
     * Refreshes the Node recursively. Only initiates the refresh; the refresh
     * itself may happen asynchronously.
     */
    public void refreshRecursively() {
        List<Node> toExpand = new ArrayList<Node>();
        refreshRecursively(Collections.singleton(this), toExpand);
        ui.performExpansion(toExpand, Collections.<Node>emptyList());
    }

    private void refreshRecursively(Collection<HtmlNode> toDo, final Collection<Node> toExpand) {
        for (HtmlNode elnod : toDo) {
            final Children ch = elnod.getChildren();
            if (ch instanceof ElementChildren) {
                ((ElementChildren) ch).resetKeys(elnod.element.getChildren());

                Collection<HtmlNode> children = (Collection<HtmlNode>) (List) Arrays.asList(ch.getNodes());
                toExpand.addAll(children);
                refreshRecursively(children, toExpand);
            }
        }
    }

    public HtmlNode getNodeForOffset(int offset) {
        if (getElement().getFrom() > offset) {
            return null;
        }

        // Inefficient linear search because the children may not be
        // ordered according to the source
        Children ch = getChildren();
        if (ch instanceof ElementChildren) {
            Node[] children = ch.getNodes();
            for (int i = 0; i < children.length; i++) {
                HtmlNode c = (HtmlNode) children[i];
                long start = c.getElement().getFrom();
                if (start <= offset) {
                    long end = c.getElement().getTo();
                    if (end >= offset) {
                        return c.getNodeForOffset(offset);
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
        Children ch = getChildren();

        //If a node that was a LEAF now has children the child type has to be changed from Children.LEAF
        //to ElementChildren to be able to hold the new child data
        if (!(ch instanceof ElementChildren) && !newDescription.isLeaf()) {
            ch = new ElementChildren(Collections.<HtmlElementDescription>emptyList(), ui, fileObject);
            setChildren(ch);
        }

        if (ch instanceof ElementChildren) {
            HashSet<HtmlElementDescription> oldSubs = new HashSet<HtmlElementDescription>(element.getChildren());


            // Create a hashtable which maps StructureItem to node.
            // We will then identify the nodes by the description. The trick is 
            // that the new and old description are equal and have the same hashcode
            Node[] nodes = ch.getNodes(true);
            HashMap<HtmlElementDescription, HtmlNode> oldD2node = new HashMap<HtmlElementDescription, HtmlNode>();
            for (Node node : nodes) {
                oldD2node.put(((HtmlNode) node).element, (HtmlNode) node);
            }

            List<HtmlElementDescription> newDescriptionChildren = newDescription.getChildren();
            // Now refresh keys
            ((ElementChildren) ch).resetKeys(newDescriptionChildren);


            // Reread nodes
            nodes = ch.getNodes(true);

            boolean alreadyExpanded = false;

            for (HtmlElementDescription newSub : newDescriptionChildren ) {
                HtmlNode node = oldD2node.get(newSub);
                if (node != null) { // filtered out
                    if (!oldSubs.contains(newSub)) {
                        nodesToExpand.add(node);
                    }
                    node.updateRecursively(newSub, nodesToExpand, nodesToExpandRec); // update the node recursively
                } else { // a new node
                    if (!alreadyExpanded) {
                        alreadyExpanded = true;
//                        if (ui.isExpandedByDefault(this)) {
//                            nodesToExpand.add(this);
//                        }
                    }
                    for (Node newNode : nodes) {
                        if (newNode instanceof HtmlNode && ((HtmlNode) newNode).getElement() == newSub) {
                            nodesToExpandRec.add(newNode);
                            break;
                        }
                    }
                }
            }
        }

        HtmlElementDescription oldDescription = element; // Remember old description        
        element = newDescription; // set new descrioption to the new node
        String oldHtml = oldDescription.getName();
        String descHtml = element.getName();
        
        if (oldHtml != null && !oldHtml.equals(descHtml)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldHtml, descHtml);
        }
//        if (oldDescription.getModifiers() != null && !oldDescription.getModifiers().equals(newDescription.getModifiers())) {
//            fireIconChange();
//            fireOpenedIconChange();
//        }
    }

    public HtmlElementDescription getElement() {
        return element;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    private static final class ElementChildren extends Children.Keys<HtmlElementDescription> {

        private HtmlNavigatorPanelUI ui;
        private FileObject fileObject;

        public ElementChildren(Collection<HtmlElementDescription> descriptions, HtmlNavigatorPanelUI ui, FileObject fileObject) {
            resetKeys(descriptions);
            this.ui = ui;
            this.fileObject = fileObject;
        }

        @Override
        protected Node[] createNodes(HtmlElementDescription key) {
            return new Node[]{new HtmlNode(key, ui, fileObject)};
        }

        void resetKeys(Collection<HtmlElementDescription> descriptions) {
            setKeys(descriptions);
        }
    }

    
}
