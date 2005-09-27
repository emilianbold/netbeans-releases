/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide;


import org.netbeans.junit.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Testing functional implementation calling the methods to interface <code>WizardDescriptor.InstantiatingIterator</code>
 * from WizardDescriptor.
 */
public class InstantiatingIteratorTest extends NbTestCase {

    
    public InstantiatingIteratorTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (InstantiatingIteratorTest.class));
        System.exit (0);
    }
    
    private WizardDescriptor wd;
    private String exceptedValue;
    private Iterator iterator;
    private int attachedInIterator = 0;
    private int attachedInPanel = 0;
    private boolean checkOrder = false;
    private boolean shouldThrowException = false;
    private boolean checkAWTQueue = false;
    private Set/*<ChangeListener>*/ changeListenersInIterator = new HashSet ();
    private Set/*<ChangeListener>*/ changeListenersInPanel = new HashSet ();

    protected final void setUp () {
        iterator = new Iterator ();
        wd = new WizardDescriptor (iterator);
        wd.addPropertyChangeListener(new Listener ());
        java.awt.Dialog d = DialogDisplayer.getDefault ().createDialog (wd);
        checkOrder = false;
        shouldThrowException = false;
        checkAWTQueue = false;
        //d.show();
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ() {
        return true;
    }
    
    public void testCleanChangeListenerAfterFinish () {
        assertEquals ("One listener is attached.", 1, changeListenersInIterator.size ());
        wd.doNextClick ();
        assertEquals ("Still only one listener is attached after Next.", 1, changeListenersInIterator.size ());
        wd.doPreviousClick ();
        assertEquals ("Still only one listener is attached after Previous.", 1, changeListenersInIterator.size ());
        wd.doFinishClick ();
        assertEquals ("No one listener is attached after Finish.", 0, changeListenersInIterator.size ());
        assertEquals ("No one listener is attached in WD.Panel after Finish.", 0, changeListenersInPanel.size ());
    }
    
    public void testCleanChangeListenerAfterCancel () {
        assertEquals ("One listener is attached.", 1, changeListenersInIterator.size ());
        wd.doCancelClick ();
        assertEquals ("No one listener is attached after Cancel.", 0, changeListenersInIterator.size ());
        assertEquals ("No one listener is attached in WD.Panel after Finish.", 0, changeListenersInPanel.size ());
    }
    
    public void testInitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized.", iterator.initialized.booleanValue ());
        assertNull ("InstantiatingIterator wasn't instantiated.", iterator.result);
    }

    public void testUninitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", iterator.initialized.booleanValue ());
        wd.doCancelClick ();
        assertFalse ("InstantiatingIterator was uninitialized after cancel.", iterator.initialized.booleanValue ());
        assertNull ("InstantiatingIterator wasn't instantiated.", iterator.result);
    }

    public void testFinishAndUninitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", iterator.initialized.booleanValue ());
        wd.doNextClick ();
        assertTrue ("InstantiatingIterator wasn't uninitialized after next.", iterator.initialized.booleanValue ());
        wd.doFinishClick ();
        assertFalse ("InstantiatingIterator wasn uninitialized after finish.", iterator.initialized.booleanValue ());
        assertNotNull ("InstantiatingIterator was instantiated.", iterator.result);
    }

    public void testUninitializeIteratorAndCalledCurrent () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", iterator.initialized.booleanValue ());
        wd.doNextClick ();
        assertTrue ("InstantiatingIterator wasn't uninitialized after next.", iterator.initialized.booleanValue ());
        wd.doFinishClick ();
        assertFalse ("InstantiatingIterator wasn uninitialized after finish.", iterator.initialized.booleanValue ());
        assertNotNull ("InstantiatingIterator was instantiated.", iterator.result);
    }

    public void testOrderStoreSettingAndInstantiate () throws Exception {
        checkOrder = true;
        wd.doNextClick ();
        wd.doFinishClick ();
        assertNotNull ("InstantiatingIterator was instantiated.", iterator.result);
    }

    public void testGetInstantiatedObjects () throws Exception {
        wd.doNextClick ();
        wd.doFinishClick ();
        assertNotNull ("InstantiatingIterator was instantiated.", iterator.result);
        Set newObjects = wd.getInstantiatedObjects ();
        assertEquals ("WD returns same objects as InstantiatingIterator instantiated.", iterator.result, newObjects);
        
    }
    
    public void testInstantiateOutsideAWTQueue () {
        checkAWTQueue = true;

        wd.doNextClick ();
        wd.doFinishClick ();
    }
    
    public void testFinishOptionWhenInstantiateFails () throws Exception {
        shouldThrowException = true;

        wd.doNextClick ();
        Object state = wd.getValue();
        wd.doFinishClick ();
        
        assertNull ("InstantiatingIterator was not correctly instantiated.", iterator.result);
//        Set newObjects = wd.getInstantiatedObjects ();
//        assertEquals ("WD returns no object.", Collections.EMPTY_SET, newObjects);
        assertSame ("The state is same as before instantiate()", state, wd.getValue ());
    }
    
    public class Panel implements WizardDescriptor.FinishablePanel {
        private JLabel component;
        private String text;
        public Panel(String text) {
            this.text = text;
        }

        public Component getComponent() {
            if (component == null) {
                component = new JLabel (text);
            }
            return component;
        }
        
        public void addChangeListener(ChangeListener l) {
            changeListenersInPanel.add (l);
        }
        
        public HelpCtx getHelp() {
            return null;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(Object settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeListenersInPanel.remove (l);
        }
        
        public void storeSettings(Object settings) {
            if (checkOrder) {
                assertNull ("WD.P.storeSettings() called before WD.I.instantiate()", iterator.result);
                // bugfix #45093, remember storeSettings could be called multiple times
                // do check order only when the first time
                checkOrder = false;
            }
            log ("storeSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
            if (exceptedValue != null) {
                assertEquals ("WD.getValue() returns excepted value.", exceptedValue, handleValue (wd.getValue ()));
            }
        }
        
        public boolean isFinishPanel () {
            return true;
        }
        
    }
    
    public class Iterator implements WizardDescriptor.InstantiatingIterator {
        int index = 0;
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
        java.util.Set helpSet;
        
        public Boolean initialized = null;
        Set result = null;
        
        public WizardDescriptor.Panel current () {
            assertTrue ("WD.current() called on initialized iterator.", initialized != null && initialized.booleanValue ());
            return panels[index];
        }
        public String name () {
            return "Test iterator";
        }
        public boolean hasNext () {
            return index < 1;
        }
        public boolean hasPrevious () {
            return index > 0;
        }
        public void nextPanel () {
            if (!hasNext ()) throw new NoSuchElementException ();
            index ++;
        }
        public void previousPanel () {
            if (!hasPrevious ()) throw new NoSuchElementException ();
            index --;
        }
        public void addChangeListener (ChangeListener l) {
            changeListenersInIterator.add (l);
        }
        public void removeChangeListener (ChangeListener l) {
            changeListenersInIterator.remove (l);
        }
        public java.util.Set instantiate () throws IOException {
            if (checkAWTQueue && SwingUtilities.isEventDispatchThread ()) {
                throw new IOException ("Don't call from AWT queue.");
            }
            if (shouldThrowException) {
                throw new IOException ("Test throw IOException during instantiate().");
            }
            if (initialized.booleanValue ()) {
                helpSet.add ("member");
                result = helpSet;
            } else {
                result = null;
            }
            return result;
        }
        public void initialize (WizardDescriptor wizard) {
            helpSet = new HashSet ();
            panels[0] = new Panel("first panel");
            panels[1] = new Panel("second panel");
            initialized = Boolean.TRUE;
        }
        public void uninitialize (WizardDescriptor wizard) {
            helpSet.clear ();
            initialized = Boolean.FALSE;
            panels = null;
        }
    }
    
    public class Listener implements PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            if (WizardDescriptor.PROP_VALUE.equals(propertyChangeEvent.getPropertyName ())) {
                log("propertyChange [time: " + System.currentTimeMillis () +
                                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));

            }
        }
        
    }
    
    public String handleValue (Object val) {
        if (val == null) return "NULL";
        if (val instanceof String) return (String) val;
        if (WizardDescriptor.FINISH_OPTION.equals (val)) return "FINISH_OPTION";
        if (WizardDescriptor.CANCEL_OPTION.equals (val)) return "CANCEL_OPTION";
        if (WizardDescriptor.CLOSED_OPTION.equals (val)) return "CLOSED_OPTION";
        if (val instanceof JButton) {
            JButton butt = (JButton) val;
            ResourceBundle b = NbBundle.getBundle ("org.openide.Bundle"); // NOI18N
            if (b.getString ("CTL_NEXT").equals (butt.getText ())) return "NEXT_OPTION";
            if (b.getString ("CTL_PREVIOUS").equals (butt.getText ())) return "NEXT_PREVIOUS";
            if (b.getString ("CTL_FINISH").equals (butt.getText ())) return "FINISH_OPTION";
            if (b.getString ("CTL_CANCEL").equals (butt.getText ())) return "CANCEL_OPTION";
        }
        return "UNKNOWN OPTION: " + val;
    }
}