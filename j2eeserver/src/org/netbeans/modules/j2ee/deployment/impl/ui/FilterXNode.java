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

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.PropertySet;
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
        super(original);
        this.xnode = xnode;
        disableDelegation(DELEGATE_GET_ACTIONS);
        this.extendsActions = extendsActions;
    }
    
    public FilterXNode(Node original, Node xnode, boolean extendsActions, Children xchildren) {
        super(original, xchildren);
        this.xnode = xnode;
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
        return merge(getOriginal().getPropertySets(), xnode.getPropertySets());
    }
    
    public void refreshChildren() {
        org.openide.nodes.Children c = getChildren();
        if (c instanceof XChildren)
            ((XChildren)c).update();
    }

    public static PropertySet merge(PropertySet overriding, PropertySet base) {
        Sheet.Set overridden;
        if (base instanceof Sheet.Set)
            overridden = (Sheet.Set) base;
        else {
            overridden = new Sheet.Set();
            overridden.put(base.getProperties());
        }
        overridden.put(overriding.getProperties());
        return overridden;
    }
    
    public static PropertySet[] merge(PropertySet[] overridingSets, PropertySet[] baseSets) {
        java.util.Map ret = new java.util.HashMap();
        for (int i=0; i<baseSets.length; i++) {
            ret.put(baseSets[i].getName(), baseSets[i]);
        }
        for (int j=0; j<overridingSets.length; j++) {
            PropertySet base = (PropertySet) ret.get(overridingSets[j].getName());
            if (base == null) {
                ret.put(overridingSets[j].getName(), overridingSets[j]);
            } else {
                base = merge(overridingSets[j], base);
                ret.put(base.getName(), base);
            }
        }

        PropertySet top = (PropertySet) ret.remove(Sheet.PROPERTIES);
        List retList = new ArrayList();
        if (top != null)
            retList.add(top);
        retList.addAll(ret.values());
        return (PropertySet[]) retList.toArray(new PropertySet[retList.size()]);
    }
}