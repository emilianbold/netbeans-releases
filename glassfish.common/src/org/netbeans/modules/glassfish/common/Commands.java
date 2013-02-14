/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map.Entry;
import java.util.*;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.Utils;

/**
 * Abstraction of commands for V3 server administration
 *
 * @author Peter Williams
 */
public class Commands {

    // ------------------------------------------------------------------------
    // Specific server commands.
    // ------------------------------------------------------------------------
    /**
     * Command to start a server domain
     */
    public static final ServerCommand START = new ServerCommand("start-domain") { // NOI18N
    };
    /**
     * Command to stop a server domain
     */
    public static final ServerCommand STOP = new ServerCommand("stop-domain") { // NOI18N
    };

    public static final ServerCommand RESTART = new ServerCommand("restart-domain") { // NOI18N
    };

    /**
     * Command to list applications current deployed on the server.
     * Uses list-components
     */
    public static final class ListComponentsCommand extends ServerCommand {

        private final String container;
        private Manifest list;
        private Map<String, List<String>> appMap;

        public ListComponentsCommand() {
            this(null,null);
        }

        public ListComponentsCommand(final String container,String target) {
            super("list-components"); // NOI18N
            if (null != target) {
                query = "DEFAULT=" + target; // NOI18N
            }
            this.container = container;
        }

        public String[] getContainers() {
            String[] result = null;
            if(appMap != null && appMap.size() > 0) {
                Set<String> containers = appMap.keySet();
                result = containers.toArray(new String[containers.size()]);
            }
            return result != null ? result : new String[0];
        }

        public Map<String, List<String>> getApplicationMap() {
            // !PW Can still modify sublist... is there a better structure?
            if(appMap != null) {
                return Collections.unmodifiableMap(appMap);
            } else {
                return Collections.emptyMap();
            }
        }

        @Override
        public void readManifest(Manifest manifest) throws IOException {
            list = manifest;
        }

        @Override
        public boolean processResponse() {
            if(list == null) {
                return false;
            }

            String appsList = null;
            try {
                String tmp = list.getMainAttributes().getValue("children"); // NOI18N
                if (null != tmp) {
                    appsList = tmp;
                    appsList = URLDecoder.decode(tmp, "UTF-8"); // NOI18N
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, "Could not URL decode with UTF-8"); //NOI18N
            } catch (IllegalArgumentException iae) {
                // ignore this for now
            }
            if(appsList == null || appsList.length() == 0) {
                // no applications deployed...
                return true;
            }

            String[] apps = appsList.split(";"); // NOI18N
            for(String appKey : apps) {
                if("null".equals(appKey)) { // NOI18N
                    Logger.getLogger("glassfish").log(Level.WARNING, "list-components contains an invalid result.  " + "Check server log for possible exceptions."); // NOI18N
                    continue;
                }

                String[] keys = appKey.split("[<>]");
                String name = keys[0];
                if(name == null || name.length() == 0) {
                    Logger.getLogger("glassfish").log(Level.FINE, "Skipping application with no name..."); // NOI18N  FIXME better log message.
                    continue;
                }
                String engine = getPreferredEngine(keys[1]); // NOI18N

                // Add app to proper list in result map
                if(appMap == null) {
                    appMap = new HashMap<String, List<String>>();
                }
                List<String> appList = appMap.get(engine);
                if(appList == null) {
                    appList = new ArrayList<String>();
                    appMap.put(engine, appList);
                }
                appList.add(name);
            }

            return true;
        }
        // XXX temporary patch to handle engine descriptions like <web, ejb>
        // until we have better display semantics for such things.
        // XXX bias order of list for JavaONE demos.
        private static final List<String> engineBias =
                Arrays.asList(new String[]{"ear", "jruby", "web", "ejb", "appclient", "connector"}); // NOI18N

        private String getPreferredEngine(String engineList) {
            String[] engines = engineList.split(",");  // NOI18N
            String engine = null;
            int bias = -1;
            for(int i = 0; i < engines.length; i++) {
                if(!skipContainer(engines[i])) {
                    engines[i] = engines[i].trim();
                    int newBias = engineBias.indexOf(engines[i]);
                    if(newBias >= 0 && (bias == -1 || newBias < bias)) {
                        bias = newBias;
                    }
                    if(engine == null) {
                        engine = engines[i];
                    }
                }
            }
            if(bias != -1) {
                engine = engineBias.get(bias);
            } else if(engine == null) {
                engine = "unknown"; // NOI18N
            }
            return engine;
        }

        /**
         * For skipping containers we don't care about.
         *
         * @param container
         * @return
         */
        private boolean skipContainer(String currentContainer) {
            return container != null ? !container.equals(currentContainer) :
                    "security_ContractProvider".equals(currentContainer); // NOI18N
        }
    };
    
    private static void appendLibraries(StringBuilder cmd, File[] libraries) {
        cmd.append(ServerCommand.PARAM_SEPARATOR).append("libraries="); // NOI18N
        boolean firstOne = true;
        for (File f : libraries) {
            if (!firstOne) {
                cmd.append(",");
            }
            cmd.append(f.getPath()); // NOI18N
            firstOne = false;
        }
    }

    /**
     * Command to deploy a directory
     */
    public static final class DeployCommand extends ServerCommand {

        private final boolean isDirDeploy;
        private final File path;

        public DeployCommand(final File path, final String name, final String contextRoot, final Boolean preserveSessions, final Map<String,String> properties, File[] libraries, String target) {
            super("deploy"); // NOI18N

            this.isDirDeploy = path.isDirectory();
            this.path = path;
            
            StringBuilder cmd = new StringBuilder(128);
            cmd.append("DEFAULT="); // NOI18N
            cmd.append(path.getAbsolutePath());
            if(name != null && name.length() > 0) {
                cmd.append(PARAM_SEPARATOR).append("name="); // NOI18N
                cmd.append(Utils.sanitizeName(name));
            }
            if(contextRoot != null && contextRoot.length() > 0) {
                cmd.append(PARAM_SEPARATOR).append("contextroot="); // NOI18N
                cmd.append(contextRoot);
            }
            if (libraries.length > 0) {
                appendLibraries(cmd, libraries);
            }
            cmd.append(PARAM_SEPARATOR).append("force=true"); // NOI18N
            addProperties(cmd,properties);
            if (null != target) {
                cmd.append(PARAM_SEPARATOR).append("target="+target);  // NOI18N
            }
            query = cmd.toString();
        }

        @Override
        public String getContentType() {
            return isDirDeploy ? null : "application/zip"; // NOI18N
        }

        @Override
        public boolean getDoOutput() {
            return !isDirDeploy;
        }

        @Override
        public InputStream getInputStream() {
                if (isDirDeploy) {
                    return null;
                } else {
                    try {
                        return new FileInputStream(path);
                    } catch (FileNotFoundException fnfe) {
                        Logger.getLogger("glassfish").log(Level.INFO, path.getPath(), fnfe); // NOI18N
                        return null;
                    }
            }
        }

        @Override
        public String getRequestMethod() {
            return isDirDeploy ? super.getRequestMethod() : "POST"; // NOI18N
        }

        @Override
        public String getInputName() {
            return path.getName();
        }

        @Override
        public String getLastModified() {
            return Long.toString(path.lastModified());
        }

        private void addProperties(StringBuilder cmd, Map<String,String> properties) {
            if (null != properties && properties.size() > 0) {
                cmd.append(ServerCommand.PARAM_SEPARATOR).append("properties="); // NOI18N
                int i = 0;
                for (Entry<String,String> e : properties.entrySet()) {
                    String k = e.getKey();
                    String v = e.getValue();
                    if (i > 0) {
                        cmd.append(":"); // NOI18N
                    }
                    cmd.append(k).append("=").append(v);
                }
            }
        }
    }

    static class ListWebservicesCommand extends ServerCommand {

        private Manifest manifest;
        private List<String> wsList;

        public ListWebservicesCommand() {
            super("__list-webservices"); // NOI18N
        }

        public List<String> getWebserviceList() {
            // !PW Can still modify sublist... is there a better structure?
            if(wsList != null) {
                return Collections.unmodifiableList(wsList);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public void readManifest(Manifest manifest) throws IOException {
            this.manifest = manifest;
        }

        @Override
        public boolean processResponse() {
            if(manifest == null) {
                return false;
            }
            
            Map <String, String> filter = new HashMap<String, String>();

            Iterator<String> keyIterator = manifest.getEntries().keySet().iterator();
            while (keyIterator.hasNext()) {
                String k = keyIterator.next();
                if (!k.contains("address:/")) // NOI18N
                    continue;
                if (k.contains("address:/wsat-wsat")) // NOI18N
                    continue;
                if (k.contains("address:/__wstx-services")) // NOI18N
                    continue;
                String a = k.replaceFirst(".* address:/", "").replaceFirst("\\. .*", ""); // NOI18N
                if (filter.containsKey(a))
                    continue;
                filter.put(a,a);
                if(wsList == null) {
                    wsList = new ArrayList<String>();
                }
                wsList.add(a);
            }

            return true;
        }
    }

    public static final class StartCluster extends ServerCommand {
        public StartCluster(String target) {
            super("start-cluster"); // NOI18N
            query = "DEFAULT="+target; // NOI18N
        }
    }

    public static final class StartInstance extends ServerCommand {
        public StartInstance(String target) {
            super("start-instance");  // NOI18N
            query = "DEFAULT="+target; // NOI18N
        }
    }
    public static final class StopCluster extends ServerCommand {
        public StopCluster(String target) {
            super("stop-cluster"); // NOI18N
            query = "DEFAULT="+target; // NOI18N
        }
    }

    public static final class StopInstance extends ServerCommand {
        public StopInstance(String target) {
            super("stop-instance"); // NOI18N
            query = "DEFAULT="+target; // NOI18N
        }
    }
}

