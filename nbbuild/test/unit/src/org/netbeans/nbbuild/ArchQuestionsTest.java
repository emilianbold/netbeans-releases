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
}
