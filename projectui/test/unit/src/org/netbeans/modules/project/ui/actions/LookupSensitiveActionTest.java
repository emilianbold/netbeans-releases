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

package org.netbeans.modules.project.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public class LookupSensitiveActionTest extends NbTestCase {

    public LookupSensitiveActionTest(String name) {
        super( name );
    }
            
    private FileObject dir, f1, f2, f3, f4;
    private DataObject d1, d2, d3, d4;
        
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        dir = FileUtil.toFileObject(getWorkDir());
        f1 = dir.createData("f1.java");
        f2 = dir.createData("f2.java");
        f3 = dir.createData("f3.properties");
        f4 = dir.createData("f4.xml");          
        d1 = DataObject.find(f1);
        d2 = DataObject.find(f2);
        d3 = DataObject.find(f3);
        d4 = DataObject.find(f4);
    }
    
    protected void tearDown() throws Exception {
        clearWorkDir();
        super.tearDown();
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testListening() throws Exception {
    
        // Lookup sensitive action has to refresh if and only if
        // it has at least one property change listener
        
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup( new Object[] { } );
        TestLSA tlsa = new TestLSA( lookup );
	assertTrue ("TestLSA action is enabled.", tlsa.isEnabled ());
	tlsa.refreshCounter = 0;
	
        lookup.change( new Object[] { d1 } );       
        assertEquals( "No refresh should be called ", 0, tlsa.refreshCounter );
        lookup.change( new Object[] { d2 } );       
        lookup.change( new Object[] { d1 } );       
        assertEquals( "No refresh should be called ", 0, tlsa.refreshCounter );
        
                
        TestPropertyChangeListener tpcl = new TestPropertyChangeListener();
        tlsa.addPropertyChangeListener( tpcl );
        lookup.change( new Object[] { d2 } ); 
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );
        
        
        tlsa.clear();
        tpcl.clear();
        lookup.change( new Object[] { d3 } );         
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );        
        
    }
    
    public void testCorrectValuesWithoutListener() throws Exception {
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup( new Object[] { } );
        TestLSA tlsa = new TestLSA( lookup );
        
        lookup.change( new Object[] { d1 } );       
        assertEquals( "Action should return correct name ", d1.getName(), tlsa.getValue( Action.NAME ) );
        
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        
        assertEquals( "Action should return correct name ", d1.getName(), tlsa.getValue( Action.NAME ) );        
        assertEquals( "Refresh should still be called only once", 1, tlsa.refreshCounter );
        
    }
    
    public void testActionGC() throws Exception {
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup( new Object[] { } );
        TestLSA tlsa = new TestLSA( lookup );
        
        WeakReference reference = new WeakReference( tlsa );
        tlsa = null;
        
        assertGC( "Action should be GCed", reference );
        
    }
    
    
    private static class TestLSA extends LookupSensitiveAction {
        
        private int performCounter;
        private int refreshCounter;
               
        public TestLSA( Lookup lookup ) {
            super( null, lookup, new Class[] { DataObject.class } );
        }
        
        protected void actionPerformed( Lookup context ) {
            performCounter++;
        }
           
        protected void refresh( Lookup context ) {
            refreshCounter++;
            
            DataObject dobj = (DataObject)context.lookup( DataObject.class );
            
            if (dobj != null) {
		putValue( Action.NAME, dobj.getName() );
	    }
            
        }
        
        public void clear() {
            performCounter = refreshCounter = 0;
        }
        
        
    }
    
    
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        
        List events = new ArrayList();
        
        public void propertyChange( PropertyChangeEvent e ) {
            events.add( e );
        }
        
        void clear() {
            events.clear();
        }
        
        List getEvents() {
            return events;
        }
                
    }
        
    
}
