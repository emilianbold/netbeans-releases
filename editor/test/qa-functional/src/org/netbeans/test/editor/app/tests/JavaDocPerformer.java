//This file was automaticaly generated. Do not modify.
/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.editor.app.tests;
import java.io.PrintWriter;
import org.openide.filesystems.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import java.io.File;
import org.netbeans.test.editor.LineDiff;


/**
 *
 * @author ehucka
 * @version 1.2
 *  */

public class JavaDocPerformer extends NbTestCase {
    public JavaDocPerformer(String name) {
        super(name);
    }
    public void tearDown() throws Exception {
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
    }
    
    /**
     * Call Action: inside
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testinside() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.inside"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: start
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void teststart() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.start"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: end
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testend() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.end"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: insideNoLeading
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testinsideNoLeading() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.insideNoLeading"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: startNoLeading
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void teststartNoLeading() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.startNoLeading"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: endNoLeading
     * Sub Test: Writting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testendNoLeading() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Writting.endNoLeading"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: ReformattingLeadingStar
     * Sub Test: Reformatting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testReformattingLeadingStar() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Reformatting.ReformattingLeadingStar"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
    
    /**
     * Call Action: ReformatingNoLeadingStar
     * Sub Test: Reformatting
     * Author:   ehucka
     * Version:  1.2
     * Comment:
     */
    
    public void testReformatingNoLeadingStar() throws Exception {
        PrintWriter ref = null;
        PrintWriter log = null;
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc.xml",
            "Reformatting.ReformatingNoLeadingStar"
        };
        try {
            ref = new PrintWriter(getRef());
            log = new PrintWriter(getLog());
            new CallTestGeneric().runTest(arguments, log, ref);
        } finally {
            if (ref != null) ref.flush();
            if (log != null) log.flush();
        }
    }
}
