/*
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

package DataLoaderTests.DataObjectTest;

import java.awt.datatransfer.Transferable;

import junit.framework.*;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.*;
import org.openide.cookies.ConnectionCookie;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author  pzajac
 */
public class SomeTests extends NbTestCase {
    // src/DataLoader/DataObjectTest/data dataFolder
    private DataObject resources;    
    private FileObject resourcesFo;
    
    /** Creates a new instance of SomeTests */
    public SomeTests(String name) {
        super(name);
    }
    
        
    /** Testing connection Listener 
     */
    private static class ConListener implements  ConnectionCookie.Listener {
        private  ConnectionCookie.Event lastEvent;
        
        public void notify(org.openide.cookies.ConnectionCookie.Event ev) throws IllegalArgumentException, ClassCastException {
            System.out.println("event fired");
            lastEvent = ev;
        }
        
        public ConnectionCookie.Event getLastEvent () {
            return lastEvent;
        }
        /** erase last event */
        public void clear() {
            lastEvent = null;
        }
        
    }
    private  static class TestNode extends AbstractNode { 
        TestNode (Lookup lookup) {
            super(Children.LEAF,lookup);
        }
        
        public static TestNode createInstance() {
            InstanceContent ic = new InstanceContent();
            AbstractLookup lookup = new AbstractLookup(ic);
            ic.add (new ConListener());
            return new TestNode(lookup);
        }

    }
    /** Testing ConnectionCookie.Type implementation
     */
    
    private  static class ConnectionTypeA implements ConnectionCookie.Type {
        
        public Class getEventClass() {
            return ConnectionCookie.Event.class;
        }
        
        public boolean isPersistent() {
            return false;
        }
        
        public boolean overlaps(org.openide.cookies.ConnectionCookie.Type type) {
            System.out.println("overlaps: " + type.getClass());
            boolean retType = type instanceof ConnectionTypeA;
            System.out.println("overlaps: " + retType);
            return retType;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    
    
    
    protected void setUp() {
            //initializing ide
            

            String xtestData = System.getProperty("xtest.data");
            File dataDir = new File (xtestData,"DataObjectTest");
            assertTrue(dataDir.exists());
            resourcesFo = FileUtil.toFileObject(dataDir);
            assertNotNull(resourcesFo);
            resources = DataFolder.findFolder(resourcesFo);
            
    }

    public void testDataConnection() throws Exception {
        //ConnectionCookie.Type 
        // get any MultiFileObject
        System.out.println(resourcesFo);
         log(resourcesFo.getPath());
         MultiDataObject dobj = (MultiDataObject) DataObject.find(resourcesFo.getFileObject("SwingFormObject.java" )); 
         assertNotNull(dobj); 
         MultiDataObject.Entry entry = dobj.getPrimaryEntry(); 
         
         ConnectionTypeA connectionType = new ConnectionTypeA();
         
         ConnectionSupport conSupport = new ConnectionSupport(entry, new ConnectionCookie.Type[]{connectionType});
         TestNode node = TestNode.createInstance();
         conSupport.register(connectionType, node);
         ConnectionCookie.Event event = new ConnectionCookie.Event(node, connectionType);
         
         Collection registeredTypes = conSupport.getRegisteredTypes() ;
         System.out.println("registered types:" + registeredTypes.size());
         assertTrue("number reagistered types must be 1", registeredTypes.size() == 1);
         
         conSupport.fireEvent(event);
         ConListener listener = (ConListener)node.getCookie(ConnectionCookie.Listener.class);
         assertNotNull(listener);
         assertNotNull(listener.getLastEvent());
         assertTrue ("fired and listened evend is not fired",  listener.getLastEvent() == event); 
         
         // test unregister
         conSupport.unregister(connectionType, node);
         listener.clear();
         conSupport.fireEvent(event);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(SomeTests.class);
        return suite;
    }
    
    public void setFilter(Filter filter) {
        super.setFilter(filter);
    }
}
