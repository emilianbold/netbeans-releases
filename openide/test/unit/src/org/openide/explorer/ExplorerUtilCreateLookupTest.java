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
    
    
    protected boolean runInEQ () {
        return true;
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
