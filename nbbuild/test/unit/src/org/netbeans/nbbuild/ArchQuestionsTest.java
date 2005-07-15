/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.xml.parsers.*;
import junit.framework.*;

import org.netbeans.junit.*;
import org.w3c.dom.Document;


/** Check the behaviour Arch task.
 *
 * @author Jaroslav Tulach
 */
public class ArchQuestionsTest extends NbTestCase {
    public ArchQuestionsTest (String name) {
        super (name);
    }
    
    public void testGenerateArchFileWhenEmpty () throws Exception {
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString ("");
        answers.delete ();
        assertFalse ("Really deleted", answers.exists ());

        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output=\"x.html\" />" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });

        assertTrue ("File is generated", answers.exists ());
    }

    public void testGenerateArchFileWhenEmptyWithDefaultAnswerForNbDepsQuestion() throws Exception {
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString ("");
        answers.delete ();
        assertFalse ("Really deleted", answers.exists ());

        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output=\"x.html\" />" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });

        assertTrue ("File is generated", answers.exists ());
        
        String res = PublicPackagesInProjectizedXMLTest.readFile(answers);
        DocumentBuilderFactory fack = DocumentBuilderFactory.newInstance();
        fack.setValidating(false);
        org.w3c.dom.Document dom = fack.newDocumentBuilder().parse(answers);

        org.w3c.dom.NodeList list = dom.getElementsByTagName("defaultanswer");
        assertTrue("There is at least one defaultanswer: " + res, list.getLength() > 0);
        BIGLOOP: for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node n = list.item(i);
            while (n != null) {
                n = n.getParentNode();
                assertNotNull ("No parent node answer found: " + res, n);
                
                if (n.getNodeName().equals ("answer")) {
                    String id = n.getAttributes().getNamedItem("id").getNodeValue();
                    if (id.equals ("dep-nb")) {
                        // ok, we were searching for answer to dep-nb question
                        return;
                    }
                    continue BIGLOOP;
                }
            }
        }
        
        fail ("dep-nb question should have a defaultanswer: " + res);
    }
    
    public void testDoNotCorruptTheFileWhenItExists() throws Exception {
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString (
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
            // "<!DOCTYPE api-answers PUBLIC '-//NetBeans//DTD Arch Answers//EN' '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd' [\n" +
            // The following lines needs to be commented out as we do not have the right relative locations!
            // instead there is a part of Arch-api-questions directly inserted into the document bellow
            //  "<!ENTITY api-questions SYSTEM '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml'>\n" +
            //"]>\n" +
"\n" +
"<api-answers\n" +
  "question-version='1.25'\n" +
  "module='Input/Output System'\n" +
  "author='jglick@netbeans.org'\n" +
">\n" +
"\n" +
  // "&api-questions;\n" +
  // replaced by part of api-questions entity            
"<api-questions version='1.25'>\n" +
    "<category id='arch' name='General Information'>\n" +
        "<question id='arch-what' when='init' >\n" +
            "What is this project good for?\n" +
            "<hint>\n" +
            "Please provide here a few lines describing the project, \n" +
            "what problem it should solve, provide links to documentation, \n" +
            "specifications, etc.\n" +
            "</hint>\n" +
        "</question>\n" +
        "<question id='arch-overall' when='init'>\n" +
            "Describe the overall architecture. \n" +
            "<hint>\n" +
            "What will be API for \n" +
            "<a href='http://openide.netbeans.org/tutorial/api-design.html#design.apiandspi'>\n" +
                "clients and what support API</a>? \n" +
            "What parts will be pluggable?\n" +
            "How will plug-ins be registered? Please use <code>&lt;api type='export'/&gt;</code>\n" +
            "to describe your general APIs.\n" +
            "If possible please provide \n" +
            "simple diagrams. \n" +
            "</hint>\n" +
        "</question>\n" +
    "</category>\n" +
"</api-questions>                \n" +
// end of Arch-api-questionx.xmls            
"\n" +
"\n" +
"<answer id='arch-what'>\n" +
"The Input/Output API is a small API module\n" +
"which contains <code>InputOutput</code> and related interfaces used in\n" +
"driving the Output Window. The normal implementation is <code>org.netbeans.core.output2</code>.\n" +
"</answer>\n" +
"\n" +
"</api-answers>    \n"
        );
        
        java.io.File output = PublicPackagesInProjectizedXMLTest.extractString("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output='" + output + "' />" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] {  
            "-Darch.generate=true",
            "-Darch.private.disable.validation.for.test.purposes=true",
                
        });

        assertTrue ("Answers still exists", answers.exists ());
        assertTrue ("Output file generated", output.exists ());
        
        String s1 = PublicPackagesInProjectizedXMLTest.readFile(answers);
        if (s1.indexOf("answer id=\"arch-overall\"") == -1) {
            fail ("There should be a answer template for arch-overall in answers: " + s1);
        }
        String s2 = PublicPackagesInProjectizedXMLTest.readFile(output);
        if (s2.indexOf("question id=\"arch-overall\"") == -1) {
            fail ("There should be a answer template for arch-overall in html output: " + s2);
        }
    }
    
    
    public void testIncludeAPIChangesDocumentIntoSetOfAnswersIfSpecified() throws Exception {
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString (
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
            // "<!DOCTYPE api-answers PUBLIC '-//NetBeans//DTD Arch Answers//EN' '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd' [\n" +
            // The following lines needs to be commented out as we do not have the right relative locations!
            // instead there is a part of Arch-api-questions directly inserted into the document bellow
            //  "<!ENTITY api-questions SYSTEM '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml'>\n" +
            //"]>\n" +
"\n" +
"<api-answers\n" +
  "question-version='1.25'\n" +
  "module='Input/Output System'\n" +
  "author='jglick@netbeans.org'\n" +
">\n" +
"\n" +
  // "&api-questions;\n" +
  // replaced by part of api-questions entity            
"<api-questions version='1.25'>\n" +
    "<category id='arch' name='General Information'>\n" +
        "<question id='arch-what' when='init' >\n" +
            "What is this project good for?\n" +
            "<hint>\n" +
            "Please provide here a few lines describing the project, \n" +
            "what problem it should solve, provide links to documentation, \n" +
            "specifications, etc.\n" +
            "</hint>\n" +
        "</question>\n" +
        "<question id='arch-overall' when='init'>\n" +
            "Describe the overall architecture. \n" +
            "<hint>\n" +
            "What will be API for \n" +
            "<a href='http://openide.netbeans.org/tutorial/api-design.html#design.apiandspi'>\n" +
                "clients and what support API</a>? \n" +
            "What parts will be pluggable?\n" +
            "How will plug-ins be registered? Please use <code>&lt;api type='export'/&gt;</code>\n" +
            "to describe your general APIs.\n" +
            "If possible please provide \n" +
            "simple diagrams. \n" +
            "</hint>\n" +
        "</question>\n" +
    "</category>\n" +
"</api-questions>                \n" +
// end of Arch-api-questionx.xmls            
"\n" +
"\n" +
"<answer id='arch-what'>\n" +
"The Input/Output API is a small API module\n" +
"which contains <code>InputOutput</code> and related interfaces used in\n" +
"driving the Output Window. The normal implementation is <code>org.netbeans.core.output2</code>.\n" +
"</answer>\n" +
"\n" +
"</api-answers>    \n"
        );
        
        java.io.File apichanges = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
//"<!DOCTYPE apichanges PUBLIC '-//NetBeans//DTD API changes list 1.0//EN' '../../nbbuild/javadoctools/apichanges.dtd'>\n" +
"<apichanges>\n" +
"<apidefs>\n" +
"<apidef name='nodes'>Nodes API</apidef>\n" +
"</apidefs>\n" +
"<changes>\n" +
"<change id='AbstractNode.setIconBaseWithExtension'>\n" +
     "<api name='nodes'/>\n" +
     "<summary>AbstractNode allows using different icon extensions</summary>\n" +
     "<version major='6' minor='5'/>\n" +
     "<date day='14' month='7' year='2005'/>\n" +
     "<author login='pnejedly'/>\n" +
     "<compatibility addition='yes' binary='compatible' source='compatible' semantic='compatible' deprecation='yes' deletion='no' modification='no'/>\n" +
     "<description>\n" +
    "Some description\n" +
     "</description>\n" +
     "<class package='org.openide.nodes' name='AbstractNode'/>\n" +
     "<issue number='53461'/>\n" +
    "</change>        \n" +
"<change id='anotherChange'>\n" +
     "<api name='nodes'/>\n" +
     "<summary>Ble</summary>\n" +
     "<version major='6' minor='3'/>\n" +
     "<date day='14' month='3' year='2005'/>\n" +
     "<author login='jtulach'/>\n" +
     "<compatibility addition='yes' binary='compatible' source='compatible' semantic='compatible' deprecation='yes' deletion='no' modification='no'/>\n" +
     "<description>\n" +
    "Some description\n" +
     "</description>\n" +
     "<class package='org.openide.nodes' name='AbstractNode'/>\n" +
     "<issue number='23461'/>\n" +
    "</change>        \n" +
"</changes>\n" +
"<htmlcontents>\n" +
"<head>\n" +
"<title>Change History for the Nodes API</title>\n" +
"<link rel='stylesheet' href='prose.css' type='text/css'/>\n" +
"</head>\n" +
"<body>\n" +
"<p class='overviewlink'>\n" +
"<a href='overview-summary.html'>Overview</a>\n" +
"</p>\n" +
"<h1>Introduction</h1>\n" +
"<h2>What do the Dates Mean?</h2>\n" +
"<p>The supplied dates indicate when the API change was made, on the CVS\n" +
"bug fix; this ought to be marked in this list.</p>\n" +
"<ul>\n" +
"<li>The <code>release41</code> branch was made on Apr 03 '05 for use in the NetBeans 4.1 release.\n" +
"Specification versions: 6.0 begins after this point.</li>\n" +
"<li>The <code>release40</code> branch was made on Nov 01 '04 for use in the NetBeans 4.0 release.\n" +
"Specification versions: 5.0 begins after this point.</li>\n" +
"</ul>\n" +
"<hr/>\n" +
"<standard-changelists module-code-name='org.openide.nodes'/>\n" +
"<hr/>\n" +
"<p>@FOOTER@</p>\n" +
"</body>\n" +
"</htmlcontents>\n" +
"</apichanges>\n" +
"");        

        java.io.File xsl = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8' ?>\n" +
"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n" +
    "<xsl:output method='xml'/>\n" +
    "<!-- Format random HTML elements as is: -->\n" +
    "<xsl:template match='@*|node()'>\n" +
        "<xsl:copy>\n" +
            "<xsl:apply-templates select='@*|node()'/>\n" +
        "</xsl:copy>\n" +
    "</xsl:template>\n" +
"</xsl:stylesheet> \n"
        );
        
        
        java.io.File output = PublicPackagesInProjectizedXMLTest.extractString("");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output='" + output + "'" +
            "     apichanges='" + apichanges + "'    xsl='" + xsl + "'\n" +
            "   />\n" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] {  
            "-Darch.generate=true",
            "-Darch.private.disable.validation.for.test.purposes=true",
                
        });

        assertTrue ("Answers still exists", answers.exists ());
        assertTrue ("Output file generated", output.exists ());

        String txt = PublicPackagesInProjectizedXMLTest.readFile(output);

        org.w3c.dom.Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(output);
        
        org.w3c.dom.NodeList list;
        list =  dom.getElementsByTagName("apidef");
        assertEquals ("One apidef element:\n" + txt, 1, list.getLength());
        
        list = dom.getElementsByTagName("change");
        assertEquals("Two change elements:\n" + txt, 2, list.getLength());
        
    }

    
    public void testReadNbDepsFromProjectXML() throws Exception {
        String[] txt = new String[1];
        Document dom = doReadNbDepsFromProjectXML("", txt);
        
        
        org.w3c.dom.NodeList list;
        list =  dom.getElementsByTagName("api");
        assertTrue("There is more than one api tag: " + txt[0], list.getLength() > 0);
        
        for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node n = list.item(i);
                
                assertEquals ("group is java", "java", n.getAttributes().getNamedItem("group").getNodeValue());
                assertEquals ("type is import", "import", n.getAttributes().getNamedItem("type").getNodeValue());
        }
    }

    
    public void testReadNbDepsFromProjectXMLWhenDefaultAnswerRequested() throws Exception {
        String[] txt = new String[1];
        Document dom = doReadNbDepsFromProjectXML(" <defaultanswer generate='here'/>", txt);
        
        
        org.w3c.dom.NodeList list;
        list =  dom.getElementsByTagName("api");
        assertTrue("There is more than one api tag: " + txt[0], list.getLength() > 0);
        
        for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node n = list.item(i);
                
                assertEquals ("group is java", "java", n.getAttributes().getNamedItem("group").getNodeValue());
                assertEquals ("type is import", "import", n.getAttributes().getNamedItem("type").getNodeValue());
        }
        
        assertEquals("Warnings are not included if defaultanswer is present: " + txt[0], -1, txt[0].indexOf("Default answer to this question"));
    }

    public void testReadNbDepsFromProjectXMLWhenDefaultAnswerProhibited () throws Exception {
        String[] txt = new String[1];
        Document dom = doReadNbDepsFromProjectXML(" <defaultanswer generate='none'/>", txt);
        
        
        org.w3c.dom.NodeList list;
        list =  dom.getElementsByTagName("api");
        
        assertEquals("There is no api tag", 0, list.getLength());
    }

    public void testReadNbDepsFromProjectXMLWithPropertiesSetToNameAnd() throws Exception {
        String[] txt = new String[3];
        // txt[0] = is reserved for the doReadNbDepsFromProjectXML method and will contain a result
        txt[1] = "-Darch.org.openide.util.name=UtilitiesAPI";
        txt[2] = "-Darch.org.openide.util.category=official";
        Document dom = doReadNbDepsFromProjectXML(" <defaultanswer generate='here'/>", txt);
        
        
        org.w3c.dom.NodeList list;
        list =  dom.getElementsByTagName("api");
        assertTrue("There is more than one api tag: " + txt[0], list.getLength() > 0);
        
        for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node n = list.item(i);
                
                assertEquals ("group is java", "java", n.getAttributes().getNamedItem("group").getNodeValue());
                assertEquals ("type is import", "import", n.getAttributes().getNamedItem("type").getNodeValue());
                
                if ("UtilitiesAPI".equals(n.getAttributes().getNamedItem("name").getNodeValue())) {
                    assertEquals("Category is official", "official", n.getAttributes().getNamedItem("category").getNodeValue());
                    return;
                }
        }
        
        fail ("There should be the name='UtilitiesAPI' used " + txt[0]);
    }
    
    private Document doReadNbDepsFromProjectXML(String inlinedCode, String[] txt) throws Exception {
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString (
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
            // "<!DOCTYPE api-answers PUBLIC '-//NetBeans//DTD Arch Answers//EN' '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd' [\n" +
            // The following lines needs to be commented out as we do not have the right relative locations!
            // instead there is a part of Arch-api-questions directly inserted into the document bellow
            //  "<!ENTITY api-questions SYSTEM '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml'>\n" +
            //"]>\n" +
"\n" +
"<api-answers\n" +
  "question-version='1.25'\n" +
  "module='Input/Output System'\n" +
  "author='jglick@netbeans.org'\n" +
">\n" +
"\n" +
  // "&api-questions;\n" +
  // replaced by part of api-questions entity            
"<api-questions version='1.25'>\n" +
    "<category id='dep' name='Dependencies'>\n" +
        "<question id='dep-nb' when='init' >\n" +
            "What other nb project this one depends on?\n" +
            "<hint>\n" +
            "Please provide here a few lines describing the project, \n" +
            "what problem it should solve, provide links to documentation, \n" +
            "specifications, etc.\n" +
            "</hint>\n" +
        "</question>\n" +
    "</category>\n" +
"</api-questions>                \n" +
// end of Arch-api-questionx.xmls            
"\n" +
"\n" +
"<answer id='dep-nb'>\n" +
"The Input/Output API is a small API module\n" +
"which contains <code>InputOutput</code> and related interfaces used in\n" +
"driving the Output Window. The normal implementation is <code>org.netbeans.core.output2</code>.\n" +
            inlinedCode + "\n" +
"</answer>\n" +
"\n" +
"</api-answers>    \n"
        );
        
        java.io.File project = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
"<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
    "<type>org.netbeans.modules.apisupport.project</type>\n" +
    "<configuration>\n" +
        "<data xmlns='http://www.netbeans.org/ns/nb-module-project/2'>\n" +
            "<code-name-base>org.openide.loaders</code-name-base>\n" +
            "<module-dependencies>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.util</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.filesystems</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.nodes</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.dialogs</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.modules</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.awt</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.explorer</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.actions</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.text</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.windows</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<specification-version>6.2</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
            "</module-dependencies>\n" +
            "<public-packages>\n" +
                "<package>org.openide.awt</package>\n" +
                "<package>org.openide.actions</package>\n" +
                "<package>org.openide.loaders</package>\n" +
                "<package>org.openide.text</package>\n" +
            "</public-packages>\n" +
        "</data>\n" +
    "</configuration>\n" +
"</project>\n" +
"");        
        
        
        java.io.File output = PublicPackagesInProjectizedXMLTest.extractString("");
        assertTrue("File can be deleted: " + output, output.delete());

        java.io.File xsl = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8' ?>\n" +
"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n" +
    "<xsl:output method='xml'/>\n" +
    "<!-- Format random HTML elements as is: -->\n" +
    "<xsl:template match='@*|node()'>\n" +
        "<xsl:copy>\n" +
            "<xsl:apply-templates select='@*|node()'/>\n" +
        "</xsl:copy>\n" +
    "</xsl:template>\n" +
"</xsl:stylesheet> \n"
        );
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output='" + output + "'" +
            "     project='" + project + "' \n" +
            "     xsl='" + xsl + "' \n" + 
            "   />\n" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        // happy hacking first of the txt argument is used to pass args to the execution script
        txt[0] = "-Darch.private.disable.validation.for.test.purposes=true";
        PublicPackagesInProjectizedXMLTest.execute (f, txt);

        assertTrue ("Answers still exists", answers.exists ());
        assertTrue ("Output file generated", output.exists ());

        // happy hacking2: and now it is used to return back the result of the execution ;-)
        txt[0] = PublicPackagesInProjectizedXMLTest.readFile(output);

        org.w3c.dom.Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(output);
        return dom;
    }

    
    public void testReadNbDepsFromProjectXMLGeneratesCVSLocation () throws Exception {
        String[] txt = new String[1];
        java.io.File answers = PublicPackagesInProjectizedXMLTest.extractString (
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
            // "<!DOCTYPE api-answers PUBLIC '-//NetBeans//DTD Arch Answers//EN' '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd' [\n" +
            // The following lines needs to be commented out as we do not have the right relative locations!
            // instead there is a part of Arch-api-questions directly inserted into the document bellow
            //  "<!ENTITY api-questions SYSTEM '../../nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml'>\n" +
            //"]>\n" +
"\n" +
"<api-answers\n" +
  "question-version='1.25'\n" +
  "module='Input/Output System'\n" +
  "author='jglick@netbeans.org'\n" +
">\n" +
"\n" +
  // "&api-questions;\n" +
  // replaced by part of api-questions entity            
"<api-questions version='1.25'>\n" +
    "<category id='arch' name='Architecture'>\n" +
        "<question id='arch-where' when='init' >\n" +
            "What other nb project this one depends on?\n" +
            "<hint>\n" +
            "Please provide here a few lines describing the project, \n" +
            "what problem it should solve, provide links to documentation, \n" +
            "specifications, etc.\n" +
            "</hint>\n" +
        "</question>\n" +
    "</category>\n" +
"</api-questions>                \n" +
// end of Arch-api-questionx.xmls            
"\n" +
"\n" +
"<answer id='arch-where'>\n" +
            "<defaultanswer generate='here' /> \n" + 
"</answer>\n" +
"\n" +
"</api-answers>    \n"
        );
        
        java.io.File project = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
                "Sun Public License Notice\n" +
"-->\n" +
"<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
    "<type>org.netbeans.modules.apisupport.project</type>\n" +
    "<configuration>\n" +
        "<data xmlns='http://www.netbeans.org/ns/nb-module-project/2'>\n" +
            "<code-name-base>org.openide.loaders</code-name-base>\n" +
            "<module-dependencies>\n" +
            "</module-dependencies>\n" +
            "<public-packages>\n" +
                "<package>org.openide.awt</package>\n" +
            "</public-packages>\n" +
        "</data>\n" +
    "</configuration>\n" +
"</project>\n" +
"");        
        
        
        java.io.File output = PublicPackagesInProjectizedXMLTest.extractString("");
        assertTrue("File can be deleted: " + output, output.delete());

        java.io.File xsl = PublicPackagesInProjectizedXMLTest.extractString(
"<?xml version='1.0' encoding='UTF-8' ?>\n" +
"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n" +
    "<xsl:output method='xml'/>\n" +
    "<!-- Format random HTML elements as is: -->\n" +
    "<xsl:template match='@*|node()'>\n" +
        "<xsl:copy>\n" +
            "<xsl:apply-templates select='@*|node()'/>\n" +
        "</xsl:copy>\n" +
    "</xsl:template>\n" +
"</xsl:stylesheet> \n"
        );
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"arch\" classname=\"org.netbeans.nbbuild.Arch\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "  <arch answers=\"" + answers + "\" output='" + output + "'" +
            "     project='" + project + "' \n" +
            "     xsl='" + xsl + "' \n" + 
            "   />\n" +
            "<target name=\"all\" >" +
            "  " +
            "</target>" +
            "</project>"

        );
        // happy hacking first of the txt argument is used to pass args to the execution script
        txt[0] = "-Darch.private.disable.validation.for.test.purposes=true";
        PublicPackagesInProjectizedXMLTest.execute (f, txt);

        assertTrue ("Answers still exists", answers.exists ());
        assertTrue ("Output file generated", output.exists ());

        // happy hacking2: and now it is used to return back the result of the execution ;-)
        txt[0] = PublicPackagesInProjectizedXMLTest.readFile(output);

        org.w3c.dom.Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(output);
        
        
        if (txt[0].indexOf("http://www.netbeans.org/source/browse/") == -1) {
            fail ("reference to CVS location should be in output: " + txt[0]);
        }
    }
    
    
}
