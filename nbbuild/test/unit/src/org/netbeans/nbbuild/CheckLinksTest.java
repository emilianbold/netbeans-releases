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
import org.netbeans.junit.NbTestCase;

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
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }

    
    public void testForbiddenExternalURLsAreCorrectlyReported () throws Exception {
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
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
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
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
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
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
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
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='.*sex.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // passes as the forbidden URL is commented out
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }
    
    
    public void testDocFilesRelativeLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"#RelativeLink\">This link should pass the checking</a>\n" +
	    "<a name=\"RelativeLink\"/>\n" + 
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
    }
    
    
    public void testDocFilesInvalidLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"#InvalidLink\">This link should NOT pass the checking</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // failure
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
            fail ("This should fail as the link is broken");
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }
    
    
    private static File extractHTMLFile (String s) throws Exception {
        File f = PublicPackagesInProjectizedXMLTest.extractString (s);
        File n = new File (f.getParentFile (), f.getName () + ".html");
        assertTrue ("Rename succeeded", f.renameTo (n));
        return n;
    }
    
}
