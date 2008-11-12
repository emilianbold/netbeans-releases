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

package org.netbeans.modules.web.client.tools.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSFileObjectLocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.web.client.tools.api.LocationMappersFactory.class, position=65535)
public final class IdentityLocationMappersFactory implements LocationMappersFactory {
    static final String[] SUPPORTED_MIME_TYPES = {
        "text/html",       // NOI18N
        "text/javascript"  // NOI18N
    };
    
    public NbJSToJSLocationMapper getNbJSToJSLocationMapper(FileObject documentBase, URI applicationContext, Map<String, Object> extendedInfo) {
        return new NbJSToJSLocationMapperImpl(documentBase, applicationContext, extendedInfo);
    }

    public JSToNbJSLocationMapper getJSToNbJSLocationMapper(FileObject documentBase, URI applicationContext, Map<String, Object> extendedInfo) {
        return new JSToNbJSLocationMapperImpl(documentBase, applicationContext, extendedInfo);
    }

    public NbJSToJSLocationMapper getNbJSToJSLocationMapper(FileObject[] documentBases, URI applicationContext, Map<String, Object> extendedInfo) {
        return new NbJSToJSLocationMapperImpl(documentBases, applicationContext, extendedInfo);
    }

    public JSToNbJSLocationMapper getJSToNbJSLocationMapper(FileObject[] documentBases, URI applicationContext, Map<String, Object> extendedInfo) {
        return new JSToNbJSLocationMapperImpl(documentBases, applicationContext, extendedInfo);
    }
    
    private static boolean hasSupportedMIMEType(FileObject fo) {
        if (fo == null) {
            return false;
        }
        String mime = fo.getMIMEType();
        for (String supportedType : SUPPORTED_MIME_TYPES) {
            if (supportedType.equals(mime)) {
                return true;
            }
        }

        return false;
    }
    
    private static final class JSToNbJSLocationMapperImpl implements JSToNbJSLocationMapper {
        private final String serverPrefix;
        private final FileObject[] documentBases;
        private FileObject welcomeFile;
        
        public JSToNbJSLocationMapperImpl(FileObject documentBase, URI applicationContext, Map<String,Object> extendedInfo) {
            this(new FileObject[] { documentBase }, applicationContext, extendedInfo);
        }
        
        public JSToNbJSLocationMapperImpl(FileObject[] documentBases, URI applicationContext, Map<String,Object> extendedInfo) {
            String prefix = applicationContext.toString();
            
            if (prefix.endsWith("/")) { // NOI18N
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            this.serverPrefix = prefix;
            this.documentBases = (FileObject[])documentBases.clone();

            String welcomePath = null;
            if (extendedInfo != null) {
                Object r = extendedInfo.get("welcome-file"); // NOI18N
                if (r instanceof String) {
                    welcomePath = (String)r;
                }
            }
            
            if (welcomePath != null) {
                this.welcomeFile = null;
                for (FileObject base : this.documentBases) {
                    FileObject testObj = base.getFileObject(welcomePath);
                    if (testObj != null) {
                        this.welcomeFile = testObj;
                    }
                }
            } else {
                this.welcomeFile = null;
            }
        }
        
        public NbJSLocation getNbJSLocation(JSLocation jsLocation, Lookup lookup) {
            if (jsLocation instanceof JSURILocation) {
                JSURILocation jsURILocation = (JSURILocation) jsLocation;
                FileObject fo = uriToFO(jsURILocation.getURI());

                if (fo == null || !hasSupportedMIMEType(fo)) {
                    return null;
                }

                return new NbJSFileObjectLocation(fo, jsURILocation.getLineNumber(), jsURILocation.getColumnNumber());
            }
            return null;
        }

        FileObject uriToFO(URI hostUri) {
            String urlPath;
            try {
                urlPath = hostUri.toURL().toExternalForm();
            } catch (MalformedURLException mue) {
                Log.getLogger().log(Level.FINE, "URI mapping failed due to URI->URL conversion: " + hostUri.toString());
                urlPath = null;
            }

            if (urlPath != null && urlPath.startsWith(serverPrefix)) {
                String relativePath = urlPath.substring(serverPrefix.length());

                // do welcome-file substitution for URLs referencing the application root
                if (welcomeFile != null && (relativePath.length() == 0 ||
                        (relativePath.length() == 1 && relativePath.charAt(0) == '/'))) {

                    return welcomeFile;
                }

                for (FileObject documentBase : documentBases) {
                    FileObject resultObj = documentBase.getFileObject(relativePath);
                    if (resultObj != null) {
                        return resultObj;
                    }
                    
                }
                
                return null;
            } else {
                return null;
            }
        }
    }
    
    private static final class NbJSToJSLocationMapperImpl implements NbJSToJSLocationMapper {
        private final String serverPrefix;
        private final FileObject[] documentBases;
        private FileObject welcomeFile;
        
        public NbJSToJSLocationMapperImpl(FileObject documentBase, URI applicationContext, Map<String,Object> extendedInfo) {
            this(new FileObject[] { documentBase }, applicationContext, extendedInfo);
            
            String welcomePath = null;
            if (extendedInfo != null) {
                Object r = extendedInfo.get("welcome-file"); // NOI18N
                if (r instanceof String) {
                    welcomePath = (String)r;
                }
            }
            
            if (welcomePath != null) {
                this.welcomeFile = null;
                for (FileObject base : this.documentBases) {
                    FileObject testObj = base.getFileObject(welcomePath);
                    if (testObj != null) {
                        this.welcomeFile = testObj;
                    }
                }
            } else {
                this.welcomeFile = null;
            }
        }
        
        public NbJSToJSLocationMapperImpl(FileObject[] documentBases, URI applicationContext, Map<String,Object> extendedInfo) {
            String prefix = applicationContext.toString();
            
            if (prefix.endsWith("/")) { // NOI18N
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            this.serverPrefix = prefix;
            this.documentBases = (FileObject[])documentBases.clone();
        }
        
        public JSLocation getJSLocation(NbJSLocation nbJSLocation, Lookup lookup) {
            if (nbJSLocation instanceof NbJSFileObjectLocation) {
                NbJSFileObjectLocation nbJSFileObjectLocation = (NbJSFileObjectLocation) nbJSLocation;
                FileObject fo = nbJSFileObjectLocation.getFileObject();
                if (!hasSupportedMIMEType(fo)) {
                    return null;
                }

                URI uri = fileObjectToUri(fo);
                if (uri != null) {
                    JSURILocation result = new JSURILocation(uri, nbJSFileObjectLocation.getLineNumber(), 
                            nbJSFileObjectLocation.getColumnNumber());
                    
                    if (welcomeFile != null && fo.equals(welcomeFile)) {
                        try {
                            URI serverURI = new URI(serverPrefix);
                            result.addEquivalentURI(serverURI);
                            if (!serverPrefix.endsWith("/")) {
                                URI altServerURI = new URI(serverPrefix + "/");
                                result.addEquivalentURI(altServerURI);
                            }
                        } catch (URISyntaxException ex) {
                            Log.getLogger().log(Level.INFO, "Could not transform create URI", ex);
                        }
                    }
                    
                    return result;
                } else {
                    return null;
                }
            }
            return null;
        }
        
        URI fileObjectToUri(FileObject fo) {
            String filePath = fo.getPath();
            for (FileObject documentBase : documentBases) {
                String basePath = documentBase.getPath();

                if (filePath.startsWith(basePath)) {
                    String relativePath = filePath.substring(basePath.length());
                    String urlPath;
                    if (relativePath.length() > 0 && relativePath.charAt(0) == '/') {
                        urlPath = serverPrefix + relativePath;
                    } else {
                        urlPath = serverPrefix + "/" + relativePath; // NOI18N

                    }

                    try {
                        return new URI(urlPath);
                    } catch (URISyntaxException ex) {
                        return null;
                    }
                }
            }
            return null;
        }
        
    }
}
