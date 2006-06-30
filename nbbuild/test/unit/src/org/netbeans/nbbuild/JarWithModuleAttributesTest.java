/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;

import org.netbeans.junit.*;


/** Checks that javac.target gets reflected in the manifest.
 *
 * @author Jaroslav Tulach
 */
public class JarWithModuleAttributesTest extends NbTestCase {
    public JarWithModuleAttributesTest (String name) {
        super (name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testAddThereVersionFromJavacTarget() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = PublicPackagesInProjectizedXMLTest.extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='2.87'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNotNull("Attribute created:\n" + PublicPackagesInProjectizedXMLTest.readFile(extracted), value);

        String[] arr = value.split(">");
        assertEquals("Two parts", 2, arr.length);
        assertEquals("Java", arr[0].trim());

        assertVersionAtLeast("2.87", arr[1]);
    }

    public void testKeepOldVersion() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = PublicPackagesInProjectizedXMLTest.extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Java-Dependencies: Java > 1.3\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='2.87'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNotNull("Attribute created:\n" + PublicPackagesInProjectizedXMLTest.readFile(extracted), value);

        String[] arr = value.split(">");
        assertEquals("Two parts", 2, arr.length);
        assertEquals("Java", arr[0].trim());

        assertVersionAtLeast("1.3", arr[1]);
        try {
            assertVersionAtLeast("1.4", arr[1]);
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        fail("Version shall not be 1.4 or higher, as it is specified in manifest to be 1.3: " + arr[1]);
    }

    public void testIgnoreWeirdJavacTarget() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = PublicPackagesInProjectizedXMLTest.extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='jsr99'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNull("Attribute not created:\n" + PublicPackagesInProjectizedXMLTest.readFile(extracted), value);
    }
    
    private final File createNewJarFile () throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest);
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }

    private static void assertVersionAtLeast(String limit, String value) {
        int[] segLimit = segments(limit);
        int[] segValue = segments(value);

        for (int i = 0; i < segLimit.length && i < segValue.length; i++) {
            if (segValue[i] < segLimit[i]) {
                fail("Version is younger than it should be. Expected: " + limit + " was: " + value);
            }
            if (segValue[i] > segLimit[i]) {
                return;
            }
        }
    }

    private static int[] segments(String version) {
        String[] arr = version.split("\\.");
        int[] ret = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = Integer.parseInt(arr[i].trim());
        }
        return ret;
    }
}
