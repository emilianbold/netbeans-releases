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

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncDeadlockTest extends AbstractSyncTestCase {
    
    public static final String TEST_XSD         = "resources/po.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    private boolean done = false;
    
    /**
     * SyncElementTest
     */
    public SyncDeadlockTest(String testName) {
	super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite(SyncDeadlockTest.class);
	return suite;
    }
    
    public void testDeadlock() throws Exception {
        final AXIDocument doc = getAXIModel().getRoot();
        doc.addPropertyChangeListener(new AXIDocumentListener(doc));
	SchemaModel sm = getAXIModel().getSchemaModel();
	// This thread will run a sync on axiom
	// this will be run when the schema model is known to be locked
	// from the listener. 
	// The listener will wait until this thread is blocked
	// then continue. will force the locking
	Thread t = new Thread(new Runnable() {
	   public void run() {
	       try {
		    doc.getModel().sync();
	       } catch (IOException ioe) {
		   fail();
	       }
	   } 
	});
        sm.addPropertyChangeListener(new SchemaModelListener(t));
        sm.startTransaction();
        Schema schema = sm.getSchema();
        schema.setTargetNamespace("http://xml.netbeans.org/schema/po");
        sm.endTransaction();
	t.join();
        assertTrue("axi model event not fired", done);
    }
        
    private class SchemaModelListener implements PropertyChangeListener {
	private Thread t;
	
	public SchemaModelListener(Thread t) {
	    this.t = t;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() instanceof Schema  &&
	       evt.getPropertyName().equals(Schema.TARGET_NAMESPACE_PROPERTY)) {
                // we are now holding a lock on the schema model
		t.start();
		while (!t.getState().equals(Thread.State.BLOCKED)) {
		    try {
			Thread.currentThread().sleep(50);
		    } catch (InterruptedException ex) {
			
		    }
		}
		getAXIModel().isIntransaction();
            }
	}        
    }
    
    private class AXIDocumentListener implements PropertyChangeListener {
        private AXIComponent source;
        
        public AXIDocumentListener(AXIComponent source) {
            this.source = source;
        }
        
	public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == source && evt.getPropertyName().equals(AXIDocument.PROP_TARGET_NAMESPACE)) {
                String name = (String)evt.getNewValue();
                assertEquals("http://xml.netbeans.org/schema/po",name);
                done = true;
            }
	}        
    }
}
