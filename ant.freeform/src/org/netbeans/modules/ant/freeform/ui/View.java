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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.ant.freeform.Actions;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
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
        return new ProjectNodeWrapper(new RootNode(project));
    }
    
    public Node findPath(Node root, Object target) {
        // Check each child node in turn.
        Node[] kids = root.getChildren().getNodes(true);
        for (Node kid : kids) {
            // First ask natures.
            for (ProjectNature nature : Lookup.getDefault().lookupAll(ProjectNature.class)) {
                Node n = nature.findSourceFolderViewPath(project, kid, target);
                if (n != null) {
                    return n;
                }
            }
            // Otherwise, check children and look for <source-folder>/<source-file> matches.
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = kid.getLookup().lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = target instanceof DataObject ? ((DataObject) target).getPrimaryFile() : (FileObject) target;
                if (kidFO == targetFO) {
                    return kid;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);
                    List<String> path = Collections.list(NbCollections.checkedEnumerationByFilter(new StringTokenizer(relPath, "/"), String.class, true)); // NOI18N
                    // XXX see original code for justification
                    path.set(path.size() - 1, targetFO.getName());
                    try {
                        return NodeOp.findPath(kid, Collections.enumeration(path));
                    } catch (NodeNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
    
    private static final class RootChildren extends Children.Keys<Element> implements AntProjectListener {
        
        private final FreeformProject p;
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        protected void addNotify() {
            super.addNotify();
            updateKeys(false);
            p.helper().addAntProjectListener(this);
            // XXX should probably listen to project.evaluator also?
        }
        
        protected void removeNotify() {
            setKeys(Collections.<Element>emptySet());
            p.helper().removeAntProjectListener(this);
            super.removeNotify();
        }
        
        private void updateKeys(boolean fromListener) {
            Element genldata = p.helper().getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
                final List<Element> keys = Util.findSubElements(itemsEl);
                if (fromListener) {
                    // #50328 - post setKeys to different thread to prevent deadlocks
                    RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                setKeys(keys);
                            }
                        });
                } else {
                    setKeys(keys);
                }
            } else {
                if (fromListener) {
                    // #58491 - post setKeys to different thread to prevent deadlocks
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            setKeys(Collections.<Element>emptySet());
                        }
                    });
                } else {
                    setKeys(Collections.<Element>emptySet());
                }
            }
        }
        
        protected Node[] createNodes(Element itemEl) {
            Element locationEl = Util.findElement(itemEl, "location", FreeformProjectType.NS_GENERAL); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            if (locationEval == null) {
                return null;
            }
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
                label = null;
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                for (ProjectNature nature : Lookup.getDefault().lookupAll(ProjectNature.class)) {
                    if (nature.getSourceFolderViewStyles().contains(style)) {
                        return new Node[] {nature.createSourceFolderView(p, file, style, location, label)};
                    }
                }
                // fall back to tree display
                // assert style.equals(STYLE_TREE);
                DataObject fileDO;
                try {
                    fileDO = DataObject.find(file);
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
                return new Node[] {new ViewItemNode((DataFolder) fileDO, location, label)};
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl; // NOI18N
                    DataObject fileDO;
                    try {
                        fileDO = DataObject.find(file);
                    } catch (DataObjectNotFoundException e) {
                        throw new AssertionError(e);
                    }
                return new Node[] {new ViewItemNode(fileDO.getNodeDelegate(), location, label)};
            }
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateKeys(true);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
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
        
        public Image getIcon(int type) {
            return Utilities.icon2Image(ProjectUtils.getInformation(p).getIcon());
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public Action[] getActions(boolean context) {
            return Actions.createContextMenu(p);
        }
        
        public boolean canRename() {
            return true;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(p, name);
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx("freeform.node." + org.netbeans.modules.ant.freeform.Util.getMergedHelpIDFragments(p)); // NOI18N
        }
        
    }
    
    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }
    
     private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        
        private final String displayName;
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
       
        public ViewItemNode(Node orig, String name, String displayName) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
        }
        
        public ViewItemNode(DataFolder folder, String name, String displayName) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
            this.name = name;
            this.displayName = displayName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                // #50425: show original name incl. annotations
                return super.getDisplayName();
            }
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
    
}
