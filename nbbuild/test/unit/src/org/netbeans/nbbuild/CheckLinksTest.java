/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    public void testQueryComponent() throws Exception {
        File html1 = extractHTMLFile(
            "<head></head><body></body>"
        );
        File html2 = extractHTMLFile(
            "<head></head><body>\n" +
            "<a href=\"" + html1.toURI() + "?is-external=true\">ought to be OK</a>\n" +
            "</body>"
        );
        File f = PublicPackagesInProjectizedXMLTest.extractString(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html1.getParent() + "' >" +
            "    <include name=\"" + html1.getName() + "\" />" +
            "    <include name=\"" + html2.getName() + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute(f, new String[] {});
    }
    
    
    private static File extractHTMLFile (String s) throws Exception {
        File f = PublicPackagesInProjectizedXMLTest.extractString (s);
        File n = new File (f.getParentFile (), f.getName () + ".html");
        assertTrue ("Rename succeeded", f.renameTo (n));
        return n;
    }
    
}
