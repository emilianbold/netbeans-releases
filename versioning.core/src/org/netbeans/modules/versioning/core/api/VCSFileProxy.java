/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.api;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.FlatFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Represents file on remote or local file system.
 *
 * @author Alexander Simon
 */
public final class VCSFileProxy {

    private final String path;
    final VCSFileProxyOperations proxy;
    boolean isFlat = false;
    
    private VCSFileProxy(String path, VCSFileProxyOperations proxy) {
        this.path = path;
        this.proxy = proxy;
    }
    
    public static VCSFileProxy createFileProxy(final File file) {
        VCSFileProxy p = new VCSFileProxy(file.getAbsolutePath(), null);
        if(file instanceof FlatFolder) {
            p.setFlat(true);
        }
        return p;
    }

    public static VCSFileProxy createFileProxy(final VCSFileProxy file, String name) {
        return new VCSFileProxy(file.getAbsolutePath() + "/" + name, file.proxy);   // NOI18N
    }
    
    public static VCSFileProxy createFileProxy(final FileObject fo) {
        try {
            VCSFileProxyOperations fileProxyOperations = getFileProxyOperations(fo.getFileSystem());
            if (fileProxyOperations == null) {
                File file = FileUtil.toFile(fo);
                if(file != null) {
                    return new VCSFileProxy(file.getAbsolutePath(), null);
                } else {
                    return null; // e.g. FileObject from a jar filesystem
                }
            } else {
                return new VCSFileProxy(fo.getPath(), fileProxyOperations);
            }
        } catch (FileStateInvalidException ex) {
            Logger.getLogger(VCSFileProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new VCSFileProxy(fo.getPath(), null);
    }

    public static VCSFileProxy createFileProxy(FileSystem fs, String path) {
        VCSFileProxyOperations fileProxyOperations = getFileProxyOperations(fs);
        if (fileProxyOperations == null) {
            return new VCSFileProxy(path, null);
        } else {
            return new VCSFileProxy(path, fileProxyOperations);
        }
    }
    
    public String getAbsolutePath() {
        return path;
    }
    
    public String getName() {
        if (proxy == null) {
            return new File(path).getName();
        } else {
            return proxy.getName(this);
        }
    }
    
    public boolean isDirectory() {
        if (proxy == null) {
            return new File(path).isDirectory();
        } else {
            return proxy.isDirectory(this);
        }
    }
    
    public boolean isFile() {
        if (proxy == null) {
            return new File(path).isFile();
        } else {
            return proxy.isFile(this);
        }
    }
    
    public boolean canWrite() {
        if (proxy == null) {
            return new File(path).canWrite();
        } else {
            return proxy.canWrite(this);
        }
    }
    
    public VCSFileProxy getParentFile() {
        if (proxy == null) {
            File parent = new File(path).getParentFile();
            if(parent == null) {
                return null;
            }
            return VCSFileProxy.createFileProxy(parent);
        } else {
            return proxy.getParentFile(this);
        }
    }
    public boolean exists() {
        if (proxy == null) {
            return new File(path).exists();
        } else {
            return proxy.exists(this);
        }
    }
    
    public VCSFileProxy[] listFiles() {
        if (proxy == null) {
            File[] files = new File(path).listFiles();
            if(files != null) {
                VCSFileProxy[] ret = new VCSFileProxy[files.length];
                for (int i = 0; i < files.length; i++) {
                    ret[i] = VCSFileProxy.createFileProxy(files[i]);
                }
                return ret;
            }
            return null;
        } else {
            return proxy.list();
        }
        
    }
    
    public boolean isFlat() {
        if (proxy == null) {
            return isFlat;
        } else {
            return proxy.isFlat();
        }
    }
    
    void setFlat(boolean flat) {
        this.isFlat = flat;
    }
    
    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 61 * hash + (this.proxy != null ? this.proxy.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VCSFileProxy other = (VCSFileProxy) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        if (this.proxy != other.proxy && (this.proxy == null || !this.proxy.equals(other.proxy))) {
            return false;
        }
        return true;
    }

    public File toFile() {
        if(proxy == null) {
            return isFlat ? new FlatFolder(path) : new File(path);
        }
        return null;
    }
    
    public FileObject toFileObject() {
        if (proxy == null) {
            return FileUtil.toFileObject(new File(FileUtil.normalizePath(path)));
        } else {
            return proxy.toFileObject(this);
        }
    }

    public VCSFileProxy normalizeFile() {
        if (proxy == null) {
            return new VCSFileProxy(FileUtil.normalizePath(path), null);
        } else {
            return proxy.normalize(this);
        }
    }
    
    private static VCSFileProxyOperations getFileProxyOperations(FileSystem fs) {
        return (VCSFileProxyOperations) getAttribute(fs, VCSFileProxyOperations.ATTRIBUTE);
    }

    private static Object getAttribute(FileSystem fileSystem, String attrName) {
        return fileSystem.getRoot().getAttribute(attrName);
    }        
}
