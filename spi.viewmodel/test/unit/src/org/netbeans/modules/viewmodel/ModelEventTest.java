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

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.viewmodel.TreeModelNode;
import org.netbeans.modules.viewmodel.TreeModelRoot;
import org.netbeans.modules.viewmodel.TreeTable;
import org.netbeans.spi.viewmodel.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.RequestProcessor;



/**
 *
 */
public class ModelEventTest  extends NbTestCase implements NodeListener {

    BasicTest.CompoundModel cm;
    Node n;
    volatile Object event;
    Vector propEvents = new Vector();

    public ModelEventTest (String s) {
        super (s);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        ArrayList l = new ArrayList ();
        cm = new CompoundModel1 ();
        l.add (cm);
        TreeTable tt = (TreeTable) Models.createView 
            (Models.createCompoundModel (l));
        BasicTest.waitFinished ();
        n = tt.getExplorerManager ().
            getRootContext ();
        n.addNodeListener(this);
    }

    public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
        propEvents.add(propertyChangeEvent.getPropertyName());
        /*
        System.out.println("propertyChangeEvent = "+propertyChangeEvent);
        assertNull("Already fired", event);
        event = propertyChangeEvent;
         */
    }
    
    public void testDisplayName() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Display Name was not fired", propEvents.contains(Node.PROP_DISPLAY_NAME));
        //assertNotNull("Was not fired", this.event);
    }

    public void testIcon() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.ICON_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Icon was not fired", propEvents.contains(Node.PROP_ICON));
    }

    public void testShortDescription() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Short Description was not fired", propEvents.contains(Node.PROP_SHORT_DESCRIPTION));
    }

    public void testChildren() {
        n.getChildren().getNodes();
        /*
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.CHILDREN_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        //assertTrue("Short Description was not fired", propEvents.contains(Node.PROP_));
        assertNotNull("Children were not fired", this.event);
         */
    }

    public final class CompoundModel1 extends BasicTest.CompoundModel {
        
        int dn = 0;
        int ib = 0;
        int sd = 0;
        int cc = 0;
        
        protected void addCall (String methodName, Object node) {
            // Ignore multiple calls
        }

        // init ....................................................................

        public String getDisplayName (Object node) throws UnknownTypeException {
            String dns = super.getDisplayName(node);
            dns += (dn++);
            return dns;
        }
        
        public String getIconBase (Object node) throws UnknownTypeException {
            String ibs = super.getIconBase(node);
            ibs += (ib++);
            return ibs;
        }
        
        public String getShortDescription (Object node) throws UnknownTypeException {
            String sds = super.getShortDescription(node);
            sds += (sd++);
            return sds;
        }
        
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public synchronized int getChildrenCount (Object node) throws UnknownTypeException {
            return super.getChildrenCount (node) + (cc++);
        }
        
        public Object[] getChildren (Object parent, int from, int to) throws UnknownTypeException {
            System.err.println("CompoundModel1.getChildren("+parent+", "+from+", "+to+")");
            //Thread.dumpStack();
            addCall ("getChildren", parent);
            Object[] ch = new Object[3 + (cc - 1)];
            if (parent == ROOT) {
                for (int i = 0; i < ch.length; i++) {
                    // Use  Character.valueOf() on 1.5
                    ch[i] = new Character((char) ('a' + i)).toString();
                }
                return ch;
            }
            if (parent instanceof String) {
                for (int i = 0; i < ch.length; i++) {
                    // Use  Character.valueOf() on 1.5
                    ch[i] = ((String) parent + new Character((char) ('a' + i)).toString());
                }
                return ch;
            }
            throw new UnknownTypeException (parent);
        }
    }
}
