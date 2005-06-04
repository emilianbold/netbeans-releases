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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.*;

import org.netbeans.junit.*;


/** Behaviour of fixing module dependencies. Knows how to replace old
 * with new ones and remove those that are not needed for compilation.
 *
 * @author Jaroslav Tulach
 */
public class FixDependenciesTest extends NbTestCase {
    public FixDependenciesTest (String name) {
        super (name);
    }
    public void testReplaceOpenideDepWithSmallerOnes () throws Exception {
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>3.17</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        String result = PublicPackagesInProjectizedXMLTest.readFile (xml);
        
        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.awt") == -1) {
            fail ("org.openide.awt should be there: " + result);
        }
        
        if (result.indexOf ("<specification-version>6.2</specification-version>") == -1) {
            fail ("Spec version must be updated to 6.2: " + result);
        }
    }
    
    
    public void testVerificationOfRemovedDependencies () throws Exception {
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString ("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "  <condition property=\"remove\" >" +
            "    <and>" +    
            "      <not>" + 
            "        <and>" +    
            "          <contains string=\"${p}\" substring=\"org.openide.keep1\"  />" +
            "          <contains string=\"${p}\" substring=\"org.openide.keep2\"  />" +
            "        </and>" + 
            "      </not>" + 
            "      <contains string=\"${p}\" substring=\"org.openide.remove\"  />" +
            "    </and>" +
            "  </condition>" +
            // fail if there is org.openide.remove and at least one 
            // of org.openide.keep is missing
            "  <fail if=\"remove\" /> " + 
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = PublicPackagesInProjectizedXMLTest.readFile (xml);
        
        if (result.indexOf ("org.openide.keep") == -1) {
            fail ("org.openide.keep should be there: " + result);
        }
        if (result.indexOf ("org.openide.remove") != -1) {
            fail ("org.openide.remove should not be there: " + result);
        }

        String written = PublicPackagesInProjectizedXMLTest.readFile (out);
        assertEquals ("First we do clean, test verify, then clean and verify three times as there are three <dependency> tags"
                , "cvcvcvcvcv", written);
    }

    public void testBrokenCoreSettingsReplacement () throws Exception {
        
        String projectXML = 
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
"                Sun Public License Notice\n" +
"\n" +
"The contents of this file are subject to the Sun Public License\n" +
"Version 1.0 (the 'License'). You may not use this file except in\n" +
"compliance with the License. A copy of the License is available at\n" +
"http://www.sun.com/\n" +
"\n" +
"The Original Code is NetBeans. The Initial Developer of the Original\n" +
"Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
"Microsystems, Inc. All Rights Reserved.\n" +
"-->\n" +
"<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
    "<type>org.netbeans.modules.apisupport.project</type>\n" +
    "<configuration>\n" +
        "<data xmlns='http://www.netbeans.org/ns/nb-module-project/2'>\n" +
            "<code-name-base>org.netbeans.modules.settings</code-name-base>\n" +
            "<module-dependencies>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<release-version>1</release-version>\n" +
                        "<specification-version>3.17</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.loaders</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency/>\n" +
                "</dependency>\n" +
            "</module-dependencies>\n" +
            "<public-packages>\n" +
                "<package>org.netbeans.spi.settings</package>\n" +
            "</public-packages>\n" +
            "<javadoc/>\n" +
        "</data>\n" +
    "</configuration>\n" +
"</project>\n";
        
        
        
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (projectXML);
        
        
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString ("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix >" +
            "  <replace codenamebase='org.openide'>\n" +  
            "   <module codenamebase='org.openide.filesystems' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.util' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.util.enumerations' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.modules' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.nodes' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.explorer' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.awt' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.dialogs' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.compat' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.options' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.windows' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.text' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.actions' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.loaders' spec='6.2'/>\n" +
            "  </replace>\n" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        org.w3c.dom.Document doc;
        doc = javax.xml.parsers.DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (xml);
        assertNotNull ("Originally can be parsed", doc);
        
        
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        doc = javax.xml.parsers.DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (xml);
        
        assertNotNull ("Still can be parsed", doc);
        
        String r = PublicPackagesInProjectizedXMLTest.readFile (xml);
        assertEquals ("No release version used as modules do not have it", -1, r.indexOf ("release-version"));
        
        
        int idx = r.indexOf ("<code-name-base>org.openide.loaders</code-name-base>");
        if (idx == -1) {
            fail ("One dep on loaders should be there: " + r);
        }
        
        assertEquals ("No next loader dep", -1, r.indexOf ("<code-name-base>org.openide.loaders</code-name-base>", idx + 10));
    }

    public void testPropertiesAreNotInfluencedByPreviousExecution () throws Exception {
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString ("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Separate namespaces\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget='verify'>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <fail if=\"remove\" /> " + 
            "  <property name='remove' value='some' />" +
            "  <fail unless=\"remove\" /> " + 
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = PublicPackagesInProjectizedXMLTest.readFile (xml);

        String written = PublicPackagesInProjectizedXMLTest.readFile (out);
        assertEquals ("The property remove is never set", "vvvvvvvvvv", written);
    }

    
  public void testOnlyCompileTimeDependenciesCanBeRemoved () throws Exception {
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
// This changes the meaning:            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString ("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "  <condition property=\"remove\" >" +
            "    <and>" +    
            "      <not>" + 
            "        <and>" +    
            "          <contains string=\"${p}\" substring=\"org.openide.keep1\"  />" +
            "          <contains string=\"${p}\" substring=\"org.openide.keep2\"  />" +
            "        </and>" + 
            "      </not>" + 
            "      <contains string=\"${p}\" substring=\"org.openide.remove\"  />" +
            "    </and>" +
            "  </condition>" +
            // fail if there is org.openide.remove and at least one 
            // of org.openide.keep is missing
            "  <fail if=\"remove\" /> " + 
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = PublicPackagesInProjectizedXMLTest.readFile (xml);
        
        if (result.indexOf ("org.openide.keep") == -1) {
            fail ("org.openide.keep should be there: " + result);
        }
        if (result.indexOf ("org.openide.remove") == -1) {
            fail ("org.openide.remove should be there: " + result);
        }

        String written = PublicPackagesInProjectizedXMLTest.readFile (out);
        assertEquals ("The remove dependency is not even asked for"
                , "cvcvccvcv", written);
    }
    
  public void testRuntimeDepOnOpenideIsSpecial () throws Exception {
        java.io.File xml = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide</code-name-base>" +
            "        <run-dependency>" + 
            "            <specification-version>5.1</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString ("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" addcompiletime='true' >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" + // always succeed
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = PublicPackagesInProjectizedXMLTest.readFile (xml);
        
        if (result.indexOf ("org.openide") > -1) {
            fail ("No org.openide should be there: " + result);
        }
        if (result.indexOf ("<dependency>") > -1) {
            fail ("No dependency should be there: " + result);
        }
    }

}
