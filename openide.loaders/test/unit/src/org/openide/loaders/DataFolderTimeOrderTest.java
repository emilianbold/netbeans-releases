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

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;

/** Does a change in order on folder fire the right properties?
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderTimeOrderTest extends NbTestCase 
implements PropertyChangeListener {
    private DataFolder aa;
    private ArrayList<String> events = new ArrayList<String>();
    
    public DataFolderTimeOrderTest (String name) {
        super (name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();

        MockServices.setServices(Pool.class);
        
        String fsstruct [] = new String [] {
            "AA/X.txt",
            "AA/Y.txt",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);

        aa = DataFolder.findFolder (lfs.findResource ("AA"));
        aa.addPropertyChangeListener (this);
    }
    
    @Override
    protected void tearDown () throws Exception {
        final DataLoader l = DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class);
        
        aa.removePropertyChangeListener (this);
    }
    
    public void testLastModifiedOrderUpdatedAfterFileIsTouched() throws Exception {
        aa.setSortMode(DataFolder.SortMode.LAST_MODIFIED);

        DataObject[] arr = aa.getChildren ();
        Node n = aa.getNodeDelegate().cloneNode();
        Node[] nodes = n.getChildren().getNodes(true);
        assertEquals ("Two objects", 2, arr.length);
        assertEquals ("Two nodes", 2, nodes.length);

        Thread.sleep(100);
        
        assertEquals("Sort mode changed: " + events, 1, events.size());
        assertEquals("sortMode", events.get(0));
        events.clear();
        
        OutputStream os = arr[1].getPrimaryFile().getOutputStream();
        os.write("Ahoj".getBytes());
        os.close();
        
        synchronized (this) {
            if (events.isEmpty()) {
                wait(5000);
            }
        }
        assertFalse("At least one event delivered: " + events, events.isEmpty());

        DataObject[] newArr = aa.getChildren();
        Node[] newNodes = n.getChildren().getNodes(true);

        assertEquals("Reverted 1", arr[0], newArr[1]);
        assertEquals("Reverted 2", arr[1], newArr[0]);

        assertEquals("Reverted node 1", nodes[0], newNodes[1]);
        assertEquals("Reverted node 2", nodes[1], newNodes[0]);
    }
    
    public synchronized void propertyChange (PropertyChangeEvent evt) {
        events.add (evt.getPropertyName ());
    }

    
    public static final class Pool extends DataLoaderPool {
        
        protected Enumeration<? extends org.openide.loaders.DataLoader> loaders() {
            return Enumerations.singleton(DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class));
        }
        
    } // end of Pool
}
