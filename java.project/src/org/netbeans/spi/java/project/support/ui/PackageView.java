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

package org.netbeans.spi.java.project.support.ui;

import org.netbeans.api.project.SourceGroup;
import org.openide.nodes.Node;

/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
    
    /** #42151: option to show tree structure instead */
    private static final boolean USE_TREE_VIEW = Boolean.getBoolean("org.netbeans.spi.java.project.support.ui.packageView.USE_TREE_VIEW"); // NOI18N
    
    private PackageView() {}
    
    /**
     * Create a node which will contain package-oriented view of a source group.
     * <p>
     * The precise structure of this node is <em>not</em> specified by the API
     * and is subject to arbitrary change (perhaps at user option).
     * Callers should not make assumptions about the nature of subnodes, the
     * code or display names of certain nodes, and so on. You may use cookies/lookup
     * to find if particular subnodes correspond to folders or files.
     * </p>
     * @param group a source group which should be represented
     * @return node which will display packages in given group
     */
    public static Node createPackageView( SourceGroup group ) {
        if (USE_TREE_VIEW) {
            return new TreeRootNode(group);
        } else {
            return new PackageRootNode(group);
        }
    }
    
    /**
     * Finds the node representing given object, if any.
     * The current implementation works only for {@link org.openide.filesystems.FileObject}s
     * and {@link org.openide.loaders.DataObject}s.
     * @param rootNode a node some descendant of which should contain the object
     * @param object object to find
     * @return a node representing the given object, or null if no such node was found
     */
    public static Node findPath(Node rootNode, Object object) {
        
        PackageRootNode.PathFinder pf = (PackageRootNode.PathFinder)rootNode.getLookup().lookup( PackageRootNode.PathFinder.class );
        
        if ( pf != null ) {
            return pf.findPath( rootNode, object );
        } else {
            TreeRootNode.PathFinder pf2 = (TreeRootNode.PathFinder) rootNode.getLookup().lookup(TreeRootNode.PathFinder.class);
            if (pf2 != null) {
                return pf2.findPath(rootNode, object);
            } else {
                return null;
            }
        }
    }
}
