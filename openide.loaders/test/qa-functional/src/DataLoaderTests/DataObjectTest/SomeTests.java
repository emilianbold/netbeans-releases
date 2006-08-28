/*
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
