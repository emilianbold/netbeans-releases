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
 * Portions Copyrighted 2011 Oracle
 */
package org.netbeans.modules.netbinox;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.osgi.launch.EquinoxFactory;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
@ServiceProvider(
    service=FrameworkFactory.class,
    supersedes={ "org.eclipse.osgi.launch.EquinoxFactory" },
    position=-10
)
public class NetbinoxFactory implements FrameworkFactory {
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.netbinox"); // NOI18N

    @SuppressWarnings("unchecked")
    public Framework newFramework(Map map) {
        Map<String,Object> configMap = new HashMap<String,Object>();
        configMap.putAll(map);
        configMap.put("osgi.hook.configurators.exclude", // NOI18N
            "org.eclipse.core.runtime.internal.adaptor.EclipseLogHook" // NOI18N
//            + ",org.eclipse.core.runtime.internal.adaptor.EclipseClassLoadingHook" // NOI18N
        );
        configMap.put("osgi.hook.configurators.include", NetbinoxHooks.class.getName()); // NOI18N
        configMap.put("osgi.user.area.default", configMap.get(Constants.FRAMEWORK_STORAGE)); // NOI18N
        configMap.put("osgi.instance.area.default", System.getProperty("netbeans.user")); // NOI18N
        configMap.put("osgi.install.area", System.getProperty("netbeans.home")); // NOI18N
        // some useless value
        configMap.put("osgi.framework.properties", System.getProperty("netbeans.user")); // NOI18N

        Object rawBundleMap = configMap.get("felix.bootdelegation.classloaders"); // NOI18N

        Map<Bundle,ClassLoader> bundleMap;
        if (rawBundleMap == null) {
            bundleMap = null;
        } else {
            bundleMap = (Map<Bundle,ClassLoader>)rawBundleMap;
        }

        NetbinoxHooks.registerMap(bundleMap);
        NetbinoxHooks.registerArchive((NetigsoArchive)configMap.get("netigso.archive")); // NOI18N

        String loc = EquinoxFactory.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        int file = loc.indexOf("file:");
        if (file > 0) {
            loc = loc.substring(file);
        }
        int exclaim = loc.indexOf("!");
        if (exclaim > 0) {
            loc = loc.substring(0, exclaim);
        }
        configMap.put("osgi.framework", loc);
        return new Netbinox(configMap);
    }
}
