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
 */

package org.netbeans.modules.ruby.railsprojects.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.ruby.railsprojects.GenerateAction;
import org.netbeans.modules.ruby.railsprojects.Generator;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// XXX need unit test

/**
 * (Copied from Java Project Source (org.netbeans.spi.gsfpath.project.support.ui)
 * Displays a package root in a tree.
 * @see "#42151"
 * @author Jesse Glick
 */
public final class TreeRootNode extends FilterNode implements PropertyChangeListener {
    
    private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
    
    private final SourceGroup g;
    private final Generator generator;
    
    public TreeRootNode(SourceGroup g, Generator generator) {
        this(DataFolder.findFolder(g.getRootFolder()), g, generator);
    }
    
    private TreeRootNode(DataFolder folder, SourceGroup g, Generator generator) {
        this (new FilterNode (folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER)), g, generator);
    }
    
    private TreeRootNode (Node originalNode, SourceGroup g, Generator generator) {
        super(originalNode, new PackageFilterChildren(originalNode),
            new ProxyLookup(
                originalNode.getLookup(),
                Lookups.singleton(generator),
                Lookups.singleton(new PathFinder(g))
                // no need for explicit search info
            ));
        this.g = g;
        this.generator = generator;
        g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
    }

    static Image PACKAGE_BADGE = Utilities.loadImage( "org/netbeans/modules/ruby/railsprojects/ui/packageBadge.gif" ); // NOI18N
    
    /** Copied from PackageRootNode with modifications. */
    private Image computeIcon(boolean opened, int type) {
        Icon icon = g.getIcon(opened);
        if (icon == null) {
            Image image = opened ? super.getOpenedIcon(type) : super.getIcon(type);
            return Utilities.mergeImages(image, /*PackageRootNode.*/PACKAGE_BADGE, 7, 7);
        } else {
            return Utilities.icon2Image(icon);
        }
    }
    
    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    @Override
    public String getName() {
        return g.getName();
    }

    @Override
    public String getDisplayName() {
        return g.getDisplayName();
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        // XXX handle SourceGroup.rootFolder change too
        fireNameChange(null, null);
        fireDisplayNameChange(null, null);
        fireIconChange();
        fireOpenedIconChange();
    }
    
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] result = initActions();
        return result;
    }


    @Override
    public Action getPreferredAction() {
        return SystemAction.get(GenerateAction.class);
    }

    // From ActionFilterNode
    private Action[] actionCache;
    
    private Action[] initActions () {
        if (actionCache == null) {
            Action[] existing = super.getActions();
            Action[] additional;
            if (generator == Generator.NONE) {
                additional = new Action[] { CommonProjectActions.newFileAction(), null }; // null: separator
            } else {
                additional = new Action[] { SystemAction.get(GenerateAction.class), null,
                  CommonProjectActions.newFileAction(), null }; // null: separator
            }
            if (existing != null && existing.length > 0) {
                actionCache = new Action[existing.length+additional.length];
                System.arraycopy(additional, 0, actionCache, 0, additional.length);
                System.arraycopy(existing, 0, actionCache, additional.length, existing.length);
            } else {
                actionCache = additional;
            }
        }
        return actionCache;
    }
    
    

    /** Copied from PhysicalView and PackageRootNode. */
    public static final class PathFinder {
        
        private final SourceGroup g;
        
        PathFinder(SourceGroup g) {
            this.g = g;
        }
        
        public Node findPath(Node rootNode, Object o) {
            FileObject fo;
            if (o instanceof FileObject) {
                fo = (FileObject) o;
            } else if (o instanceof DataObject) {
                fo = ((DataObject) o).getPrimaryFile();
            } else {
                return null;
            }
            FileObject groupRoot = g.getRootFolder();
            if (FileUtil.isParentOf(groupRoot, fo) /* && group.contains(fo) */) {
                FileObject folder = fo.isFolder() ? fo : fo.getParent();
                String relPath = FileUtil.getRelativePath(groupRoot, folder);
                List<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
                while (strtok.hasMoreTokens()) {
                    String token = strtok.nextToken();
                   path.add(token);
                }
                try {
                    Node folderNode =  folder.equals(groupRoot) ? rootNode : NodeOp.findPath(rootNode, Collections.enumeration(path));
                    if (fo.isFolder()) {
                        return folderNode;
                    } else {
                        Node[] childs = folderNode.getChildren().getNodes(true);
                        for (int i = 0; i < childs.length; i++) {
                           DataObject dobj = childs[i].getLookup().lookup(DataObject.class);
                           if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                               return childs[i];
                           }
                        }
                    }
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            } 
            return null;
        }
    }
    
    /** Copied from PhysicalView. */
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        private static final long serialVersionUID = 1L; // in case a DataFolder.ClonedFilterHandle saves me
        
        private final EventListenerList ell = new EventListenerList();
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }
        
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }
        
        public void stateChanged(ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i+1]).stateChanged(event);
                }
            }
        }
        
        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
        
    }
    
    
    private static final class PackageFilterChildren extends FilterNode.Children {
        
        public PackageFilterChildren (final Node originalNode) {
            super (originalNode);
        }       
                
        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder) ? new PackageFilterNode (originalNode) : super.copyNode(originalNode);
        }
    }
    
    private static final class PackageFilterNode extends FilterNode {
        
        public PackageFilterNode (final Node origNode) {
            super (origNode, new PackageFilterChildren (origNode));
        }
        
        @Override
        public void setName (final String name) {
            if (Utilities.isJavaIdentifier (name)) {
                super.setName (name);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                    NbBundle.getMessage(TreeRootNode.class,"MSG_InvalidPackageName"), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }                
        
    }
    
}
