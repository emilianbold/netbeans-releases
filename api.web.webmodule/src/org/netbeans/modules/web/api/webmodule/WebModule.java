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

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.netbeans.modules.web.spi.webmodule.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** WebModule should be used to access contens and properties of web module.
 * <p>
 * A client may obtain a WebModule instance using 
 * <code>WebModule.getWebModule(fileObject)</code> static method, for any 
 * FileObject in the web module directory structure.
 * <p>
 * <div class="nonnormative">
 * Note that the directory structure for web module is not guaranteed by this API.
 * For example the folder returned for {@link WebModule#getJavaSourcesFolder} may 
 * or may not be a subfolder of the {@link WebModule#getDocumentBase} folder, 
 * <code>WEB-INF</code> folder or various deployment descriptors can be located 
 * anywhere in source tree, etc.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {
    
    private WebModuleImplementation impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(WebModuleProvider.class));
    
    static  {
        WebModuleAccessor.DEFAULT = new WebModuleAccessor() {
            public WebModule createWebModule(WebModuleImplementation spiWebmodule) {
                return new WebModule(spiWebmodule);
            }

            public WebModuleImplementation getWebModuleImplementation(WebModule wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private WebModule (WebModuleImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    public static WebModule getWebModule (FileObject f) {
        if (f == null) {
            throw new IllegalArgumentException ();
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebModuleProvider impl = (WebModuleProvider)it.next();
            WebModule wm = impl.findWebModule (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Folder that contains sources of the static documents for 
     * the web module (html, JSPs, etc.).
     */
    public FileObject getDocumentBase () {
        return impl.getDocumentBase ();
    }
    
    /** Folder that contains Java sources for the web module.
     */
    public FileObject getJavaSourcesFolder () {
        return impl.getJavaSourcesFolder ();
    }
    
    public String getContextPath () {
        return impl.getContextPath ();
    }
    
    public void setContextPath (String path) {
        impl.setContextPath (path);
    }
}