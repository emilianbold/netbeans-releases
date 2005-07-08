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
}
