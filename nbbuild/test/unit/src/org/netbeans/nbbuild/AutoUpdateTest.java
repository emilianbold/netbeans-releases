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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AutoUpdateTest extends NbTestCase {

    public AutoUpdateTest(String name) {
        super(name);
    }

    public void testDownloadAndExtractModule() throws Exception {
        clearWorkDir();

        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        PublicPackagesInProjectizedXMLTest.extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        PublicPackagesInProjectizedXMLTest.execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(target, "platform11"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());

        File jar = new File(
            new File(new File(target, "platform11"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());
    }


    public void testUpdateAlreadyInstalled() throws Exception {
        clearWorkDir();

        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        PublicPackagesInProjectizedXMLTest.extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platform11"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();

        File y = new File(
            new File(new File(new File(target, "platform11"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.getParentFile().mkdirs();
        File x = new File(
            new File(new File(target, "platform11"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.createNewFile();
        
        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common\">\n" +
"    <module_version install_time=\"10\" specification_version=\"1.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        FileOutputStream osx = new FileOutputStream(x);
        osx.write(txtx.getBytes());
        osx.close();


        PublicPackagesInProjectizedXMLTest.execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(new File(target, "platform11"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created:\n" + PublicPackagesInProjectizedXMLTest.getStdOut(), xml.exists());

        File jar = new File(
            new File(new File(target, "platform11"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (PublicPackagesInProjectizedXMLTest.getStdOut().contains("Writing ")) {
            fail("No writes, the module is already installed:\n" + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
    }

    public void testUpdateAlreadyInstalledAndOld() throws Exception {
        clearWorkDir();

        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        PublicPackagesInProjectizedXMLTest.extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platform11"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();

        File x = new File(
            new File(new File(new File(target, "platform11"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common\">\n" +
"    <module_version install_time=\"10\" specification_version=\"1.0.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        FileOutputStream osx = new FileOutputStream(x);
        osx.write(txtx.getBytes());
        osx.close();

        Thread.sleep(1000);
        long last = x.lastModified();

        PublicPackagesInProjectizedXMLTest.execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(new File(target, "platform11"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());

        File jar = new File(
            new File(new File(target, "platform11"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (!PublicPackagesInProjectizedXMLTest.getStdOut().contains("Writing ")) {
            fail("Writes should be there:\n" + PublicPackagesInProjectizedXMLTest.getStdOut());
        }

        if (last >= jar.lastModified()) {
            fail("Newer timestamp for " + jar);
        }
    }

    public File generateNBM (String name, String... files) throws IOException {
        File f = new File (getWorkDir (), name);

        ZipOutputStream os = new ZipOutputStream (new FileOutputStream (f));
        os.putNextEntry (new ZipEntry ("Info/info.xml"));
        os.write ("nothing".getBytes ());
        os.closeEntry ();
        for (String n : files) {
            os.putNextEntry(new ZipEntry(n));
            os.write("empty".getBytes());
            os.closeEntry();
        }
        os.close();

        return f;
    }

}
