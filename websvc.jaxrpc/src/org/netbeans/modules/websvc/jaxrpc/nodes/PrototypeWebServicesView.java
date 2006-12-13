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
package org.netbeans.modules.websvc.jaxrpc.nodes;

import org.openide.nodes.Node;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewImpl;
import org.openide.filesystems.FileObject;

public class PrototypeWebServicesView implements WebServicesViewImpl
{
    public PrototypeWebServicesView()
	{}
	

    public Node createWebServicesView(FileObject srcRoot)
    {
	return new WebServicesRootNode(srcRoot);
    }
	
    public static Node findPath(Node rootNode, Object object)
    {
        /*  TO-DO: Need to implement this
        WebServicesRootNode.PathFinder pf = 
               (WebServicesRootNode.PathFinder)rootNode.getLookup().
                        lookup( WebServicesRootNode.PathFinder.class );
        
        if ( pf != null ) {
            return pf.findPath( rootNode, object );
        } else {
            return null;
        }
        */
        return null;
    }
}
