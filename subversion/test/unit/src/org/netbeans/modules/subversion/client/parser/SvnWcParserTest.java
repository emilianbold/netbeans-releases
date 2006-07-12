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
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client.parser;

import junit.framework.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Ed Hillmann
 */
public class SvnWcParserTest extends TestCase {

    private String dataRootDir;
    private SvnWcParser svnWcParser;

    public SvnWcParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        svnWcParser = new SvnWcParser();

        //data.root.dir defined in project.properties
        dataRootDir = System.getProperty("data.root.dir");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SvnWcParserTest.class);

        return suite;
    }

    public void testGetSingleStatusNoChanges() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(2, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileChanges() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-changes/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getTextStatus());
        assertEquals(2, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileUnknown() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-unknown/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    /**
     * Tests a specific case... where the file doesn't exist, and there is no entry in the SVN
     * files, but it's still being queried by the module.  Return unversioned
     */
    public void testGetSingleStatusFileUnknownAnywhere() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileAdded() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-added/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileConflict() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-conflicts/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.CONFLICTED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());
        assertEquals(5, parsedStatus.getLastChangedRevision().getNumber());
        assertNotNull(parsedStatus.getConflictNew());
        assertNotNull(parsedStatus.getConflictOld());
        assertNotNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T04:12:27.726955Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(5, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileRemoved() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-removed/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.DELETED, parsedStatus.getTextStatus());
        assertEquals(6, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T04:22:27.194329Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(6, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileCopied1() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-copied1/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrlCopiedFrom().toString());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());        
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileCopied2() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-copied2/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrlCopiedFrom().toString());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusPropertyAdded() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/prop-added/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(8, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T06:55:09.997277Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(8, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusPropertyModified() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/prop-modified/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(9, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:01:25.704780Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(9, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileLocked() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-locked/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(10, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertEquals("", parsedStatus.getLockComment());
        assertEquals("ed", parsedStatus.getLockOwner());
        expectedDate = SvnWcUtils.parseSvnDate("2006-05-27T04:15:00.168100Z");
        assertEquals(expectedDate, parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusNoChangesKeywords() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes-keywords/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertNull(parsedStatus.getUrlCopiedFrom());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(11, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-06-05T11:03:38.718355Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(11, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusSymbolicLink() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-symbolic-link/bin/myLink");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
    }
}
