/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;

/**
 *
 * @author Alexander Simon
 */
public class RelocatablePathMapperImpl implements RelocatablePathMapper {
    public static final Logger LOG = Logger.getLogger(RelocatablePathMapperImpl.class.getName());
    private static final boolean TEST = false;
    private final List<MapperEntry> mapper = new ArrayList<MapperEntry>();
    /**
     * Local path mapper file.
     * If file specified, discovery uses it for mapping build artifacts:
     * <pre>
     * -J-Dmakeproject.pathMapper.file=path_to_path_mapper
     * </pre>
     * File format is:
     * <pre>
     * beginning_of_path_of_build_artifacts_1=beginning_of_sources_path_2
     * beginning_of_path_of_build_artifacts_2=beginning_of_sources_path_2
     * ...
     * </pre>
     * Order is important.
     */
    private final String PATH_MAPPER_FILE = System.getProperty("makeproject.pathMapper.file"); // NOI18N
    /**
     * By default IDE tries to discover path mapper by analyzing source root.
     * If IDE automatically discovers wrong path mapper,
     * you can provide own path mapper see {@link org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl#PATH_MAPPER_FILE)}
     * or forbid discovering by this flag:
     * <pre>
     * -J-Dmakeproject.pathMapper.forbid_auto=true
     * </pre>
     * To trace path mapper logic use flag:
     * <pre>
     * -J-Dorg.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.level=FINE
     * or
     * -J-Dorg.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.level=FINER
     * </pre>
     */
    private final boolean FORBID_AUTO_PATH_MAPPER = Boolean.getBoolean("makeproject.pathMapper.forbid_auto"); // NOI18N
    
    public RelocatablePathMapperImpl(ProjectProxy project) {
        if(project != null) {
            Project makeProject = project.getProject();
            List<String> list = null;
            if (makeProject != null) {
                // init path mapper from project
                //PathMapperStorage storage = makeProject.getLookup().lookup(PathMapperStorage);
                //list = storage.getList();
            }
            if (list == null || list.isEmpty()) {
                String mapperFile = PATH_MAPPER_FILE;
                if (mapperFile != null) {
                    File file = new File(mapperFile);
                    if (file.exists() && file.canRead()) {
                        BufferedReader in = null;
                        try {
                            in = new BufferedReader(new FileReader(file));
                            list = new ArrayList<String>();
                            while (true) {
                                String line = in.readLine();
                                if (line == null) {
                                    break;
                                }
                                line = line.trim();
                                int i = line.indexOf('='); // NOI18N
                                if (i > 0) {
                                    list.add(line.substring(0,i));
                                    list.add(line.substring(i+1));
                                }
                            }
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, "Cannot read mapper file {0}", mapperFile); // NOI18N
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
            if (list != null) {
                for(int i = 0; i < list.size(); i+=2) {
                    if (i+1 < list.size()) {
                        mapper.add(new MapperEntry(list.get(i), list.get(i+1)));
                        LOG.log(Level.FINE, "Init path map {0} -> {1}", new Object[]{list.get(i), list.get(i+1)}); // NOI18N
                    }
                }
            }
        }
    }

    @Override
    public ResolvedPath getPath(String path) {
        path = path.replace('\\', '/'); //NOI18N
        synchronized(mapper) {
            for(MapperEntry entry : mapper) {
                if (path.startsWith(entry.from)) {
                    if (path.equals(entry.from)) {
                        return new ResolvedPathImpl(entry.to, entry.to);
                    } else {
                        if (path.charAt(entry.from.length()) == '/') { //NOI18N
                            return new ResolvedPathImpl(entry.to, entry.to + path.substring(entry.from.length()));
                        }
                    }
                }
            }
        }
        return null;
    }
    
    List<MapperEntry> dump() {
        List<MapperEntry> res;
        synchronized(mapper) {
            res = new ArrayList<MapperEntry>(mapper);
        }
        return res;
    }
    
    @Override
    public boolean discover(FS fs, String root, String unknown) {
        if (FORBID_AUTO_PATH_MAPPER) {
            return false;
        }
        MapperEntry mapperEntry = getMapperEntry(fs, root, unknown);
        if (mapperEntry == null) {
            LOG.log(Level.FINER, "Cannot discover path map of root {0} and canidate {1}", new Object[]{root, unknown}); // NOI18N
            return false;
        }
        LOG.log(Level.FINE, "Discover path map of root {0} and canidate {1}", new Object[]{root, unknown}); // NOI18N
        LOG.log(Level.FINE, "Found path map {0} -> {1}", new Object[]{mapperEntry.from, mapperEntry.to}); // NOI18N
        synchronized(mapper) {
            if (!mapper.contains(mapperEntry)) {
                mapper.add(mapperEntry);
            }
        }
        return true;
    }
    
    @Override
    public boolean add(String from, String to) {
        MapperEntry mapperEntry = new MapperEntry(from, to);
        synchronized(mapper) {
            mapper.add(mapperEntry);
        }
        return true;
    }

    private MapperEntry getMapperEntry(FS fs, String root, String unknown) {
        root = root.replace('\\', '/'); //NOI18N
        boolean driverRoot = false;
        if (root.startsWith("/")) { //NOI18N
            root = root.substring(1);
        } else {
            driverRoot = true;
        }
        boolean driverPath = false;
        unknown = unknown.replace('\\', '/'); //NOI18N
        if (unknown.startsWith("/")) { //NOI18N
            unknown = unknown.substring(1);
        } else {
            driverPath = true;
        }
        String[] rootSegments = root.split("/"); //NOI18N
        String[] unknownSegments = unknown.split("/"); //NOI18N
        int min = 0;
        for(int k = 0; k < Math.min(unknownSegments.length, rootSegments.length); k++) {
            if (!unknownSegments[k].equals(rootSegments[k])) {
                break;
            }
            min = k;
        }
        if (min > 2) {
            return null;
        }
        for(int k = 1; k < unknownSegments.length; k++) {
            for(int i = rootSegments.length - 1; i > 1; i--) {
                StringBuilder buf = new StringBuilder();
                for(int j = 0; j < i; j++) {
                    buf.append('/'); //NOI18N
                    buf.append(rootSegments[j]);
                }
                if (TEST) {
                    buf.append('|'); //NOI18N
                }
                for(int j = k; j < unknownSegments.length; j++) {
                    buf.append('/'); //NOI18N
                    buf.append(unknownSegments[j]);
                }
                String path = driverRoot ? buf.substring(1) : buf.toString();
                if (TEST) {
                    System.out.println(path);
                    path = path.substring(0, path.indexOf('|'))+path.substring(path.indexOf('|')+1); //NOI18N
                }
                if (fs.exists(path)) {
                    if (k < 2 && k < unknownSegments.length -1 && i < rootSegments.length - 1) {
                        if (unknownSegments[k].equals(rootSegments[i])) {
                            k++;
                            i++;
                        }
                    }
                    StringBuilder from = new StringBuilder();
                    for(int j = 0; j < k; j++) {
                        from.append('/'); //NOI18N
                        from.append(unknownSegments[j]);
                    }
                    StringBuilder to = new StringBuilder();
                    for(int j = 0; j < i; j++) {
                        to.append('/'); //NOI18N
                        to.append(rootSegments[j]);
                    }
                    String aFrom = driverPath ? from.substring(1) : from.toString();
                    String aTo = driverRoot ? to.substring(1) : to.toString();
                    return new MapperEntry(aFrom, aTo);
                }
            }
        }
        return null;
    }
    
    static final class MapperEntry {
        final String from;
        final String to;
        MapperEntry(String from, String to) {
            this.to = to;
            this.from = from;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + (this.from != null ? this.from.hashCode() : 0);
            hash = 71 * hash + (this.to != null ? this.to.hashCode() : 0);
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
            final MapperEntry other = (MapperEntry) obj;
            if ((this.from == null) ? (other.from != null) : !this.from.equals(other.from)) {
                return false;
            }
            if ((this.to == null) ? (other.to != null) : !this.to.equals(other.to)) {
                return false;
            }
            return true;
        }
    }

    static final class ResolvedPathImpl implements ResolvedPath {
        private final String root;
        private final String path;
        ResolvedPathImpl(String root, String path) {
            this.root = root;
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getRoot() {
            return root;
        }
    }
}
