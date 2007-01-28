/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    /** Creates a new instance of OutlineManagerListener */
    public OutlineManagerListener() {
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] selectedNodes = (Node[])evt.getNewValue();

            Set<DesignContext> contexts = getDesignContextsForNodes(selectedNodes);

            if (contexts.isEmpty()) {
                return;
            }

            Set tcs = TopComponent.getRegistry().getOpened();
            for (Iterator it = tcs.iterator(); it.hasNext(); ) {
                TopComponent tc = (TopComponent)it.next();

                if (!isMultiViewTopComponent(tc)) {
                    continue;
                }

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
        return "org.netbeans.modules.visualweb.designer.DesignerTopComp".equals(tc.getClass().getName()); // NOI18N
    }

    // XXX NB #49507 See below.
    private static boolean isMultiViewTopComponent(TopComponent tc) {
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
