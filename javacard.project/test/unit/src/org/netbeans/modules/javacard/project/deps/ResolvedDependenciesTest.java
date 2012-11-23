/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.javacard.project.deps;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class ResolvedDependenciesTest {

    public ResolvedDependenciesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    Dependency a;
    Dependency b;
    Dependency c;
    Dependency d;
    Dependency e;
    Dependencies deps;
    ResolvedDependencies rd;

    Dependency[] all;

    File tmp = new File (System.getProperty("java.io.tmpdir"));
    File af;
    File bf;
    File cf;
    File df;
    File ef;
    File projf;

    FileObject afo;
    FileObject bfo;
    FileObject cfo;
    FileObject dfo;
    FileObject efo;
    FileObject projfo;
    Eval eval = new Eval();

    @Before
    public void setUp() throws Exception {
        for (char h='a'; h < 'f'; h++) {
            File f = new File (tmp, h + ".jar");
            if (!f.exists()) {
                f.createNewFile();
            }
        }
        af = new File (tmp, "a.jar");
        bf = new File (tmp, "b.jar");
        cf = new File (tmp, "c.jar");
        df = new File (tmp, "d.jar");
        ef = new File (tmp, "e.jar");
        projf = new File(tmp, "project");
        if (!projf.exists()) {
            projf.mkdir();
        }

        afo = FileUtil.toFileObject(FileUtil.normalizeFile(af));
        bfo = FileUtil.toFileObject(FileUtil.normalizeFile(bf));
        cfo = FileUtil.toFileObject(FileUtil.normalizeFile(cf));
        dfo = FileUtil.toFileObject(FileUtil.normalizeFile(df));
        efo = FileUtil.toFileObject(FileUtil.normalizeFile(ef));
        projfo = FileUtil.toFileObject(FileUtil.normalizeFile(projf));

        deps = new Dependencies();
        deps.add(a = new Dependency("a", DependencyKind.RAW_JAR, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add(b = new Dependency("b", DependencyKind.CLASSIC_LIB, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add(c = new Dependency("c", DependencyKind.EXTENSION_LIB, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add(d = new Dependency("d", DependencyKind.JAR_WITH_EXP_FILE, DeploymentStrategy.ALREADY_ON_CARD));
        e = new Dependency("e", DependencyKind.CLASSIC_LIB_JAR, DeploymentStrategy.ALREADY_ON_CARD);
        DependenciesResolver res = new DependenciesResolver(projfo, eval);
        rd = new ResolvedDependencies(deps, res) {

            @Override
            protected void doSave() throws IOException {
                System.err.println("doSave");
            }
        };
        all = new Dependency[] { a, b, c, d, e };
    }

    @After
    public void tearDown() throws Exception {
        af.delete();
        bf.delete();
        cf.delete();
        df.delete();
        ef.delete();
        projf.delete();
    }

    @Test
    public void testAll() {
        System.out.println("all");
        List<Dependency> l = new ArrayList<Dependency> (Arrays.asList(a, b, c, d));
        List<Dependency> ll = new ArrayList<Dependency>();
        assertEquals (l.size(), rd.all().size());
        for (ResolvedDependency r : rd.all()) {
            ll.add(r.dep());
            assertEquals (r.dep(), r.getDependency());
            assertSame (r.dep(), r.getDependency());
        }
        assertEquals (l, ll);
        Map<ArtifactKind, String> m = new HashMap<ArtifactKind, String>();
        m.put (ArtifactKind.ORIGIN, System.getProperty("java.io.tmpdir"));
        rd.add(e, m);
        assertEquals (5, rd.all().size());
    }

    @Test
    public void testIsValid() throws Exception {
        for (ResolvedDependency dep : rd.all()) {
            assertTrue (dep.isValid());
            assertFalse (dep.isModified());
        }
        assertTrue (rd.isValid());
        eval.returnGarbage = true;
        for (ResolvedDependency dep : rd.all()) {
            assertFalse (dep.isValid());
            assertFalse (dep.isModified());
        }
        assertFalse (rd.isModified());
        assertFalse (rd.isValid());
    }

    @Test
    public void testMutability() throws Exception {
        rd.all().get(0).setDeploymentStrategy(DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES);
        assertTrue (rd.all().get(0).isModified());
        assertTrue (rd.isModified());
        assertTrue (rd.isValid());
        rd.all().get(1).setPath(ArtifactKind.ORIGIN, System.getProperty("java.io.tmpdir"));
        assertTrue (rd.all().get(1).isValid());
        assertTrue (rd.all().get(1).isModified());
        assertEquals(System.getProperty("java.io.tmpdir"), rd.all().get(1).getPath(ArtifactKind.ORIGIN));
        assertTrue (rd.isValid());


        rd.all().get(2).setPath(ArtifactKind.ORIGIN, "com/foo/bar/baz/asdfjkhasdlf");
        assertEquals("com/foo/bar/baz/asdfjkhasdlf", rd.all().get(2).getPath(ArtifactKind.ORIGIN));
        eval.returnGarbage = true;
        assertTrue (rd.all().get(2).isModified());
        String path = rd.all().get(2).getPath(ArtifactKind.ORIGIN);
        assertEquals ("com/foo/bar/baz/asdfjkhasdlf", path);
        assertFalse (rd.all().get(2).isValid());
        assertFalse (rd.isValid());
    }

    private class Eval implements PropertyEvaluator {
        boolean returnGarbage;
        public String getProperty(String prop) {
            if (returnGarbage) {
                return "boooooooooooooooggggggggggggggggggggggggaaaaaaaaaaaa";
            }
            char c = prop.charAt(0);
            switch (c) {
                case 'a' : return af.getAbsolutePath();
                case 'b' : return bf.getAbsolutePath();
                case 'c' : 
                    System.err.println("C: return garbage? " + returnGarbage);
                    return returnGarbage ? "booggggga" : cf.getAbsolutePath();
                case 'd' : return df.getAbsolutePath();
                case 'e' : return ef.getAbsolutePath();
                default :
                    throw new AssertionError ("Unknown property " + prop);
            }
        }

        public String evaluate(String text) {
            if (text.startsWith ("{")) {
                text = text.substring(1);
            }
            if (text.endsWith("}")) {
                text = text.substring(0, text.length() - 1);
            }
            return getProperty(text);
        }

        public Map<String, String> getProperties() {
            return Collections.<String, String>emptyMap();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
