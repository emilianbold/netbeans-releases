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
package org.netbeans.modules.cnd.remote.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Sergey Grinev
 */
public class HostMappingsAnalyzer {

    private final PlatformInfo secondPI;
    private final PlatformInfo firstPI;

    public HostMappingsAnalyzer(ExecutionEnvironment remoteExecEnv) {
        this(remoteExecEnv, ExecutionEnvironmentFactory.getLocal());
    }

    public HostMappingsAnalyzer(ExecutionEnvironment secondEnv, ExecutionEnvironment firstEnv) {
        secondPI = PlatformInfo.getDefault(secondEnv);
        firstPI = PlatformInfo.getDefault(firstEnv);
    }

    public Map<String, String> getMappings() {
        Map<String, String> mappingsFirst2Second = new HashMap<String, String>();
        // all maps are host network name -> host local name
        Map<String, String> firstNetworkNames2Inner = populateMappingsList(firstPI, secondPI);
        Map<String, String> secondNetworkNames2Inner = populateMappingsList(secondPI, firstPI);

        if (firstNetworkNames2Inner.size() > 0 && secondNetworkNames2Inner.size() > 0) {
            for (Map.Entry<String, String> firstNetworkName : firstNetworkNames2Inner.entrySet()) {
                for (Map.Entry<String, String> secondNetworkName : secondNetworkNames2Inner.entrySet()) {
                    //TODO: investigate more complex cases
                    if (firstNetworkName.getKey().equals(secondNetworkName.getKey())) {
                        mappingsFirst2Second.put(firstNetworkName.getValue(), secondNetworkName.getValue());
                    }
                }
            }
        }

        for (HostMappingProvider provider : singularProviders) {
            if (provider.isApplicable(secondPI, firstPI)) {
                Map<String, String> map = provider.findMappings(
                        secondPI.getExecutionEnvironment(), firstPI.getExecutionEnvironment());
                mappingsFirst2Second.putAll(map);
            }
            if (provider.isApplicable(firstPI, secondPI)) {
                Map<String, String> map = provider.findMappings(
                        firstPI.getExecutionEnvironment(), secondPI.getExecutionEnvironment());
                mappingsFirst2Second.putAll(map);
            }
        }

        return mappingsFirst2Second;
    }

    // host is one we are searching on
    // other is host in which context we are interested in mappings
    private Map<String, String> populateMappingsList(PlatformInfo hostPlatformInfo, PlatformInfo otherPlatformInfo) {
        Map<String, String> map = new HashMap<String, String>();
        for (HostMappingProvider prov : pairedProviders) {
            if (prov.isApplicable(hostPlatformInfo, otherPlatformInfo)) {
                map.putAll(prov.findMappings(
                        hostPlatformInfo.getExecutionEnvironment(), otherPlatformInfo.getExecutionEnvironment())  );
            }
        }
        return map;
    }

    private static final List<HostMappingProvider> pairedProviders;
    private static final List<HostMappingProvider> singularProviders;
    
    static {
        //providers
        pairedProviders = new ArrayList<HostMappingProvider>();
        singularProviders = new ArrayList<HostMappingProvider>();
        // TODO: should it be Lookup?
        pairedProviders.add(new HostMappingProviderWindows());
        pairedProviders.add(new HostMappingProviderSamba());
        // TODO: this kind of API is st...range 
        singularProviders.add(new HostMappingProviderSolaris());
        singularProviders.add(new HostMappingProviderLinux());
    }
}
