/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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


package org.openide.loaders;

import java.util.logging.Level;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Tests for internals of FolderList as there seems to be some
 * inherent problems.
 *
 * @author Jaroslav Tulach
 */
public class FolderListTest extends NbTestCase {
    private FileObject folder;
    private FolderList list;
    
    
    public FolderListTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        
        folder = FileUtil.createFolder(lfs.getRoot(), "folder");

        FileUtil.createData(folder, "A.txt");
        FileUtil.createData(folder, "B.txt");
        FileUtil.createData(folder, "C.txt");
        
        list = FolderList.find(folder, true);
    }

    protected void tearDown() throws Exception {
    }

    public void testComputeChildrenList() throws Exception {
        class L implements FolderListListener {
            private int cnt;
            private boolean finished;
            
            public void process(DataObject obj, List arr) {
                cnt++;
            }

            public void finished(List arr) {
                assertTrue(arr.isEmpty());
                finished = true;
            }
        }
        
        L listener = new L();       
        RequestProcessor.Task t = list.computeChildrenList(listener);
        t.waitFinished();
        
        assertEquals("Three files", 3, listener.cnt);
        assertTrue("finished", listener.finished);
    }
    
}
