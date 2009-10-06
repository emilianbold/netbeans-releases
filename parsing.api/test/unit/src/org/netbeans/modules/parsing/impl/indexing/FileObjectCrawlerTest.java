/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class FileObjectCrawlerTest extends NbTestCase {

    public FileObjectCrawlerTest(String name) {
        super(name);
    }


    public void testIncludesExcludes() throws IOException {
        clearWorkDir();
        File wd = getWorkDir();
        final FileObject wdFO = FileUtil.toFileObject(wd);
        final FileObject src = FileUtil.createFolder(wdFO, "src");
        final FileObject cache = FileUtil.createFolder(wdFO, "cache");

        CacheFolder.setCacheFolder(cache);

        assertNotNull(src);

        FileUtil.createData(src, "p1/Included1.java");
        FileUtil.createData(src, "p1/Included2.java");
        FileUtil.createData(src, "p1/a/Included3.java");
        FileUtil.createData(src, "p1/a/Included4.java");
        FileUtil.createData(src, "p2/Excluded1.java");
        FileUtil.createData(src, "p2/Excluded2.java");
        FileUtil.createData(src, "p2/a/Excluded3.java");
        FileUtil.createData(src, "p2/a/Excluded4.java");

        ClassPath cp = ClassPathSupport.createClassPath(Arrays.asList(new FilteringPathResourceImplementation() {
            private final Pattern p = Pattern.compile("p1/.*");

            public boolean includes(URL root, String resource) {
                return p.matcher(resource).matches();
            }

            public URL[] getRoots() {
                try {
                    return new URL[]{src.getURL()};
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public ClassPathImplementation getContent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        }));

        Collection<IndexableImpl> resources = new FileObjectCrawler(src, false, cp.entries().get(0), new CancelRequest() {
            public boolean isRaised() {
                return false;
            }
        }).getResources();

        assertEquals(4, resources.size());

        Set<String> out = new HashSet<String>();

        for (IndexableImpl i : resources) {
            out.add(i.getRelativePath());
        }

        Set<String> golden = new HashSet<String>(Arrays.asList("p1/Included1.java",
                                                           "p1/Included2.java",
                                                           "p1/a/Included3.java",
                                                           "p1/a/Included4.java"));
        assertEquals(golden, out);
    }
}