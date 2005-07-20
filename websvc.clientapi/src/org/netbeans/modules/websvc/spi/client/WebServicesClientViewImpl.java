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

package org.netbeans.modules.websvc.spi.client;

import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesClientViewImpl {
    
	// PW FIXME not all these methods will stay.  As of 8/31, the one used
	// is the FileObject wsdlFolder version.
    public Node createWebServiceClientView(Project p);
    
    public Node createWebServiceClientView(SourceGroup sg);
	
    public Node createWebServiceClientView(FileObject wsdlFolder);
	
}
