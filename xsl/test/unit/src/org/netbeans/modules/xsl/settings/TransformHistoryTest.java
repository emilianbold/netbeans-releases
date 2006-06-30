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
package org.netbeans.modules.xsl.settings;

import java.util.*;
import java.io.*;
import java.rmi.MarshalledObject;

import junit.framework.*;
import org.netbeans.junit.*;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.xsl.utils.TransformUtil;

/**
 *
 * @author Libor Kramolis
 */
public class TransformHistoryTest extends NbTestCase {

    public TransformHistoryTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformHistoryTest.class);
        
        return suite;
    }
    
    
    public void testIt () {
        System.out.println("testIt");
        
        TransformHistory history = new TransformHistory();
        
        // current max number of items in history is 5!
        assertTrue ("[1/5][1/5] OK!", checkHistory (history, 1, 1, "in.xml", "trans.xsl", "out.put"));
        assertTrue ("[1/5][1/5] OK!", checkHistory (history, 1, 1, "in.xml", null, null));
        assertTrue ("[2/5][1/5] OK!", checkHistory (history, 2, 1, "in2.xml", null, "out2.put"));
        assertTrue ("[3/5][1/5] OK!", checkHistory (history, 3, 1, "in3.xml", null, "out3.put"));
        assertTrue ("[4/5][1/5] OK!", checkHistory (history, 4, 1, "in4.xml", null, "out4.put"));
        assertTrue ("[5/5][1/5] OK!", checkHistory (history, 5, 1, "in5.xml", null, "out5.put"));
        assertTrue ("[6/5][1/5] OK!", checkHistory (history, 5, 1, "in6.xml", null, "out6.put"));
        assertTrue ("Output for in6.xml is out6.put!", "out6.put".equals (history.getXMLOutput("in6.xml")));
        assertTrue ("Output for in.xml is null!", (history.getXMLOutput("in.xml") == null));
    }
    
    
    private boolean checkHistory (TransformHistory history, int xi, int ti, String xml, String xsl, String output) {
        // modify history
        history.setOverwriteOutput (!history.isOverwriteOutput()); // negate
        history.setProcessOutput ((history.getProcessOutput()+1)%3); // rotate
        if ( xml != null ) {
            history.addXML (xml, output);
        }
        if ( xsl != null ) {
            history.addXSL (xsl, output);
        }

        // test number of XMLs
        if ( history.getXMLs().length != xi ) {
            System.out.println("    history.getXMLs().length: " + history.getXMLs().length);
            return false;
        }
        // test number of XSLs
        if ( history.getXSLs().length != ti ) {
            System.out.println("    history.getXSLs().length: " + history.getXSLs().length);
            return false;
        }
        
        // (de)marshal
        TransformHistory newHistory = null;
        try {
            MarshalledObject marshalled = new MarshalledObject (history);
            newHistory = (TransformHistory) marshalled.get();
        } catch (Exception exc) {
            System.err.println("!!! " + exc);
            return false;
        }
        
        // test if equals
        return (history.equals (newHistory));
    }
    
}
