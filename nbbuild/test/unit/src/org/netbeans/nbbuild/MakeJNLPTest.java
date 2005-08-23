/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.junit.*;


/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach
 */
public class MakeJNLPTest extends NbTestCase {
    public MakeJNLPTest (String name) {
        super (name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    
    public void testGenerateJNLPAndSignedJarForSimpleModule() throws Exception {
        Manifest m;
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = genereteKeystore("jnlp", "netbeans-test");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);
        
        if (files[0].endsWith("jnlp")) {
            String fx = files[0];
            files[0] = files[1];
            files[1] = fx;
        }
        
        assertEquals("The JAR file is org-my-module.jar", "org-my-module.jar", files[0]);
        assertEquals("The JNLP file is org-my-module.jnlp", "org-my-module.jnlp", files[1]);
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = ModuleDependenciesTest.readFile (jnlp);
        
        
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permitions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);

        CHECK_SIGNED: {
            File jar = new File(output, "org-my-module.jar");
            JarFile signed = new JarFile(jar);
            Enumeration it = signed.entries();
            while (it.hasMoreElements()) {
                JarEntry entry = (JarEntry)it.nextElement();
                if (entry.getName().endsWith(".SF")) {
                    break CHECK_SIGNED;
                }
            }
            fail ("File does not seem to be signed: " + jar);
        }
    }
    
    
    public void testOneCanChangeTheCodeBase() throws Exception {
        Manifest m;
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = genereteKeystore("jnlp", "netbeans-test");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' codebase='http://www.my.org/' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);
        
        if (files[0].endsWith("jnlp")) {
            String fx = files[0];
            files[0] = files[1];
            files[1] = fx;
        }
        
        assertEquals("The JAR file is org-my-module.jar", "org-my-module.jar", files[0]);
        assertEquals("The JNLP file is org-my-module.jnlp", "org-my-module.jnlp", files[1]);
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = ModuleDependenciesTest.readFile (jnlp);
        
        
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permitions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the codebases can be changed: ", "http://www.my.org/", base);
    }

    public void testGenerateJNLPAndSignedJarForModuleWithClassPath() throws Exception {
        File ext = doClassPathModuleCheck(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        
        File output = ext.getParentFile();
        String[] files = output.list();
        assertEquals("It has three files", 3, files.length);

        java.util.Arrays.sort(files);
        
        assertEquals("The JAR file is aaa-my-module.jar", "aaa-my-module.jar", files[1]);
        assertEquals("The JNLP file is aaa-my-module.jnlp", "aaa-my-module.jnlp", files[2]);
        assertEquals("The ext JAR file is there", ext.getName(), files[0]);
        
        File jnlp = new File(output, "aaa-my-module.jnlp");
        String res = ModuleDependenciesTest.readFile (jnlp);

        int first = res.indexOf("jar href");
        if (first < 0 || res.indexOf("jar href", first + 1) < 0) {
            fail ("There should be two jar references in the file: " + res);
        }
        
    }

    public void testGenerateJNLPAndSignedJarForModuleWithClassPathAndSignedJar() throws Exception {
        File ks = genereteKeystore("external", "netbeans-test");
        
        File ext = doClassPathModuleCheck(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <signjar jar='${test.ext}' alias='external' storepass='netbeans-test' keystore='${test.ks}' />\n" +
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        
        JarFile f = new JarFile(ext);
        Enumeration en = f.entries();
        StringBuffer sb = new StringBuffer();
        int cnt = 0;
        while (en.hasMoreElements()) {
            JarEntry e = (JarEntry)en.nextElement();
            if (e.getName().endsWith("SF")) {
                cnt++;
                if (!e.getName().equals("META-INF/EXTERNAL.SF")) {
                    fail("Signed with wrong entity: " + e.getName());
                }
            }
            sb.append(e.getName());
            sb.append('\n');
        }

        if (cnt == 0) {
            fail("Signed with wrong file:\n" + sb);
        }
        
        
        
        File output = ext.getParentFile();
        String[] files = output.list();
        assertEquals("It has three files", 4, files.length);

        java.util.Arrays.sort(files);
        
        File jnlp = new File(output, "aaa-my-module.jnlp");
        
        String extJnlpName = "aaa-my-module-ext-" + ext.getName();
        extJnlpName = extJnlpName.replaceAll(".jar", ".jnlp");
        File extJnlp = new File(output, extJnlpName);
        
        
        assertTrue("The JAR file is aaa-my-module.jar", new File (output, "aaa-my-module.jar").exists());
        assertTrue("The JNLP file is aaa-my-module.jnlp", jnlp.exists());
        assertTrue("The ext JAR file is there", ext.canRead());
        assertTrue("The ext jnlp file is there", extJnlp.canRead());
        
        String res = ModuleDependenciesTest.readFile (jnlp);

        int first = res.indexOf("jar href");
        assertEquals("Just one jar href ", -1, res.indexOf("jar href", first + 1));
        
    }
    
    public void testInformationIsTakenFromLocalizedBundle() throws Exception {
        Manifest m;
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        
        Properties props = new Properties();
        props.put("OpenIDE-Module-Name", "Module Build Harness");
        props.put("OpenIDE-Module-Display-Category", "Developing NetBeans");
        props.put("OpenIDE-Module-Short-Description", "Lets you build external plug-in modules from sources.");
        props.put("OpenIDE-Module-Long-Description", "XXX");
        
        File simpleJar = generateJar (new String[0], m, props);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = genereteKeystore("jnlp", "netbeans-test");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);
        
        if (files[0].endsWith("jnlp")) {
            String fx = files[0];
            files[0] = files[1];
            files[1] = fx;
        }
        
        assertEquals("The JAR file is org-my-module.jar", "org-my-module.jar", files[0]);
        assertEquals("The JNLP file is org-my-module.jnlp", "org-my-module.jnlp", files[1]);
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = ModuleDependenciesTest.readFile (jnlp);

        int infoBegin = res.indexOf("<information>");
        int infoEnd = res.indexOf("</information>");
        
        if (infoEnd == -1 || infoBegin == -1) {
            fail ("Both information tags must be present: " + res);
        }
        
        String info = res.substring(infoBegin, infoEnd);
        
        if (info.indexOf("<title>Module Build Harness</title>") == -1) {
            fail("Title should be there with Module Build Harness inside itself: " + info);
        }
        
        if (info.indexOf("<description kind='one-line'>Lets you build external plug-in modules from sources.</description>") == -1) {
            fail("one-line should be there with 'lets you...' inside itself: " + info);
        }
        
        if (info.indexOf("<description kind='short'>XXX</description>") == -1) {
            fail("short should be there with XXX inside itself: " + info);
        }
    }
    
    public void testGenerateJNLPFailsForModulesWithExtraFiles() throws Exception {
        doCompareJNLPFileWithUpdateTracking(true, null, "");
    }
    public void testGenerateJNLPSucceedsWithExtraFiles() throws Exception {
        doCompareJNLPFileWithUpdateTracking(false, null, "");
    }
    public void testGenerateJNLPSucceedsWhenExtraFileIsExcluded() throws Exception {
        doCompareJNLPFileWithUpdateTracking(false, "lib/nbexec", " verifyexcludes=' one, lib/nbexec, three ' ");
    }
    
    private void doCompareJNLPFileWithUpdateTracking(boolean useNonModule, String fakeEntry, String extraScript) throws Exception {
        File nonModule = generateJar (new String[0], ModuleDependenciesTest.createManifest());
        
        Manifest m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "aaa.my.module/3");
        File module = generateJar (new String[0], m);
        
        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());

        File enableXML = new File(new File(getWorkDir(), "config"), "Modules");
        enableXML.getParentFile().mkdirs();
        enableXML.createNewFile();
        
        File trackingFile = new File(updateTracking, "aaa-my-module.xml");
        FileWriter w = new FileWriter(trackingFile);
        w.write(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<module codename='org.apache.tools.ant.module/3'>\n" +
    "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
        (useNonModule ? ("<file name='modules/" + nonModule.getName() + "' crc='1536373800'/>\n") : "") +
        "<file name='modules/" + module.getName() + "' crc='3245456472'/>\n" +
        "<file name='config/Modules/aaa-my-module.xml' crc='43434' />\n" +
        (fakeEntry != null ? "<file name='" + fakeEntry + "' crc='43222' />\n" : "") +
"    </module_version>\n" +
"</module>\n"
        );
        w.close();
        
        
        
        String script =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' verify='true' " + extraScript + " >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>";

        assertEquals("Both modules in the same dir", module.getParentFile(), nonModule.getParentFile());
        
        File output = new File(getWorkDir(), "output");
        File ks = genereteKeystore("jnlp", "netbeans-test");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (script);
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
                "-Dtest.output=" + output, 
                "-Dtest.parent=" + module.getParent(), 
                "-Dtest.name=" + module.getName(),
                "-Dtest.ks=" + ks,
            });
            if (useNonModule) {
                fail("The task has to fail");   
            }
            
            assertTrue ("Output exists", output.exists ());
            assertTrue ("Output directory created", output.isDirectory());

            File ext = new File (output, module.getName());


            String[] files = ext.getParentFile().list();
            assertEquals("Two files are there", 2, files.length);
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            if (!useNonModule) {
                throw ex;
            } else {
                // ok, this is fine
                assertTrue ("Output exists", output.exists ());
                assertTrue ("Output directory created", output.isDirectory());

                File ext = new File (output, module.getName());


                String[] files = ext.getParentFile().list();
                assertEquals("Output dir is empty as nothing has been generated", 0, files.length);
            }
        }
        
    }
    
    private File doClassPathModuleCheck(String script) throws Exception {
        Manifest m;

        File extJar = generateJar (new String[0], ModuleDependenciesTest.createManifest());
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "aaa.my.module/3");
        m.getMainAttributes ().putValue ("Class-Path", extJar.getName());
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        
        assertEquals("Both modules in the same dir", parent, extJar.getParentFile());
        
        
        File output = new File(parent, "output");
        File ks = genereteKeystore("jnlp", "netbeans-test");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (script);
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Dtest.output=" + output, 
            "-Dtest.parent=" + parent, 
            "-Dtest.name=" + simpleJar.getName(),
            "-Dtest.ks=" + ks,
            "-Dtest.ext=" + extJar
        });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        return new File (output, extJar.getName());
    }
    
    
    private final File createNewJarFile () throws IOException {
        File dir = new File(this.getWorkDir(), "modules");
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        return generateJar(content, manifest, null);
    }
    
    protected final File generateJar (String[] content, Manifest manifest, Properties props) throws IOException {
        File f = createNewJarFile ();
        
        if (props != null) {
            manifest.getMainAttributes().putValue("OpenIDE-Module-Localizing-Bundle", "some/fake/prop/name/Bundle.properties");
        }
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest);
        
        if (props != null) {
            os.putNextEntry(new JarEntry("some/fake/prop/name/Bundle.properties"));
            props.store(os, "# properties for the module");
            os.closeEntry();
        }
        
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
    private final File genereteKeystore(String alias, String password) throws Exception {
        File where = new File(getWorkDir(), "key.ks");
        
        String script = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Generate Keystore\" basedir=\".\" default=\"all\" >" +
            "<target name=\"all\" >" +
            "<genkey \n" +
              "alias='" + alias + "' \n" +
              "keystore='" + where + "' \n" +
              "storepass='" + password + "' \n" +
              "dname='CN=A NetBeans Friend, OU=NetBeans, O=netbeans.org, C=US' \n" +
            "/>\n" +
            "</target></project>\n";
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (script);
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        return where;
    }
}
