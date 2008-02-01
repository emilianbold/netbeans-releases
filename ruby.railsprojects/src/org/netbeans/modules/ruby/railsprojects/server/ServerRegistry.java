
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
package org.netbeans.modules.ruby.railsprojects.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstanceProvider;
import org.openide.util.lookup.Lookups;

/**
 * A server registry for servers with Ruby capabilities.
 *
 * TODO: a work in progess
 * 
 * @author peterw99, Erno Mononen
 */
class ServerRegistry {

    private static final ServerRegistry defaultRegistry = new ServerRegistry();
    private final List<RubyInstance> servers = new ArrayList<RubyInstance>();
    private static Map<RubyPlatform, List<RubyServer>> rubyServers;

    private ServerRegistry() {
    }

    public static ServerRegistry getDefault() {
        return defaultRegistry;
    }

    public List<RubyInstance> getServers() {
        for (RubyInstanceProvider provider : Lookups.forPath("Servers/Ruby").lookupAll(RubyInstanceProvider.class)) {
            servers.addAll(provider.getInstances());
        }
        servers.addAll(getRubyServers());
        return servers;
    }

    static List<? extends RubyInstance> getServers(RubyPlatform platform) {
        return getServerMap().get(platform);
    }

    static RubyInstance getServer(String uri, RubyPlatform platform) {
        for (RubyInstance each : getServerMap().get(platform)) {
            if (each.getServerUri().equals(uri)) {
                return each;
            }
        }
        return null;
    }
    
    private static Map<RubyPlatform, List<RubyServer>> getServerMap(){
        if (rubyServers == null) {
            rubyServers = new HashMap<RubyPlatform, List<RubyServer>>();
            for (RubyPlatform platform : RubyPlatformManager.getPlatforms()) {
                rubyServers.put(platform, getServersFor(platform));
            }
        }
        return Collections.<RubyPlatform, List<RubyServer>>unmodifiableMap(rubyServers);
    }
    
    static List<RubyServer> getRubyServers() {
        List<RubyServer> result = new ArrayList<RubyServer>();
        for (List<RubyServer> each : getServerMap().values()) {
            result.addAll(each);
        }
        return result;
    }

    private static List<RubyServer> getServersFor(RubyPlatform platform) {
        List<RubyServer> result = new  ArrayList<RubyServer>();
        // assume there is always webrick when creating rails applications
        result.add(new WEBrick(platform));
        GemManager gemManager = platform.getGemManager();
        if (gemManager == null) {
            return result;
        }
        String mongrelVersion = gemManager.getVersion(Mongrel.GEM_NAME);
        if (mongrelVersion != null) {
            result.add(new Mongrel(platform, mongrelVersion));
        }
        return result;
    }

}
