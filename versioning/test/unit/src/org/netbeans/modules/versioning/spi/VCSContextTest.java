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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.versioning.spi;

import junit.framework.TestCase;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;

import java.io.FileFilter;
import java.io.File;

/**
 * Versioning SPI unit tests of VCSContext.
 * 
 * @author Maros Sandor
 */
public class VCSContextTest extends TestCase {
    
    private File dataRootDir;

    public VCSContextTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir"));
    }

    public void testForEmptyNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[0]);
        assertTrue(ctx.getRootFiles().size() == 0);
        assertTrue(ctx.getFiles().size() == 0);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 0);
    }

    public void testForFileNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "dummy2")), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 2);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 2);
        
        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "workdir/root")), new DummyFileNode(new File(dataRootDir, "workdir/root/a.txt")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().size() == 0);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);        
    }

    public void testForProjectNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyProjectNode(new File(dataRootDir, "workdir/root")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().size() == 1);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 0);
    }
    
    private class DummyFileDilter implements FileFilter {
        
        private final boolean acceptAll;

        public DummyFileDilter() {
            this(true);
        }

        public DummyFileDilter(boolean acceptAll) {
            this.acceptAll = acceptAll;
        }

        public boolean accept(File pathname) {
            return acceptAll;
        }
    }
    
    private class DummyFileNode extends AbstractNode {
        public DummyFileNode(File file) {
            super(Children.LEAF, Lookups.fixed(file));
        }
    }

    private class DummyProjectNode extends AbstractNode {
        
        public DummyProjectNode(File file) {
            super(Children.LEAF, Lookups.fixed(new DummyProject(file)));
        }
    }

    private static class DummyProject implements Project {
        
        private final File file;

        public DummyProject(File file) {
            this.file = file;
        }

        public FileObject getProjectDirectory() {
            return FileUtil.toFileObject(file);
        }
        
        public Lookup getLookup() {
            return Lookups.fixed(file);
        }
    }
}
