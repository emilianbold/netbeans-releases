/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.openide.actions.OpenLocalExplorerAction;

import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.nodes.FilterNode;

import org.netbeans.spi.project.ui.support.LogicalViews;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.openide.util.Utilities;

import java.awt.Image;
import java.util.Collections;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.Actions;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.Util;

import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 * Logical view of a freeform project.
 * @author Jesse Glick
 */
public final class View implements LogicalViewProvider {
    
    private final FreeformProject project;
    
    public View(FreeformProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    public Node findPath(Node root, Object target) {
        // XXX
        return null;
    }
    
    private static final class RootChildren extends Children.Keys {
        
        private final FreeformProject p;
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        protected void addNotify() {
            super.addNotify();
            Element genldata = p.helper().getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
                setKeys(Util.findSubElements(itemsEl));
            } else {
                setKeys(Collections.EMPTY_SET);
            }
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Element itemEl = (Element)key;
            Element locationEl = Util.findElement(itemEl, "location", FreeformProjectType.NS_GENERAL); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            FileObject file = p.helper().resolveFileObject(locationEval);
            if (file == null) {
                // Not there... skip this node.
                return null;
            }
            String label;
            Element labelEl = Util.findElement(itemEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
            if (labelEl != null) {
                label = Util.findText(labelEl);
            } else {
                label = file.getNameExt();
            }
            Node base;
            boolean pkgUi;
            DataObject fileDO;
            try {
                fileDO = DataObject.find(file);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                if (style.equals("tree")) { // NOI18N
                    // XXX filter by VisibilityQuery
                    pkgUi = false;
                    base = fileDO.getNodeDelegate();
                } else {
                    assert style.equals("packages") : style;
                    pkgUi = true;
                    base = new AbstractNode(PackageView.createPackageView(file), Lookups.singleton(fileDO));
                }
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl;
                pkgUi = false;
                base = fileDO.getNodeDelegate();
            }
            return new Node[] {new ViewItemNode(base, location, label, pkgUi)};
        }
        
    }
    
    private static final class RootNode extends AbstractNode {
        
        private final FreeformProject p;
        
        public RootNode(FreeformProject p) {
            super(new RootChildren(p), Lookups.singleton(p));
            this.p = p;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(p).getName();
        }
        
        public String getDisplayName() {
            return ProjectUtils.getInformation(p).getDisplayName();
        }
        
        public String getShortDescription() {
            // XXX I18N
            return "Freeform project in " + FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/AntIcon.gif", true); // NOI18N
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public Action[] getActions(boolean context) {
            return Actions.createContextMenu(p);
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
    }
    
    private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        
        private final String displayName;
        
        private final boolean pkgUi;
        
        public ViewItemNode(Node orig, String name, String displayName, boolean pkgUi) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
            this.pkgUi = pkgUi;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        public Action[] getActions(boolean context) {
            if (pkgUi) {
                return new Action[] {
                    SystemAction.get(OpenLocalExplorerAction.class),
                    SystemAction.get(FindAction.class),
                    null,
                    LogicalViews.newFileAction(),
                };
            } else {
                return super.getActions(context);
            }
        }
        
        public Image getIcon(int type) {
            if (pkgUi) {
                return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/packageRoot.gif", true); // NOI18N
            } else {
                return super.getIcon(type);
            }
        }
        
        public Image getOpenedIcon(int type) {
            if (pkgUi) {
                return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/packageRootOpen.gif", true); // NOI18N
            } else {
                return super.getOpenedIcon(type);
            }
        }
        
    }
    
}
