/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class CheckLicenseTest extends NbTestCase {
    
    public CheckLicenseTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testWeCanSearchForSunPublicLicense() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });

        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf(license.getPath()) > - 1) {
            fail("file name shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf("no license") > - 1) {
            fail("warning shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
    }        

    public void testTheTaskFailsIfItIsMissing() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf(license.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf("no license") == - 1) {
            fail("warning shall be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
    }        
    
}
