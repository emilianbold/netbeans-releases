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
    
    private PackageView() {}
    
    /** Create a node which will contain package-oriented view.
     * @param group SourceGroup which should be represented.
     * @return node which will display packages in given group
     */
    public static Node createPackageView( SourceGroup group ) {
        return new PackageRootNode( group );
    }
    
    /** Finds node representing given object. Currently only works for 
     * FileObjects.
     * @param rootNode the node which should contain the object
     * @param object object to find
     * @return node which represents given object or null if no node was found
     * @throws IllegalArgumentException if the supplied node is not a source group root
     *         node created by the {@link #createPackageView(SourceGroup)} method.
     */
    public static Node findPath(Node rootNode, Object object) throws IllegalArgumentException {
        
        PackageRootNode.PathFinder pf = (PackageRootNode.PathFinder)rootNode.getLookup().lookup( PackageRootNode.PathFinder.class );
        
        if ( pf != null ) {
            return pf.findPath( rootNode, object );
        }
        else {
            throw new IllegalArgumentException( rootNode + " is not a node provided by the PackageView.createPackageView( Source group ) method." );
        }
    }
}
