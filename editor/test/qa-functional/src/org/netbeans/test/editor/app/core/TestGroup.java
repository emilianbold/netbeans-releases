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
import java.util.Vector;
import org.netbeans.test.editor.app.gui.Main;

import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
//import java.beans.beancontext.*;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.netbeans.test.editor.app.gui.Main;

import java.beans.beancontext.BeanContext;

import org.w3c.dom.Element;
import java.beans.IntrospectionException;
import java.util.Collection;

import org.netbeans.test.editor.app.gui.Main;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestGroup extends TestNode /*implements BeanContext*/ {
    
    public final static String CHANGE_CHILD = "Change child";
    public final static String REMOVE_CHILD = "Remove node";
    public final static String ADD_CHILD = "Add child";
    public final static String UP_CHILD = "Up child";
    public final static String DOWN_CHILD = "Down child";
    
    private Vector nodes;
    
    /** Creates new TestGroup */
    public TestGroup(String name) {
        super(name);
        nodes=new Vector();
    }
    
    public TestGroup(Element node) {
        super(node);
        nodes = new Vector();
        addNodes(Test.loadSubNodes(node));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        return Test.saveSubNodes(node, nodes);
    }
    
    public void addNode(TestNode node) {
        node.owner = this;
        nodes.add(node);
        super.firePropertyChange (ADD_CHILD,null,node);
    }
    
    public void addNodes(TestNode[] n) {
        for (int i=0;i < n.length;i++) {
            nodes.add(n[i]);
            n[i].owner=this;
        }
        firePropertyChange (CHANGE_CHILD,null,null);
    }
    
    public void addNodes(Vector n) {
        for (int i=0;i < n.size();i++) {
            TestNode element = (TestNode) n.elementAt(i);
            nodes.add(element);
            element.owner=this;
        }
        firePropertyChange (CHANGE_CHILD,null,null);
    }
    
    public Vector getChildNodes() {
        return nodes;
    }
    
    public TestNode get(int i) {
        return (TestNode)nodes.elementAt(i);
    }
    
    public int getChildCount() {
        return nodes.size();
    }
    
    public TestNode remove(int i) {
        TestNode result = (TestNode) nodes.remove(i);
        firePropertyChange (CHANGE_CHILD,null,null);
        return result;
    }
    
    public void remove(TestNode node) {
        nodes.remove((Object) node);
        firePropertyChange (CHANGE_CHILD,null,null);
    }
    
    public void upNode(TestNode n) {
        TestNode upper;
        
        for (int i=0;i < nodes.size();i++) {
            if (((TestNode)nodes.get(i)) == n && i > 0) {
                upper=remove(i-1);
                nodes.insertElementAt(upper,i);
                firePropertyChange (UP_CHILD,null,n);
                break;
            }
        }
    }
    
    public void downNode(TestNode n) {
        TestNode down;
        for (int i=0;i < nodes.size();i++) {
            if (((TestNode)nodes.get(i)) == n && i < nodes.size()-1) {
                down=remove(i+1);
                nodes.insertElementAt(down,i);
                firePropertyChange (DOWN_CHILD,null,n);
                break;
            }
        }
    }
    
    public TestNode[] getChilds() {
        int i,c;
        TestNode[] ret;
        
        c=nodes.size();
        ret=new TestNode[c];
        for(i=0;i < c;i++) {
            ret[i]=get(i);
        }
        return ret;
    }
    
    public boolean isParent() {
        return true;
    }
    
    private boolean performFinished = false;
    
    public void perform(String what) {
        int point = what.indexOf('.');
        
        if (point == (-1)) {
            //Only call action
            for (int cntr = 0; cntr < getChildCount(); cntr++) {
                final TestNode node = get(cntr);
                performFinished = false;
                
                System.err.println("Testing node of type:" + node.getClass() + ", name: " + node.getName());
                if (node instanceof TestCallAction && node.getName().equals(what)) {
                    System.err.println("Before trying to perform: ");
                    ((TestCallAction)node).performAndWait();
                    System.err.println("After perform.");
                    return;
                };
            };
            System.err.println("Call action: " + point + " not found.");
            return;
        };
        
        String step = what.substring(0, point);
        
        for (int cntr = 0; cntr < getChildCount(); cntr++) {
            TestNode node = get(cntr);
            
            if (node instanceof TestGroup && node.getName().equals(step)) {
                ((TestGroup)node).perform(what.substring(point + 1));
                return;
            };
        };
        System.err.println("Step: " + step + " not found.");
    }
    
    public Vector getPerformedActions(String what) {
        int point = what.indexOf('.');
        
        if (point == (-1)) {
            //Only call action
            for (int cntr = 0; cntr < getChildCount(); cntr++) {
                final TestNode node = get(cntr);
                performFinished = false;
                
                if (node instanceof TestCallAction && node.getName().equals(what)) {
                    //                    System.err.println("Before trying to perform: ");
                    return ((TestCallAction)node).getPerformedActions();
                };
            };
            //            System.err.println("Call action: " + point + " not found.");
            return null;
        };
        
        String step = what.substring(0, point);
        
        //        System.err.println("step=" + step);
        
        for (int cntr = 0; cntr < getChildCount(); cntr++) {
            TestNode node = get(cntr);
            
            //            System.err.println("node.getName()=" + node.getName());
            //            System.err.println("node.getClass().getName()=" + node.getClass().getName());
            
            if (node instanceof TestGroup && node.getName().equals(step)) {
                return ((TestGroup)node).getPerformedActions(what.substring(point + 1));
            };
        };
        //        System.err.println("Step: " + step + " not found.");
        return null;
    }
    
    private class SetNewType extends NewType {
        Class setAction;
        
        public SetNewType(Class setAction) {
            this.setAction = setAction;
        }
        
        public void create () {
            try {
                Constructor constructor = setAction.getConstructor(new Class[] {int.class});
                TestSetAction action = (TestSetAction) constructor.newInstance(new Object[] {new Integer(getNameCounter())});
                
                addNode(action);
            } catch (InstantiationException e) {
                e.printStackTrace(System.err);
                System.err.flush();
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.err);
                System.err.flush();
            } catch (IllegalArgumentException e) {
                e.printStackTrace(System.err);
                System.err.flush();
            } catch (InvocationTargetException e) {
                e.printStackTrace(System.err);
                System.err.flush();
            } catch (NoSuchMethodException e) {
                e.printStackTrace(System.err);
                System.err.flush();
            }
        }
        
        public String getName() {
            return setAction.getName();
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    protected final Collection generateSetNewTypes() {
        Set setActions = SetActionsRegistry.getDefault().getSetActionsSet();
        Iterator iterator = setActions.iterator();
        Vector result = new Vector();
        
        while (iterator.hasNext()) {
            Class item = (Class)iterator.next();
            
            if (item != null) {
                result.add(new SetNewType(item));
            }
        }
        return result;
    }
    
    protected abstract Collection getNewTypes();
    
    protected static class TestGroupNodeDelegate extends TestNodeDelegate {
        
        private static class Childs extends Children.Keys {
            TestGroup owner;
            PropertyChangeListener listener;
            
            public Childs(TestGroup owner) {
                this.owner = owner;
            }
            
            protected Node[] createNodes(Object key) {
                if (key instanceof TestNode) {
                    return new Node[] {((TestNode) key).getNodeDelegate()};
                } else {
                    throw new IllegalArgumentException("Node is not TestNode");
                }
            }
            
            protected void addNotify() {
                owner.addPropertyChangeListener(listener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        Childs.this.setKeys(owner.getChilds());
                    }
                });
                setKeys(owner.getChilds());
            }
            
            protected void removeNotify() {
                owner.removePropertyChangeListener(listener);
                setKeys(Collections.EMPTY_SET);
            }
            
        }
        
        public TestGroupNodeDelegate(TestGroup bean) throws IntrospectionException {
            super(bean, new Childs(bean));
        }
        
        protected Collection createActionsCollection() {
            Collection actions = super.createActionsCollection();
            actions.add((SystemAction) SystemAction.findObject(org.openide.actions.NewAction.class, true));
            return actions;
        }
        
        public NewType[] getNewTypes() {
            Collection newTypes = ((TestGroup) getBean()).getNewTypes();
            
            Iterator iterator = newTypes.iterator();
            
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj == null) {
                    throw new IllegalArgumentException("In the collection is *null*.");
                }
                if (!(obj instanceof NewType)) {
                    throw new IllegalArgumentException("In the collection not *NewType*, class: " + obj.getClass().getName());
                }
            }
            if (newTypes.size() == 0) {
                Main.log("new types collection size = 0");
            }
            
            return (NewType[]) newTypes.toArray(new NewType[newTypes.size()]);
        }
    }
    
    public Node createNodeDelegate() {
        try {
            return new TestGroupNodeDelegate(this);
        } catch (IntrospectionException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }
    
    
}