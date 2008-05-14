/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSLocation;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSURILocation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author quynguyen
 */
public class IdentityURLMapper implements NbJSToJSLocation, JSToNbJSLocation {
    static final String[] SUPPORTED_MIME_TYPES = {
        "text/html",       // NOI18N
        "text/javascript"  // NOI18N
    };
    
    private final String serverPrefix;
    private final FileObject documentBase;
    private final FileObject welcomeFile;
    
    public IdentityURLMapper(String serverPrefix, FileObject documentBase, String welcomePath) {
        if (serverPrefix.endsWith("/")) { // NOI18N
            serverPrefix = serverPrefix.substring(0, serverPrefix.length()-1);
        }
        
        this.serverPrefix = serverPrefix;
        this.documentBase = documentBase;
        
        if (welcomePath != null) {
            this.welcomeFile = this.documentBase.getFileObject(welcomePath);
        }else {
            this.welcomeFile = null;
        }
    }

    public JSLocation getJSLocation(NbJSLocation nbJSLocation, Lookup lookup) {
        if (nbJSLocation instanceof NbJSFileObjectLocation) {
            NbJSFileObjectLocation nbJSFileObjectLocation = (NbJSFileObjectLocation) nbJSLocation;
            FileObject fo = nbJSFileObjectLocation.getFileObject();
            if (!hasSupportedMIMEType(fo)) {
                return null;
            }

            URL url = fileObjectToUrl(fo);
            if (url != null) {
                return getJSURILocationForURL(url, nbJSFileObjectLocation.getLineNumber(), nbJSFileObjectLocation.getColumnNumber());
            }else {
                return null;
            }
        }
        return null;
    }

    public NbJSLocation getNbJSLocation(JSLocation jsLocation, Lookup lookup) {
        if (jsLocation instanceof JSURILocation) {
            JSURILocation jsURILocation = (JSURILocation) jsLocation;
            FileObject fo = null;
            try {
                fo = urlToFO(jsURILocation.getURI().toURL());
            } catch (MalformedURLException ex) {
                Log.getLogger().log(Level.WARNING, "Could not transform URI to URL: " + jsURILocation.getURI());
            }

            if (fo == null || !hasSupportedMIMEType(fo)) {
                return null;
            }

            return getNbJSFileObjectLocationForFO(fo, jsURILocation.getLineNumber(), jsURILocation.getColumnNumber());
        }
        return null;
    }

    JSURILocation getJSURILocationForURL(URL url, int line, int col) {
        try {
            return new JSURILocation(url.toURI(), line, col);
        } catch (URISyntaxException ex) {
            Log.getLogger().log(Level.SEVERE, "URI syntax exception", ex);
        }
        return null;
    }
    
    NbJSFileObjectLocation getNbJSFileObjectLocationForFO(FileObject fo, int line, int col) {
        return new NbJSFileObjectLocation(fo, line, col);
    }
    
    URL fileObjectToUrl(FileObject fo) {
        String basePath = documentBase.getPath();
        String filePath = fo.getPath();

        if (filePath.startsWith(basePath)) {
            String relativePath = filePath.substring(basePath.length());
            String urlPath;
            if (relativePath.length() > 0 && relativePath.charAt(0) == '/') {
                urlPath = serverPrefix + relativePath;
            } else {
                urlPath = serverPrefix + "/" + relativePath; // NOI18N
            }
            
            try {
                return new URL(urlPath);
            }catch (MalformedURLException mue) {
                return null;
            }
        }
        
        return null;
    }
    
    FileObject urlToFO(URL hostUrl) {
        String urlPath = externalFormWithoutQuery(hostUrl);
        
        if (urlPath.startsWith(serverPrefix)) {
            String relativePath = urlPath.substring(serverPrefix.length());
            
            // do welcome-file substitution for URLs referencing the application root
            if (welcomeFile != null && (relativePath.length() == 0 || 
                (relativePath.length() == 1 && relativePath.charAt(0) == '/'))) {
                
                return welcomeFile;
            }
            
            return documentBase.getFileObject(relativePath);
        }else {
            return null;
        }
    }
    
    boolean hasSupportedMIMEType(FileObject fo) {
        if (fo == null) return false;
        
        String mime = fo.getMIMEType();
        for (String supportedType : SUPPORTED_MIME_TYPES) {
            if (supportedType.equals(mime)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String externalFormWithoutQuery(URL u) {
        if (u == null) return "";
        
        // compute length of StringBuffer
	int len = u.getProtocol().length() + 1;
	if (u.getAuthority() != null && u.getAuthority().length() > 0)
	    len += 2 + u.getAuthority().length();
	if (u.getPath() != null) {
	    len += u.getPath().length();
	}
	if (u.getQuery() != null) {
	    len += 1 + u.getQuery().length();
	}
	if (u.getRef() != null) 
	    len += 1 + u.getRef().length();

	StringBuffer result = new StringBuffer(len);
	result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(u.getPath());
        }
        
        return result.toString();
    }
}
