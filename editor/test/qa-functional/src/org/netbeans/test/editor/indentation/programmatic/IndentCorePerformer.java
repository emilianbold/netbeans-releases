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
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new org.netbeans.junit.diff.SimpleDiff());
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
