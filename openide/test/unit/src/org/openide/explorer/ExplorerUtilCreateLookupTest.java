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

package org.openide.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JTextField;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Check the behaviour of ExplorerManager's lookup by doing the same 
 * operations as in case of TopComponent's lookup. Done by providing a fake 
 * component that converts setActivatedNodes to ExplorerManager calls.
 *
 * @author Jaroslav Tulach
 */
public class ExplorerUtilCreateLookupTest extends org.openide.windows.TopComponentGetLookupTest {
    public ExplorerUtilCreateLookupTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new NbTestSuite(ExplorerUtilCreateLookupTest.class);
    }
    
    /** Run all tests in AWT thread */
    public final void run(final junit.framework.TestResult result) {
        try {
            // XXX ExplorerManager when updating selected nodes
            // replanes all firing into AWT thread, therefore the test
            // has to run in AWT.
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ExplorerUtilCreateLookupTest.super.run (result);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException ();
        }
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        class ExTC extends org.openide.windows.TopComponent 
        implements java.beans.PropertyChangeListener {
            ExplorerManager em = new ExplorerManager ();
            {
                addPropertyChangeListener (this);
                em.setRootContext (new AbstractNode (new Children.Array ()));
            }
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                if ("activatedNodes".equals (ev.getPropertyName())) {
                    try {
                        Node[] arr = getActivatedNodes ();
                        Children.Array ch = (Children.Array)em.getRootContext ().getChildren ();
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].getParentNode() != em.getRootContext()) {
                                assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                            }
                        }
                        em.setSelectedNodes (getActivatedNodes ());
                    } catch (java.beans.PropertyVetoException ex) {
                        ex.printStackTrace();
                        fail (ex.getMessage());
                    }
                }
            }
        }
        ExTC e = new ExTC ();
        
        top = e;
        lookup = ExplorerUtils.createLookup (e.em, e.getActionMap ());
    }
    
}
