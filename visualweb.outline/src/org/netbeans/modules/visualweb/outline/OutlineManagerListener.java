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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.visualweb.outline;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Listener which listens on outline panel explorer manager's node selection
 * changes, and notifies corresponding designer window component accordingly.
 *
 * @author Peter Zavadsky
 */
class OutlineManagerListener implements PropertyChangeListener {

    
    private final OutlinePanel outlinePanel;
    
    
    /** Creates a new instance of OutlineManagerListener */
    public OutlineManagerListener(OutlinePanel outlinePanel) {
        this.outlinePanel = outlinePanel;
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] selectedNodes = (Node[])evt.getNewValue();
            
            Set<DesignContext> contexts = getDesignContextsForNodes(selectedNodes);

            if (contexts.isEmpty()) {
                // XXX #126818 The node selection might not have the context or be empty,
                // try to get the contexts from the first child under root (which is the page bean).
                Node[] nodes = outlinePanel.getExplorerManager().getRootContext().getChildren().getNodes();
                if (nodes != null && nodes.length > 0) {
                    contexts = getDesignContextsForNodes(new Node[] {nodes[0]});
                }
            }

            if (contexts.isEmpty()) {
                return;
            }
            
            Set tcs = TopComponent.getRegistry().getOpened();
            for (Iterator it = tcs.iterator(); it.hasNext(); ) {
                TopComponent tc = (TopComponent)it.next();

                if (!isMultiViewTopComponent(tc)) {
                    continue;
                }

                // XXX This doesn't work, one can't get to the component from the perspective, nor set the activated nodes.
                // TODO Find a better solution (some connection to designer/jsf?).
                // Maybe one need to listen on the changes in the navigator (with outline?), there doesn't seem to be any support for that.
//                MultiViewHandler multiViewHandler = MultiViews.findMultiViewHandler(tc);
//                if (multiViewHandler == null) {
//                    continue;
//                }
//                
//                MultiViewPerspective multiViewPerspective = multiViewHandler.getSelectedPerspective();

                // XXX NB #73301 see below.
                TopComponent elementTC = getSelectedMultiView(tc);

                if (!isDesignerTopComponent(elementTC)) {
                    continue;
                }

                Set<DesignContext> contextsFromTC = getDesignContextsForNodes(elementTC.getActivatedNodes());

                // XXX Revise: Shouldn't we validate the TC has only one context, to be sure it is designer?
                if (contexts.containsAll(contextsFromTC)) {
                    // XXX Ugly hack to set activated nodes of the designer,
                    // now when outline provides the activated nodes, there would be also another solution
                    // but what for the case when we will be inside NB outline, which doesn't provide the activated nodes.

                    // XXX NB #73301 Direct call on multiview component doesn't work.
                    elementTC.setActivatedNodes(selectedNodes);

//                    // XXX Heavy hack to temp activate designer in order to cause change in activated nodes,
//                    // so the properties window gets notified.
//                    TopComponent activeTC = TopComponent.getRegistry().getActivated();
//                    if (activeTC != null && activeTC != tc) {
//                        tc.requestActive();
//                        activeTC.requestActive();
//                    }

                    // #6338212 There could be cloned the designer multiview.
//                    break;
                }
            }
        }
    }

    // XXX Revise, it might be better to work against the main context.
    private static Set<DesignContext> getDesignContextsForNodes(Node[] nodes) {
        if (nodes == null) {
            return Collections.emptySet();
        }

        Set<DesignContext> contexts = new HashSet<DesignContext>();
        for (Node node : nodes) {
            if (node == null) {
                continue;
            }
            
            DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);

            if (designBean == null) {
                continue;
            }
            
            DesignContext context = designBean.getDesignContext();
            if (context != null) {
                contexts.add(context);
            }
        }
        
        return contexts;
    }
    

    // XXX Hack methods, copied from toolbox, due to missing NB API,
    // TODO crate some openide extension module which will group these
    // 'extensions'. New name is needed, the 'extension' is used for customizing
    // the NB modules already. Maybe we need a name switch customize <-> extension.
    
    // XXX Is there a better way, like checking the ID?
    private static boolean isDesignerTopComponent(TopComponent tc) {
        if(tc == null) {
            return false;
        }
        // XXX Is it better to use client property or other mechanism
        // or to put something in its context?
        // XXX This is definitelly not correct way.
        return "org.netbeans.modules.visualweb.designer.jsf.ui.JsfTopComponent".equals(tc.getClass().getName()); // NOI18N
    }

    // XXX NB #49507 See below.
    private static boolean isMultiViewTopComponent(TopComponent tc) {
        // XXX This string comparison is bad too.
        return tc != null
            && "org.netbeans.core.multiview.MultiViewCloneableTopComponent".equals(tc.getClass().getName()); // NOI18N
    }
    
    // XXX NB #49507 Ugly hack, demand NB to provide API!
    private static TopComponent getSelectedMultiView(TopComponent tc) {
        TopComponent[] containedTCs = findDescendantsOfTopComponent(tc);
        for (TopComponent containedTC : containedTCs) {
            if(containedTC.isVisible()) { // XXX Means is selected in that multiview (terrible hack)
                return containedTC;
            }
        }
        return null;
    }
    
    private static TopComponent[] findDescendantsOfTopComponent(Container parent) {
        List<TopComponent> list = new ArrayList<TopComponent>();
        Component[] children = parent.getComponents();
        for (Component child : children) {
            if(child instanceof TopComponent) {
                list.add((TopComponent)child);
                continue;
            }
            if(child instanceof Container) {
                list.addAll(java.util.Arrays.asList(findDescendantsOfTopComponent((Container)child)));
            }
        }
        
        return list.toArray(new TopComponent[0]);
    }
}
