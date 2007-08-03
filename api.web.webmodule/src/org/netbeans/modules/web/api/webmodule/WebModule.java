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

package org.netbeans.modules.web.api.webmodule;

import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.netbeans.modules.web.spi.webmodule.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** WebModule should be used to access contens and properties of web module.
 * <p>
 * A client may obtain a WebModule instance using
 * <code>WebModule.getWebModule(fileObject)</code> static method, for any
 * FileObject in the web module directory structure.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Use classpath API to obtain classpath for the document base (this classpath
 * is used for code completion of JSPs). An example:
 * <pre>
 *   WebModule wm = ...;
 *   FileObject docRoot = wm.getDocumentBase ();
 *   ClassPath cp = ClassPath.getClassPath(docRoot, ClassPath.EXECUTE);
 * 
 * </pre>
 * <p>
 * Note that the particular directory structure for web module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {
    
    //TO-DO: the J2EE_13_LEVEL and J2EE_14_LEVEL constants should be got from org.netbeans.modules.j2ee.common.J2eeProjectConstants 
    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N
    public static final String JAVA_EE_5_LEVEL = "1.5"; //NOI18N
    
    private WebModuleImplementation impl;
    private static final Lookup.Result implementations =
            Lookup.getDefault().lookupResult(WebModuleProvider.class);
    
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
    
    /** Find the WebModule for given file or null if the file does not belong
     * to any web module.
     */
    public static WebModule getWebModule (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to WebModule.getWebModule(FileObject)"); // NOI18N
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
    
    /** WEB-INF folder for the web module.
     * It may return null for web module that does not have any WEB-INF folder.
     * <div class="nonnormative">
     * The WEB-INF folder would typically be a child of the folder returned 
     * by {@link #getDocumentBase} but does not need to be.
     * </div>
     */
    public FileObject getWebInf () {
        return impl.getWebInf ();
    }

    /** Deployment descriptor (web.xml file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned 
     * by {@link #getWebInf} but does not need to be.
     * </div>
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor ();
    }
    
    /** Context path of the web module.
     */
    public String getContextPath () {
        return impl.getContextPath ();
    }
    
    /** J2EE platform version - one of the constants {@link #J2EE_13_LEVEL}, 
     * {@link #J2EE_14_LEVEL}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion ();
    }
    
    /** Source roots associated with the web module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.
     * </div>
     */
    public FileObject[] getJavaSources() {
        return impl.getJavaSources();
    }
    
    /** Returns true if the object represents the same web module.
     */
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
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
    
    public MetadataModel<WebAppMetadata> getMetadataModel() {
        return impl.getMetadataModel();
    }
}
