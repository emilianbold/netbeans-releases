/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.favorites;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import javax.swing.event.ChangeListener;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class VisibilityQueryWorksTest extends NbTestCase {
    private FileObject hiddenFO;
    private FileObject folderFO;
    private FileObject targetFO;
    private FileObject favoritesFO;
    
    private DataObject hiddenDO;
    private DataFolder folderDO;
    private DataFolder targetDO;
    private DataFolder favoritesDO;
    
    private ErrorManager err;

    private DataFolder rootDO;
    
    
    public VisibilityQueryWorksTest(String name) {
        super (name);
    }
    
    
    /** If execution fails we wrap the exception with 
     * new log message.
     */
    @Override
    protected void runTest () throws Throwable {
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        }
    }

    
    @Override
    protected void setUp () throws Exception {
        clearWorkDir();

        VQI vqi = new VQI();
        vqi.init();
        MockLookup.setInstances(vqi, new ErrManager(), new Repository(FileUtil.createMemoryFileSystem()));

        ErrManager.log = getLog();
        err = ErrorManager.getDefault().getInstance("TEST-" + getName() + "");
        
        err.log("Starting test");
        
        super.setUp ();

        try {
            File folder = new File(getWorkDir(), "folder");
            folder.mkdirs();
            this.folderFO = FileUtil.toFileObject(folder);
            assertNotNull("Directory object found", folderFO);
            
            err.log("folder create");

            File hidden = new File(folder, "a-hidden.txt");
            hidden.createNewFile();
            this.hiddenFO = FileUtil.toFileObject(hidden);
            assertNotNull("File object found", hiddenFO);
            
            err.log("a-hidden.txt created");

            File target = new File(getWorkDir(), "target");
            target.mkdirs();
            this.targetFO = FileUtil.toFileObject(target);
            assertNotNull("Directory object found", targetFO);
            
            err.log("target created");

            this.favoritesFO = FileUtil.createFolder (Repository.getDefault().getDefaultFileSystem().getRoot(), "Favorites");
            assertNotNull("Created favorites folder", this.favoritesFO);
            assertEquals("One child", 1, Repository.getDefault().getDefaultFileSystem().getRoot().getChildren().length);
            
            err.log("Favorites created");

            FileObject[] arr = this.favoritesFO.getChildren();
            for (int i = 0; i < arr.length; i++) {
                err.log("Delete: " + arr[i]);
                arr[i].delete();
                err.log("Done");
            }

            this.hiddenDO = DataObject.find(hiddenFO);
            this.folderDO = DataFolder.findFolder(folderFO);
            this.favoritesDO = DataFolder.findFolder(favoritesFO);
            this.targetDO = DataFolder.findFolder(targetFO);
            this.rootDO = DataFolder.findFolder(FileUtil.toFileObject(getWorkDir()));
            
            err.log("DataObjects created");

            DataObject res;
            res = hiddenDO.createShadow(favoritesDO);
            err.log("shadow created: " + res);
            res = folderDO.createShadow(favoritesDO);
            err.log("shadow created: " + res);
            res = targetDO.createShadow(favoritesDO);
            err.log("shadow created: " + res);
            res = rootDO.createShadow(favoritesDO);
            err.log("shadow created: " + res);

            assertEquals("Four items in favorites", 4, favoritesDO.getChildren().length);
            err.log("Children are ok");
            assertEquals("Four items in node favorites", 4, favoritesDO.getNodeDelegate().getChildren().getNodes(true).length);
            err.log("Nodes are ok");
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        }
            
    }
    
    public void testLinksAreVisibleAllTheTime() throws Exception {
        Node[] arr = Favorites.getNode().getChildren().getNodes(true);
        assertNodeForDataObject("hidden object is there", hiddenDO, true, arr);
        assertNodeForDataObject("folder as well", folderDO, true, arr);
    }

    public void testHiddenFilesInFoldersAreHidden() throws Exception {
        Node[] arr = Favorites.getNode().getChildren().getNodes(true);
        Node f = assertNodeForDataObject("folder as well", folderDO, true, arr);
        
        arr = f.getChildren().getNodes(true);
        
        assertNodeForDataObject("hidden object is not there", hiddenDO, false, arr);
        assertEquals("No children at all", 0, arr.length);

        VQI vqi = (VQI) Lookup.getDefault().lookup(VQI.class);
        vqi.showAll = true;
        vqi.fire();

        // initialize the children
        Node some = f.getChildren().findChild(null);
        assertNotNull("Some node needs to be found", some);
        arr = f.getChildren().getNodes(true);
        assertNodeForDataObject("hidden object is now there", hiddenDO, true, arr);
        assertEquals("One child at all", 1, arr.length);
    }

    /* these tests were created to fix issue 62863, but it is not going 
      to be fixed this way, so leaving commented out...
     
    public void testCopyOfFolderIgnoresHiddenFile() throws Exception {
        doCopyOrCut(true);
    }
    public void testCutOfFolderIgnoresHiddenFile() throws Exception {
        doCopyOrCut(false);
    }
    
    private void doCopyOrCut(boolean copy) throws Exception {
        Node[] arr = Favorites.getNode().getChildren().getNodes(true);
        Node f = assertNodeForDataObject("folder is there ", rootDO, true, arr);
        arr = f.getChildren().getNodes(true);
        f = assertNodeForDataObject("folder is there ", folderDO, true, arr);
        Node t = assertNodeForDataObject("target as well", targetDO, true, arr);
        
        Transferable trans = copy ? f.clipboardCopy() : f.clipboardCut();
        PasteType[] pastes = t.getPasteTypes(trans);
        assertEquals ("One paste", 1, pastes.length);
        
        pastes[0].paste();
        
        arr = t.getChildren().getNodes(true);
        assertEquals("No children at all", 0, arr.length);
        
        Thread.sleep(1000);
        
        assertEquals("No children on loader level", 0, targetDO.getChildren().length);
        assertEquals("No children on fs level", 0, targetDO.getPrimaryFile().getChildren().length);
        assertEquals("No children on disk", 0, FileUtil.toFile(targetDO.getPrimaryFile()).list().length);
    }
     */
    
    /** @return node that contains the data object or null */
    private Node assertNodeForDataObject(String msg, DataObject obj, boolean shouldBeThere, Node[] arr) {
        for (int i = 0; i < arr.length; i++) {
            boolean ok;
            DataObject in = (DataObject)arr[i].getCookie(DataObject.class);
            
            if (obj == in || ((in instanceof DataShadow) && ((DataShadow)in).getOriginal() == obj)) {
                if (shouldBeThere) {
                    return arr[i];
                } else {
                    fail(msg + " at " + i + " as " + arr[i]);
                }
            }
        }
        
        if (shouldBeThere) {
            fail(msg + " in " + Arrays.asList(arr));
        }
        return null;
    }
    
    private static final class VQI implements VisibilityQueryImplementation {
        
        public void init() {
            showAll = false;
//            listener = null;
        }

        boolean showAll;
        
        public boolean isVisible(FileObject file) {
            if (showAll) {
                return true;
            }
            return file.getPath().indexOf("hidden") == -1;
        }

        
        private final ChangeSupport cs = new ChangeSupport(this);
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        public void fire() {
            cs.fireChange();
        }
    }
    //
    // Logging support
    //
    private static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;

        private static PrintStream log;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                true
//                name.startsWith ("org.openide.loaders.FolderList")
//              || name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                messages.append (prefix);
                messages.append (s);
                messages.append ('\n');
                
                if (messages.length() > 30000) {
                    messages.delete(0, 15000);
                }
                
                log.print(prefix);
                log.println(s);
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }
        
    } // end of ErrManager
    
}
