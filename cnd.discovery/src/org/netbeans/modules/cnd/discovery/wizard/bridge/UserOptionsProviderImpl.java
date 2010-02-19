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

package org.netbeans.modules.cnd.discovery.wizard.bridge;

import org.netbeans.modules.cnd.discovery.api.QtInfoProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider.class)
public class UserOptionsProviderImpl implements UserOptionsProvider {

    public UserOptionsProviderImpl(){
    }

    @Override
    public List<String> getItemUserIncludePaths(List<String> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res =new ArrayList<String>(includes);
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            for(PackageConfiguration pc : getPackages(compilerOptions.getAllOptions(compiler), makeConfiguration)) {
                res.addAll(pc.getIncludePaths());
            }
        }
        if (makeConfiguration.isQmakeConfiguration()) {
            res.addAll(QtInfoProvider.getDefault().getQtIncludeDirectories(makeConfiguration));
        }
        return res;
    }

    @Override
    public List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res =new ArrayList<String>(macros);
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            for(PackageConfiguration pc : getPackages(options, makeConfiguration)) {
                res.addAll(pc.getMacros());
            }
            if (options.indexOf("-fopenmp") >= 0) { // NOI18N
                res.add("_OPENMP=200505"); // NOI18N
            } else if (options.indexOf("-xopenmp") >= 0) { // NOI18N
                int i = options.indexOf("-xopenmp"); // NOI18N
                String rest = options.substring(i+8);
                if (rest.length()==0 || rest.charAt(0)==' ' || rest.startsWith("=parallel") || rest.startsWith("=noopt")) { // NOI18N
                    res.add("_OPENMP"); // NOI18N
                }
            }
        }
        return res;
    }
    
    private List<PackageConfiguration> getPackages(String s, MakeConfiguration conf){
        List<PackageConfiguration> res = new ArrayList<PackageConfiguration>();
        while(true){
            int i = s.indexOf("`pkg-config "); // NOI18N
            if (i >= 0) {
                String pkg = s.substring(i+12);
                int j = pkg.indexOf('`'); // NOI18N
                if (j > 0) {
                    pkg = pkg.substring(0,j).trim();
                    s = s.substring(i+12+j+1);
                    StringTokenizer st = new StringTokenizer(pkg);
                    boolean readFlags = false;
                    String findPkg = null;
                    while(st.hasMoreTokens()) {
                        String aPkg = st.nextToken();
                        if (aPkg.equals("--cflags")) { //NOI18N
                            readFlags = true;
                            continue;
                        }
                        if (aPkg.startsWith("-")) { //NOI18N
                            readFlags = false;
                            continue;
                        }
                        findPkg = aPkg;
                    }
                    if (readFlags && findPkg != null) {
                        PkgConfig configs = PkgConfigManager.getDefault().getPkgConfig(conf);
                        PackageConfiguration config = configs.getPkgConfig(findPkg);
                        if (config != null){
                            res.add(config);
                        }
                    }
                    continue;
                }
            }
            return res;
        }
    }
}
