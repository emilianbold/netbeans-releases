/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.clearcase.client.status;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.clearcase.Clearcase;
import org.netbeans.modules.clearcase.ClearcaseException;
import org.netbeans.modules.clearcase.FileInformation;
import org.netbeans.modules.clearcase.FileStatusCache;
import org.netbeans.modules.clearcase.client.ClearcaseCommand;
import org.netbeans.modules.clearcase.client.test.DummyCleartool;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 */
public class ListStatusTest extends TestCase {
    
    private Method createFileInformation = null;
    private FileStatusCache cache;
    
    public ListStatusTest(String testName) {
        super(testName);        
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        
        cache = Clearcase.getInstance().getFileStatusCache();
        createFileInformation = cache.getClass().getDeclaredMethod("createFileInformation", FileEntry.class);        
        createFileInformation.setAccessible(true);
        
        Method m = cache.getClass().getDeclaredMethod("isIgnored", File.class);        
        m.setAccessible(true);        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUptodate() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String rawOutput = "version                Main.java@@/main/1                     Rule: element * /main/LATEST";
        List<FileEntry> entryList = execList(rawOutput);        
        
        assertEquals(1, entryList.size());        
        assertListOutput(entryList.get(0), null, "element * /main/LATEST", new File("Main.java"), "/main", 1L, "/main/1", null, -1, null, false, "version");                
        
        FileEntry entry = convert(entryList.get(0));    
        FileInformation info = createFileInformation(entry);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());
    }
        
    public void testViewPrivate() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        String rawOutput = "view private object    file0";
        List<FileEntry> entryList = execList(rawOutput);
        
        assertEquals(1, entryList.size());        
        assertListOutput(entryList.get(0), null, null, new File("file0"), null, -1, null, null, -1, null, false, "view private object");        
        
        FileEntry entry = convert(entryList.get(0));        
        FileInformation info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);        
    }

    public void testCheckedout() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        String lsRawOutput = "version                README@@/main/CHECKEDOUT from /main/3  Rule: element * CHECKEDOUT";
        String lscoRawOutput = "README<~=~>cctomas<~=~>reserved";
        List<FileEntry> entryList = execList(lsRawOutput, lscoRawOutput);
        
        assertEquals(1, entryList.size());                
        assertListOutput(entryList.get(0), null, "element * CHECKEDOUT", new File("README"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 3L, "/main/3", true, "version");                
        
        FileEntry entry = convert(entryList.get(0));  
        FileInformation info = createFileInformation(entry);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());                
        assertEquals("Reserved", info.getShortStatusText());
        assertEquals("Reserved", info.getStatusText());
    }

    public void testSeemedToBeCheckedout() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        String lsRawOutput = "version                README@@/main/CHECKEDOUT from /main/3  Rule: element * CHECKEDOUT";
        String lscoRawOutput = "README<~=~>cctomas<~=~>";
        List<FileEntry> entryList = execList(lsRawOutput, lscoRawOutput);
        
        assertEquals(1, entryList.size());                
        assertListOutput(entryList.get(0), null, "element * CHECKEDOUT", new File("README"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 3L, "/main/3", true, "version");                
        
        FileEntry entry = convert(entryList.get(0));  
        FileInformation info = createFileInformation(entry);
        assertTrue((FileInformation.STATUS_VERSIONED_CHECKEDOUT & info.getStatus()) != 0);                        
    }
            
    public void testCheckedoutUnreserved() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        String lsRawOutput = "version                README@@/main/CHECKEDOUT from /main/1  Rule: element * CHECKEDOUT";
        String lscoRawOutput = "README<~=~>cctomas<~=~>unreserved";

        List<FileEntry> entryList = execList(lsRawOutput, lscoRawOutput);
        
        assertEquals(1, entryList.size());                
        assertListOutput(entryList.get(0), null, "element * CHECKEDOUT", new File("README"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 1L, "/main/1", true, "version");                
        
        FileEntry entry = convert(entryList.get(0));  
        FileInformation info = createFileInformation(entry);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT | FileInformation.STATUS_UNRESERVED, info.getStatus());                
        assertEquals("Unreserved", info.getShortStatusText());        
        assertEquals("Unreserved", info.getStatusText());        
    }
    
    public void testCheckedoutButRemoved() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String[] lsRawOutput = new String[] {"version                README@@/main/CHECKEDOUT from /main/1 [checkedout but removed]",
                                             "version                test1@@/main/CHECKEDOUT from /main/2 [not loaded, checkedout but removed]"};        
        String[] lscoRawOutput = new String[] {"README<~=~>cctomas<~=~>reserved",
                                               "test1<~=~>cctomas<~=~>reserved"};
        
        List<FileEntry> entryList = execList(lsRawOutput, lscoRawOutput);
        
        assertEquals(2, entryList.size());                 
        assertListOutput(entryList.get(0), "[checkedout but removed]", null, new File("README"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 1L, "/main/1", true, "version");                
        assertListOutput(entryList.get(1), "[not loaded, checkedout but removed]", null, new File("test1"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 2L, "/main/2", true, "version");                        
        
        FileEntry entry = convert(entryList.get(0));                    
        FileInformation info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED);                        
        
        entry = convert(entryList.get(1));
        info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED);                        
    }

    public void testCheckedoutButRemovedUnreserved() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String[] lsRawOutput = new String[] {"version                README@@/main/CHECKEDOUT from /main/1 [checkedout but removed]"};        
        String[] lscoRawOutput = new String[] {"README<~=~>cctomas<~=~>unreserved"};
        
        List<FileEntry> entryList = execList(lsRawOutput, lscoRawOutput);
        
        assertEquals(1, entryList.size());                 
        assertListOutput(entryList.get(0), "[checkedout but removed]", null, new File("README"), "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", "/main", 1L, "/main/1", true, "version");                        
        
        FileEntry entry = convert(entryList.get(0));                    
        FileInformation info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED | FileInformation.STATUS_UNRESERVED);                                
    }
    
    public void testLoadedButMissing() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        String rawOutput = "version                test1@@/main/2 [loaded but missing]    Rule: element * /main/LATEST";
        List<FileEntry> entryList = execList(rawOutput);
        
        assertEquals(1, entryList.size());                 
        assertListOutput(entryList.get(0), "[loaded but missing]", null, new File("test1"), "/main", 2L, "/main/2", null, -1, null, false, "version");                
        
        FileEntry entry = convert(entryList.get(0));
        FileInformation info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_VERSIONED_LOADED_BUT_MISSING);        
    }        

    public void testHijacked() throws IOException, ClearcaseException , IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String rawOutput = "version                test1@@/main/2 [hijacked]              Rule: element * /main/LATEST";
        List<FileEntry> entryList = execList(rawOutput);
        
        assertEquals(1, entryList.size());                   
        assertListOutput(entryList.get(0), "[hijacked]", "element * /main/LATEST", new File("test1"), "/main", 2L, "/main/2", null, -1, null, false, "version");                
        
        FileEntry entry = convert(entryList.get(0));       
        FileInformation info = createFileInformation(entry);
        assertEquals(info.getStatus(), FileInformation.STATUS_VERSIONED_HIJACKED);        
    }        

    public void testEclipsed() throws IOException, ClearcaseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String[] rawOutput = new String[] {            
            "file element           Makefile@@ [eclipsed]",
            "view private object    Makefile"
        };
        List<FileEntry> entryList = execList(rawOutput);
        
        assertEquals(1, entryList.size()); 
        assertListOutput(entryList.get(0), "[eclipsed]", null, new File("Makefile"), null, -1, null, null, -1, null, false, "file element");                        
        
        FileEntry entry = convert(entryList.get(0));       
        FileInformation info = createFileInformation(entry);
        assertNotSame(info.getStatus(), FileInformation.STATUS_NOTVERSIONED_ECLIPSED);
        
        rawOutput = new String[] {                        
            "view private object    Makefile",
            "file element           Makefile@@ [eclipsed]"
        };
        entryList = execList(rawOutput);
        
        assertEquals(1, entryList.size());                
        assertListOutput(entryList.get(0), "[eclipsed]", null, new File("Makefile"), null, -1, null, null, -1, null, false, "file element");                                
    }        
    
    public void testCrap() throws IOException, ClearcaseException {
        List<FileEntry> entryList = execList(null, new String[]{});
        assertEquals(entryList.size(), 0);       
        
        String[] rawOutput = new String[] {
            "x",
            "",
            "crap crap crap",            
        };
        
        entryList = execList(rawOutput);        
        assertEquals(0, entryList.size());               
        
        rawOutput = new String[] {            
            "version                Main.java@@/main/1                     ",            
            "version                Main1.java@@/main/",
            "version                Main2.java@@/main/xxx",
            "version                Main3.java@@",
            "version                Main4.java@",
            "version                Main5.java",
            "version       ",            
        };
        entryList = execList(rawOutput);   
        assertEquals(7, entryList.size());               
                
    }        
            
    private void assertListOutput(FileEntry fe, String annotation, String rule, File file, String versionPath, 
                                  long version, String versionSelector, String originVersionPath, 
                                  long originVersion, String originVersionSelector, boolean checkedout, String type) {         
        assertEquals(annotation, fe.getAnnotation());
        if(rule != null) {
            assertEquals(rule, fe.getRule());               
        }        
        assertEquals(file, fe.getFile());
        if(versionSelector != null) {
            assertNotNull(fe.getVersion());
            assertEquals(versionPath,       fe.getVersion().getPath());
            assertEquals(version,           fe.getVersion().getVersionNumber());
            assertEquals(versionSelector,   fe.getVersion().getVersionSelector());   
            assertEquals(checkedout,        fe.getVersion().isCheckedout());            
        } else {
            assertNull(fe.getVersion());   
        }        
        if(originVersionSelector != null) {
            assertNotNull(fe.getOriginVersion());
            assertEquals(originVersionPath,         fe.getOriginVersion().getPath());
            assertEquals(originVersion,             fe.getOriginVersion().getVersionNumber());
            assertEquals(originVersionSelector,     fe.getOriginVersion().getVersionSelector());               
        } else {
            assertNull(fe.getOriginVersion());
        }                                
        assertEquals(type, fe.getType());
    }

    public static List<FileEntry> execList(String lsRawOutput) throws IOException, ClearcaseException {
        return execList(new String[] { lsRawOutput }, new String[] {});
    }
    
    public static List<FileEntry> execList(String[] lsRawOutput) throws IOException, ClearcaseException {
        return execList(lsRawOutput, new String[] {});
    }

    public static List<FileEntry> execList(String lsRawOutput, String lscoRawOutput) throws IOException, ClearcaseException {
        return execList(new String[] { lsRawOutput }, new String[] { lscoRawOutput });
    }
    
    public static List<FileEntry> execList(String[] lsRawOutput, String[] lscoRawOutput) throws IOException, ClearcaseException {

        ListCommandExecutor executor = new ListCommandExecutor(lsRawOutput, lscoRawOutput);
        DummyCleartool ct = new DummyCleartool(executor);

        ListStatus lf = new ListStatus(new File(""), false);
        for (ClearcaseCommand c : lf) {
            ct.exec(c);
        }

        List<FileEntry> entryList = new  ArrayList<FileEntry>(lf.getOutput());
        return entryList;
    }
    
    private static class ListCommandExecutor implements DummyCleartool.CommandExecutor {
        final private String[] lsRawOutput;
        final private String[] lscoRawOutput;

        public ListCommandExecutor(String[] lsRawOutput, String[] lscoRawOutput) {
            this.lsRawOutput = lsRawOutput;
            this.lscoRawOutput = lscoRawOutput;
        }
        
        public void exec(ClearcaseCommand command) {
            if(lsRawOutput == null) {
                command.outputText(null);
                return;
            }
            if(command.getClass().getName().indexOf("ListCommand") > -1) {
                for (String string : lsRawOutput) {
                    command.outputText(string);                        
                }    
            } else if(command.getClass().getName().indexOf("LSCOCommand") > -1) {
                for (String string : lscoRawOutput) {
                    command.outputText(string);                        
                }                    
            }
            
        }        
    }    
    
    private FileInformation createFileInformation(FileEntry entry) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (FileInformation) createFileInformation.invoke(cache, new Object[] {entry});
    }
    
    private FileEntry convert(FileEntry entry) {
        return new FileEntry(entry.getType(), FileUtil.normalizeFile(entry.getFile()), entry.getOriginVersion(), entry.getVersion(), entry.getAnnotation(), entry.getRule(), entry.isReserved(), "");
    }
}
