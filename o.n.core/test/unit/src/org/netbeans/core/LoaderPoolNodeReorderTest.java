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

package org.netbeans.core;

import java.io.*;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jaroslav Tulach
 */
public class LoaderPoolNodeReorderTest extends NbTestCase {
    private OldStyleLoader oldL;
    private NewStyleLoader newL;

    public LoaderPoolNodeReorderTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
        LoaderPoolNode.installationFinished();

        oldL = DataLoader.getLoader(OldStyleLoader.class);
        newL = DataLoader.getLoader(NewStyleLoader.class);
        LoaderPoolNode.doAdd (oldL, null);
        LoaderPoolNode.doAdd (newL, null);
        LoaderPoolNode.waitFinished();
    }

    protected @Override void tearDown() throws Exception {
        for (Enumeration en = LoaderPoolNode.getNbLoaderPool().loaders(); en.hasMoreElements(); ) {
            DataLoader l = (DataLoader)en.nextElement();

            LoaderPoolNode.remove(l);
        }
        LoaderPoolNode.waitFinished();
    }

    public void testReorderABit () throws Exception {
        {
            Node[] arr = extractFixed(LoaderPoolNode.getLoaderPoolNode().getChildren().getNodes());
            assertEquals(2, arr.length);
            assertEquals(arr[0].getDisplayName(), oldL.getDisplayName());
            assertEquals(arr[1].getDisplayName(), newL.getDisplayName());
        }

        Index idx = LoaderPoolNode.getLoaderPoolNode().getLookup().lookup(Index.class);
        assertNotNull("There is index in the node", idx);

        idx.reorder(new int[] { 1, 0 });
        LoaderPoolNode.waitFinished();


        {
            Node[] arr = extractFixed(LoaderPoolNode.getLoaderPoolNode().getChildren().getNodes());
            assertEquals(2, arr.length);
            assertEquals("reord1", arr[1].getDisplayName(), oldL.getDisplayName());
            assertEquals("reord0", arr[0].getDisplayName(), newL.getDisplayName());
        }

        idx.reorder(new int[] { 1, 0 });
        LoaderPoolNode.waitFinished();

        {
            Node[] arr = extractFixed(LoaderPoolNode.getLoaderPoolNode().getChildren().getNodes());
            assertEquals(2, arr.length);
            assertEquals("noreord0", arr[0].getDisplayName(), oldL.getDisplayName());
            assertEquals("noreord1", arr[1].getDisplayName(), newL.getDisplayName());
        }
    }


    private static Node[] extractFixed(Node[] arr) {
        List<Node> all = new ArrayList<Node>();
        for (Node n : arr) {
            if (!n.getDisplayName().contains("fixed")) {
                all.add(n);
            }
        }
        return all.toArray(new Node[0]);
    }
    
    public static class OldStyleLoader extends UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super(MultiDataObject.class.getName());

            setDisplayName(getClass().getName());
        }

        protected MultiDataObject createMultiObject(FileObject fo) throws IOException {
            throw new IOException ("Not implemented");
        }

        protected SystemAction[] defaultActions () {
            defaultActionsCalled = true;
            SystemAction[] retValue;
            
            retValue = super.defaultActions();
            return retValue;
        }
        
        
    }
    
    public static final class NewStyleLoader extends OldStyleLoader {
        protected String actionsContext () {
            return "Loaders/IamTheNewBeggining";
        }
    }
    public static final class L1 extends OldStyleLoader {
    }
    public static final class L2 extends OldStyleLoader {
    }
    public static final class L3 extends OldStyleLoader {
    }
    public static final class L4 extends OldStyleLoader {
    }
    public static final class L5 extends OldStyleLoader {
    }
}
