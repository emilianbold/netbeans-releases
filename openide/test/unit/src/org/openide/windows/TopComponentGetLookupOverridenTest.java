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

package org.openide.windows;

import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.explorer.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;



/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes when TopComponent is
 * using its own lookup as in examples of ExplorerUtils...
 *
 * @author Jaroslav Tulach
 */
public class TopComponentGetLookupOverridenTest extends TopComponentGetLookupTest {
    public TopComponentGetLookupOverridenTest (java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TopComponentGetLookupOverridenTest.class);
        
        return suite;
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        top = new ListingYourComponent ();
        lookup = top.getLookup ();
    }

    private static class ListingYourComponent extends YourComponent 
    implements java.beans.PropertyChangeListener {
        public ListingYourComponent () {
            addPropertyChangeListener (this);
            getExplorerManager ().setRootContext (new AbstractNode (new Children.Array ()));
            java.lang.ref.SoftReference ref = new java.lang.ref.SoftReference (new Object ());
            assertGC ("Trying to simulate issue 40842, to GC TopComponent$SynchronizeNodes", ref);
        }
        
        private ThreadLocal callbacks = new ThreadLocal ();
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            ExplorerManager manager = getExplorerManager ();
            
            if ("activatedNodes".equals (ev.getPropertyName())) {
                if (Boolean.TRUE.equals (callbacks.get ())) {
                    return;
                }
                try {
                    callbacks.set (Boolean.TRUE);
                    Node[] arr = getActivatedNodes ();
                    
                    // first of all clear the previous values otherwise
                    // we will not test SynchronizeNodes (associateLookup (..., true))
                    setActivatedNodes (new Node[0]);
                    
                    Children.Array ch = (Children.Array)manager.getRootContext ().getChildren ();
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].getParentNode() != manager.getRootContext()) {
                            assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                        }
                    }
                    manager.setSelectedNodes (arr);
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                    fail (ex.getMessage());
                } finally {
                    callbacks.set (null);
                }
            }
        }
    } // end of ListingYourComponent
    
    // The following class is copied from example in ExplorerUtils:
    //
    public static class YourComponent extends TopComponent
    implements ExplorerManager.Provider, Lookup.Provider {
        private ExplorerManager manager;
        public YourComponent() {
            this.manager = new ExplorerManager ();
            ActionMap map = getActionMap ();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false
            
            associateLookup (ExplorerUtils.createLookup (manager, map));
        }
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        // It is good idea to switch all listeners on and off when the
        // component is shown or hidden. In the case of TopComponent use:
        protected void componentActivated() {
            ExplorerUtils.activateActions(manager, true);
        }
        protected void componentDeactivated() {
            ExplorerUtils.activateActions(manager, false);
        }
    } // end of YourComponent
}  
    
