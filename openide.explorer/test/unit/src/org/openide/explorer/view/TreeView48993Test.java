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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for inconsistnecy in tree structures when getters change structure
 */
public class TreeView48993Test extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;
    /** exception thrown in AWT thread */
    private static Throwable exc;
    
    JFrame f;
    
    static {
        System.setProperty("sun.awt.exception.handler", TreeView48993Test.class.getName());
    }
    
    /** Needed because we implement the handler.
     */
    public TreeView48993Test(){
        super("");
    }
    
    public TreeView48993Test(String name) {
        super(name);
    }
    
    protected void setUp() {
        exc = null;
    }
    
    protected void tearDown() {
        if (f != null) {
            f.setVisible(false);
            f.dispose();
        }
        
        if (exc != null) {
            AssertionFailedError ass = new AssertionFailedError("There should be no exception in AWT");
            ass.initCause(exc);
            throw ass;
        }
    }
    
    protected int timeOut() {
        return 15000; // 15s
    }

    public void testNotReentrantWhenDoingUpdate() throws Throwable {
        f = new JFrame();
        final MyChildren ch = new MyChildren();
        final AbstractNode root = new AbstractNode(ch);
        root.setName("test root");
        
        ch.keys(new String[] {"A", "B", "C", "D" } );
        
        final Node[] all = ch.getNodes();
        final UglyNode u = (UglyNode)all[2];
        
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            Panel p = new Panel();
            p.getExplorerManager().setRootContext(root);
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {all[0]});
            } catch (PropertyVetoException pve) {
                fail(pve.toString());
            }
            
            BeanTreeView btv = new BeanTreeView();
            p.add(BorderLayout.CENTER, btv);
            
            f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
            f.getContentPane().add(BorderLayout.CENTER, p);
            f.setBounds(300, 300, 200, 200);
            f.setVisible(true);
        }});
        
        u.waitOn(10000);
        
        waitForAWT(); // Thread.sleep(5000); // wait for the window to finish opening
        
        assertTrue("Somebody asked for the icon", u.flag);
        assertEquals("Still four children", 4, ch.getNodesCount());
        
        ((VisualizerNode)Visualizer.findVisualizer(u)).getIcon(true, true); // this clears the internal cache
        
        synchronized (u) {
            u.flag = false;
            u.keys = new String[] {"C" };
            ch.keys(new String[] { "B", "C", "D", "A" });
            u.fire();
            u.waitOn(10000);
        }
        Thread.sleep(1000);
        
        assertTrue("Somebody asked for the display name again", u.flag);
        assertEquals("Just one child", 1, ch.getNodesCount());
    }
    
    public void testSomeChildrenDisappearDuringFirstPaint() throws Throwable {
        assertFalse("Won't work from AWT thread", SwingUtilities.isEventDispatchThread());
        
        f = new JFrame();
        final MyChildren ch = new MyChildren();
        final AbstractNode root = new AbstractNode(ch);
        root.setName("test root");
        
        ch.keys(new String[] {"A", "B", "C", "D" } );
        
        Node[] all = ch.getNodes();
        final UglyNode u = (UglyNode)all[1];
        
        synchronized (u) {
            Panel p = new Panel();
            p.getExplorerManager().setRootContext(root);
            p.getExplorerManager().setSelectedNodes(new Node[] {all[0]});
            
            BeanTreeView btv = new BeanTreeView();
            p.add(BorderLayout.CENTER, btv);
            
            
            f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
            f.getContentPane().add(BorderLayout.CENTER, p);
            f.pack();
            f.setVisible(true);
            u.waitOn(10000000);
            
            Thread.sleep(2000);
            ((VisualizerNode)Visualizer.findVisualizer(u)).getIcon(true, true); // this clears the internal cache
            
            u.flag = false;
            u.keys = new String[] { "B", "C", "D" };
        }
        
        f.setBounds(300, 300, 200, 200);
        f.setVisible(false);
        f.invalidate();
        f.validate();
        f.repaint();
        f.setVisible(true);
        
        u.waitOn(10000000); // Waits for getIcon to run
        
        waitForAWT();
        
        assertTrue("Somebody asked for the icon", u.flag);
        assertEquals("And now we 3 ", 3, ch.getNodesCount());
    }
    
    /**
     * Waits for AWT queue to finish queued processing.
     */
    private static void waitForAWT() throws InterruptedException, InvocationTargetException {
        for (int i=0; i<5; i++) {
            SwingUtilities.invokeAndWait(new Runnable() {public void run() {}});;
        }
    }
    
    private static class UglyNode extends AbstractNode {
        private boolean flag;
        private Object[] keys;
        boolean ticked;
        
        public UglyNode(String name) {
            super(Children.LEAF);
            setName(name);
        }
        
        public String toString() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode());
            
        }
        
        public Image getIcon(int type) {
            synchronized(this) {
                tick();
                flag = true;
                if (keys != null) {
                    ((MyChildren)getParentNode().getChildren()).keys(keys);
                }
                return super.getIcon(type);
            }
        }
        
        public void fire() {
            fireIconChange();
        }
        
        /**
         * @return true if returning due to timeout 
         */
        public boolean waitOn(int timeout) {
            synchronized(this) {
                boolean tck;
                if(!ticked) { //while (!ticked) {
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        throw new InternalError();
                    }
                }
                tck = ticked;
                ticked = false; // reusable
                return !tck;
            }
        }
        
        public void tick() {
            synchronized(this) {
                ticked = true;
                notifyAll();
            }
        }
        
    }
    
    public void testRemoveOneOfEqualsChildren() throws Throwable {
        class EqualsNode extends AbstractNode {
            private Object token;
            
            public EqualsNode(String name, Object token) {
                super(Children.LEAF);
                this.token = token;
                setName(name);
            }
            
            public boolean equals(Object obj) {
                if (obj instanceof EqualsNode) {
                    return ((EqualsNode)obj).token.equals(token);
                }
                return super.equals(obj);
            }
        }
        
        class EqualsChildren extends Children.Keys {
            Object token;
            EqualsChildren(Object token) {
                this.token = token;
            }
            
            protected Node[] createNodes(Object key) {
                return new Node[] {new EqualsNode(key.toString(), token)};
            }
            
            public void keys(Object[] keys) {
                setKeys(keys);
            }
        }
        
        Object token = new Object();
        assertFalse("Won't work from AWT thread", SwingUtilities.isEventDispatchThread());
        
        EqualsChildren ch = new EqualsChildren(token);
        AbstractNode root = new AbstractNode(ch);
        root.setName("test root");
        
        Panel p = new Panel();
        p.getExplorerManager().setRootContext(root);
        BeanTreeView btv = new BeanTreeView();
        p.add(BorderLayout.CENTER, btv);
        
        f = new JFrame();
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        f.getContentPane().add(BorderLayout.CENTER, p);
        f.pack();
        f.setVisible(true);
        waitForAWT();
        
        ch.keys(new String[] {"A", "B"} );
        
        waitForAWT();
        
        ch.keys(new String[] {"a", "B"} );
        
        waitForAWT();
    }
    
    
    
    
    private static class MyChildren extends Children.Keys {
        protected Node[] createNodes(Object key) {
            return new Node[] {new UglyNode(key.toString())};
        }
        
        public void keys(Object[] keys) {
            setKeys(keys);
        }
    }
    
    /** The name MUST be handle and MUST be public */
    public static void handle(Throwable t) {
        exc = t;
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
