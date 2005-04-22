/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;



/** 
 * @author Some Czech
 */
public class SheetTest extends NbTestCase {
    
    public SheetTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SheetTest.class));
    }

    public void testSheetEvents() {
        
        AbstractNode node = new AbstractNode( Children.LEAF );
        
        Sheet sheet = node.getSheet();
        
        SheetListener sl = new SheetListener();
        TestNodeListener tnl = new TestNodeListener();
        
        node.addNodeListener( tnl );
        node.addPropertyChangeListener( sl );
                
        Sheet.Set ss = new Sheet.Set();
        ss.setName("Karel");
        sheet.put( ss );
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        PropertySupport.Name prop = new PropertySupport.Name (node);
        ss.put (prop);
        
        tnl.assertEvents( "NodePropertySets change again", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
        
        sheet.remove(ss.getName());
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        ss.remove (prop.getName());
        
        tnl.assertEvents( "No change in Node, as the set is removed", new PropertyChangeEvent[] {} );
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
    }
    
    
    private static class SheetListener implements PropertyChangeListener {
        
        List events = new ArrayList();
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {            
            events.add( evt );            
        }
        
        public void assertEvents( String message, PropertyChangeEvent[] pevents ) {
            
            if ( events.size() != pevents.length ) {
                fail( message );
            }
            
            int i = 0;
            for( Iterator it = events.iterator(); it.hasNext(); i++ ) {
                PropertyChangeEvent pche = (PropertyChangeEvent)it.next();
                assertEquals( message + " [" + i + "] ", pevents[i].getSource(), pche.getSource ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropertyName(), pche.getPropertyName());
                assertEquals( message + " [" + i + "] ", pevents[i].getOldValue (), pche.getOldValue());
                assertEquals( message + " [" + i + "] ", pevents[i].getNewValue(), pche.getNewValue ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropagationId(), pche.getPropagationId());
            }
            
            events.clear();
        }
        
    }
    
    private static class TestNodeListener extends SheetListener implements NodeListener {
        
        public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        }
        
        public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        }
        
    }

}
