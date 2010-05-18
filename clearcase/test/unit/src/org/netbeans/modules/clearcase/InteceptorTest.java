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

package org.netbeans.modules.clearcase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.clearcase.client.Arguments;
import org.netbeans.modules.clearcase.client.CheckinCommand;
import org.netbeans.modules.clearcase.client.CheckoutCommand;
import org.netbeans.modules.clearcase.client.FilesCommand;
import org.netbeans.modules.clearcase.client.MkElemCommand;
import org.netbeans.modules.clearcase.client.UnCheckoutCommand;
import org.netbeans.modules.clearcase.client.status.FileEntry;
import org.netbeans.modules.clearcase.util.ClearcaseUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public class InteceptorTest extends NbTestCase {
   
    private ClearcaseInterceptor interceptor;
    private File testRoot;
    private FileStatusCache cache;
    
    private String MOCKUP_KEY = "org.netbeans.modules.clearcase.client.mockup.vobRoot";
    private String MOCKUP_ROOT = "/tmp/vob";
    
    public InteceptorTest(String testName) throws IOException {
        super(testName);
        
        // run with mockup
//         System.setProperty(MOCKUP_KEY, MOCKUP_ROOT);
//         testRoot = new File(MOCKUP_ROOT + "/inteceptortest"); 
//        
        // run with cleartool
         testRoot = new File("/data/ccase/tester/deletetest"); 
    }            

    @Override
    protected void setUp() throws Exception {
        cleanUp();
        init();
        
        cache = new FileStatusCache();
        interceptor = new ClearcaseInterceptor();
        super.setUp();
    }

    private void cleanUp() {
        if(!testRoot.exists()) {
            return;
        }
        String mockup = System.getProperty(MOCKUP_KEY);
        File[] files = testRoot.listFiles();
        for (File f : files) {
            if(mockup != null && !mockup.trim().equals("")) {
                Utils.deleteRecursively(f);    
            } else {
                File parent = f.getParentFile();
                ensureMutable(parent);            
                FileEntry entry = ClearcaseUtils.readEntry(Clearcase.getInstance().getClient(), f);
                if(entry != null && !entry.isViewPrivate()) {
                    uncheckout(f);
                    Clearcase.getInstance().getClient().exec(new RmElemCommand(f), false);            
                    FileUtil.refreshFor(parent);
                    Clearcase.getInstance().getClient().exec(new CheckinCommand(new File[] {parent}, null, true, false), false);
                } else {
                    Utils.deleteRecursively(f);    
                }
            }    
        }
    }
    
    private void init() {
        FileEntry entry = ClearcaseUtils.readEntry(Clearcase.getInstance().getClient(), testRoot);
        if(entry == null || entry.isViewPrivate()) {
            testRoot.mkdirs();
            add(testRoot);    
        }        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeleteNotMananaged() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File file = File.createTempFile("file", null);
        file.createNewFile();
        
        refreshImmediatelly(file);
        
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());
        
        assertFalse(delete(file)); // interceptor refused to handle the file
        
//        // however, let's call delete on the interceptor 
//        interceptorDelete(file);
//                
//        refreshImmediatelly(file);
//        
//        // test
//        assertFalse(file.exists());                                         // file is deleted        
//        info = cache.getInfo(file);
//        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown                
    }
    
    public void testDeleteViewPrivate() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File file = new File(testRoot, "file");
        file.createNewFile();
        
        refreshImmediatelly(file);
        
        // create notmanaged file 
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus());
        
        // test
        assertFalse(delete(file)); // interceptor refused to handle the file
                
//        // however, let's call delete on the interceptor 
//        interceptorDelete(file);                
//        refreshImmediatelly(file);
//        
//        // test
//        assertFalse(file.exists());                                         // file is deleted        
//        info = cache.getInfo(file);
//        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown                        
    }

    // XXX try to emulate also remotely checkedout scenario
    public void testDeleteUptodateFileUptodateParent() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File parent = new File(testRoot, "parent");
        parent.mkdirs();
        
        // create uptodate file and folder
        File file = new File(parent, "file");
        file.createNewFile();        
        add(parent, file);        
        refreshImmediatelly(parent);
    
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());        
        info = cache.getInfo(parent);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());
        
        // delete file
        assertTrue(delete(file));
        refreshImmediatelly(parent);
        refreshImmediatelly(file);
        
        // test
        assertFalse(file.exists());                                         // file is deleted        
        info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown
        
        info = cache.getInfo(parent);       
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
    }
    
    public void testDeleteUptodateFileCheckedoutParent() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File parent = new File(testRoot, "parent");
        parent.mkdirs();
        
        // create uptodate file and folder
        File file = new File(parent, "file");
        file.createNewFile();        
        add(parent, file);        
        refreshImmediatelly(parent);
        refreshImmediatelly(file);
        ensureMutable(parent);
        refreshImmediatelly(parent);
        
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());        
        info = cache.getInfo(parent);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
        
        // delete file
        assertTrue(delete(file));
        refreshImmediatelly(file);
        refreshImmediatelly(parent);
        
        // test
        assertFalse(file.exists());                                         // file is deleted        
        info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown
        
        info = cache.getInfo(parent);       
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
    }
    
    public void testDeleteCheckedoutFileUptodateParent() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File parent = new File(testRoot, "parent");
        parent.mkdirs();
        
        // create uptodate file and folder
        File file = new File(parent, "file");
        file.createNewFile();        
        add(parent, file);
        refreshImmediatelly(parent);
        refreshImmediatelly(file);
        ensureMutable(file);
        refreshImmediatelly(file);
        
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());        
        info = cache.getInfo(parent);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());
        
        // delete file
        assertTrue(delete(file));
        refreshImmediatelly(file);
        refreshImmediatelly(parent);
        
        // test
        assertFalse(file.exists());                                         // file is deleted        
        info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown
        
        info = cache.getInfo(parent);       
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
    }

    public void testDeleteCheckedoutFileCheckedoutParent() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File parent = new File(testRoot, "parent");
        parent.mkdirs();
        
        // create uptodate file and folder
        File file = new File(parent, "file");
        file.createNewFile();        
        add(parent, file);
        refreshImmediatelly(parent);
        refreshImmediatelly(file);
        ensureMutable(file);
        ensureMutable(parent);
        refreshImmediatelly(parent);
        refreshImmediatelly(file);
        
        FileInformation info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());        
        info = cache.getInfo(parent);
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
        
        // delete file
        assertTrue(delete(file));
        refreshImmediatelly(file);
        refreshImmediatelly(parent);
        
        // test
        assertFalse(file.exists());                                         // file is deleted        
        info = cache.getInfo(file);
        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());     // chache keeps track of it as unknown
        
        info = cache.getInfo(parent);       
        assertEquals(FileInformation.STATUS_VERSIONED_CHECKEDOUT, info.getStatus());
    }
    
    public void testMoveFromNotManagedFileToNotManaged() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File from = File.createTempFile("fromfile", null);
        from.createNewFile();
        File to = File.createTempFile("tofile", null);
        
        refreshImmediatelly(to);
        refreshImmediatelly(from);
        
        FileInformation info = cache.getInfo(from);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());
        info = cache.getInfo(to);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());
        
        assertFalse(move(from, to)); // interceptor refused to handle the file        
     
        // however, let's call move on the interceptor 
        interceptorMove(from, to);
        refreshImmediatelly(to);
        refreshImmediatelly(from);
        
        // test
        assertFalse(from.exists());                                                         // from is gone
        assertTrue(to.exists());                                                            // to was created
        info = cache.getInfo(from);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());     // once notmanaged, stays notmanaged

        info = cache.getInfo(to);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());     //  --------------- "" -------------
    }

    public void testMoveFromViewPrivateFileToViewPrivateFolder() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        File fromParent = new File(testRoot, "fromparent");
        fromParent.mkdirs();
        File toParent = new File(testRoot, "toparent");
        toParent.mkdirs();
        
        // create uptodate file and folder
        File from = new File(fromParent, "fromfile");
        from.createNewFile();        
        File to = new File(toParent, "tofile");
        to.createNewFile();        
        
        add(fromParent, toParent);        
        refreshImmediatelly(fromParent);
        refreshImmediatelly(toParent);        
        refreshImmediatelly(to);
        refreshImmediatelly(from);
        
        FileInformation info; 
        info = cache.getInfo(fromParent); assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());      
        info = cache.getInfo(toParent);   assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());      
        info = cache.getInfo(from);       assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus()); 
        info = cache.getInfo(to);         assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus()); 
        
        // test
        assertFalse(move(from, to)); // interceptor refused to handle the file        
     
        // however, let's call move on the interceptor 
        interceptorMove(from, to);
        refreshImmediatelly(to);
        refreshImmediatelly(from);
        
        // test
        assertFalse(from.exists());                                                         // from is gone
        assertTrue(to.exists());                                                            // to was created
        info = cache.getInfo(from); assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());                     // cache keeps track as unknown
        info = cache.getInfo(to);   assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus());     //  --------------- "" -------------
    }
    
//    public void testMoveViewPrivateFile2ViewPrivate() throws IOException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
//        File fromParent = new File(testRoot, "fromparent");
//        fromParent.mkdirs();
//        File toParent = new File(testRoot, "toparent");
//        toParent.mkdirs();
//        
//        // create uptodate file and folder
//        File from = new File(fromParent, "fromfile");
//        from.createNewFile();        
//        File to = new File(toParent, "tofile");
//        to.createNewFile();        
//        
//        add(fromParent, toParent);        
//        refreshImmediatelly(fromParent);
//        refreshImmediatelly(toParent);        
//        refreshImmediatelly(to);
//        refreshImmediatelly(from);
//        
//        FileInformation info = cache.getInfo(from);
//        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus());
//        info = cache.getInfo(to);
//        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus());
//        
//        assertFalse(move(from, to)); // interceptor refused to handle the file        
//     
//        // however, let's call move on the interceptor 
//        interceptorMove(from, to);
//        refreshImmediatelly(to);
//        refreshImmediatelly(from);
//        
//        // test
//        assertFalse(from.exists());                                                         // from is gone
//        assertTrue(to.exists());                                                            // to was created
//        info = cache.getInfo(from);
//        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());                     // cache keeps track as unknown
//
//        info = cache.getInfo(to);
//        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, info.getStatus());     //  --------------- "" -------------
//    }

    private boolean delete(File file) throws IOException {
        boolean delete = interceptor.beforeDelete(file);
        if (delete) {
            interceptorDelete(file);
            return true;  // interceptor handled the delete
        } else {
            return false; // interceptor refused handling the delete
        }                
    }

    private boolean interceptorDelete(File file) throws IOException {        
        interceptor.doDelete(file);        
        interceptor.afterDelete(file);
        
        // the doDelete works asynchronusly. lets give him some time ...
        for(int i = 0; i < 30; i++) {
            waitALittleBit(200);
            if(!file.exists()) {
                break;
            }
        }
        return true; // interceptor handled the delete
    }   
    
    private boolean move(File from, File to) throws IOException {
        boolean move = interceptor.beforeMove(from, to);
        if (move) {
            interceptorMove(from, to);
            return true;  // interceptor handled the move
        } else {
            return false; // interceptor refused handling the move
        }        
    }

    private void interceptorMove(File from, File to) throws IOException {
        interceptor.afterMove(from, to);
        interceptor.doMove(from, to);
        // the doMove works asynchronusly. lets give him some time ...
        for(int i = 0; i < 30; i++) {
            waitALittleBit(200);
            if(!from.exists() || to.exists()) {
                break;
            }
        }
    }
    
    private void refreshImmediatelly(File file) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        FileUtil.refreshFor(file.getParentFile());
        Method m = cache.getClass().getDeclaredMethod("refresh", new Class[] {File.class, boolean.class});
        m.setAccessible(true);
        m.invoke(cache, new Object[] {file, true});
    }
    
    private void uncheckout(File file) {        
        FileEntry entry = ClearcaseUtils.readEntry(Clearcase.getInstance().getClient(), file);
        if (entry != null && entry.isCheckedout()) {
            Clearcase.getInstance().getClient().exec(new UnCheckoutCommand(new File[]{file}, false), true);
        }
        File[] files = file.listFiles();
        if(files == null) {
            return;
        }
        for (File f : files) {
            uncheckout(f);
        }        
    }

    private void add(File... files) {
        Clearcase.getInstance().getClient().exec(new MkElemCommand(files, null, MkElemCommand.Checkout.Checkin, false), true);
    }

    private static void ensureMutable(File file) {
        if (file.isDirectory()) {
            FileEntry entry = ClearcaseUtils.readEntry(Clearcase.getInstance().getClient(), file);                
            if (entry == null || entry.isCheckedout() || entry.isViewPrivate()) {
                return;
            }
        } else {
            if (file.canWrite()) return;
        }
        CheckoutCommand command = new CheckoutCommand(new File[]{ file }, null, CheckoutCommand.Reserved.Reserved, true);
        Clearcase.getInstance().getClient().exec(command, true);                
    }
    
    private void waitALittleBit(long l) {
        try {
            Thread.sleep(l);    // this is so slow ...
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static class RmElemCommand extends FilesCommand {        
        public RmElemCommand(File... files) {
            super(files);        
}
        public void prepareCommand(Arguments arguments) throws ClearcaseException {
            arguments.add("rmelem");                
            arguments.add("-force");
            arguments.add("-nco");
            addFileArguments(arguments);
        }
    }    
}
