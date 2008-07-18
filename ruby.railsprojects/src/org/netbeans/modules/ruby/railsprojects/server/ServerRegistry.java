
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstanceProvider;
import org.openide.util.lookup.Lookups;

/**
 * A server registry for servers with Ruby capabilities.
 *
 * TODO: a work in progess. Need to be better integrated with RubyInstanceProvider,
 * possibly implement an instance provider for WEBrick/Mongrel instead of 
 * handling them here.
 * 
 * @author peterw99, Erno Mononen
 */
public class ServerRegistry implements VetoableChangeListener {

    private static ServerRegistry defaultRegistry; 

    private ServerRegistry() {
    }

    public synchronized static ServerRegistry getDefault() {
        if (defaultRegistry == null) {
            defaultRegistry = new ServerRegistry();
            RubyPlatformManager.addVetoableChangeListener(defaultRegistry);
        }
        return defaultRegistry;
    }

    public List<RubyInstance> getServers() {
        List<RubyInstance> result = new ArrayList<RubyInstance>();

        for (RubyInstanceProvider provider : Lookups.forPath("Servers/Ruby").lookupAll(RubyInstanceProvider.class)) {
            result.addAll(provider.getInstances());
        }
        result.addAll(getRubyServers());

        return result;
    }

    List<RubyInstance> getServers(RubyPlatform platform) {
        List<RubyInstance> result = new  ArrayList<RubyInstance>();
        for (RubyInstance each : getServers()) {
            if (each.isPlatformSupported(platform)) {
                result.add(each);
            }
        }
        return result;
    }
    
    List<RubyServer> getRubyServers() {
        List<RubyServer> result = new  ArrayList<RubyServer>();
        for (RubyPlatform each : RubyPlatformManager.getPlatforms()) {
            result.addAll(RubyServerFactory.getInstance(each).getServers());
        }
        return result;
    }
    
    public RubyInstance getServer(String serverId, RubyPlatform platform) {

        for (RubyInstanceProvider provider : Lookups.forPath("Servers/Ruby").lookupAll(RubyInstanceProvider.class)) {
            RubyInstance instance = provider.getInstance(serverId); 
            if (instance != null && instance.isPlatformSupported(platform)) {
                return instance;
            }
        }
        
        for (RubyServer each : RubyServerFactory.getInstance(platform).getServers()) {
            if (each.getServerUri().equals(serverId) && each.isPlatformSupported(platform)) {
                return each;
            }
        }
        return null;
        
    }
    
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (evt.getPropertyName().equals("platforms")) { //NOI18N
            ServerInstanceProviderImpl.getInstance().fireServersChanged();
        }
    }

    /**
     * A factory for Mongrel and WEBrick instances for a Ruby platform. Takes care 
     * of caching the instances and reinitializing 
     * the server list when there are changes in the gems of the platform.
     */
    private static class RubyServerFactory implements PropertyChangeListener {

        private static final Map<RubyPlatform, RubyServerFactory> instances = new HashMap<RubyPlatform, ServerRegistry.RubyServerFactory>();
        private final RubyPlatform platform;
        private final Set<RubyServer> servers = new HashSet<RubyServer>();

        private RubyServerFactory(RubyPlatform platform) {
            this.platform = platform;
        }

        public static synchronized RubyServerFactory getInstance(RubyPlatform platform) {
            RubyServerFactory existing = instances.get(platform);
            if (existing != null) {
                return existing;
            }
            RubyServerFactory result = new RubyServerFactory(platform);
            result.initGlassFish();
            result.initWEBrick();
            result.initMongrel();
            platform.addPropertyChangeListener(result);
            instances.put(platform, result);
            return result;
        }

        public List<RubyServer> getServers() {
            return new ArrayList<RubyServer>(servers);
        }
        
        private void initGlassFish() {
            if(platform.isJRuby()) {
                GemManager gemManager = platform.getGemManager();
                if (gemManager == null) {
                    return;
                }

                List<GemInfo> versions = gemManager.getVersions(GlassFishGem.GEM_NAME);
                GemInfo glassFishGemInfo = versions.isEmpty() ? null : versions.get(0);
                if (glassFishGemInfo == null) {
                    // remove all glassfish from gems
                    for (Iterator<RubyServer> it = servers.iterator(); it.hasNext(); ) {
                        if (it.next() instanceof GlassFishGem) {
                            it.remove();
                        }
                    }
                    return;

                }

                GlassFishGem candidate = new GlassFishGem(platform, glassFishGemInfo);
                if (!servers.contains(candidate)) {
                    servers.add(candidate);
                }
            }
        }

        private void initMongrel() {
            GemManager gemManager = platform.getGemManager();
            if (gemManager == null) {
                return;
            }
            
            String mongrelVersion = gemManager.getLatestVersion(Mongrel.GEM_NAME);
            if (mongrelVersion == null) {
                // remove all mongrels
                for (Iterator<RubyServer> it = servers.iterator(); it.hasNext(); ) {
                    if (it.next() instanceof Mongrel) {
                        it.remove();
                    }
                }
                return;

            }
            Mongrel candidate = new Mongrel(platform, mongrelVersion);
            if (!servers.contains(candidate)) {
                servers.add(candidate);
            }
        }

        private void initWEBrick() {
            WEBrick candidate = new WEBrick(platform);
            if (!servers.contains(candidate)) {
                servers.add(candidate);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("gems")) { //NOI18N
                initGlassFish();
                initMongrel();
                initWEBrick();
                ServerInstanceProviderImpl.getInstance().fireServersChanged();
            }
        }
    }

}
