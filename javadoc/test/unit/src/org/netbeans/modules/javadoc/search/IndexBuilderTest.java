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

package org.netbeans.modules.javadoc.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javadoc.search.IndexBuilder.SimpleTitleParser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Jan Pokorsky
 */
public class IndexBuilderTest extends NbTestCase {
    
    private LocalFileSystem fs;
    private static final String JDK14_INDEX_PATH = "docs_jdk14/api/index-files";
    private static final String JDK14_JA_INDEX_PATH = "docs_jdk14_ja/api/index-files";
    private static final String JDK15_INDEX_PATH = "docs_jdk15/api/index-files";
    private static final String JDK15_JA_INDEX_PATH = "docs_jdk15_ja/api/index-files";

    /** Creates a new instance of IndexBuilderTest */
    public IndexBuilderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        File dataFile = getDataDir();
        assertNotNull("missing data file", dataFile);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dataFile);
    }

    public void testTitleInJDK14() throws Exception {
        FileObject html = fs.findResource(JDK14_INDEX_PATH + "/index-4.html");

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE v1.4.2)", titlestr);
        } finally {
            is.close();
        }
    }

    public void testTitleInJDK15() throws Exception {
        FileObject html = fs.findResource(JDK15_INDEX_PATH + "/index-4.html");

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE 5.0)", titlestr);
        } finally {
            is.close();
        }
    }

    public void testTitleInJDK14_ja() throws Exception {
        FileObject html = fs.findResource(JDK14_JA_INDEX_PATH + "/index-4.html");
        FileObject html2 = fs.findResource(JDK14_JA_INDEX_PATH + "/index-4.title");
        japanaseIndexes(html, html2, "iso-2022-jp");
    }

    public void testTitleInJDK15_ja() throws Exception {
        FileObject html = fs.findResource(JDK15_JA_INDEX_PATH + "/index-4.html");
        FileObject html2 = fs.findResource(JDK15_JA_INDEX_PATH + "/index-4.title");
        japanaseIndexes(html, html2, "euc-jp");
    }

    private void japanaseIndexes(FileObject html, FileObject title, String charset) throws Exception {
        assertNotNull(html);
        assertNotNull(title);

        Reader r = new java.io.InputStreamReader(title.getInputStream(), charset);

        int ic;
        StringBuilder sb = new StringBuilder();
        try {
            while ((ic = r.read()) != -1) {
                sb.append((char) ic);
            }
        } finally {
            r.close();
        }

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", sb.toString(), titlestr);
        } finally {
            is.close();
        }
    }
}
