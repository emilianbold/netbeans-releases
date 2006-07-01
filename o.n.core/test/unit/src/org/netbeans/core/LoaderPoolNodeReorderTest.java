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

package org.netbeans.core;

import java.io.*;
import java.util.*;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.loaders.DataLoader;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jaroslav Tulach
 */
public class LoaderPoolNodeReorderTest extends org.netbeans.junit.NbTestCase {
    private OldStyleLoader oldL;
    private NewStyleLoader newL;

    public LoaderPoolNodeReorderTest (String testName) {
        super (testName);
    }

    protected void setUp () throws java.lang.Exception {
        LoaderPoolNode.installationFinished();

        oldL = (OldStyleLoader)OldStyleLoader.getLoader (OldStyleLoader.class);
        newL = (NewStyleLoader)NewStyleLoader.getLoader (NewStyleLoader.class);
        LoaderPoolNode.doAdd (oldL, null);
        LoaderPoolNode.doAdd (newL, null);
        LoaderPoolNode.waitFinished();
    }

    protected void tearDown () throws java.lang.Exception {
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
        ArrayList all = new ArrayList();
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].getDisplayName().contains("fixed")) {
                all.add(arr[i]);
            }
        }
        return (Node[])all.toArray(new Node[0]);
    }
    
    public static class OldStyleLoader extends org.openide.loaders.UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super (org.openide.loaders.MultiDataObject.class);

            setDisplayName(getClass().getName());
        }

        protected org.openide.loaders.MultiDataObject createMultiObject (FileObject fo) throws IOException {
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
