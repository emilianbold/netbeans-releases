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

package org.netbeans.modules.websvc.api.webservices;

import java.util.Iterator;
import org.netbeans.modules.websvc.webservices.WebServicesViewAccessor;
import org.netbeans.modules.websvc.spi.webservices.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** WebServicesView should be used to retrieve information and display objects
 *  for the webservices in a project.
 * <p>
 * A client may obtain a WebServicesView instance using 
 * <code>WebServicesView.getWebServicesView(fileObject)</code> static 
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesView {
    
    private WebServicesViewImpl impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(WebServicesViewProvider.class));
    
    static  {
        WebServicesViewAccessor.DEFAULT = new WebServicesViewAccessor() {
            public WebServicesView createWebServicesView(WebServicesViewImpl spiWebServicesView) {
                return new WebServicesView(spiWebServicesView);
            }

            public WebServicesViewImpl getWebServicesViewImpl(WebServicesView wsv) {
                return wsv == null ? null : wsv.impl;
            }
        };
    }
    
    private WebServicesView(WebServicesViewImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the WebServicesView for given file or null if the file does not belong
     * to any module support web services.
     */
    public static WebServicesView getWebServicesView(FileObject f) {
        if (f != null) {
           Iterator it = implementations.allInstances().iterator();
           while (it.hasNext()) {
              WebServicesViewProvider impl = (WebServicesViewProvider)it.next();
              WebServicesView wsv = impl.findWebServicesView (f);
              if (wsv != null) {
                return wsv;
              }
           }
        }
        return null;
    }

	// Delegated methods from WebServicesViewImpl
	
    public Node createWebServicesView(FileObject  srcRoot) {
		return impl.createWebServicesView(srcRoot);
	}
	
	
    
/* !! What to put here?
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
