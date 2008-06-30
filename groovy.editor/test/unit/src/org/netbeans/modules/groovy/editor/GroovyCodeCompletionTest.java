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

package org.netbeans.modules.groovy.editor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.completion.CodeCompleter;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author schmidtm
 */
public class GroovyCodeCompletionTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/";

    static {
        GroovyCodeCompletionTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", GroovyCodeCompletionTest.Lkp.class.getName());
        Assert.assertEquals(GroovyCodeCompletionTest.Lkp.class, Lookup.getDefault().getClass());
    }

    
    public static class Lkp extends ProxyLookup {

        private static Lkp DEFAULT;

        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                    new Lookup[]{
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l),
                    });
        }

        public void setLookupsWrapper(Lookup... l) {
            setLookups(l);
        }

    }

    private ClassPath createBootPath () throws MalformedURLException {
        String bootPath = System.getProperty ("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL>roots = new ArrayList<URL> (paths.length);
        for (String path : paths) {
            File f = new File (path);            
            if (!f.exists()) {
                continue;
            }
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add (url);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    
    private ClassPath createCompilePath () {
        return ClassPathSupport.createClassPath(Collections.EMPTY_LIST);
    }
    
    private ClassPath createSourcePath () throws IOException {
        File workdir = this.getWorkDir();
        File root = new File (workdir, "src");
        File test = new File (workdir, "test/unit/data/testfiles/completion");
        
        return ClassPathSupport.createClassPath(new URL[] {root.toURI().toURL(), test.toURI().toURL()});
    }


    public GroovyCodeCompletionTest(String testName) {
        super(testName);
        Logger.getLogger(CodeCompleter.class.getName()).setLevel(Level.FINEST);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CodeCompleter.setTesting(true);
    }


    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }


    public void testMethodCompletion1() throws Exception {
        
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath sourcePath = createSourcePath();

        ClassLoader l = GroovyCodeCompletionTest.class.getClassLoader();
        
        Lkp.DEFAULT.setLookupsWrapper(
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                Lookups.singleton(new ClassPathProvider() {

            public ClassPath findClassPath(FileObject file, String type) {
                if (ClassPath.BOOT.equals(type)) {
                    return bootPath;
                }

                if (ClassPath.SOURCE.equals(type)) {
                    return sourcePath;
                }

                if (ClassPath.COMPILE.equals(type)) {
                    return compilePath;
                }
                return null;
            }
        }));

        checkCompletion(TEST_BASE + "MethodCompletionTestCase.groovy", "new String().^toS", false);
    }


    public void testFileOwnership() throws IOException{

        File inputFile = new File(getDataDir(), TEST_BASE + "MethodCompletionTestCase.groovy");

        if (!inputFile.exists()) {
            fail("File " + inputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(inputFile.getParentFile());
        assertNotNull(fo);

        // I've learned from Martin, that FileOwnerQuery.getOwner() isn't fully supported
        // in tests.
        
        Project p = FileOwnerQuery.getOwner(fo);
        assertNull(p);
    }


    public void testDummy() {
        assertTrue(true);
    }

//    public void testPrefix1() throws Exception {
//        checkPrefix("testfiles/cc-prefix1.js");
//    }
//
//    public void testAutoQueryStrings() throws Exception {
//        assertAutoQuery(QueryType.COMPLETION, "foo^ 'foo'", ".");
//        assertAutoQuery(QueryType.NONE, "'^foo'", ".");
//        assertAutoQuery(QueryType.NONE, "/f^oo/", ".");
//        assertAutoQuery(QueryType.NONE, "\"^\"", ".");
//        assertAutoQuery(QueryType.NONE, "\" foo^ \"", ".");
//    }
//


}
