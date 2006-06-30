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

package org.netbeans.modules.websvc.api.client;

import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.netbeans.modules.websvc.client.WebServicesClientViewAccessor;

/** WebServicesClientView should be used to retrieve information and display objects
 *  for the webservices in a project.
 * <p>
 * A client may obtain a WebServicesClientView instance using 
 * <code>WebServicesClientView.getWebServicesClientView(fileObject)</code> static 
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesClientView {

	private WebServicesClientViewImpl impl;
	private static final Lookup.Result implementations =
		Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientViewProvider.class));

	static  {
		WebServicesClientViewAccessor.DEFAULT = new WebServicesClientViewAccessor() {
			public WebServicesClientView createWebServicesClientView(WebServicesClientViewImpl spiWebServicesClientView) {
				return new WebServicesClientView(spiWebServicesClientView);
			}

			public WebServicesClientViewImpl getWebServicesClientViewImpl(WebServicesClientView wsv) {
				return wsv == null ? null : wsv.impl;
			}
		};
	}

	private WebServicesClientView(WebServicesClientViewImpl impl) {
		if (impl == null)
			throw new IllegalArgumentException ();
		this.impl = impl;
	}

	/** Find the WebServicesClientView for given file or null if the file does 
	 *  not belong to any module support web services.
	 */
	public static WebServicesClientView getWebServicesClientView(FileObject f) {
		if (f == null) {
			throw new NullPointerException("Passed null to WebServicesClientView.getWebServicesClientView(FileObject)"); // NOI18N
		}
		Iterator it = implementations.allInstances().iterator();
		while (it.hasNext()) {
			WebServicesClientViewProvider impl = (WebServicesClientViewProvider)it.next();
			WebServicesClientView wsv = impl.findWebServicesClientView (f);
			if (wsv != null) {
				return wsv;
			}
		}

		WebServicesClientViewProvider impl = (WebServicesClientViewProvider) Lookup.getDefault().lookup(WebServicesClientViewProvider.class);
		if(impl != null) {
			WebServicesClientView wsv = impl.findWebServicesClientView(f);
			return wsv;
		}
		return null;
	}

	// Delegated methods from WebServicesClientViewImpl

	/** This method is not implemented.
	 */
	public Node createWebServiceClientView(Project p) {
		return impl.createWebServiceClientView(p);
	}

	/** This method is not implemented.
	 */
	public Node createWebServiceClientView(SourceGroup sg) {
		return impl.createWebServiceClientView(sg);
	}

	/**
	 * 1. Returns a parent node that the project's logical view can use to display
	 *    the services consumed by this project/module.
	 * 2, Parent node is prepopulated with children representing the services 
	 *    found in the WSDL files in the WSDL folder.
	 *
	 * ISSUE: Does J2ME even have a WSDL folder concept?
	 *
	 * @param wsdlFolder FileObject representing the wsdl folder of the module
	 * containing these web service clients.
	 * @return Node The root node of the web service client subtree intended for
	 * display in the project logical view in the explorer.
	 */
	public Node createWebServiceClientView(FileObject wsdlFolder) {
		return impl.createWebServiceClientView(wsdlFolder);
	}

    
/* !PW FIXME What to put here?  (commented code came from WebModule API)
 *
	public boolean equals (Object obj) {
        if (!WebModule.class.isAssignableFrom(obj.getClass()))
            return false;
        WebModule wm = (WebModule) obj;
        return getDocumentBase().equals(wm.getDocumentBase())
            && getJ2eePlatformVersion().equals (wm.getJ2eePlatformVersion())
            && getContextPath().equals(wm.getContextPath());
    }
    
    public int hashCode () {
        return getDocumentBase ().getPath ().length () + getContextPath ().length ();
    }
 */
}