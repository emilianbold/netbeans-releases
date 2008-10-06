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

package org.netbeans.modules.maven.classpath;

import org.netbeans.modules.maven.classpath.ExcludingResourceImpl;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.apache.maven.model.Resource;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class ExcludingResourceImplTest extends TestCase {
    
    public ExcludingResourceImplTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetRoots() throws Exception {
        System.out.println("getRoots");
        // oh well, comment out the test. it seems to be OS platform dependent.
        
//        File file = FileUtil.normalizeFile(new File("/home/mkleint2/tmp"));
//            
//        ExcludingResourceImpl instance = new ExcludingResourceImpl2(false, 
//                Collections.singletonList(createRes(file.getAbsolutePath(),
//                new String[] {
//                    "NOTE.txt",
//                    "LICENSE.txt"
//                },
//                null)));
//        URL expResult = new URL("file:/"+file.getAbsolutePath()+"/");
//        URL[] result = instance.getRoots();
//        assertEquals(1, result.length);
//        
//        assertEquals(expResult, result[0]);
    }

    public void testIncludes() {
        System.out.println("includes");
        File file = FileUtil.normalizeFile(new File("/home/mkleint2/tmp/"));
        ExcludingResourceImpl instance = new ExcludingResourceImpl2(false, 
                Collections.singletonList(createRes(file.getAbsolutePath(),
                new String[] {
                    "NOTE.txt",
                    "LICENSE.txt"
                },
                null)));
        URL url = instance.getRoots()[0];
        assertFalse(instance.includes(url, "src/test/java"));
        assertFalse(instance.includes(url, "Note.txt"));
        assertFalse(instance.includes(url, "src/LICENSE.txt"));
        assertTrue(instance.includes(url, "LICENSE.txt"));
        
        instance = new ExcludingResourceImpl2(false, 
                Collections.singletonList(createRes(file.getAbsolutePath(),
                new String[] {
                    "**/Bundle.properties",
                    "*/Bundle_ja.properties",
                    "org/milos/**/*.gif"
                },
                new String[] {
                    "org/milos/obsolete/**/Bundle.properties",
                    "**/xman.gif"
                }
        )));
        url = instance.getRoots()[0];
        assertTrue(instance.includes(url, "Bundle.properties"));
        assertTrue(instance.includes(url, "org/milos/Bundle.properties"));
        assertFalse(instance.includes(url, "org/milos/obsolete/Bundle.properties"));
        assertFalse(instance.includes(url, "org/milos/obsolete/xman/Bundle.properties"));
        assertFalse(instance.includes(url, "org/milos/Bundle_ja.properties"));
        assertTrue(instance.includes(url, "org/Bundle_ja.properties"));
        
        assertTrue(instance.includes(url, "org/milos/xman/xman2.gif"));
        assertFalse(instance.includes(url, "org/milos/xman2/xman.gif"));
        
        instance = new ExcludingResourceImpl2(false, 
                Collections.singletonList(createRes(file.getAbsolutePath(),
                null, null
        )));
        url = instance.getRoots()[0];
        
        assertTrue(instance.includes(url, "Bundle.properties"));
        assertTrue(instance.includes(url, "org/milos/Bundle.properties"));
        assertTrue(instance.includes(url, "org/milos/obsolete/Bundle.properties"));
        assertTrue(instance.includes(url, "org/milos/obsolete/xman/Bundle.properties"));
        assertTrue(instance.includes(url, "org/milos/Bundle_ja.properties"));
        assertTrue(instance.includes(url, "org/Bundle_ja.properties"));
        
    }

    private Resource createRes(String basedir, String[] includes, String[] excludes) {
        Resource res = new Resource();
        res.setDirectory(basedir);
        if (includes != null) {
            res.setIncludes(Arrays.asList(includes));
        }
        if (excludes != null) {
            res.setExcludes(Arrays.asList(excludes));
        }
        return res;
    }
    
    private class ExcludingResourceImpl2 extends ExcludingResourceImpl {
        private List<Resource> resources;

        ExcludingResourceImpl2(boolean test, List<Resource> res) {
            super(test);
            resources = res;
        }
        
        @Override
        protected File getBase() {
            return new File(System.getProperty("user.home"));
        }

        @Override
        protected List<Resource> getResources(boolean istest) {
            return resources;
        }
        
    }

}
