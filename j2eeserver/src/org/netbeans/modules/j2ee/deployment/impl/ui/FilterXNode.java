/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.FilterNode;
import javax.swing.Action;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/*
 * FilterXNode.java
 * 
 * A node to filter the original children and extending its action by
 * an extension node.
 *
 * Created on December 19, 2003, 11:21 AM
 * @author  nn136682
 */
public class FilterXNode extends FilterNode {
    protected Node xnode;
    private boolean extendsActions = true;

    public FilterXNode(Node original, Node xnode, boolean extendsActions) {
        this(original, xnode, extendsActions, true);
    }
    
    public FilterXNode(Node original, Node xnode, boolean extendsActions, boolean extendsChildren) {
        super(original);
        this.xnode = xnode;
        if (extendsChildren) {
            setChildren(new XChildren(xnode));
        }
        disableDelegation(DELEGATE_GET_ACTIONS);
        this.extendsActions = extendsActions;
    }
    
    public Node getXNode() {
        return xnode;
    }
    
    public static class XChildren extends Children {
        public XChildren(Node original) {
            super(original);
        }
        public void update() {
            addNotify();
        }
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        if (extendsActions) {
            actions.addAll(Arrays.asList(xnode.getActions(context)));
        }
        actions.addAll(Arrays.asList(getOriginal().getActions(context)));
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }

    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        org.openide.nodes.Node.Cookie c = null;
        if (xnode != null)
            c = xnode.getCookie(type);
        if (c == null)
            c = getOriginal().getCookie(type);
        if (c == null) 
            c = super.getCookie(type);
        return c;
    }
    
    public PropertySet[] getPropertySets() {
        List ret = new ArrayList();
        ret.addAll(Arrays.asList(getOriginal().getPropertySets()));
        ret.addAll(Arrays.asList(xnode.getPropertySets()));
        return (PropertySet[]) ret.toArray(new PropertySet[ret.size()]);
    }
    
    public void refreshChildren() {
        org.openide.nodes.Children c = getChildren();
        if (c instanceof XChildren)
            ((XChildren)c).update();
    }
}