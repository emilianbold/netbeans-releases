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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * NodePopupMenuProvider.java
 *
 * Created on February 2, 2007, 6:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.Pin;

/**
 *
 * @author joelle
 */
public class PageFlowPopupProvider implements PopupMenuProvider {

    /**
     * Creates a Popup for any right click on Page Flow Editor
     * @param graphScene The related PageFlow Scene.
     * @param tc
     */
    public PageFlowPopupProvider(PageFlowScene graphScene, TopComponent tc) {
        setTc(tc);
        setGraphScene( graphScene );
        initialize();
    }
    
    public void destroy() {
        setTc(null);
        setGraphScene(null);
    }
    // <actions from layers>
    private static final String PATH_PAGEFLOW_NODE_ACTIONS = "PageFlowEditor/PopupActions/PageFlowSceneElement"; // NOI18N
    private static final String PATH_PAGEFLOW_SCENE_ACTIONS = "PageFlowEditor/PopupActions/Scene"; // NOI18N

    private void initialize() {
        InstanceContent ic = new InstanceContent();
        ic.add(getGraphScene());
    }

    /* Point and widget are actually not needed. */
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        Object obj = getGraphScene().getHoveredObject();



        if (obj != null) {

            Set elements = getGraphScene().getSelectedObjects();            
            if( !elements.contains(obj)) {
                Set<Object> set = new HashSet<Object>();
                set.add(obj);
                getGraphScene().setSelectedObjects(set);
            }

//          Node nodes[] = tc.getActivatedNodes();
            if (obj instanceof Page) {
                Page pageNode = (Page) obj;
                Action[] actions;
                Action[] pageNodeActions = pageNode.getActions(true);
                Action[] fileSystemActions = SystemFileSystemSupport.getActions(PATH_PAGEFLOW_NODE_ACTIONS);
                if (pageNodeActions == null || pageNodeActions.length == 0) {
                    actions = fileSystemActions;
                } else if (fileSystemActions == null || fileSystemActions.length == 0) {
                    actions = pageNodeActions;
                } else {
                    actions = new Action[pageNodeActions.length + fileSystemActions.length];
                    System.arraycopy(fileSystemActions, 0, actions, 0, fileSystemActions.length);
                    System.arraycopy(pageNodeActions, 0, actions, fileSystemActions.length, pageNodeActions.length);
                }
                return Utilities.actionsToPopup(actions, getTc().getLookup());
            } else if (obj instanceof Pin) {
                Pin pinNode = (Pin) obj;
                Action[] actions = pinNode.getActions();
                return Utilities.actionsToPopup(actions, getTc().getLookup());
            }
            return Utilities.actionsToPopup(SystemFileSystemSupport.getActions(PATH_PAGEFLOW_NODE_ACTIONS), getTc().getLookup());
        }
        return Utilities.actionsToPopup(SystemFileSystemSupport.getActions(PATH_PAGEFLOW_SCENE_ACTIONS), getTc().getLookup());
    }
    /** Weak reference to the lookup. */
    private WeakReference<Lookup> lookupWRef = new WeakReference<Lookup>(null);

    /** Adds <code>NavigatorLookupHint</code> into the original lookup,
     * for the navigator. */
    private Lookup getLookup() {
        Lookup lookup = lookupWRef.get();

        if (lookup == null) {
            InstanceContent ic = new InstanceContent();
            //                ic.add(firstObject);
            ic.add(getGraphScene());
            lookup = new AbstractLookup(ic);
            lookupWRef = new WeakReference<Lookup>(lookup);
        }

        return lookup;
    }

    private WeakReference<PageFlowScene> refPageFlowScene;
    public PageFlowScene getGraphScene() {
        PageFlowScene scene = null;
        if( refPageFlowScene != null ){
            scene = refPageFlowScene.get();
        }
        return scene;
    }

    public void setGraphScene(PageFlowScene graphScene) {
        refPageFlowScene = new WeakReference<PageFlowScene>(graphScene);
    }

    private WeakReference<TopComponent> refTC;
    public TopComponent getTc() {
        TopComponent tc = null;
        if( refTC != null ){
            tc = refTC.get();
        }
        return tc;
    }

    public void setTc(TopComponent tc) {
        refTC = new WeakReference<TopComponent>(tc);
    }
}
