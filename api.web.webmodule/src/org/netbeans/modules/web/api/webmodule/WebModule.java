/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.netbeans.modules.web.spi.webmodule.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * This class encapsulates a web module.
 * 
 * <p>A client may obtain a <code>WebModule</code> instance using
 * method {@link #getWebModule}, for any
 * {@link org.openide.filesystems.FileObject} in the web module directory structure.</p>
 * <div class="nonnormative">
 * <p>Use the classpath API to obtain the classpath for the document base (this classpath
 * is used for code completion of JSPs). An example:</p>
 * <pre>
 *     WebModule wm = ...;
 *     FileObject docRoot = wm.getDocumentBase ();
 *     ClassPath cp = ClassPath.getClassPath(docRoot, ClassPath.EXECUTE);
 * </pre>
 * <p>Note that no particular directory structure for web module is guaranteed 
 * by this API.</p>
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {
    
    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N
    public static final String JAVA_EE_5_LEVEL = "1.5"; //NOI18N
    
    private final WebModuleImplementation impl;
    private static final Lookup.Result implementations =
            Lookup.getDefault().lookupResult(WebModuleProvider.class);
    
    static  {
        WebModuleAccessor.DEFAULT = new WebModuleAccessor() {
            public WebModule createWebModule(WebModuleImplementation spiWebmodule) {
                return new WebModule(spiWebmodule);
            }
        };
    }
    
    private WebModule (WebModuleImplementation impl) {
        Parameters.notNull("impl", impl); // NOI18N
        this.impl = impl;
    }
    
    /**
     * Finds the web module that a given file belongs to. The given file should
     * be one known to be owned by a web module (e.g., it can be a file in a
     * Java source group, such as a servlet, or it can be a file in the document
     * base, such as a JSP page).
     *
     * @param  file the file to find the web module for; never null.
     * @return the web module this file belongs to or null if the file does not belong
     *         to any web module.
     * @throws NullPointerException if the <code>file</code> parameter is null.
     */
    public static WebModule getWebModule (FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebModuleProvider impl = (WebModuleProvider)it.next();
            WebModule wm = impl.findWebModule (file);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /**
     * Returns the folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     *
     * @return the static documents folder; can be null.
     */
    public FileObject getDocumentBase () {
        return impl.getDocumentBase ();
    }
    
    /**
     * Returns the WEB-INF folder for the web module.
     * It may return null for web module that does not have any WEB-INF folder.
     * <div class="nonnormative">
     * <p>The WEB-INF folder would typically be a child of the folder returned
     * by {@link #getDocumentBase} but does not need to be.</p>
     * </div>
     *
     * @return the WEB-INF folder; can be null.
     */
    public FileObject getWebInf () {
        return impl.getWebInf ();
    }

    /**
     * Returns the deployment descriptor (<code>web.xml</code> file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the <code>web.xml</code> file; can be null.
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor ();
    }
    
    /**
     * Returns the context path of the web module.
     *
     * @return the context path; can be null.
     */
    public String getContextPath () {
        return impl.getContextPath ();
    }
    
    /**
     * Returns the J2EE platform version of this module. The returned value is
     * one of the constants {@link #J2EE_13_LEVEL}, {@link #J2EE_14_LEVEL} or 
     * {@link #JAVA_EE_5_LEVEL}.
     *
     * @return J2EE platform version; never null.
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion ();
    }
    
    /**
     * Returns the Java source roots associated with the web module.
     * <div class="nonnormative">
     * <p>Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.</p>
     * </div>
     *
     * @return this web module's Java source roots; never null.
     *
     * @deprecated This method is deprecated, because its return values does
     * not contain enough information about the source roots. Source roots
     * are usually implemented by a <code>org.netbeans.api.project.SourceGroup</code>,
     * which is more than just a container for a {@link org.openide.filesystems.FileObject}.
     */
    @Deprecated
    public FileObject[] getJavaSources() {
        return impl.getJavaSources();
    }
    
    /**
     * Returns a model describing the metadata of this web module (servlets,
     * resources, etc.).
     *
     * @return this web module's metadata model; never null.
     */
    public MetadataModel<WebAppMetadata> getMetadataModel() {
        return impl.getMetadataModel();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!WebModule.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        WebModule wm = (WebModule) obj;
        if (!getDocumentBase().equals(wm.getDocumentBase())) {
            return false;
        }
        if (!getJ2eePlatformVersion().equals(wm.getJ2eePlatformVersion())) {
            return false;
        }
        String contextPath = getContextPath();
        String wmContextPath = wm.getContextPath();
        if (contextPath != null && wmContextPath != null) {
            return contextPath.equals(wmContextPath);
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hashCode = getDocumentBase().getPath().hashCode();
        String contextPath = getContextPath();
        if (contextPath != null) {
            hashCode += contextPath.hashCode();
        }
        return hashCode;
    }
}
