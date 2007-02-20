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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.Actions;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

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
    
    private static final class RootNode extends AbstractNode {
        
        private final FreeformProject p;
        
        public RootNode(FreeformProject p) {
            super(NodeFactorySupport.createCompositeChildren(p, "Projects/org-netbeans-modules-ant-freeform/Nodes"), Lookups.singleton(p));
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

    
}
