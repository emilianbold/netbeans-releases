/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JLabel;
import javax.swing.JPanel;
import junit.textui.TestRunner;
import java.net.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

import org.netbeans.junit.*;
import org.netbeans.core.*;
import org.openide.awt.StatusLineElementProvider;


/** Test what MainWindow returns the Status Line Elements and
 * a listener on lookup.result changes.
 * @author Jiri Rechtacek
 */
public class StatusLineElementProviderTest extends NbTestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", "org.netbeans.core.windows.view.ui.StatusLineElementProviderTest$Lkp");
    }
    
    static private InstanceContent ic = null;
    static private Impl impl1, impl2;
    static private JPanel statusLine;
    
    public StatusLineElementProviderTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(StatusLineElementProviderTest.class));
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    protected void setUp() {
        impl1 = new Impl ("First");
        impl2 = new Impl ("Second");
        statusLine = new JPanel ();
        Lookup.getDefault ();
    }
    
    public void testGetStatusLineElements () {
        // initialy contains comp1
        JPanel panel = MainWindow.getStatusLineElements (statusLine);
        assertNotNull ("getStatusLineElements() returns a panel.", panel);
        assertEquals ("Panel contains only one component.", 1, panel.getComponentCount ());
        assertTrue ("Panel contains the component Comp1.", Arrays.asList (panel.getComponents ()).contains (impl1.myComponent));
        
        // remove impl1 from lookup
        ic.remove (impl1);
        panel = MainWindow.getStatusLineElements (statusLine);
        assertNull ("getStatusLineElements() returns null, panel: " + panel, panel);
        
        // add impl1 back
        ic.add (impl1);
        panel = MainWindow.getStatusLineElements (statusLine);
        assertNotNull ("getStatusLineElements() returns a panel.", panel);
        assertEquals ("Panel contains only one component.", 1, panel.getComponentCount ());
        assertTrue ("Panel contains the component Comp1.", Arrays.asList (panel.getComponents ()).contains (impl1.myComponent));
        
        // add impl2
        ic.add (impl2);
        panel = MainWindow.getStatusLineElements (statusLine);
        assertNotNull ("getStatusLineElements() returns a panel.", panel);
        assertEquals ("Panel contains two components.", 2, panel.getComponentCount ());
        assertTrue ("Panel contains the component from impl1.", Arrays.asList (panel.getComponents ()).contains (impl1.myComponent));
        assertTrue ("Panel contains the component from impl2.", Arrays.asList (panel.getComponents ()).contains (impl2.myComponent));
    }
    

    public static final class Lkp extends AbstractLookup {
        public Lkp () {
            this (new InstanceContent ());
        }

        private Lkp (InstanceContent instanceContent) {
            super (instanceContent);
            ic = instanceContent;
            ic.add (impl1);
        }
    }    

     private static class Impl implements StatusLineElementProvider {
        public Component myComponent; 
        private String myId; 
        public Impl (String id) {
            myId = id;
        } 
        public Component getStatusLineElement() {
            myComponent = new JLabel (myId);
            return myComponent;
        }
     }
     
}