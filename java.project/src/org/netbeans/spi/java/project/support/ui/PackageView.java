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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.project.PackageViewSettings;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
        
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
        return new RootNode (group);                
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
    
    /**
     * FilterNode which listens on the PackageViewSettings and changes the view to 
     * the package view or tree view
     *
     */
    private static final class RootNode extends FilterNode implements PropertyChangeListener {
        
        private SourceGroup sourceGroup;
        private PackageViewSettings settings;
        
        private RootNode (SourceGroup group) {
            super (getOriginalNode (group, PackageViewSettings.getDefault()));
            this.sourceGroup = group;
            this.settings = PackageViewSettings.getDefault();
            this.settings.addPropertyChangeListener(WeakListeners.propertyChange(this, this.settings));
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (PackageViewSettings.PROP_PACKAGE_VIEW_TYPE.equals(event.getPropertyName())) {
                changeOriginal(getOriginalNode (this.sourceGroup, this.settings), true);
            }
        }
        
        private static Node getOriginalNode (SourceGroup group, PackageViewSettings settings) {            
            assert settings != null : "PackageViewSettings can't be null"; //NOI18N
            switch (settings.getPackageViewType()) {
                case PackageViewSettings.TYPE_PACKAGE_VIEW:
                    return new PackageRootNode(group);
                case PackageViewSettings.TYPE_TREE:
                    return new TreeRootNode(group);
                default:
                    assert false : "Unknown PackageView Type"; //NOI18N
                    return new PackageRootNode(group);
            }
        }        
    }
}
