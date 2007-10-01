/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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