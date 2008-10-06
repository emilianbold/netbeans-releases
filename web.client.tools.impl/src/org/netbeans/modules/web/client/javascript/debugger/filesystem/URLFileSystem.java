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

package org.netbeans.modules.web.client.javascript.debugger.filesystem;

import java.awt.Image;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Set;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * 
 * @author quynguyen
 */
public class URLFileSystem extends FileSystem {
    private static final long serialVersionUID = 281881787206L;
    static final String JAVASCRIPT_MIMETYPE = "text/javascript";
    static final String HTML_MIMETYPE = "text/html";
    static final String CSS_MIMETYPE = "text/x-css";
    
    
    private URLRootFileObject root;
    private final Status displayStatus;
    private String sessionId;
    
    // Keep contentProvider as a WeakReference so WeakHashMap caching works
    private transient WeakReference<URLContentProvider> contentProvider;
    
    public URLFileSystem() {
        displayStatus = new URLStatus();
        sessionId = NbBundle.getMessage(URLFileSystem.class, "DEFAULT_FS_DISPLAY_NAME");
    }

    public URLContentProvider getContentProvider() {
        return (contentProvider != null) ? contentProvider.get() : null;
    }

    public void setContentProvider(URLContentProvider contentProvider) {
        this.contentProvider = new WeakReference<URLContentProvider>(contentProvider);
    }
    
    public URLFileObject initialize(URL url) {
        root = new URLRootFileObject(this);
        
        URLFileObject result = new URLFileObject(url, this, root);
        root.addChild(result);
        
        return result;
    }
    
    public URLFileObject addURL(URL url) {
        if (root == null) {
            return initialize(url);
        }else {
            URLFileObject newFileObject = new URLFileObject(url, this, root);
            
            if (root.getFileObject(newFileObject.getPath()) == null) {
                root.addChild(newFileObject);
                return newFileObject;
            } else {
                throw new IllegalArgumentException(url.toExternalForm() + " is already in URLFileSystem");
            }
        }
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    @Override
    public String getDisplayName() {
        return sessionId;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public FileObject getRoot() {
        return root;
    }

    @Override
    public FileObject findResource(String name) {
        if (root != null) {
            return root.getFileObject(name);
        }else {
            return null;
        }
    }

    @Override
    public Status getStatus() {
        return displayStatus;
    }    
    
    @Override
    public SystemAction[] getActions() {
        return new SystemAction[0];
    }

    protected void fireStatusChange(URLFileObject obj) {
        FileStatusEvent evt = new FileStatusEvent(this, obj, false, true);
        fireFileStatusChanged(evt);
    }

    private static final class URLStatus implements Status, Serializable {
        private static final long serialVersionUID = 620102785902L;

        private static final int MAX_SIZE = 20;
        
        private static Image JAVASCRIPT_IMAGE = new ImageIcon(URLFileSystem.class.getResource("javascript.png")).getImage();
        private static Image HTML_IMAGE = new ImageIcon(URLFileSystem.class.getResource("html.png")).getImage();
        
        public String annotateName(String name, Set<? extends FileObject> files) {
            for (FileObject fo : files) {
                if (fo instanceof URLFileObject) {
                    URLFileObject urlFO = (URLFileObject)fo;
                    URL sourceURL = urlFO.getActualURL();
                    
                    if (sourceURL != null && urlFO.getNameExt().equals(name)) {
                        String displayName = sourceURL.toExternalForm();
                        if (displayName.length() <= MAX_SIZE) {
                            return displayName;
                        } else {
                            String path = sourceURL.getPath();
                            String query = sourceURL.getQuery();
                            String protocol = sourceURL.getProtocol();

                            int lastSlashIndex = -1;
                            if (path != null) {
                                for (int i = path.length() - 2; i >= 0; i--) {
                                    if (path.charAt(i) == '/') {
                                        lastSlashIndex = i;
                                        break;
                                    }
                                }
                            }

                            if (lastSlashIndex == -1) {
                                return displayName;
                            } else {
                                StringBuffer truncatedName = new StringBuffer();
                                truncatedName.append(protocol);
                                truncatedName.append("://...");
                                truncatedName.append(path.substring(lastSlashIndex));

                                if (query != null && query.length() > 0) {
                                    truncatedName.append("?");
                                    truncatedName.append(query);
                                }

                                return truncatedName.toString();
                            }
                        }
                    }
                }
            }
            
            return name;
        }

        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            for (FileObject fo : files) {
                if (fo instanceof URLFileObject) {
                    URLFileObject urlFO = (URLFileObject)fo;
                    String mimeType = urlFO.getMIMEType();
                    if (JAVASCRIPT_MIMETYPE.equals(mimeType)) {
                        return JAVASCRIPT_IMAGE;
                    } else if (HTML_MIMETYPE.equals(mimeType)) {
                        return HTML_IMAGE;
                    }
                }
            }
            return icon;
        }
        
    }
    
}
