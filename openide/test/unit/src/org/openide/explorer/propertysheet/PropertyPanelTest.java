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

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;

import org.openide.*;
import org.openide.explorer.propertysheet.*;

import junit.framework.*;
import junit.textui.TestRunner;

import org.netbeans.junit.*;
import java.beans.PropertyDescriptor;
import java.awt.IllegalComponentStateException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;

/** A test of a property panel.
 */
public final class PropertyPanelTest extends NbTestCase {
    
    
    public PropertyPanelTest(String name) {
        super(name);
    }
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (PropertyPanelTest.class));
    }
    
    //
    // Sample property impl
    //
    
    private String prop;
    
    public void setProp (String x) {
        prop = x;
    }
    
    public String getProp () {
        return prop;
    }
   
    protected void setUp() throws Exception {
    }


    public void testStateUpdates () throws Exception {
        PropertyDescriptor feature = new PropertyDescriptor ("prop", this.getClass ());
        feature.setPropertyEditorClass (Ed.class);
        DefaultPropertyModel model = new DefaultPropertyModel (
            this, feature
        );
        
        PropertyPanel pp = new PropertyPanel (model, PropertyPanel.PREF_CUSTOM_EDITOR);
        assertTrue ("Ed editor created", pp.getPropertyEditor() instanceof Ed);
        
        Ed ed = (Ed)pp.getPropertyEditor ();
        
        assertNotNull("Environment has been attached", ed.env);
        
        Listener envListener = new Listener ();
        Listener panelListener = new Listener ();
        
        pp.addPropertyChangeListener(panelListener);
        ed.env.addPropertyChangeListener (envListener);
        ed.env.addVetoableChangeListener (envListener);
        
        ed.env.setState (PropertyEnv.STATE_INVALID);
        
        assertEquals ("State of panel is invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        ed.env.setState (PropertyEnv.STATE_INVALID);
        assertEquals ("Remains invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("No changes notified", 0, 0);
        panelListener.assertChanges ("No changes notified in panel", 0, 0);
        
        pp.updateValue();
        
        assertEquals ("Update valud does not change the state if invalid", PropertyEnv.STATE_INVALID, pp.getState ());
        envListener.assertChanges ("Changes notified in env", 0, 0);
        panelListener.assertChanges ("Notified in panel", 0, 0);
        
        ed.env.setState (PropertyEnv.STATE_NEEDS_VALIDATION);
        assertEquals ("Now we need validation", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);

        pp.updateValue ();
        assertEquals ("Update from needs validation shall switch to valid state if not vetoed", PropertyEnv.STATE_VALID, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        ed.env.setState (PropertyEnv.STATE_NEEDS_VALIDATION);
        assertEquals ("Now we need validation", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("Notified in environment", 1, 1);
        panelListener.assertChanges ("Notified in panel", 1, 0);
        
        
        envListener.shallVeto = true;
        pp.updateValue ();
        assertTrue ("Was vetoed", !envListener.shallVeto);
        
        assertEquals ("The state remains", PropertyEnv.STATE_NEEDS_VALIDATION, pp.getState ());
        envListener.assertChanges ("No approved property changes", 0, -1);
        panelListener.assertChanges ("No approved property changes", 0, -1);
        
        
        //
        // Now try to do the cleanup
        //
        
        DefaultPropertyModel replace = new DefaultPropertyModel (this, "prop");
        pp.setModel (replace);
        
        assertEquals ("Model changed", replace, pp.getModel());
        
        
        WeakReference wEd = new WeakReference (ed);
        WeakReference wEnv = new WeakReference (ed.env);
        
        ed = null;
        
        assertGC ("Property editor should disappear", wEd);
        assertGC ("Environment should disapper", wEnv);
    }

    public void testPropertyPanelShallGCEvenIfEditorExists () throws Exception {
        PropertyDescriptor feature = new PropertyDescriptor ("prop", this.getClass ());
        feature.setPropertyEditorClass (Ed.class);
        DefaultPropertyModel model = new DefaultPropertyModel (
            this, feature
        );
        
        PropertyPanel pp = new PropertyPanel (model, PropertyPanel.PREF_CUSTOM_EDITOR);
        
        assertTrue ("Ed editor created", pp.getPropertyEditor() instanceof Ed);
        
        Ed ed = (Ed)pp.getPropertyEditor ();
        assertNotNull ("Environment has been attached", ed.env);
        
        //
        // Make sure that the panel listens on changes in env
        //
        Listener panelListener = new Listener ();
        
        pp.addPropertyChangeListener (panelListener);
        ed.env.setState (PropertyEnv.STATE_INVALID);
        panelListener.assertChanges ("Change notified in panel", 1, 0);
        

        WeakReference weak = new WeakReference (pp);
        pp = null;
        model = null;
        feature = null;
        
        assertGC ("Panel should disappear even if we have reference to property editor", weak);
    }
    
    /** Listener that counts changes.
     */
    private static final class Listener 
    implements PropertyChangeListener, VetoableChangeListener {
        public boolean shallVeto;
        
        private int veto;
        private int change;
        
        public void assertChanges (String t, int c, int v) {
            if (c != -1) {
                assertEquals (t + " [propertychange]", c, change);
            }
            
            if (v != -1) {
                assertEquals (t + " [vetochange]", v, veto);
            }
            
            change = 0;
            veto = 0;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            change++;
        }
        
        public void vetoableChange(java.beans.PropertyChangeEvent propertyChangeEvent) throws java.beans.PropertyVetoException {
            if (shallVeto) {
                shallVeto = false;
                PropertyVetoException e = new PropertyVetoException ("Veto", propertyChangeEvent);
                
                // marks this exception as one that we do not want to notify
                PropertyDialogManager.doNotNotify (e);
                throw e;
            }
            
            veto++;
        }
        
    }

    /** Sample property editor.
     */
    private static final class Ed extends java.beans.PropertyEditorSupport 
    implements ExPropertyEditor {
        public PropertyEnv env;
        
        public Ed () {
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        //The two methods below are added because, in the property panel
        //rewrite, the property panel uses polling with a ReusablePropertyEnv
        //to determine valid state for editors that do not support a custom
        //editor - and the PropertyPanel cannot be initialized into custom
        //editor mode for a property editor that doesn't actually support
        //custom editors
        public boolean supportsCustomEditor() {
            return true;
        }
        
        //To avoid NPE when propertypanel tries to add the custom editor
        public Component getCustomEditor() {
            return new JPanel();
        }
    }
    
}



