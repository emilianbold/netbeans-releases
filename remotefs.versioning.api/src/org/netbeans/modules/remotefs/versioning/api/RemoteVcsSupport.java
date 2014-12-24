/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remotefs.versioning.api;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.netbeans.modules.remotefs.versioning.spi.RemoteVcsSupportImplementation;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.*;
import static org.openide.util.lookup.Lookups.proxy;

/**
 *
 * @author vkvashin
 */
public final class RemoteVcsSupport {

    private RemoteVcsSupport() {
    }

    /**
     * @param proxy defines FS and initial selection
     * @return file chooser or null if no providers found
     */
    public static JFileChooser createFileChooser(VCSFileProxy proxy) {
        final File file = proxy.toFile();
        if (file !=  null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(file);
            return  chooser;
        } else {
            RemoteVcsSupportImplementation impl = getImpl();
            if (impl!= null) {
                return impl.createFileChooser(proxy);
            }
        }
        return null;
    }

    public static VCSFileProxy getSelectedFile(JFileChooser chooser) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getSelectedFile(chooser);
        }
        return null;
    }

    public static FileSystem getFileSystem(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getFileSystem(proxy);
        }
        return null;
    }

    public static FileSystem[] getFileSystems() {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getFileSystems();
        }
        return new FileSystem[0];
    }

    public static FileSystem getDefaultFileSystem() {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getDefaultFileSystem();
        }
        return null;
    }

    public static boolean isSymlink(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.isSymlink(proxy);
        }        
        return false;
    }

    public static boolean canRead(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.canRead(proxy);
        }
        return false;
    }
    
    public static boolean canRead(VCSFileProxy base, String subdir) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.canRead(base, subdir);
        }
        return false;
    }

    public static VCSFileProxy getCanonicalFile(VCSFileProxy proxy) throws IOException {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getCanonicalFile(proxy);
        }
        return proxy;
    }

    public static String getCanonicalPath(VCSFileProxy proxy) throws IOException {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getCanonicalPath(proxy);
        }
        return proxy.getPath();        
    }    

    public static boolean isMac(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.isMac(proxy);
        }
        return false;
    }

    public static boolean isUnix(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.isUnix(proxy);
        }
        return true;
    }

    public static long getSize(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getSize(proxy);
        }
        return 0;
    }

    public static String getFileSystemKey(FileSystem proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.getFileSystemKey(proxy);
        }
        return null; // TODO: throw???
    }

    public static String toString(VCSFileProxy proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.toString(proxy);
        }
        return null; // TODO: throw???
    }

    public static VCSFileProxy fromString(String proxy) {
        RemoteVcsSupportImplementation impl = getImpl();
        if (impl != null) {
            return impl.fromString(proxy);
        }
        return null; // TODO: throw???
    }

    private static RemoteVcsSupportImplementation getImpl() {
        RemoteVcsSupportImplementation impl = Lookup.getDefault().lookup(RemoteVcsSupportImplementation.class);
        if (impl == null && !providerAbsenceReported) {
            providerAbsenceReported = true;
            Exceptions.printStackTrace(new IllegalStateException("No provider found for " + //NOI18N
                    RemoteVcsSupportImplementation.class.getName() ));
        }
        return impl;
    }

    private static volatile boolean providerAbsenceReported = false;
}
