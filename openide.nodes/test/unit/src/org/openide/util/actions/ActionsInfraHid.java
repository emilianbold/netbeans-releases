/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import junit.framework.Assert;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class ActionsInfraHid implements ContextGlobalProvider {
    
    private static Node[] currentNodes = null;
    private static final NodeLookup nodeLookup = new NodeLookup();
    
    public Lookup createGlobalContext() {
        return nodeLookup;
    }

    private static Lookup.Result nodeResult;
    static {
        try {
            nodeResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
            Assert.assertEquals(Collections.emptySet(), new HashSet(nodeResult.allInstances()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setCurrentNodes(Node[] nue) {
        currentNodes = nue;
        nodeLookup.refresh();
        Assert.assertEquals(nue != null ? new HashSet(Arrays.asList(nue)) : Collections.EMPTY_SET,
                new HashSet(nodeResult.allInstances()));
    }

    private static final class NodeLookup extends AbstractLookup implements InstanceContent.Convertor {
        private final InstanceContent content;
        public NodeLookup() {
            this(new InstanceContent());
        }
        private NodeLookup(InstanceContent content) {
            super(content);
            this.content = content;
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            if (currentNodes != null) {
                content.set(Arrays.asList(currentNodes), null);
            } else {
                content.set(Collections.singleton(new Object()), this);
            }
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Node.class;
        }
        public String id(Object obj) {
            return "none"; // magic, see NodeAction.NodesL.resultChanged
        }
        public String displayName(Object obj) {
            return null;
        }
    }

    /*
    private static final class NodeLookup extends ProxyLookup implements InstanceContent.Convertor {
        public NodeLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            setLookups(new Lookup[] {
                currentNodes != null ?
                    Lookups.fixed(currentNodes) :
                    Lookups.fixed(new Object[] {null}, this),
            });
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Object.class;
        }
        public String id(Object obj) {
            return "none";
        }
        public String displayName(Object obj) {
            return null;
        }
    }
     */

    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }
    
}
