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
package org.netbeans.modules.cnd.api.remote;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.openide.util.Lookup;

/**
 * Interface for a remote host information utility provider which can/will be implemented in another module.
 *
 * @author gordonp
 * @author Sergey Grinev
 */
public abstract class HostInfoProvider {

    /**
     * This function returns path mapper for the host stated by hkey
     */
    public abstract PathMap getMapper(String hkey);

    /**
     * This function returns PlatformTypes constant representing remote host platform
     */
    public abstract int getPlatform(String hkey);

    /**
     * This function returns system environment for the host stated by hkey
     */
    public abstract Map<String, String> getEnv(String hkey);

    /**
     * Validates file existence
     */
    public abstract boolean fileExists(String hkey, String path);
    
    /**
     * Returns dir where libraries are located
     */
    public abstract String getLibDir(String hkey);
    
    /** Static method to obtain the provider.
     * @return the resolver
     */
    public static synchronized HostInfoProvider getDefault() {
        if (defaultProvider == null) {
            HostInfoProvider provider = Lookup.getDefault().lookup(HostInfoProvider.class);
            defaultProvider = new DefaultProvider(provider);
        }
        return defaultProvider;
    }
    private static HostInfoProvider defaultProvider;

    /**
     * Provider which supplied HostInfo for localhost and can provide HostInfo
     * for other hosts if there is actual provider
     */
    private static class DefaultProvider extends HostInfoProvider {

        private final HostInfoProvider provider;

        public DefaultProvider(HostInfoProvider provider) {
            this.provider = provider;
        }

        @Override
        public PathMap getMapper(String hkey) {
            if (RemoteUtils.isLocalhost(hkey)) {
                return local;
            } else if (provider != null) {
                return provider.getMapper(hkey);
            } else {
                throw getRE();
            }
        }

        @Override
        public Map<String, String> getEnv(String hkey) {
            if (RemoteUtils.isLocalhost(hkey)) {
                return System.getenv();
            } else if (provider != null) {
                return provider.getEnv(hkey);
            } else {
                throw getRE();
            }
        }
        
        @Override
        public String getLibDir(String hkey) {
            if (RemoteUtils.isLocalhost(hkey)) {
                return null;
            } else if (provider != null) {
                return provider.getLibDir(hkey);
            } else {
                throw getRE();
            }
        }
        
        private static PathMap local = new LocalPathMap();

        @Override
        public int getPlatform(String hkey) {
            if (RemoteUtils.isLocalhost(hkey)) {
                return CompilerSetManager.computeLocalPlatform();
            } else if (provider != null) {
                return provider.getPlatform(hkey);
            } else {
                throw getRE();
            }
        }
        
        private static RuntimeException getRE() {
            return new RuntimeException("No HostInfoProvider able to handle remote host was found"); //NOI18N
        }

        private static class LocalPathMap implements PathMap {

            public boolean isRemote(String path, boolean fixMissingPath) {
                return false;
            }

            public String getLocalPath(String rpath) {
                return rpath;
            }

            public String getRemotePath(String lpath) {
                return lpath;
            }
        }

        @Override
        public boolean fileExists(String hkey, String path) {
            if (CompilerSetManager.LOCALHOST.equals(hkey)) {
                return new File(path).exists();
            } else if (provider != null) {
                return provider.fileExists(hkey, path);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
