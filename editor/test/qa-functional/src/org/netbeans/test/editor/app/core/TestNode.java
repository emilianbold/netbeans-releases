/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import java.beans.*;
import javax.swing.JEditorPane;
import org.netbeans.test.editor.app.gui.Main;

import java.util.Vector;

import org.w3c.dom.Element;
import java.io.IOException;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextChild;
import java.util.Vector;
import java.util.Collection;
import javax.swing.JEditorPane;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.BeanNode;
import org.openide.nodes.BeanChildren;
import org.openide.util.actions.SystemAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;
/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestNode extends Object implements java.io.Serializable {
    
    public static final String NAME = "Name";
    
    protected String name;
    
    protected boolean isPerforming;    
    
    protected static int nameCounter=1;
    
    public TestGroup owner;
    
    protected PropertyChangeSupport propertySupport;
    
    /** Creates new TestNode */
    public TestNode(String name) {
//        Main.log("name=" + name);
        propertySupport = new PropertyChangeSupport ( this );
        this.name=name;
        isPerforming=false;
        getCookieSet().add(new PerformCookie() {
            public void perform() {
                TestNode.this.perform();
            }
            public boolean isPerforming() {
                return TestNode.this.isPerfoming();
            }
        });
    }
    
    public TestNode(Element node) {
        this(node.getAttribute("Name"));
        isPerforming=false;        
    }
    
    public Element toXML(Element node) {
        node.setAttribute("Name", name);
        return node;
    }
    
    public JEditorPane getEditor() {
        return owner.getEditor();
    }
    
    public Logger getLogger() {
        return owner.getLogger();
    }
    
    public String getName () {
        return name;
    }
    
    public void setName (String value) {
        String oldValue = name;
        name = value;
        firePropertyChange (NAME, oldValue, name);
    }
    
    public abstract boolean isParent();
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }
    
    public void firePropertyChange(String name,Object oldV, Object newV) {
        if (oldV == newV) oldV=null;
        propertySupport.firePropertyChange (name,oldV,newV);
    }
        
    public abstract void perform();
    
    public boolean isPerfoming() {
        return isPerforming;
    }
    
    public abstract void stop();    
    
    public void delete() {
        owner.remove(this);
    }
    
    public int getNameCounter() {
        return nameCounter++;
    }    
    private CookieSet cookieSet = new CookieSet();
    
    protected CookieSet getCookieSet() {
        return cookieSet;
    }
    
    public Cookie getCookie(Class cookie) {
        return cookieSet.getCookie(cookie);
    }
                
    private Node nodeDelegate = null;
    private TestNode me = this;
    
    protected static class TestNodeDelegate extends BeanNode {
        
        private static class TestNodeFactory implements BeanChildren.Factory {
            public Node createNode (Object bean) throws IntrospectionException {
                if (bean instanceof TestNode) {
                    return ((TestNode) bean).getNodeDelegate();
                } else {
                    return new BeanNode (bean);
                }
            }
        }
        
        private static final TestNodeFactory TEST_NODE_FACTORY = new TestNodeFactory();
        
        private static Children getChildren (Object bean) {
            if (bean instanceof BeanContext)
                return new BeanChildren ((BeanContext)bean, TEST_NODE_FACTORY);
            if (bean instanceof BeanContextProxy) {
                BeanContextChild bch = ((BeanContextProxy)bean).getBeanContextProxy();
                if (bch instanceof BeanContext)
                    return new BeanChildren ((BeanContext)bch);
            }
            return Children.LEAF;
        }
        
        public TestNodeDelegate(TestNode bean) throws IntrospectionException {
            this(bean, Children.LEAF);
//            System.err.println("TestNodeDelegate created!");
        }

        public TestNodeDelegate(TestNode bean, Children children) throws IntrospectionException {
            super(bean, children);
//            System.err.println("TestNodeDelegate created!");
        }
        
        public Cookie getCookie(Class cookie) {
            Cookie cookieObject = ((TestNode)getBean()).getCookie(cookie);
            
            if (cookieObject == null) {
                cookieObject = super.getCookie(cookie);
            }
            return cookieObject;
        }
        
        protected Collection createActionsCollection() {
            Vector actions = new Vector();
            SystemAction[] superActions = super.createActions();
            
            for (int cntr = 0; cntr < superActions.length; cntr++ ) {
                actions.add(superActions[cntr]);
            }
            actions.addAll(ActionRegistry.getDefault().getActions(this.getCookieSet()));
            actions.addAll(ActionRegistry.getDefault().getActions(((TestNode)getBean()).getCookieSet()));
            actions.add(SystemAction.findObject(DeleteAction.class, true));
            actions.add(SystemAction.findObject(RenameAction.class, true));
            return actions;
        }
            
        
        protected final SystemAction[] createActions() {
            Collection actions = createActionsCollection();
            return (SystemAction[]) actions.toArray(new SystemAction[actions.size()]);
        }
        
        public void destroy() throws IOException {
            ((TestNode)getBean()).delete();
            super.destroy();
        }
        
        public boolean canDestroy() {
            return true;
        }
        
        public Object clone() throws CloneNotSupportedException {
//            System.err.println("Clone of TestNodeDelegate called.");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return super.clone();
        }

        public Node cloneNode() {
//            System.err.println("CloneNode of TestNodeDelegate called.");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return super.cloneNode();
        }
        
    }
    
    public Node createNodeDelegate() {
        try {
            return new TestNodeDelegate(this);
        } catch (IntrospectionException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }
    
    public final Node getNodeDelegate() {
        if (nodeDelegate == null) {
            nodeDelegate = createNodeDelegate();
        }
        return nodeDelegate;
    }
 
    public TestNode getTestNode() {
        return me;
    }

}