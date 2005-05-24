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


/** Check the behaviour of CheckLinks.
 *
 * @author Jaroslav Tulach
 */
public class CheckLinksTest extends NbTestCase {
    public CheckLinksTest (String name) {
        super (name);
    }

    public void testByDefaultAllURLsAreAllowed () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' failonerror='true' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }

    
    public void testForbidenExternalURLsAreCorrectlyReported () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' failonerror='true' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='http://www.netbeans.org/download/[a-zA-Z0-9\\.]*/javadoc/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
            fail ("This should fail as the URL is forbidden");
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }
  
  
    public void testAnyURLCanBeForbidden () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' failonerror='true' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='http://www.sex.org/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
            fail ("This should fail as the URL is forbidden");
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testIfAcceptedFirstThenItDoesNotMatterThatItIsForbiddenLater () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' failonerror='true' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='true' pattern='.*sex.*' /> " +
            "    <filter accept='false' pattern='http://www.sex.org/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // passes as .*sex.* is acceptable
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }
    
    
    public void testSkipCommentedOutLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            " <!-- This is commented out \n" + 
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "  here ends the comment -->" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' failonerror='true' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='.*sex.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // passes as the forbidden URL is commented out
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }
    
    
    private static File extractHTMLFile (String s) throws Exception {
        File f = PublicPackagesInProjectizedXMLTest.extractString (s);
        File n = new File (f.getParentFile (), f.getName () + ".html");
        assertTrue ("Rename succeeded", f.renameTo (n));
        return n;
    }
    
}
