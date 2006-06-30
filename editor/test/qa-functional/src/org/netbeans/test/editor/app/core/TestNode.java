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
package org.netbeans.test.editor.app.core;

import java.beans.*;
import javax.swing.JEditorPane;

import java.util.Vector;

import org.w3c.dom.Element;
import java.io.IOException;
import java.util.Vector;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.tree.TreeNode;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.cookies.Cookie;
import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.core.properties.StringProperty;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TestDownAction;
import org.netbeans.test.editor.app.gui.actions.TestPropertiesAction;
import org.netbeans.test.editor.app.gui.actions.TestRenameAction;
import org.netbeans.test.editor.app.gui.actions.TestUpAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestNode extends Object implements java.io.Serializable, XMLNode {
    
    public static final String CHANGE_NAME = "Change Name";
    
    public static final String NAME = "Name";
    
    protected String name;
    
    protected boolean isPerforming;
    
    protected static int nameCounter=1;
    
    public TestGroup owner;
    
    protected PropertyChangeSupport propertySupport;
    
    private TreeNode nodeDelegate = null;
    
    protected HashMap cookieSet;
    
    /** Creates new TestNode */
    public TestNode(String name) {
        propertySupport = new PropertyChangeSupport( this );
        this.name=name;
        isPerforming=false;
        registerActions();
        registerCookies();
    }
    
    public TestNode(Element node) {
        this(node.getAttribute(NAME));
    }
    
    public abstract boolean isParent();
    public abstract void perform();
    public abstract void stop();
    protected abstract void registerCookies();
    
    public Element toXML(Element node) {
        node.setAttribute(NAME, name);
        return node;
    }
    
    public JEditorPane getEditor() {
        return owner.getEditor();
    }
    
    public Logger getLogger() {
        return owner.getLogger();
    }
    
    public String getName() {
        return name;
    }
    
    public final void setName(String value) {
        String oldValue = name;
        name = value;
        firePropertyChange(CHANGE_NAME, oldValue, name);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public void firePropertyChange(String name,Object oldV, Object newV) {
        if (oldV == newV) oldV=null;
        propertySupport.firePropertyChange(name,oldV,newV);
    }
    
    public boolean isPerfoming() {
        return isPerforming;
    }
    
    public void delete() {
        owner.remove(this);
    }
    
    public static int getNameCounter() {
        return nameCounter++;
    }
    
    public TreeNode createNodeDelegate() {
        return new TestNodeDelegate(this);
    }
    
    public final TreeNode getNodeDelegate() {
        if (nodeDelegate == null) {
            nodeDelegate = createNodeDelegate();
        }
        return nodeDelegate;
    }
    //?????????????????????????????????????????????
    public HashMap getCookieSet() {
        if (cookieSet == null) {
            cookieSet=new HashMap();
        }
        return cookieSet;
    }
    
    public Cookie getCookie(Class clazz) {
        return (Cookie)cookieSet.get(clazz);
    }
    
    protected void registerActions() {
        ActionsCache.getDefault().addNodeActions(getClass(), ActionRegistry.getDefault().getActions(getCookieSet().values()));
        ActionsCache.getDefault().addNodeAction(getClass(), new TestUpAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestDownAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestRenameAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestDeleteAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestRenameAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestPropertiesAction());
    }
    
    public final Vector getActions() {
        Vector v=ActionsCache.getDefault().getActions(getClass());
        if (v == null) {
            registerActions();
            v=ActionsCache.getDefault().getActions(getClass());
        }
        return v;
    }
    
    public TestGroup getOwner() {
        return owner;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        name=node.getAttribute(NAME);
    }
    
    public Properties getProperties() {
        Properties ret=new Properties();
        ret.put(NAME, new StringProperty(name));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(NAME) == 0) {
            return new StringProperty(name);
        } else {
            throw new BadPropertyNameException(name+" isn't name of any property.");
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(NAME) == 0) {
            setName(((StringProperty)value).getProperty());
        } else {
            throw new BadPropertyNameException(name+" isn't name of any property.");
        }
    }
    
    public String toString() {
        return name;
    }
    
}