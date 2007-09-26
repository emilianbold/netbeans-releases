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

package org.netbeans.spi.project.ui.support;

import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
public class NodeFactorySupportTest extends TestCase {
    
    public NodeFactorySupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of createCompositeChildren method, of class org.netbeans.spi.project.ui.support.NodeFactorySupport.
     */
    public void testCreateCompositeChildren() throws InterruptedException, InvocationTargetException {
        System.out.println("createCompositeChildren");
        InstanceContent ic = new InstanceContent();
        final TestDelegates dels = new TestDelegates(ic);
        final Node node1 = new AbstractNode(Children.LEAF);
        final Node node2 = new AbstractNode(Children.LEAF);
        final Node node3 = new AbstractNode(Children.LEAF);
        final Node node4 = new AbstractNode(Children.LEAF);
        node1.setName("node1");
        node2.setName("node2");
        node3.setName("node3");
        node4.setName("node4");
        NodeFactory fact1 = new TestNodeFactory(node1);
        NodeFactory fact2 = new TestNodeFactory(node2);
        NodeFactory fact3 = new TestNodeFactory(node3);
        NodeFactory fact4 = new TestNodeFactory(node4);
        List col = new ArrayList();
        col.add(fact1);
        col.add(fact2);
        ic.set(col, null);
        
        Node[] nds = dels.getNodes();
        assertEquals(nds[0], node1);
        assertEquals(nds[1], node2);
        
        col.add(0, fact4);
        col.add(fact3);
        col.remove(fact2);
        ic.set(col, null);
        //#115995, caused by fix for #115128
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Node[] nds = dels.getNodes();
                assertEquals(nds[0], node4);
                assertEquals(nds[1], node1);
                assertEquals(nds[2], node3);
            }
        });
        
    }

   private class TestNodeFactory implements NodeFactory {
       
       Node node;
       public TestNodeFactory(Node node) {
           this.node = node;
       }
        public NodeList createNodes(Project p) {
            return NodeFactorySupport.fixedNodeList(new Node[] {node});
        }
   }
   
   private class TestDelegates extends NodeFactorySupport.DelegateChildren  {
       public AbstractLookup.Content content;
       TestDelegates(AbstractLookup.Content cont) {
           super(null, null);
           content = cont;
       }
       
       protected Lookup createLookup() {
           return new AbstractLookup(content);
       }
       
       public void addNotify() {
           super.addNotify();
       }
       
       public void removeNotify() {
           super.removeNotify();
       }
   }
    
}
