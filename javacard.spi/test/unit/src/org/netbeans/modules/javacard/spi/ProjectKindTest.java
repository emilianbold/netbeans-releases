/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import org.openide.filesystems.FileUtil;
import java.io.FileOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.netbeans.modules.javacard.spi.ProjectKind.*;

/**
 *
 * @author Tim Boudreau
 */
public class ProjectKindTest {
    @Test
    public void testFastKindForProject() throws IOException {
        InputStream in = new BufferedInputStream (ProjectKindTest.class.getResourceAsStream("project-data-extapp.xml"));
        ProjectKind kind = ProjectKind.fastKindForProject(in);
        assertEquals(ProjectKind.EXTENDED_APPLET, kind);

        in = new BufferedInputStream (ProjectKindTest.class.getResourceAsStream("project-data-web.xml"));
        kind = ProjectKind.fastKindForProject(in);
        assertEquals(ProjectKind.WEB, kind);

        in = new BufferedInputStream (ProjectKindTest.class.getResourceAsStream("project-data-clslib.xml"));
        kind = ProjectKind.fastKindForProject(in);
        assertEquals(ProjectKind.CLASSIC_LIBRARY, kind);

        in = new BufferedInputStream (ProjectKindTest.class.getResourceAsStream("project-data-extlib.xml"));
        kind = ProjectKind.fastKindForProject(in);
        assertEquals(ProjectKind.EXTENSION_LIBRARY, kind);

        in = new BufferedInputStream (ProjectKindTest.class.getResourceAsStream("project-data-cap.xml"));
        kind = ProjectKind.fastKindForProject(in);
        assertEquals(ProjectKind.CLASSIC_APPLET, kind);
    }

    private static final String[] JARS = new String[] {
        "ClassicApplet17.cap", "ClassicLibrary4.cap", "ExtendedApplet1.eap",
        "ExtensionLibrary8.jar", "WebApplication14.war", "RandomJar.jar"
    };
    private static final ProjectKind[] KINDS = new ProjectKind[] {
        CLASSIC_APPLET, CLASSIC_LIBRARY, EXTENDED_APPLET, EXTENSION_LIBRARY,
        WEB, null
    };

    private Map<ProjectKind, File> copyJars() throws IOException {
        assertEquals (JARS.length, KINDS.length);
        Map<ProjectKind, File> result = new HashMap<ProjectKind, File> ();
        File tmpdir = new File (System.getProperty("java.io.tmpdir"));
        assertTrue (tmpdir.exists());
        assertTrue (tmpdir.isDirectory());
        for (int i=0; i < JARS.length; i++) {
            String res = JARS[i];
            ProjectKind kind = KINDS[i];
            File f = new File (tmpdir, res);
            if (f.exists()) {
                assertTrue (f.delete());
            }
            FileOutputStream out = new FileOutputStream (f);
            InputStream in = ProjectKindTest.class.getResourceAsStream(res);
            assertNotNull ("Missing " + res, in);
            try {
                FileUtil.copy (in, out);
            } finally {
                in.close();
                out.close();
            }
            result.put (kind, f);
        }
        return result;
    }

    @Test
    public void testKindForJAR() throws Exception {
        Map<ProjectKind, File> m = copyJars();
        try {
            doTestKindForJar(m);
        } finally {
            for (File f : m.values()) {
                f.delete();
            }
        }
    }

    private void doTestKindForJar(Map<ProjectKind, File> m) throws IOException {
        for (ProjectKind kind : m.keySet()) {
            File jar = m.get(kind);
            assertTrue (jar.exists());
            ProjectKind foundKind = ProjectKind.forJarFile(jar);
            System.err.println("JAR is " + jar.getAbsolutePath());
            System.err.println("Expected " + kind + " found " + foundKind);
            assertEquals (kind, foundKind);
        }
    }
}