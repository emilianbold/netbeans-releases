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

import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Peter Williams
 */
public class PrototypeWebServicesClientView implements WebServicesClientViewImpl {

	public PrototypeWebServicesClientView() {
	}
	
	public Node createWebServiceClientView(Project p) {
		return null;
	}

	public Node createWebServiceClientView(SourceGroup sg) {
		return null;
	}
	
	public Node createWebServiceClientView(FileObject wsdlFolder) {
		Node root = null;
		
		try {
			root = new ClientRootNode(wsdlFolder);
		} catch(DataObjectNotFoundException ex) {
			// rewrite nodes so we don't have to deal with this here
		}
		
		return root;
	}
}

