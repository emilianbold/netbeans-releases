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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapper.ResolvedPath;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.MapperEntry;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;

/**
 *
 * @author alsimon
 */
public class PathMapperTest extends NbTestCase {

    public PathMapperTest() {
        super("PathMapperTest");
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        System.getProperties().put("cnd.dwarfdiscovery.trace.read.errors",Boolean.TRUE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testLocalBuildMapper() {
        // build locally, binary avaliable by using paths /net/server
        String path = Dwarf.fileFinder("/net/server/home/user/projects/application/dist/Debug/GNU-MacOSX", "/home/user/projects/application/main.cc");
        assertEquals("/net/server/home/user/projects/application/main.cc", path);
    }

    public void testLocalBuildMapperWin() {
        // build locally, binary avaliable by using paths /net/server
        String path = Dwarf.fileFinder("K:/net/server/home/user/projects/application/dist/Debug/GNU-MacOSX", "C:/home/user/projects/application/main.cc");
        assertEquals("K:/net/server/home/user/projects/application/main.cc", path);
    }
    
    public void testNetBuildMapper() {
        // build by using paths /net/server, executable avaliable locally
        String path = Dwarf.fileFinder("/home/user/projects/application/dist/Debug/GNU-MacOSX", "/net/server/home/user/projects/application/main.cc");
        assertEquals("/home/user/projects/application/main.cc", path);
    }

    public void testNetBuildMapperWin() {
        // build by using paths /net/server, executable avaliable locally
        String path = Dwarf.fileFinder("C:/home/user/projects/application/dist/Debug/GNU-MacOSX", "K:/net/server/home/user/projects/application/main.cc");
        assertEquals("C:/home/user/projects/application/main.cc", path);
    }

    public void testTwoLocalLinkMapper() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("/scratch/user/view/project", "/ade/view/project/main.cc");
        assertEquals("/scratch/user/view/project/main.cc", path);
    }

    public void testTwoLocalLinkMapperWin() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("K:/scratch/user/view/project", "K:/ade/view/project/main.cc");
        assertEquals("K:/scratch/user/view/project/main.cc", path);
    }

    public void testFarmMapper() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data/kdr.c");
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server/ram/data/kdr.c", path);
    }

//    public void testFarmImcludeMapper() {
//        // build by using paths /ade/view/project and /scratch/user/view/project
//        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/ade/b/1226108341/oracle/rdbms/src/hdir");
//        assertEquals(path, "/scratch/user1/view_storage/user1_my_rdbms/oracle/rdbms/src/hdir");
//    }
//
//    public void testFarmImcludeOutMapper() {
//        // build by using paths /ade/view/project and /scratch/user/view/project
//        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/ade/b/1226108341/oracle/oracore/port/include");
//        assertEquals(path, "/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include");
//    }
    
    public void testMapperDetector() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/ade/b/1226108341/oracle/oracore/port/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include", path.getPath());
    }

    public void testMapperDetector2() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/ade/user1_my_rdbms/oracle/oracore/port/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include", path.getPath());
    }

    public void testMapperDetector3() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server/ram/data", path.getPath());
    }

    public void testMapperDetectorAll() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        String root;
        String unknown;
        ResolvedPath path;
        MapperEntry mapperEntry;
        
        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.init(fs, root, unknown));
        
        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/ade/user1_my_rdbms/oracle/oracore/port/include";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.init(fs, root, unknown));

        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/ade/b/1226108341/oracle/oracore/port/include";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.init(fs, root, unknown));
        
        path = mapper.getPath("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/user1_my_rdbms/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/b/1226108341/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        assertEquals(3, mapper.dump().size());
        //for(MapperEntry entry : mapper.dump()) {
        //    System.out.println(entry.from+" -> "+entry.to);
        //}
    }

    public void testMapperDetectorAll2() throws IOException {
        File storage = File.createTempFile("mapper", ".txt");
        System.setProperty("makeproject.pathMapperFile", storage.getPath()); // NOI18N
        BufferedWriter wr = new BufferedWriter(new FileWriter(storage));
        wr.append("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649=/scratch/user1/view_storage/user1_my_rdbms\n");
        wr.append("/ade/user1_my_rdbms=/scratch/user1/view_storage/user1_my_rdbms\n");
        wr.append("/ade/b/1226108341=/scratch/user1/view_storage/user1_my_rdbms\n");
        wr.close();
        
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(new ProjectProxy() {

            @Override
            public boolean createSubProjects() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Project getProject() {
                return null;
            }

            @Override
            public String getMakefile() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getSourceRoot() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getExecutable() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getWorkingFolder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean mergeProjectProperties() {
                throw new UnsupportedOperationException();
            }
        });
        assertEquals(3, mapper.dump().size());
        ResolvedPath path;
        
        path = mapper.getPath("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/user1_my_rdbms/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/b/1226108341/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
    }

    public void testMapperDetectorHomeLink() {
        FS2 fs = new FS2("/home/user1/tmp-link/pkg-config-0.25");
        String root = "/home/user1/tmp-link/pkg-config-0.25";
        String unknown = "/var/tmp/user1-cnd-test-downloads/pkg-config-0.25/glib-1.2.10/gcache.c";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/home/user1/tmp-link", path.getRoot());
        assertEquals("/home/user1/tmp-link/pkg-config-0.25/glib-1.2.10/gcache.c", path.getPath());
    }

    public void testMapperDetectorCTX() {
        FS3 fs = new FS3("/scratch/user1/view_storage/user1_vk_ctx_3");
        String root = "/scratch/user1/view_storage/user1_vk_ctx_3/ctx_src_4/src";
        String unknown = "/ade/user1_vk_ctx_3/oracle/ctx/src/gx/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3/oracle/ctx/src/gx/include", path.getPath());
    }

    public void testMapperDetectorCTX2() {
        FS3 fs = new FS3("/scratch/user1/view_storage/user1_vk_ctx_3");
        String root = "/scratch/user1/view_storage/user1_vk_ctx_3/ctx_src_4/src/ext/zfm";
        String unknown = "/ade/user1_vk_ctx_3/oracle/ctx/src/gx/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.init(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3/oracle/ctx/src/gx/include", path.getPath());
    }
    
    private static final class FS implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS(String prefix) {
            set.add(prefix+"/rdbms");
            set.add(prefix+"/rdbms/src");
            set.add(prefix+"/rdbms/src/server");
            set.add(prefix+"/rdbms/src/server/ram");
            set.add(prefix+"/rdbms/src/server/ram/data");
            set.add(prefix+"/rdbms/src/client");
            set.add(prefix+"/rdbms/src/hdir");
            set.add(prefix+"/rdbms/src/port");
            set.add(prefix+"/rdbms/include");
            set.add(prefix+"/rdbms/port");
            set.add(prefix+"/oracle");
            set.add(prefix+"/oracle/rdbms");
            set.add(prefix+"/oracle/oracore");
            set.add(prefix+"/oracle/oracore/port");
            set.add(prefix+"/oracle/oracore/port/include");
            set.add(prefix+"/oracore");
            set.add(prefix+"/oracore/port");
            set.add(prefix+"/oracore/public");
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }
    }

    private static final class FS2 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS2(String prefix) {
            set.add(prefix+"/glib-1.2.10");
            set.add(prefix+"/glib-1.2.10/gcache.c");
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }
    }

    private static final class FS3 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS3(String prefix) {
            set.add(prefix+"/ctx_src_4/src");
            set.add(prefix+"/oracle/ctx/src/gx/include");
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }
    }
}
