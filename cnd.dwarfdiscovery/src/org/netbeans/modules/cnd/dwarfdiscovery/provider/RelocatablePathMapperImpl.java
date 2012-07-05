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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander Simon
 */
public class RelocatablePathMapperImpl implements RelocatablePathMapper {
    private static final boolean TEST = false;
    private final List<MapperEntry> mapper = new ArrayList<MapperEntry>();

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
    public boolean init(FS fs, String root, String unknown) {
        MapperEntry mapperEntry = getMapperEntry(fs, root, unknown);
        if (mapperEntry == null) {
            return false;
        }
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
        for(int k = 0; k < unknownSegments.length; k++) {
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
