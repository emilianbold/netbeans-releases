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
package org.netbeans.test.editor.indentation.programmatic;
import java.io.*;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import java.io.File;
public class IndentCorePerformer extends NbTestCase {
    public IndentCorePerformer(String testCase) {
        super(testCase);
    }
    public void tearDown() throws Exception {
        assertFile("Output does not match golden file.", getGoldenFile(),
        new File(getWorkDir(), this.getName() + ".ref"), new File(getWorkDir(),
        this.getName() + ".diff"), new org.netbeans.test.editor.LineDiff(false));
    }
    public void testwholeClass() throws Exception {
        PrintWriter log = null;
        PrintWriter ref = null;
        try {
            log = new PrintWriter(getLog());
            ref = new PrintWriter(getRef());
            Map indentationProperties = new HashMap();
            new IndentCore().run(log, ref, "data/testfiles/IndentCorePerformer/wholeClass.txt", "text/x-java", indentationProperties);
        } finally {
            log.flush();
            ref.flush();
        }
    }
    public void testcomplexMethod() throws Exception {
        PrintWriter log = null;
        PrintWriter ref = null;
        try {
            log = new PrintWriter(getLog());
            ref = new PrintWriter(getRef());
            Map indentationProperties = new HashMap();
            new IndentCore().run(log, ref, "data/testfiles/IndentCorePerformer/complexMethod.txt", "text/x-java", indentationProperties);
        } finally {
            log.flush();
            ref.flush();
        }
    }
    public void testcomplexClass() throws Exception {
        PrintWriter log = null;
        PrintWriter ref = null;
        try {
            log = new PrintWriter(getLog());
            ref = new PrintWriter(getRef());
            Map indentationProperties = new HashMap();
            new IndentCore().run(log, ref, "data/testfiles/IndentCorePerformer/complexClass.txt", "text/x-java", indentationProperties);
        } finally {
            log.flush();
            ref.flush();
        }
    }
    public void testwholeMethod() throws Exception {
        PrintWriter log = null;
        PrintWriter ref = null;
        try {
            log = new PrintWriter(getLog());
            ref = new PrintWriter(getRef());
            Map indentationProperties = new HashMap();
            new IndentCore().run(log, ref, "data/testfiles/IndentCorePerformer/wholeMethod.txt", "text/x-java", indentationProperties);
        } finally {
            log.flush();
            ref.flush();
        }
    }
    public void testEngine() throws Exception {
        PrintWriter log = null;
        PrintWriter ref = null;
        try {
            log = new PrintWriter(getLog());
            ref = new PrintWriter(getRef());
            Map indentationProperties = new HashMap();
            new IndentCore().run(log, ref, "data/testfiles/IndentCorePerformer/Engine.txt", "text/x-java", indentationProperties);
        } finally {
            log.flush();
            ref.flush();
        }
    }
}
