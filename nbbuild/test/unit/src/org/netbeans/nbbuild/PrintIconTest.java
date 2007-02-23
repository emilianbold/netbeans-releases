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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import junit.framework.TestCase;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class PrintIconTest extends NbTestCase {
    
    public PrintIconTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPrintOutSameIcons() throws Exception {
        java.io.File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        java.io.File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"printicon\" classname=\"org.netbeans.nbbuild.PrintIcon\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("( |\n)+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }
    
    
    public void testDuplicatesFromTheSameSet() throws Exception {
        java.io.File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        java.io.File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"printicon\" classname=\"org.netbeans.nbbuild.PrintIcon\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("( |\n)+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }

    public void testBrokenImageThatCould() throws Exception {
        doBrokenImageTest("data/columnIndex.gif");
    }
    public void testBrokenImageThatCoul2() throws Exception {
        doBrokenImageTest("data/Category.png");
    }
    
    private void doBrokenImageTest(String res) throws Exception {
        java.io.File img = PublicPackagesInProjectizedXMLTest.extractResource(res);
        java.io.File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        java.io.File img3 = PublicPackagesInProjectizedXMLTest.extractResource(res);
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"printicon\" classname=\"org.netbeans.nbbuild.PrintIcon\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("( |\n)+");
        assertEquals(file, 6, threeParts.length);

        long prevHash;
        {
            prevHash = Long.parseLong(threeParts[0], 16);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is the same:\n" + file, prevHash, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }
    
    public void testPrintExtra() throws Exception {
        java.io.File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        java.io.File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        java.io.File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"printicon\" classname=\"org.netbeans.nbbuild.PrintIcon\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <printicon difference='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        if (!file.startsWith("-")) {
            fail("Should start with - as one icon is missing in new version:\n" + file);
        } else {
            file = file.substring(1);
        }
        
        String[] threeParts = file.split("( |\n)+");
        assertEquals(file, 3, threeParts.length);
        
        long hash = Long.parseLong(threeParts[0], 16);
        assertEquals("Hash code is 10ba4f25:\n" + file, 0x10ba4f25L, hash);
        assertEquals("Name is from img2:\n" + file, img2.getName(), threeParts[1]);
        assertEquals("Full name is img2:\n" + file, img2.toURL().toExternalForm(), threeParts[2]);
    }
}
